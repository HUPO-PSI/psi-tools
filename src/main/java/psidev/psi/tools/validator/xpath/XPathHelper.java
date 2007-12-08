package psidev.psi.tools.validator.xpath;

import psidev.psi.tools.validator.ValidatorException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <b> XPath Helper utilities </b>.
 * <p/>
 *
 * @author Matthias Oesterheld
 * @version $Id: XPathHelper.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 05.01.2006; 15:19:47
 */
public class XPathHelper {

    /**
     * TODO document that method.
     *
     * @param xpath
     * @param root
     * @return TODO
     * @throws ValidatorException
     */
    public static List<XPathResult> evaluateXPath( String xpath, Object root ) throws ValidatorException {
        return evaluateXPathWithClass( xpath, root, XPathResult.class );
    }

    /**
     * TODO document that method.
     *
     * @param xpath
     * @param root
     * @param clazz
     * @return TODO
     * @throws ValidatorException
     */
    protected static List<XPathResult> evaluateXPathWithClass( String xpath, Object root, Class clazz ) throws ValidatorException {
        JXPathContext ctx = JXPathContext.newContext( root );
        Iterator iter = ctx.iteratePointers( xpath );
        List<XPathResult> results = new ArrayList<XPathResult>();
        while ( iter.hasNext() ) {
            Pointer p = ( Pointer ) iter.next();
            try {
                Constructor constructor = clazz.getConstructor( new Class[]{Pointer.class, JXPathContext.class} );
                results.add( ( XPathResult ) constructor.newInstance( new Object[]{p, ctx} ) );
            } catch ( Exception e ) {
                throw new ValidatorException( "Error creating XPath Result class" );
            }
        }
        return results;
    }

    public static boolean hasTrailingSlash( String xpath ) {
        return xpath.trim().endsWith( "/" );
    }

    public static boolean hasLeadingSlash( String xpath ) {
        return xpath.trim().startsWith( "/" );
    }

    public static String removeTrailingSlash( String xpath ) {
        if( hasTrailingSlash( xpath )) {
            xpath = xpath.substring(0, xpath.length()-1);
        }
        return xpath;
    }
}