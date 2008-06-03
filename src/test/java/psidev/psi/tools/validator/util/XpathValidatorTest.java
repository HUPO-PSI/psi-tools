package psidev.psi.tools.validator.util;

import static org.junit.Assert.*;
import org.junit.*;
import psidev.psi.tools.validator.rules.cvmapping.protein.Protein;
import psidev.psi.tools.validator.rules.cvmapping.protein.CrossReference;
import psidev.psi.tools.validator.rules.cvmapping.protein.Modification;

import java.util.ArrayList;

/**
 * XpathValidator Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @since 2.0.0
 * @version $Id$
 */
public class XpathValidatorTest {

    @Test
    public void valid() throws Exception {
        final String xpath = "modifications/@accession";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );
        protein.getModifications().add( new Modification( "MOD:00001" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        Assert.assertNull( msg, msg );
    }

    @Test
    public void valid2() throws Exception {
        final String xpath = "modifications/accession";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );
        protein.getModifications().add( new Modification( "MOD:00001" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        Assert.assertNull( msg, msg );
    }

    @Test
    public void valid3() throws Exception {
        final String xpath = "/modifications/accEssion";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        System.out.println( msg );
        Assert.assertNull( msg );
    }

    @Test
    public void valid4() throws Exception {
        final String xpath = "/modifications/accEssion";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        System.out.println( msg );
        Assert.assertNull( msg );
    }

    @Test
    public void error() throws Exception {
        final String xpath = "modifcations/@accession";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );
        protein.getModifications().add( new Modification( "MOD:00001" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        System.out.println( msg );
        Assert.assertNotNull( msg );
    }

    @Test
    public void error2() throws Exception {
        final String xpath = "modifications/@accEssion";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );
        protein.getModifications().add( new Modification( "MOD:00001" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        System.out.println( msg );
        Assert.assertNotNull( msg );
    }

    @Test
    public void error3() throws Exception {
        final String xpath = "/bla/modifications/@accEssion";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );
        protein.getModifications().add( new Modification( "MOD:00001" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        System.out.println( msg );
        Assert.assertNotNull( msg );
    }

    @Test
    public void error4() throws Exception {
        final String xpath = "/modifications/accEssion";
        final Protein protein = new Protein("P12345", new CrossReference( "P12345" ) );
        protein.setModifications( new ArrayList<Modification>( ) );
        protein.getModifications().add( new Modification( "MOD:00001" ) );

        XpathValidator validator = new XpathValidator( xpath );
        final String msg = validator.validate( protein );
        System.out.println( msg );
        Assert.assertNotNull( msg );
    }
}
