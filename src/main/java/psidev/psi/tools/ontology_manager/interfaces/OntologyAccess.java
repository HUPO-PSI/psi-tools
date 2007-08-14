package psidev.psi.tools.ontology_manager.interfaces;

import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;

import java.net.URI;
import java.util.Set;

/**
 * Author: florian
 * Date: 07-Aug-2007
 * Time: 14:10:02
 */
public interface OntologyAccess {

    void loadOntology( String ontologyID, String name, String version, String format, URI uri) throws OntologyLoaderException;

    /**
     * Creates a Set of valid ontology term IDs accoring to the specified parameters.
     * @param id the ontology term ID to search for.
     * @param allowChildren if true, will add the IDs of all children of the specified term.
     * @param useTerm if false, will not include the ID of the term itself.
     * @return a Set of ontology IDs according to the specified parameters. Must always return a set. It might be empty. but must not be null.
     */
    Set<String> getValidIDs( String id, boolean allowChildren, boolean useTerm );

    boolean isObsoleteID( String id );

    String getTermNameByID( String id );

    Set<String> getDirectParentsIDs( String id );

    // ToDo: isParent, getParents, is Child, getChildren(term, level),

}
