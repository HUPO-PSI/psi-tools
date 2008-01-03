package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.local.model.OntologyTerm;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Florian Reisinger
 * Date: 07-Aug-2007
 */
public class LocalOntology implements OntologyAccess {

    public static final Log log = LogFactory.getLog( LocalOntology.class );

    private Ontology ontology;
    String ontologyID;

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
                throw new IllegalArgumentException( "The given CVSource doesn't have a URL" );
            } else {
                URL url;
                try {
                    url = uri.toURL();
                } catch ( MalformedURLException e ) {
                    throw new IllegalArgumentException( "The given CVSource doesn't have a valid URL: " + uri );
                }

                // parse the URL and load the ontology
                OboLoader loader = new OboLoader();
                try {
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

        OntologyTerm resultTerm = ontology.search( id ); // will return null if no such term found

        if ( resultTerm != null ) {
            if ( useTerm ) {
                terms.add( resultTerm.getId() );
            }
            if ( allowChildren ) {
                Set<OntologyTerm> childTerms = resultTerm.getAllChildren();
                for ( OntologyTerm childTerm : childTerms ) {
                    terms.add( childTerm.getId() );
                }
            }
        } else {
            log.warn( "No matching entries in local ontology '" + ontologyID
                      + "' for term '" + id + "'. Returning empty set of valid terms." );
        }
        log.debug( "Returning " + terms.size() + " valid IDs for ontology= " + ontologyID + " id=" + id + " allowChilrden=" + allowChildren + " useTerm=" + useTerm );
        return terms;
    }

    public boolean isObsoleteID( String id ) {
        OntologyTerm term = ontology.search( id );
        if ( term == null ) {
            throw new IllegalStateException( "Checking obsolete on non existing term!" );
        }
        if( log.isDebugEnabled() ) {
            log.debug( "Term '" + id + "' obsolete? " + term.isObsolete() );
        }
        return term.isObsolete();
    }

    public String getTermNameByID( String id ) {
        String result = null;
        // ToDo: check if shortName (or fullName) is the one to use
        OntologyTerm term = ontology.search( id );
        if ( term != null ) {
            result = term.getShortName();
        }
        if( log.isDebugEnabled() ) {
            log.debug( "Name for term '" + id + "' is: " + term.getShortName() );
        }
        return result;
    }

    public Set<String> getDirectParentsIDs( String id ) {
        Set<String> result = new HashSet<String>();
        Collection<OntologyTerm> terms = ontology.search( id ).getParents();
        for ( OntologyTerm term : terms ) {
            result.add( term.getId() );
        }
        if( log.isDebugEnabled() ) {
            log.debug( "Term '" + id + "' has " + result.size() + " parent terms." );
        }
        return result;
    }
}