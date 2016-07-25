package psidev.psi.tools.objectRuleReader;

/**
 * Ontology confid reader exception.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class ObjectRuleReaderException extends Exception {
    public ObjectRuleReaderException() {
        super();
    }

    public ObjectRuleReaderException( String message ) {
        super( message );
    }

    public ObjectRuleReaderException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ObjectRuleReaderException( Throwable cause ) {
        super( cause );
    }
}