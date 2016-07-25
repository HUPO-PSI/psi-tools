package psidev.psi.tools.cvrReader;

/**
 * CvMapping exception.
 *
 * @author Samuel Kerrien
 * @version $Id: CvMappingException.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public class CvRuleReaderException extends Exception {
    public CvRuleReaderException() {
        super();
    }

    public CvRuleReaderException( String message ) {
        super( message );
    }

    public CvRuleReaderException( String message, Throwable cause ) {
        super( message, cause );
    }

    public CvRuleReaderException( Throwable cause ) {
        super( cause );
    }
}