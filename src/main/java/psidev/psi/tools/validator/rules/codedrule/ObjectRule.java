package psidev.psi.tools.validator.rules.codedrule;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.AbstractRule;

import java.util.Collection;

/**
 * Rule intended to perform custom check on a object of type T.
 *
 * @author florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.03
 */
public abstract class ObjectRule<T> extends AbstractRule {

    public ObjectRule( OntologyManager ontologyManager ) {
        super( ontologyManager );
    }

    /**
     * Verifies if the current rule can apply its check on the given object. Usually that verification is applied to
     * the Class of the given object.
     *
     * @param t the object we want to check on.
     * @return true is the current rule is able to check on it.
     */
    public abstract boolean canCheck( Object t );

    /**
     * Validates the given object.
     *
     * @param t the object to be validated.
     * @return a non null collection of messages.
     * @throws ValidatorException should an error occur in the process of validating the given object.
     */
    public abstract Collection<ValidatorMessage> check( T t ) throws ValidatorException;
}