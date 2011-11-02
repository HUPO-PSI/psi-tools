package psidev.psi.tools.ontology_manager.impl.local;

import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.util.Collection;
import java.util.Set;

/**
 * Template for Ontology.
 *
 * NOTE : Ontology was the original interface but did not offer flexibility for using different extensions of OntologyTermI.
 * That is why this template interface has been created.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public interface OntologyTemplate<T extends OntologyTermI> {
    boolean hasTerms();

    T search( String id );

    Collection<T> getRoots();

    Collection<T> getOntologyTerms();

    Collection<T> getObsoleteTerms();

    boolean isObsoleteTerm( T term );

    Set<T> getDirectParents( T term );

    Set<T> getDirectChildren( T term );

    Set<T> getAllParents( T term );

    Set<T> getAllChildren( T term );

    public void addTerm( T term );
    public void addObsoleteTerm( T term );
    public void addLink( String parentId, String childId );
}
