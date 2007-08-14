package psidev.psi.tools.ontology_manager.impl.local;

import psidev.psi.tools.ontology_manager.impl.local.model.OntologyTerm;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Author: Florian Reisinger
 * Date: 07-Aug-2007
 */
public class LocalOntology implements OntologyAccess {

    private Ontology ontology;
    String ontologyID;

    public LocalOntology() {
        ontology = null;
    }


    public void loadOntology( String ontologyID, String name, String version, String format, URI uri) throws OntologyLoaderException {
        this.ontologyID = ontologyID;
        System.out.print("##### DEBUG: Building a LocalOntology from values: ");
        System.out.println("name: " + name + " version: " + version + " format: " + format + " location: " + uri);

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
                } catch (OntologyLoaderException e) {
                    throw new OntologyLoaderException("OboFile parser failed with Exception: ", e);
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }
    }

    public Set<String> getValidIDs( String id, boolean allowChildren, boolean useTerm) {
        Set<String> terms = new HashSet<String>();

        OntologyTerm resultTerm = ontology.search(id); // will return null if no such term found

        if ( resultTerm != null ) {
            if ( useTerm ) {
                terms.add(resultTerm.getId());
            }
            if ( allowChildren ) {
                Set<OntologyTerm> childTerms = resultTerm.getAllChildren();
                for (OntologyTerm childTerm : childTerms) {
                    terms.add(childTerm.getId());
                }
            }
        } else {
            System.out.println("##### DEBUG: No matching entries in local ontology '" + ontologyID
                    + "' for term: " + id + " returning empty set of valid terms.");
        }

        return terms;
    }

    public boolean isObsoleteID(String id) {
        OntologyTerm term = ontology.search( id );
        if ( term == null ) {
            throw new IllegalStateException("Checking obsolete on non existing term!");
        }
        return term.isObsolete();
    }

    public String getTermNameByID(String id) {
        String result = null;
        // ToDo: check if shortName (or fullName) is the one to use
        OntologyTerm term = ontology.search( id );
        if ( term != null ) {
            result = term.getShortName();
        }
        return result;
    }

    public Set<String> getDirectParentsIDs(String id) {
        Set<String> result = new HashSet<String>();
        Collection<OntologyTerm> terms = ontology.search( id ).getParents();
        for (OntologyTerm term : terms) {
            result.add( term.getId() );
        }
        return result;
    }


}
