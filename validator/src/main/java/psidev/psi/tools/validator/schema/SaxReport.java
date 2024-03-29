package psidev.psi.tools.validator.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Models the product of a SAX Validation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class SaxReport {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( SaxReport.class );

    private boolean valid = true;

    private List<SaxMessage> messages = new ArrayList<>();

    public SaxReport() {
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid( boolean valid ) {
        this.valid = valid;
    }

    public Collection<SaxMessage> getMessages() {
        return Collections.unmodifiableList( messages );
    }

    public void addMessage( SaxMessage message ) {
        log.info( "Adding message: " + message );
        messages.add( message );
        valid = false;
    }
}