package psidev.psi.tools.ontology_manager.impl.ols;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

/**
 * Author: florian
 * Date: 07-Aug-2007
 * Time: 15:12:54
 */
public class OlsOntology implements OntologyAccess {

    static Query query;
    String ontologyID;

    public OlsOntology() {
        System.out.print("##### DEBUG: OlsOntology created. ");
        if ( query == null ) {
            System.out.println("Creating new OLS query client.");
            try {
                QueryService locator = new QueryServiceLocator();
                query = locator.getOntologyQuery();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Reusing statically created OLS query client.");
        }
    }


    public void loadOntology(String ontologyID, String name, String version, String format, URI uri) {
        this.ontologyID = ontologyID;
    }

    public Set<String> getValidIDs(String id, boolean allowChildren, boolean useTerm) {
        Set<String> terms = new HashSet<String>();

        try {
            if ( useTerm ) {
                String result = query.getTermById(id, ontologyID );

                // check if the id returns a valid term name - if not, the id is not valid for this ontology
                if ( result.equalsIgnoreCase(id) ) {
                    // term was not found for id
                } else {
                    // id is valid for this ontology
                    terms.add(id);
                }
            }

            if ( allowChildren ) { // get all children
                int[] relationshipTypes = new int[4];
                relationshipTypes[0] = 1;
                relationshipTypes[1] = 2;
                relationshipTypes[2] = 3;
                relationshipTypes[3] = 4;

                Map resultMap = query.getTermChildren(id, ontologyID, -1, relationshipTypes );
                if ( resultMap == null ) {
                    // not sure what is returned from OLS when term has no children
                } else {
                    // ToDo: check asignment check if all keys of the map are Strings
                    terms.addAll(resultMap.keySet());
                }

            }

        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException while trying to connect to OLS.");
        }

        return terms;
    }

    public boolean isObsoleteID(String id) {
        boolean result;
        try {
            // check the existence of the term first -> OLS does return false if the term does not exist!
            String s = query.getTermById( id, ontologyID );
            if ( s.equalsIgnoreCase(id) ) {
                // term not in database (if instead of the term name the accession is returned)
                throw new IllegalStateException("Checking obsolete on non existing term!");
            }
            result =  query.isObsolete( id, ontologyID );
        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException while trying to connect to OLS.");
        }
        return result;
    }

    public String getTermNameByID(String id) {
        String result;
        try {
            result =  query.getTermById( id, ontologyID );
        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException while trying to query OLS for: "
                    + id + " in ontology: " + ontologyID);
        }
        return result;
    }

    public Set<String> getDirectParentsIDs(String id) {
        Set<String> result;
        try {
            result = query.getTermParents( id, ontologyID ).keySet();
        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException while trying to connect to OLS.");
        }
        return result;
    }


}
