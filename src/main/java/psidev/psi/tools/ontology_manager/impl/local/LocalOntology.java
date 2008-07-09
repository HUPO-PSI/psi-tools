package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.local.model.OntologyTerm;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Access to a local ontology in the form of an OBO file.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * Date: 07-Aug-2007
 */
public class LocalOntology implements OntologyAccess {

    public static final Log log = LogFactory.getLog( LocalOntology.class );

    private Ontology ontology;

    private final String DEFAULT_ONTOLOGY_DIRECTORY = ".downloaded-ontologies";

    private File ontologyDirectory = null;

    private String ontologyID;

    public LocalOntology() {
        log.info( "Creating new LocalOntology..." );
        ontology = null;
    }

    ////////////////////////////
    // OntologyAccess methods

    public void loadOntology( String ontologyID, String name, String version, String format, URI uri ) throws OntologyLoaderException {
        this.ontologyID = ontologyID;

        // first check the format
        if ( "OBO".equals( format ) ) {
            if ( uri == null ) {
                throw new IllegalArgumentException( "The given CvSource doesn't have a URL" );
            } else {
                URL url;
                try {
                    url = uri.toURL();
                } catch ( MalformedURLException e ) {
                    throw new IllegalArgumentException( "The given CvSource doesn't have a valid URL: " + uri );
                }

                // parse the URL and load the ontology
                OboLoader loader = new OboLoader( getOntologyDirectory() );
                try {
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Parsing URL: " + url );
                    }

                    ontology = loader.parseOboFile( url );
                } catch ( OntologyLoaderException e ) {
                    throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Successfully created LocalOntology from values: ontology="
                      + ontologyID + " name=" + name + " version=" + version + " format=" + format + " location=" + uri );
        }
    }

    public Set<String> getValidIDs( String id, boolean allowChildren, boolean useTerm ) {
        Set<String> terms = new HashSet<String>();

        OntologyTermI resultTerm = ontology.search( id ); // will return null if no such term found

        if ( resultTerm != null ) {
            if ( useTerm ) {
                terms.add( resultTerm.getTermAccession() );
            }
            if ( allowChildren ) {
                Collection<OntologyTermI> childTerms = ontology.getAllChildren( resultTerm );
                for ( OntologyTermI childTerm : childTerms ) {
                    terms.add( childTerm.getTermAccession() );
                }
            }
        } else {
            log.warn( "No matching entries in local ontology '" + ontologyID
                      + "' for term '" + id + "'. Returning empty set of valid terms." );
        }
        log.debug( "Returning " + terms.size() + " valid IDs for ontology= " + ontologyID + " id=" + id + " allowChilrden=" + allowChildren + " useTerm=" + useTerm );
        return terms;
    }

    public void setOntologyDirectory( File directory ) {
        if ( directory != null ) ontologyDirectory = directory;
    }

    public Set<OntologyTermI> getValidTerms( String accession, boolean allowChildren, boolean useTerm ) {
        throw new UnsupportedOperationException( "NOT IMPLEMENTED YET." );
    }

    public OntologyTermI getTermForAccession( String accession ) {
        return ontology.search( accession );
    }

    public boolean isObsolete( OntologyTermI term ) {
        return ontology.isObsoleteTerm( term );
    }

    public Set<OntologyTermI> getDirectParents( OntologyTermI term ) {
        return ontology.getDirectParents( term );
    }

    public Set<OntologyTermI> getDirectChildren( OntologyTermI term ) {
        return ontology.getDirectChildren(  term );
    }

    public Set<OntologyTermI> getAllParents( OntologyTermI term ) {
        return ontology.getAllParents( term );
    }

    public Set<OntologyTermI> getAllChildren( OntologyTermI term ) {
        return ontology.getAllChildren( term );
    }

    /**
     * Create a directory for the validator either in the user's home directory or if it cannot, in the system's temp
     * directory.
     *
     * @return the created directory, never null.
     */
    private File getOntologyDirectory() throws OntologyLoaderException {
        if ( ontologyDirectory != null ) {
            if ( !ontologyDirectory.exists() ) {

                if ( !ontologyDirectory.mkdirs() ) {
                    throw new OntologyLoaderException( "Cannot create home directory for ontologies: " + ontologyDirectory.getAbsolutePath() );
                }
                return ontologyDirectory;

            } else {

                if ( ontologyDirectory.canWrite() ) {
                    return ontologyDirectory;
                }
            }
            log.warn( "Could not create or write to specified ontologies directory." );
        }

        log.info( "Using default for ontology directory." );
        String path = System.getProperty( "user.home" ) + File.separator + DEFAULT_ONTOLOGY_DIRECTORY;

        File dir = new File( path );

        if ( !dir.exists() ) {

            if ( !dir.mkdirs() ) {
                throw new OntologyLoaderException( "Cannot create home directory for ontologies: " + dir.getAbsolutePath() );
            }

            return dir;

        } else {

            if ( dir.canWrite() ) {
                return dir;
            } else {
                // TODO log error
                dir = null;
                path = System.getProperty( "java.io.tmpdir", "tmp" ) + File.separator + ontologyDirectory;
                dir = new File( path );

                if ( !dir.exists() ) {

                    if ( !dir.mkdirs() ) {
                        throw new OntologyLoaderException( "Cannot create home directory for ontologies: " + dir.getAbsolutePath() );
                    }

                    return dir;

                } else {

                    if ( dir.canWrite() ) {
                        return dir;
                    } else {
                        // TODO log error
                        throw new OntologyLoaderException( "Cannot create home directory for ontologies, pleast check your config." );
                    }
                }
            }
        }
    }


}