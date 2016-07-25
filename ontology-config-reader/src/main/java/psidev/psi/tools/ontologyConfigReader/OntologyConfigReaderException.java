package psidev.psi.tools.ontologyConfigReader;

/**
 * Ontology confid reader exception.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class OntologyConfigReaderException extends Exception {
    public OntologyConfigReaderException() {
        super();
    }

    public OntologyConfigReaderException( String message ) {
        super( message );
    }

    public OntologyConfigReaderException( String message, Throwable cause ) {
        super( message, cause );
    }

    public OntologyConfigReaderException( Throwable cause ) {
        super( cause );
    }
}