package psidev.psi.tools.validator.xpath;

import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import psidev.psi.tools.validator.xpath.XPathResult;
import psidev.psi.tools.validator.xpath.XPathHelper;

/**
 * XPathHelper Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: XPathHelperTest.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since TODO artifact version
 */
public class XPathHelperTest {

    ////////////////////////////////
    // Compatibility with JUnit 3

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter( XPathHelperTest.class );
    }

    //////////////////////////
    // Initialisation

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    ////////////////////
    // Dummy model

    public static class A {
        int aa = 1;
        B b = new B();

        public int getAa() {
            return aa;
        }

        public B getB() {
            return b;
        }
    }

    public static class B {
        int bb = 2;
        C c = new C();

        public int getBb() {
            return bb;
        }

        public C getC() {
            return c;
        }
    }

    public static class C {
        int cc = 3;

        public int getCc() {
            return cc;
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append( "C" );
            sb.append( "{cc=" ).append( cc );
            sb.append( '}' );
            return sb.toString();
        }
    }

    ////////////////////
    // Tests  

    @Test
    public void lala() throws Exception {
        A a = new A();

        List<XPathResult> results = XPathHelper.evaluateXPath( "/b/c", a );
        System.out.println( "Found " + results.size() + " result(s)" );
        for ( XPathResult result : results ) {
            System.out.println( result );
        }

    }
}
