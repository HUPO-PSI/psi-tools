package psidev.psi.tools.ontology_manager.impl.local;

import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

/**
 * <b> Behaviour of an Ontology.</b>
 * <p/>
 *
 * NOTE : the Ontology interface is now extending OntologyTemplate. Nothing has changed in the methods of this interface which is still using OntologyTermI.
 * As we needed some flexibility when using different extension of the basic OntologyTermI, we created a template for Ontology and for retrocompatibility,
 * this interface has been kept and extends OntologyTemplate<OntologyTermI>
 *
 * @author Samuel Kerrien
 * @author Matthias Oesterheld
 * @version $Id: Ontology.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public interface Ontology extends OntologyTemplate<OntologyTermI> {

}