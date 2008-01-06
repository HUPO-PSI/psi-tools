package psidev.psi.tools.validator.rules.cvmapping;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import psidev.psi.tools.cvrReader.CvRuleReader;
import psidev.psi.tools.cvrReader.CvRuleReaderException;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.xpath.XPathHelper;
import psidev.psi.tools.validator.xpath.XPathResult;
import psidev.psi.tools.validator.rules.cvmapping.house.House;
import psidev.psi.tools.validator.rules.cvmapping.house.HouseFactory;
import psidev.psi.tools.validator.rules.cvmapping.house.Bike;
import psidev.psi.tools.validator.rules.cvmapping.protein.Modification;
import psidev.psi.tools.validator.rules.cvmapping.protein.Protein;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * CvMappingRule Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: CvMappingRuleTest.java 668 2007-06-29 16:44:18 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public class CvMappingRuleTest {

    static InputStream oMngrConfig = CvMappingRuleTest.class.getClassLoader().getResourceAsStream("flo/ontologies-test.xml");
    static OntologyManager ontologyMngr;

    static {
        try {
            ontologyMngr = new OntologyManager(oMngrConfig);
        } catch (OntologyLoaderException e) {
            e.printStackTrace();
        }
    }

    private void print( Collection<ValidatorMessage> messages ) {
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
    }

    //////////////////////////
    // Tests checkCvMapping

    @Test
    public void checkCvMapping_noError() throws Exception {
        // check a fine mapping.
        File input = new File( CvMappingRuleTest.class.getResource( "/fine-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void checkCvMapping_obsolete() throws Exception {
        // checks that one out of two cvterm is obsolete, so it gets removed.
        // Yet the rule remains as it still has one valid cvTerm.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-obsolete-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        CvRule rule = ruleMngr.getCvRules().iterator().next(); // only one rule here
        int termsBeforeCheck = rule.getCVTerms().size();
        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        int termsAfterCheck = rule.getCVTerms().size();
        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 1, messages.size() );
        // test the removal of one CVTerm
        Assert.assertEquals( "One CVTerm should have been removed due to it being obsolete.", termsBeforeCheck -1, termsAfterCheck );
    }

    @Test
    public void checkCvMapping_all_cvterm_obsolete() throws Exception {
        // checks that both cvTerm in the rule are obsolete and that the rule was removed.
        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-obsolete2-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        int rulesBeforeCheck = ruleMngr.getCvRules().size();
        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        int rulesAfterCheck = ruleMngr.getCvRules().size();
        Assert.assertNotNull( messages );
        print( messages );
        // one error for each cvTerm and one for the rule
        Assert.assertEquals( 3, messages.size() );
        // test removal of one complete rule
        Assert.assertEquals( "One complete rule should have been removed due to all its CvTerms being obsolete.", rulesBeforeCheck - 1, rulesAfterCheck);
    }

    @Test
    public void checkCvMapping_mixed_errors() throws Exception {
        // checks that both cvTerm in the rule are removed (obsolete and term non existing in an ontology)
        // and that the rule gets also removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-obsolete3-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        // one error for each cvTerm and one for the rule
        Assert.assertEquals( 3, messages.size() );
    }

    @Test
    public void checkCvMapping_children_issue() throws Exception {

        // checks that both cvTerm in the rule are removed (obsolete and tern non existing in an ontology)
        // and that the rule gets also removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-error1-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        // expect 1 message:
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void checkCvMapping_unknown_terms() throws Exception {

        // checks that both cvTerm in the rule are removed (they don't exist in MS) and that the rule gets also removed.

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-error2-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        // one error for each cvTerm and one for the rule
        // expect 5 messages: one error for each term
        // + one message that the rule has been removed doe to no longer containing terms
        Assert.assertEquals( 5, messages.size() );
    }

    @Test
    public void checkCvMapping_unusable_term() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-error3-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        // expect 1 message: CvTerm has been removed due to: neither the term nor any of its children can be used
        Assert.assertEquals( 1, messages.size() );
    }

    ////////////////////
    // Tests check

    @Test
    public void check_noMessage() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void check_useTerm_is_false() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }      

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_allowChildren_1() throws Exception {

        // This test will check that the term pointed out by the XPath is a children of the one specified in the cvmapping.
        // more precisely, that PSI:1000084 is a child term of PSI:1000010

        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "PSI:1000084" ); // PSI:1000084 is a child of PSI:1000010
        Collection<ValidatorMessage> messages = ruleMngr.check( house, "/house" );

        Assert.assertNotNull( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void check_allowChildren_2() throws Exception {

        // This test will report an error because the term pointed out by the XPath is not a children of the one specified in the cvmapping.
        // more precisely, that PSI:1000084 is a child term of PSI:1000010

        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals("Ther should not be any messages from the cv rule self check.", 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "PSI:1000292" ); // Mass Spectrograph: different branch than PSI:1000010 (analyser)
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_term_not_found_may() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 1, messages.size() );

        ValidatorMessage m = messages.iterator().next();
        Assert.assertEquals( MessageLevel.INFO, m.getLevel() );
    }

    @Test
    public void check_term_not_found_should() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample3-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 1, messages.size() );

        ValidatorMessage m = messages.iterator().next();
        Assert.assertEquals( MessageLevel.WARN, m.getLevel() );
    }

    @Test
    public void check_term_not_found_must() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample4-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 1, messages.size() );

        ValidatorMessage m = messages.iterator().next();
        Assert.assertEquals( MessageLevel.ERROR, m.getLevel() );
    }

    @Test
    public void check_bad_xpath() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample5-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        house.getKitchen().setNote( "FAKE:00000" ); // that id is not in any given ontology
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        for (Iterator<CvRule> lIterator = cvRules.iterator(); lIterator.hasNext();) {
            CvRule cvRule = lIterator.next();
            messages.addAll(cvRule.check(house, "/house"));
        }

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 2, messages.size() );
    }

    @Ignore
    @Test
    public void check_obsoleteTerm() {
        // check that the terms in the CvTerm are not obsolote. If so, create a ValidationMessage
        // do this at build time
    }

    @Test
    public void check_nonRepeatable() throws CvRuleReaderException, ValidatorException {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample8-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();

        Collection<CvRule> cvRules = ruleMngr.getCvRules();
        Assert.assertEquals( 1, cvRules.size() );
        final CvRule cvRule = cvRules.iterator().next();
        final Collection<ValidatorMessage> messages = cvRule.check( house, "/house" );

        Assert.assertNotNull( messages );
        print( messages );
        
        // expect 2 messages because of the following reasons:
        // 1) term 'bike' not in ontology -- TODO is that really an error ?
        // 2) term 'alias type' appears two times although it is specified as not repeatable
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_not_a_child_of() {
    }

    @Ignore
    @Test
    public void playingWithXpath() throws Exception {

         House house = HouseFactory.buildSimpleHouse();

        final List<XPathResult> results = XPathHelper.evaluateXPath( "/garage/bikes", house );
        for ( XPathResult result : results ) {
            System.out.println( result.getResult() ); // gives a Bike

            Bike bike = (Bike ) result.getResult();
            final List<XPathResult> colors = XPathHelper.evaluateXPath( "/@color", bike );
            for ( XPathResult color : colors ) {
                System.out.println( "   > " + color.getResult() );
            }
        }
    }

    @Test
    public void check_useTermName() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample7-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        House house = HouseFactory.buildSimpleHouse();
        Collection<ValidatorMessage> messages = ruleMngr.check( house, "/house" );

        Assert.assertNotNull( messages );
        print( messages );
        // expect 3 message: two 'alias type' (useTerm="false") + one 'bike' (not in ontology) are not in the valid terms
        Assert.assertEquals( 3, messages.size() );
    }

    @Ignore // that's a stupid test, one cannot have both terms is there's only a single color by bike !
    @Test
    public void check_and_operator() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample9-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        House house = HouseFactory.buildSimpleHouse();
        messages = ruleMngr.check( house, "/house" );

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test( expected = ValidatorException.class )
    public void check_null() throws Exception {

        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-house-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages0 = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages0 );
        Assert.assertEquals( 0, messages0.size() );

        ruleMngr.check( null );
    }

    ///////////////////////////////
    // Protein model based tests

    @Test
    public void check_or_operator_protein() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample1-protein-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print(messages);
        Assert.assertEquals( 0, messages.size() );

        // building model to apply checks on
        Protein protein = new Protein( "prot1", new Modification( "MOD:00001" ) );
        messages = ruleMngr.check( protein, "/protein" );

        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void check_or_operator2_protein() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample2-protein-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print(messages);
        Assert.assertEquals( 0, messages.size() );

        // building model to apply checks on
        Protein protein = new Protein( "prot1",
                                       new Modification( "MOD:00001" ),
                                       new Modification( "MOD:00400" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        protein = new Protein( "prot1",
                               new Modification( "MOD:00001" ),
                               new Modification( "MOD:00400" ),  
                               new Modification( "MOD:00649" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void check_and_operator1_protein() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample3-protein-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print(messages);
        Assert.assertEquals( 0, messages.size() );

        // building model to apply checks on
        Protein protein = new Protein( "prot1",
                                       new Modification( "MOD:00001" ),
                                       new Modification( "MOD:00649" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        protein = new Protein( "prot1",
                               new Modification( "MOD:00649" ) );
        messages = ruleMngr.check( protein, "/protein" );
        // message should be about having only 2 CV terms instead of the 3 required (operator is AND)
        print( messages );
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_xor_operator1_protein() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample4-protein-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        // building model to apply checks on
        Protein protein = new Protein( "prot1",
                               new Modification( "MOD:00649" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        protein = new Protein( "prot1",
                               new Modification( "MOD:00001" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        protein = new Protein( "prot1",
                               new Modification( "MOD:00400" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        protein = new Protein( "prot1",
                               new Modification( "MOD:00001" ),
                               new Modification( "MOD:00649" ) );
        messages = ruleMngr.check( protein, "/protein" );
        // message should be about not exactly 1 term in the protein when we specific the operator XOR
        print( messages );
        Assert.assertEquals( 1, messages.size() );

        protein = new Protein( "prot1",
                               new Modification( "MOD:00001" ),
                               new Modification( "MOD:00400" ),
                               new Modification( "MOD:00649" ) );
        messages = ruleMngr.check( protein, "/protein" );
        // message should be about not exactly 1 term in the protein when we specific the operator XOR
        print( messages );
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_xor_error() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample4-protein-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        // building model to apply checks on - one term from the list and 2 others not expected
        Protein protein = new Protein( "prot1",
                               new Modification( "MOD:00003" ),
                               new Modification( "MOD:00004" ) );
        messages = ruleMngr.check( protein, "/protein" );
        // message should be about not exactly 1 term in the protein when we specific the operator XOR
        print( messages );
        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void check_xor_failing() throws Exception {
        File input = new File( CvMappingRuleTest.class.getResource( "/sample4-protein-cvmapping.xml" ).getFile() );
        CvRuleReader reader = new CvRuleReader();
        CvMapping cvMapping = reader.read( input );
        CvRuleManager ruleMngr = new CvRuleManager( ontologyMngr, cvMapping );

        Collection<ValidatorMessage> messages = ruleMngr.checkCvMapping();
        Assert.assertNotNull( messages );
        print( messages );
        Assert.assertEquals( 0, messages.size() );

        // building model to apply checks on - one term from the list and 2 others not expected
        Protein protein = new Protein( "prot1",
                               new Modification( "MOD:00003" ),
                               new Modification( "MOD:00649" ),
                               new Modification( "MOD:00004" ) );
        messages = ruleMngr.check( protein, "/protein" );
        print( messages );
        Assert.assertEquals( 0, messages.size() );
    }
}