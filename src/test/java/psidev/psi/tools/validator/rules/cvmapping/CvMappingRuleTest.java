package psidev.psi.tools.validator.rules.cvmapping;

import junit.framework.JUnit4TestAdapter;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.tools.cvrReader.CvRuleReader;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRules;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.cvmapping.house.House;
import psidev.psi.tools.validator.rules.cvmapping.house.HouseFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * CvMappingRule Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: CvMappingRuleTest.java 668 2007-06-29 16:44:18 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public class CvMappingRuleTest {

    // ToDo: all rules are designed for the object to test -> no xpath definition necessary
    // ToDo: make this more generic: all rules have the xpath starting form the root and the check(object, xpath) method is used

    static InputStream oMngrConfig = CvMappingRuleTest.class.getClassLoader().getResourceAsStream("flo/ontologies-test.xml");
    static OntologyManager ontologyMngr;

    static {
        try {
            ontologyMngr = new OntologyManager(oMngrConfig);
        } catch (OntologyLoaderException e) {
            e.printStackTrace();
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter( CvMappingRuleTest.class );
    }

    //////////////////////////
    // Tests checkCvMapping

    @Test
    public void checkCvMapping() throws Exception {
        System.out.println("========== Test: checkCvMapping");

        // check a fine mapping.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample5-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
//        CvRuleManager rule = CvMappingRuleFactory.getInstance().create( cvMapping );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void checkCvMapping_obsolete() throws Exception {
        System.out.println("========== Test: checkCvMapping_obsolete");

        // checks that one out of two cvterm is obsolete, so it gets removed.
        // Yet the rule remains as it still has one valid cvTerm.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-obsolete-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        CvRule rule = ruleMngr.getCvRules().iterator().next(); // only one rule here
        int termsBeforeCheck = rule.getCVTerms().size();
        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        int termsAfterCheck = rule.getCVTerms().size();
        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        Assert.assertEquals( 1, messages.size() );
        // test the removal of one CVTerm
        Assert.assertEquals( "One CVTerm should have been removed due to it being obsolete.", termsBeforeCheck -1, termsAfterCheck );
    }

    @Test
    public void checkCvMapping_all_cvterm_obsolete() throws Exception {
        System.out.println("========== Test: checkCvMapping_all_cvterm_obsolete");

        // checks that both cvTerm in the rule are obsolete and that the rule was removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-obsolete2-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        int rulesBeforeCheck = ruleMngr.getCvRules().size();
        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        int rulesAfterCheck = ruleMngr.getCvRules().size();
        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // one error for each cvTerm and one for the rule
        Assert.assertEquals( 3, messages.size() );
        // test removal of one complete rule
        Assert.assertEquals( "One complete rule should have been removed due to all its CvTerms being obsolete.", rulesBeforeCheck - 1, rulesAfterCheck);
    }

    @Test
    public void checkCvMapping_mixed_errors() throws Exception {
        System.out.println("========== Test: checkCvMapping_mixed_errors");
        // checks that both cvTerm in the rule are removed (obsolete and term non existing in an ontology)
        // and that the rule gets also removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-obsolete3-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // one error for each cvTerm and one for the rule
        Assert.assertEquals( 3, messages.size() );
    }

    @Test
    public void checkCvMapping_children_issue() throws Exception {

        System.out.println("========== Test: checkCvMapping_children_issue");
        // checks that both cvTerm in the rule are removed (obsolete and tern non existing in an ontology)
        // and that the rule gets also removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-error1-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // expect 1 message: 
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void checkCvMapping_unknown_terms() throws Exception {

        System.out.println("========== Test: checkCvMapping_unknown_terms");
        // checks that both cvTerm in the rule are removed (they don't exist in MS) and that the rule gets also removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-error2-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // one error for each cvTerm and one for the rule
        // expect 5 messages: one error for each term
        // + one message that the rule has been removed doe to no longer containing terms
        Assert.assertEquals( 5, messages.size() );
    }

    @Test
    public void checkCvMapping_unusable_term() throws Exception {

        System.out.println("========== Test: checkCvMapping_unusable_term");
        // TODO this is wrong ... it is possible that a term cannot be used ... update the CvMappingRule
        // ToDo: check that a specific term was NOT used?
        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-error3-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // expect 1 message: CvTerm has been removed due to: neither the term nor any of its children can be used
        Assert.assertEquals( 1, messages.size() );
    }

    ////////////////////
    // Tests check

    @Test
    public void check_noMessage() throws Exception {

        System.out.println("========== Test: check_noMessage");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("There should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void check_useTerm_is_false() throws Exception {

        System.out.println("========== Test: check_useTerm_is_false");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // ToDo: check!
        // expect 2 messages:
//        Assert.assertEquals( 2, messages.size() );
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_allowChildren_1() throws Exception {

        System.out.println("========== Test: check_allowChildren_1");
        // This test will check that the term pointed out by the XPath is a children of the one specified in the cvmapping.
        // more precisely, that PSI:1000084 is a child term of PSI:1000010

        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "PSI:1000084" ); // time-of-flight: children term of PSI:1000010 (analyser)
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void check_allowChildren_2() throws Exception {

        System.out.println("========== Test: check_allowChildren_2");
        // This test will report an error because the term pointed out by the XPath is not a children of the one specified in the cvmapping.
        // more precisely, that PSI:1000084 is a child term of PSI:1000010

        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "PSI:1000292" ); // Mass Spectrograph: different branch than PSI:1000010 (analyser)
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        // ToDo: check!
        //expect 2 messages:
//        Assert.assertEquals( 2, messages.size() );
        Assert.assertEquals( 1, messages.size() );

    }

    @Test
    public void check_term_not_found_may() throws Exception {

        System.out.println("========== Test: check_term_not_found_may");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        Assert.assertEquals( 1, messages.size() );

        ValidatorMessage m = messages.iterator().next();
        Assert.assertEquals( MessageLevel.INFO, m.getLevel() );

    }

    @Test
    public void check_term_not_found_should() throws Exception {

        System.out.println("========== Test: check_term_not_found_should");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample3-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        Assert.assertEquals( 1, messages.size() );

        ValidatorMessage m = messages.iterator().next();
        Assert.assertEquals( MessageLevel.WARN, m.getLevel() );

    }

    @Test
    public void check_term_not_found_must() throws Exception {

        System.out.println("========== Test: check_term_not_found_must");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample4-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        Assert.assertEquals( 1, messages.size() );

        ValidatorMessage m = messages.iterator().next();
        Assert.assertEquals( MessageLevel.ERROR, m.getLevel() );

    }

    @Test
    public void check_bad_xpath() throws Exception {

        System.out.println("========== Test: check_bad_xpath");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample5-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( "message = " + message );
        }
        Assert.assertEquals( 1, messages.size() );

//        ValidatorMessage m = messages.iterator().next();
//        Assert.assertEquals( MessageLevel.ERROR, m.getLevel() );

    }

    @Test
    public void check_obsoleteTerm() {
        // check that the terms in the CvTerm are not obsolote. If so, create a ValidationMessage
        // do this at build time
    }

    @Test
    public void check_nonRepeatable() {
    }

    @Test
    public void check_not_a_child_of() {
    }

    @Test
    public void check_useTermName() throws Exception {

        System.out.println("========== Test: check_useTermName");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample7-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("There should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        Collection<ValidatorMessage> messages = ruleMngr.check( house );

        Assert.assertNotNull( messages );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
        Assert.assertEquals( 2, messages.size() );
    }



    @Test( expected = ValidatorException.class )
    public void check_null() throws Exception {

        System.out.println("========== Test: check_null");
        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMappingRules cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        ruleMngr.check( null );
    }
}
