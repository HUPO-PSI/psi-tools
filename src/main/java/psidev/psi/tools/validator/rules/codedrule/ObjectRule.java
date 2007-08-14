package psidev.psi.tools.validator.rules.codedrule;

import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.rules.Rule;

import java.util.Collection;

/**
 * Author: florian
 * Date: 18-Jul-2007
 * Time: 11:57:39
 */
public interface ObjectRule<T> extends Rule {

    boolean canCheck( Object object );

    Collection<ValidatorMessage> check(T t ) throws ValidatorException;
    
}
