package psidev.psi.tools.validator;

/**
 * The context of the Validator
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27-Apr-2010</pre>
 */

public class ValidatorContext {
    
    private ValidatorConfig validatorConfig;

    private static ThreadLocal<ValidatorContext> instance = ThreadLocal.withInitial(() -> new ValidatorContext());

    private ValidatorContext(){
         this.validatorConfig = new ValidatorConfig();
    }

    public ValidatorConfig getValidatorConfig() {
        return validatorConfig;
    }

    public static ValidatorContext getCurrentInstance() {
        return instance.get();
    }
}
