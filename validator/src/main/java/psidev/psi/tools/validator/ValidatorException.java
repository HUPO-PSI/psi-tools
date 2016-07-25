package psidev.psi.tools.validator;

/**
 * <b> Exception specific to the validator framework. </b>.
 *
 * @author Matthias Oesterheld
 * @version $Id: ValidatorException.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 2006-01-04
 */
public class ValidatorException extends Exception {

    public ValidatorException( String message ) {
        super( message );
    }

    public ValidatorException( String message, Throwable cause ) {
        super( message, cause );
    }
}