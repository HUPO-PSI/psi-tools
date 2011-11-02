package psidev.psi.tools.ontology_manager.interfaces;

/**
 * Defines what can be asked to an ontology.
 *
 * NOTE : the OntologyAccess is now extending OntologyAccessTemplate. Nothing has changed in the methods of this interface which is still using OntologyTermI.
 * As we needed some flexibility when using different extension of the basic OntologyTermI, we created a template for OntologyAccess and for retrocompatibility,
 * this interface has been kept and extends OntologyAccessTemplate<OntologyTermI>
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.0
 */
public interface OntologyAccess extends OntologyAccessTemplate<OntologyTermI>{

}