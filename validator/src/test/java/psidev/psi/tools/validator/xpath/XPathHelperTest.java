package psidev.psi.tools.validator.xpath;

import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    public void evaluateXPath() throws Exception {
        A a = new A();
        List<XPathResult> results = XPathHelper.evaluateXPath( "/b/c", a );
        Assert.assertEquals( 1, results.size() );
    }

    @Test
    public void hasLeadingSlash() throws Exception {
        Assert.assertTrue( XPathHelper.hasLeadingSlash( "/b/c" ) );
        Assert.assertFalse( XPathHelper.hasLeadingSlash( "b/c" ) );
    }

    @Test
    public void hasTrailingSlash() throws Exception {
        Assert.assertTrue( XPathHelper.hasTrailingSlash( "/b/c/" ) );
        Assert.assertFalse( XPathHelper.hasTrailingSlash( "b/c" ) );
    }

    @Test
    public void removeeTrailingSlash() throws Exception {
        Assert.assertEquals( "/b/c", XPathHelper.removeTrailingSlash( "/b/c/" ) );
        Assert.assertEquals( "/b/c", XPathHelper.removeTrailingSlash( "/b/c" ) );
    }
}
