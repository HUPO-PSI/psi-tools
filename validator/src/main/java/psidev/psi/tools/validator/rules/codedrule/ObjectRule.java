package psidev.psi.tools.validator.rules.codedrule;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.AbstractRule;

import java.util.Collection;

/**
 * Rule intended to perform custom check on a object of type T.
 *
 * A rule is not thread safe so if the ontologyManager or the rule itself is shared in a web application, it is not recommended to
 * create a rule which will modify the ontologyManager or one of the variables of the rule itself.
 *
 * A valid object rule should only use and read the variables of the ontologyManager when needed and never changes any of its variables.
 * Indeed, during a validation, there is no order for executing an object rule so if the environment of the rules set at the beginning is changing during
 * the validation, we can't predict the effect on the other rules.
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