/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import psidev.psi.tools.ontology_manager.impl.local.model.OntologyTerm;
import uk.ac.ebi.ook.loader.impl.AbstractLoader;
import uk.ac.ebi.ook.loader.parser.OBOFormatParser;
import uk.ac.ebi.ook.model.interfaces.TermRelationship;
import uk.ac.ebi.ook.model.ojb.TermBean;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Wrapper class that hides the way OLS handles OBO files.
 *
 * @author Samuel Kerrien
 * @version $Id: OboLoader.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>30-Sep-2005</pre>
 */
public class OboLoader extends AbstractLoader {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( OboLoader.class );

    private static final String ONTOLOGY_REGISTRY_NAME = "ontology.registry.map";

    private File ontologyDirectory;

    private boolean keepDownloadedOntologiesOnDisk = true;

//    private UserPreferences userPreferences = new UserPreferences(); // set to default

    public OboLoader(File ontologyDirectory) {
        this.ontologyDirectory = ontologyDirectory;
    }

    ////////////////////
    // Getter + Setter

    public boolean isKeepDownloadedOntologiesOnDisk() {
        return keepDownloadedOntologiesOnDisk;
    }

    public void setKeepDownloadedOntologiesOnDisk(boolean keepDownloadedOntologiesOnDisk) {
        this.keepDownloadedOntologiesOnDisk = keepDownloadedOntologiesOnDisk;
    }




    /////////////////////////////
    // AbstractLoader's methods

    protected void configure() {
        /**
         * ensure we get the right logger
         */
        logger = Logger.getLogger( OboLoader.class );

        parser = new OBOFormatParser();
        ONTOLOGY_DEFINITION = "PSI MI";
        FULL_NAME = "PSI Molecular Interactions";
        SHORT_NAME = "PSI-MI";
    }

    protected void parse( Object params ) {
        try {
            Vector v = new Vector();
            v.add( ( String ) params );
            ( ( OBOFormatParser ) parser ).configure( v );
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

    private OntologyImpl buildOntology() {

        OntologyImpl ontology = new OntologyImpl();

        // 1. convert and index all terms (note: at this stage we don't handle the hierarchy)
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            TermBean term = ( TermBean ) iterator.next();

            // convert term into a OboTerm
            OntologyTerm ontologyTerm = new OntologyTerm( term.getIdentifier() );

            // try to split the name into short and long name
            int index = term.getName().indexOf( ':' );
            if ( index != -1 ) {
                // found it !
                String name = term.getName();
                String shortName = name.substring( 0, index ).trim();
                String longName = name.substring( index + 1, name.length() ).trim();

                ontologyTerm.setShortName( shortName );
                ontologyTerm.setFullName( longName );

            } else {
                // not found
                ontologyTerm.setShortName( term.getName() );
                ontologyTerm.setFullName( term.getName() );
            }

            ontologyTerm.setObsolete( term.isObsolete() );

            // TODO OboTerm.setObsoleteMessage( );

            ontology.addTerm( ontologyTerm );
        }

        // 2. build hierarchy based on the relations of the Terms
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            TermBean term = ( TermBean ) iterator.next();

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

//    public void setUserPreferences( UserPreferences userPreferences ) {
//        this.userPreferences = userPreferences;
//    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file. It has to exist and to be readable, otherwise it will break.
     * @return a non null IntactOntology.
     */
    public Ontology parseOboFile( File file ) {

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

        // hardcode

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
    public Ontology parseOboFile( URL url ) throws OntologyLoaderException {

        // load config file (ie. a map)
        // check if that URL has already been loaded
        // if so, get the associated temp file and check if available
        // if available, then load it and skip URL load
        // if any of the above failed, load it from the network.

        if ( url == null ) {
            throw new IllegalArgumentException( "Please give a non null URL." );
        }


        log.info( "User work directory: " + ontologyDirectory.getAbsolutePath() );
        log.info( "keepTemporaryFile: " + isKeepDownloadedOntologiesOnDisk() );

        if ( ontologyDirectory == null ) {
            throw new IllegalStateException();
        }

        if ( !ontologyDirectory.exists() ) {
            throw new IllegalStateException();
        }

        if ( !ontologyDirectory.canWrite() ) {
            throw new IllegalStateException();
        }

        File ontologyFile = null;
        Map registryMap = null;

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
                                log.info( "Reuse existing cache: " + ontologyFile.getAbsolutePath() );

                            } else {

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
                e.printStackTrace();
                log.error( e );
            } catch ( ClassNotFoundException e ) {
                // optional, so just display message in the log
                e.printStackTrace();
                log.error( e );
            }
        }

        try {
            if ( ontologyFile == null || !ontologyFile.exists() || !ontologyFile.canRead() ) {

                // if it is not defined, not there or not readable...

                // Read URL content
                log.info( "Loading URL: " + url );

                URLConnection con = url.openConnection();
                int size = con.getContentLength();        // -1 if not stat available

                log.info( "size = " + size );

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
                if ( !isKeepDownloadedOntologiesOnDisk() ) {
                    log.info( "Request file to be deleted on exit." );
                    ontologyFile.deleteOnExit();
                }

                log.info( "The OBO file will be temporary stored as: " + ontologyFile.getAbsolutePath() );

                FileOutputStream out = new FileOutputStream( ontologyFile );

                int length = 0;
                int current = 0;
                byte[] buf = new byte[1024 * 1024];

                // TODO write a nicer text-progress-bar...
                while ( ( length = is.read( buf ) ) != -1 ) {

                    current += length;

                    out.write( buf, 0, length );

                    log.info( "length = " + length );
                    log.info( "Percent: " + ( ( current / ( float ) size ) * 100 ) + "%" );
                }

                is.close();

                out.flush();
                out.close();

                if ( isKeepDownloadedOntologiesOnDisk() ) {
                    // if the user has requested for the ontology file to be kept, store file reference in the registry
                    if ( registryMap == null ) {
                        registryMap = new HashMap();
                    }

                    registryMap.put( url, ontologyFile.getAbsolutePath() );

                    // serialize the map
                    log.info( "Serializing Map" );
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