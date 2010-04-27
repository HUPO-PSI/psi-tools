package psidev.psi.tools.validator;

/**
 * The context of the Validator
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27-Apr-2010</pre>
 */

public abstract class ValidatorContext {
    
    protected ValidatorConfig validatorConfig;

    protected ValidatorContext(){
        
    }

    public ValidatorConfig getValidatorConfig() {
        return validatorConfig;
    }
}
