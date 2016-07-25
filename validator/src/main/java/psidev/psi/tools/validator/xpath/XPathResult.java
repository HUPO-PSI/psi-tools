package psidev.psi.tools.validator.xpath;

import psidev.psi.tools.validator.Context;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * <b> -- Short Description -- </b>.
 * <p/>
 * TODO document that method.
 *
 * @author Matthias Oesterheld
 * @version $Id: XPathResult.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 05.01.2006; 15:18:58
 */
public class XPathResult {

    /**
     * TODO document that method.
     */
    protected Pointer pointer;

    /**
     * TODO document that method.
     */
    protected JXPathContext rootContext;

    /**
     * TODO document that method.
     * @param pointer
     * @param rootContext
     */
    public XPathResult( Pointer pointer, JXPathContext rootContext ) {
        this.pointer = pointer;
        this.rootContext = rootContext;
    }

    /**
     * TODO document that method.
     * @return  TODO
     */
    public Object getResult() {
        return pointer.getNode();
    }

    public Object getRootNode() {
        return pointer.getRootNode();
    }

    /**
     * TODO document that method.
     * @return TODO
     */
    public Context getContext() {
        return new Context( pointer.getNode().toString() );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "XPathResult" );
        sb.append( "{pointer=" ).append( pointer );
        sb.append( ", rootContext=" ).append( rootContext );
        sb.append( ", result=" ).append( getResult() );
        sb.append( '}' );
        return sb.toString();
    }

    public String asPath() {
        return this.pointer.asPath();
    }
}