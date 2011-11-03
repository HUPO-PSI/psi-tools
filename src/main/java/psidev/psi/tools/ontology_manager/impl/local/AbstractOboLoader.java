package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.OntologyManagerContext;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;
import uk.ac.ebi.ols.loader.impl.BaseAbstractLoader;
import uk.ac.ebi.ols.loader.parser.OBOFormatParser;
import uk.ac.ebi.ols.model.interfaces.Term;
import uk.ac.ebi.ols.model.interfaces.TermRelationship;
import uk.ac.ebi.ols.model.interfaces.TermSynonym;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Abstract OBO loader
 *
 * NOTE : OnoLoader was the original class but did not offer flexibility for using different extensions of OntologyTermI.
 * That is why this template abtract class has been created. The template could have been simpler (AbstractOboLoader<T extends OntologyTermI> instead of AbstractOboLoader<T extends OntologyTermI, O extends OntologyTemplate<T>>) but we needed an absolute retrocompatibility
 * with the OboLoader class which returns an Ontology and not OntologyTemplate<OntologyTerm>.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public abstract class AbstractOboLoader<T extends OntologyTermI, O extends OntologyTemplate<T>> extends BaseAbstractLoader {


    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(OboLoader.class);

    protected static final String ONTOLOGY_REGISTRY_NAME = "ontology.registry.map";

    public AbstractOboLoader( File ontologyDirectory ) {
    }

    /////////////////////////////
    // AbstractLoader's methods

    protected abstract void configure();

    protected void parse( Object params ) {
        try {
            Vector v = new Vector();
            v.add( ( String ) params );
            ( (OBOFormatParser) parser ).configure( v );
            parser.parseFile();

        } catch ( Exception e ) {
            logger.fatal( "Parse failed: " + e.getMessage(), e );
        }
    }

    protected void printUsage() {
        // done to comply to AbstractLoader requirements
    }

    //////////////////////////////
    // User's methods

    protected abstract O createNewOntology();
    protected abstract T createNewOntologyTerm(String identifier, String name);


    protected O buildOntology() {

        O ontology = createNewOntology();

        // 1. convert and index all terms (note: at this stage we don't handle the hierarchy)
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            Term term = ( Term ) iterator.next();

            // convert term into a OboTerm
            T ontologyTerm = createNewOntologyTerm( term.getIdentifier(), term.getName() );
            final Collection<TermSynonym> synonyms = term.getSynonyms();
            if( synonyms != null ) {
                for ( TermSynonym synonym : synonyms ) {
                    ontologyTerm.getNameSynonyms().add( synonym.getSynonym() );
                }
            }

            ontology.addTerm( ontologyTerm );

            if ( term.isObsolete() ) {
                ontology.addObsoleteTerm( ontologyTerm );
            }
        }

        // 2. build hierarchy based on the relations of the Terms
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            Term term = ( Term ) iterator.next();

            if ( term.getRelationships() != null ) {
                for ( Iterator iterator1 = term.getRelationships().iterator(); iterator1.hasNext(); ) {
                    TermRelationship relation = ( TermRelationship ) iterator1.next();

                    ontology.addLink( relation.getObjectTerm().getIdentifier(),
                                      relation.getSubjectTerm().getIdentifier() );
                }
            }
        }

        return ontology;
    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file. It has to exist and to be readable, otherwise it will break.
     * @return a non null IntactOntology.
     */
    public O parseOboFile( File file ) {

        if ( !file.exists() ) {
            throw new IllegalArgumentException( file.getAbsolutePath() + " doesn't exist." );
        }

        if ( !file.canRead() ) {
            throw new IllegalArgumentException( file.getAbsolutePath() + " could not be read." );
        }

        //setup vars
        configure();

        //parse obo file
        parse( file.getAbsolutePath() );

        //process into relations
        process();

        return buildOntology();
    }

    private File getRegistryFile() throws OntologyLoaderException {
        File ontologyDirectory = OntologyManagerContext.getInstance().getOntologyDirectory();

        File[] registry = ontologyDirectory.listFiles( new FileFilter() {
            public boolean accept( File pathname ) {
                return ONTOLOGY_REGISTRY_NAME.equals( pathname.getName() );
            }
        } );

        if ( registry.length == 1 ) {
            // found our file
            File validatorRegistry = registry[0];
            return validatorRegistry;
        } else {
            // create it
            return new File( ontologyDirectory.getAbsolutePath() + File.separator + ONTOLOGY_REGISTRY_NAME );
        }
    }

    /**
     * Load an OBO file from an URL.
     *
     * @param url the URL to load (must not be null)
     * @return an ontology
     * @see #parseOboFile(File file)
     */
    public O parseOboFile( URL url ) throws OntologyLoaderException {

        // load config file (ie. a map)
        // check if that URL has already been loaded
        // if so, get the associated temp file and check if available
        // if available, then load it and skip URL load
        // if any of the above failed, load it from the network.

        if ( url == null ) {
            throw new IllegalArgumentException( "Please give a non null URL." );
        }


        File ontologyFile = null;
        File ontologyDirectory = OntologyManagerContext.getInstance().getOntologyDirectory();
        boolean isKeepDownloadedOntologiesOnDisk = OntologyManagerContext.getInstance().isStoreOntologiesLocally();
        Map registryMap = null;

        if( isKeepDownloadedOntologiesOnDisk ) {

            if ( ontologyDirectory == null ) {
                throw new IllegalArgumentException( "ontology directory cannot be null, please set it using OntologyManagerContext" );
            }

            if ( !ontologyDirectory.exists() ) {
                throw new IllegalArgumentException( "ontology directory must exist" );
            }

            if ( !ontologyDirectory.canWrite() ) {
                throw new IllegalArgumentException( "ontology directory must be writeable" );
            }

            if ( log.isInfoEnabled() ) {
                log.info( "User work directory: " + ontologyDirectory.getAbsolutePath() );
                log.info( "keepTemporaryFile: " + OntologyManagerContext.getInstance().isStoreOntologiesLocally() );
            }

            File registryFile = getRegistryFile();

            if ( null != registryFile ) {

                // TODO replace Map by a properties file so it can be read/edited easily
                // MI.file.path=
                // MI.last.loaded=
                // MI.refresh.after=
                // TODO check on the length of the file and compare it to the length on the web site.
                // MI.length=

                // deserialise the Map
                try {
                    if ( registryFile.length() > 0 ) {
                        // the file has some content
                        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( registryFile ) );
                        registryMap = ( Map ) ois.readObject();

                        if ( registryMap != null ) {
                            if ( registryMap.containsKey( url ) ) {
                                ontologyFile = new File( ( String ) registryMap.get( url ) );

                                if ( ontologyFile.exists() && ontologyFile.canRead() ) {

                                    // Cool, find it ! use it instead of the provided URL
                                    if ( log.isInfoEnabled() )
                                        log.info( "Reuse existing cache: " + ontologyFile.getAbsolutePath() );

                                } else {

                                    if ( log.isInfoEnabled() )
                                        log.info( "Could not find " + ontologyFile.getAbsolutePath() );

                                    // cleanup map
                                    registryMap.remove( url );

                                    // save map
                                    log.info( "Saving registry file..." );
                                    File f = getRegistryFile();
                                    ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( f ) );
                                    oos.writeObject( registryMap );
                                    oos.flush();
                                    oos.close();
                                }
                            }
                        } else {
                            log.info( "could not deserialize the Map" );
                        }
                    } else {
                        log.info( "The file is empty" );
                    }
                } catch ( IOException e ) {
                    // optional, so just display message in the log
                    log.error( "Error while deserializing the map", e );
                } catch ( ClassNotFoundException e ) {
                    // optional, so just display message in the log
                    log.error( "Error while deserializing the map", e );
                }
            }
        }


        try {
            if ( ontologyFile == null || !ontologyFile.exists() || !ontologyFile.canRead() ) {

                // if it is not defined, not there or not readable...

                // Read URL content
                if ( log.isInfoEnabled() ) log.info( "Loading URL: " + url );

                URLConnection con = url.openConnection();
                int size = con.getContentLength();        // -1 if not stat available

                if ( log.isInfoEnabled() ) log.info( "size = " + size );

                InputStream is = url.openStream();

                // Create a temp file and write URL content in it.
                if ( !ontologyDirectory.exists() ) {
                    if ( !ontologyDirectory.mkdirs() ) {
                        throw new IOException( "Cannot create temp directory: " + ontologyDirectory.getAbsolutePath() );
                    }
                }

                // make the temporary file name specific to the URL
                String name = null;
                String filename = url.getFile();
                int idx = filename.lastIndexOf( '/' );
                if ( idx != -1 ) {
                    name = filename.substring( idx, filename.length() );
                } else {
                    name = "unknown";
                }

                // build the file
                ontologyFile = new File( ontologyDirectory + File.separator + name + System.currentTimeMillis() + ".obo" );
                if ( ! isKeepDownloadedOntologiesOnDisk ) {
                    log.info( "Request file to be deleted on exit." );
                    ontologyFile.deleteOnExit();
                }

                if ( log.isDebugEnabled() )
                    log.debug( "The OBO file will be temporary stored as: " + ontologyFile.getAbsolutePath() );

                FileOutputStream out = new FileOutputStream( ontologyFile );

                int length = 0;
                int current = 0;
                byte[] buf = new byte[1024 * 1024];

                // TODO write a nicer text-progress-bar...
                while ( ( length = is.read( buf ) ) != -1 ) {
                    current += length;
                    out.write( buf, 0, length );

                    if ( log.isInfoEnabled() ) {
                        log.info( "length = " + length );
                        log.info( "Percent: " + ( ( current / ( float ) size ) * 100 ) + "%" );
                    }
                }

                is.close();

                out.flush();
                out.close();

                if ( isKeepDownloadedOntologiesOnDisk ) {
                    // if the user has requested for the ontology file to be kept, store file reference in the registry
                    if ( registryMap == null ) {
                        registryMap = new HashMap();
                    }

                    registryMap.put( url, ontologyFile.getAbsolutePath() );

                    // serialize the map
                    if ( log.isInfoEnabled() ) log.info( "Serializing Map" );
                    File f = getRegistryFile();
                    ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( f ) );
                    oos.writeObject( registryMap );
                    oos.flush();
                    oos.close();
                }
            }

            if ( ontologyFile == null ) {
                log.error( "The ontology file is still null..." );
            }

            // Parse file
            return parseOboFile( ontologyFile );

        } catch ( IOException e ) {
            throw new OntologyLoaderException( "Error while loading URL (" + url + ")", e );
        }
    }
}
