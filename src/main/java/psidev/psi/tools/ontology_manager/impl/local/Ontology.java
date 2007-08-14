package psidev.psi.tools.ontology_manager.impl.local;

import psidev.psi.tools.ontology_manager.impl.local.model.OntologyTerm;

import java.util.Collection;

/**
 * <b> Behaviour of an Ontology.</b>
 * <p/>
 *
 * @author Samuel Kerrien, Matthias Oesterheld
 * @version $Id: Ontology.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 04.01.2006; 18:34:54
 */
public interface Ontology {
    
    boolean hasTerms();

    OntologyTerm search( String id );

    Collection<OntologyTerm> getRoots();

    Collection<OntologyTerm> getOntologyTerms();

    Collection<OntologyTerm> getObsoleteTerms();
}