package psidev.psi.tools.validator.rules.codedrule;

import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.AbstractRule;
import psidev.psi.tools.ontology_manager.OntologyManager;

import java.util.Collection;

/**
 * Rule intended to perform custom check on a object of type T.
 * Author: florian, Samuel Kerrien (skerrien@ebi.ac.uk)
 * 
 * @since 1.03
 * @version $Id$
 */
public abstract class ObjectRule<T> extends AbstractRule {

    public ObjectRule( OntologyManager ontologyManager ) {
        super( ontologyManager );
    }

    public abstract boolean canCheck( Object object );

    public abstract Collection<ValidatorMessage> check(T t ) throws ValidatorException;
    
}
