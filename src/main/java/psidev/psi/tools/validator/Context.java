package psidev.psi.tools.validator;

/**
 * <b>Context of a Validator Message</b>.
 * <p/>
 * This is kept very simple. <br/> That class can be extended and given more specific attributes, also, when extending
 * the toString() should be customized so that the ValidatorMessage using it get to display a meaningfull context.
 *
 * @author Matthias Oesterheld
 * @version $Id: Context.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 04.01.2006; 15:51:32
 */
public class Context {

    /**
     * The default context has only a String.
     */
    private String context;

    public Context( String context ) {
        this.context = context;
    }

    /**
     * Returns the context
     *
     * @return description of context.
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the context
     *
     * @param context description of context
     */
    public void setContext( String context ) {
        this.context = context;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer( 128 );
        sb.append( "Context(" );
        if ( context != null ) {
            sb.append( context );

        } else {
            sb.append( " No context specified" );
        }
        sb.append( " )" );
        return sb.toString();
    }
}