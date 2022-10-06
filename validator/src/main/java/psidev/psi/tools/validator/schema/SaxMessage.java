package psidev.psi.tools.validator.schema;

import org.xml.sax.SAXParseException;

/**
 * Represents the content of a SAX Message.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Apr-2006</pre>
 */
public class SaxMessage {

    private String publicId;
    private String systemId;
    private int lineNumber;
    private int columnNumber;
    private String message;
    private String cause;

    //////////////////////////
    // Constructor

    public SaxMessage() {
    }

    public SaxMessage( SAXParseException e ) {
        setPublicId( e.getPublicId() );
        setSystemId( e.getSystemId() );
        setLineNumber( e.getLineNumber() );
        setColumnNumber( e.getColumnNumber() );
        setMessage( e.getMessage() );
        if ( e.getCause() != null ) {
            setCause( e.getCause().getMessage() );
        }
    }

    /////////////////////////
    // Getters and Setters

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId( String publicId ) {
        this.publicId = publicId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId( String systemId ) {
        this.systemId = systemId;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber( int lineNumber ) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber( int columnNumber ) {
        this.columnNumber = columnNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public String getCause() {
        return cause;
    }

    public void setCause( String cause ) {
        this.cause = cause;
    }

    /////////////////////////////////
    // Object's override

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final SaxMessage that = (SaxMessage) o;

        if ( columnNumber != that.columnNumber ) {
            return false;
        }
        if ( lineNumber != that.lineNumber ) {
            return false;
        }
        if ( !cause.equals( that.cause ) ) {
            return false;
        }
        if ( !message.equals( that.message ) ) {
            return false;
        }
        if ( !publicId.equals( that.publicId ) ) {
            return false;
        }
        if ( !systemId.equals( that.systemId ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = publicId.hashCode();
        result = 29 * result + systemId.hashCode();
        result = 29 * result + lineNumber;
        result = 29 * result + columnNumber;
        result = 29 * result + message.hashCode();
        result = 29 * result + cause.hashCode();
        return result;
    }


    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "SaxMessage" );
        sb.append( "{publicId='" ).append( publicId ).append( '\'' );
        sb.append( ", systemId='" ).append( systemId ).append( '\'' );
        sb.append( ", lineNumber=" ).append( lineNumber );
        sb.append( ", columnNumber=" ).append( columnNumber );
        sb.append( ", message='" ).append( message ).append( '\'' );
        if ( cause != null ) {
            sb.append( ", cause='" ).append( cause ).append( '\'' );
        }
        sb.append( '}' );
        return sb.toString();
    }
}