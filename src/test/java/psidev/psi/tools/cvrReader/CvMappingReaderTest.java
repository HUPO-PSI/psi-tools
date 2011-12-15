package psidev.psi.tools.cvrReader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvReference;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;

import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * CvMappingReader Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: CvMappingReaderTest.java 669 2007-06-29 16:45:04 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 0.3
 */
public class CvMappingReaderTest {

    public static final String SAMPLE_RULE_DEFINITION =
            "<CvMapping modelName=\"string\" modelURI=\"http://www.company.org/lala/sonoras\" modelVersion=\"string\">\n" +
            "\n" +
            "    <CvReferenceList cvSourceVersion=\"anySimpleType\">\n" +
            "        <CvReference cvIdentifier=\"A\" cvName=\"string\"/>\n" +
            "        <CvReference cvIdentifier=\"B\" cvName=\"string\"/>\n" +
            "    </CvReferenceList>\n" +
            "\n" +
            "    <CvMappingRuleList>\n" +
            "        <CvMappingRule id=\"R1\" name=\"sample rule 1\" scopePath=\"/a/b/c/d/e\" cvElementPath=\"/a/b/c\" cvTermsCombinationLogic=\"AND\" requirementLevel=\"SHOULD\">\n" +
            "            <CvTerm cvIdentifierRef=\"A\" termAccession=\"XX:0001\" termName=\"lala\" useTermName=\"false\" useTerm=\"true\" allowChildren=\"false\" isRepeatable=\"true\"/>\n" +
            "            <CvTerm cvIdentifierRef=\"B\" termAccession=\"YY:0001\" termName=\"foo\" useTermName=\"true\" useTerm=\"false\" allowChildren=\"true\" isRepeatable=\"false\"/>\n" +
            "            <Description>some description</Description>\n" +
            "            <HowToFixTips>" +
            "                <Tip>tip one</Tip>" +
            "                <Tip>tip two</Tip>" +
            "            </HowToFixTips>" +
            "        </CvMappingRule>\n" +
            "        <CvMappingRule id=\"R2\" name=\"sample rule 2\" scopePath=\"/a/b/c/d/e\" cvElementPath=\"/a/b/c\" cvTermsCombinationLogic=\"AND\" requirementLevel=\"SHOULD\">\n" +
            "            <CvTerm cvIdentifierRef=\"A\" termAccession=\"XX:0001\" termName=\"lala\" useTermName=\"false\" useTerm=\"true\" allowChildren=\"false\" isRepeatable=\"true\"/>\n" +
            "        </CvMappingRule>\n" +
            "    </CvMappingRuleList>\n" +
            "</CvMapping>";

    private File getTargetDirectory() {
        String outputDirPath = CvMappingReaderTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }

    @BeforeClass
    public static void initializeStreamHandler() {
        MockMemoryStreamHandler.initHandler();
    }

    @Test
    public void readURL() throws Exception {
        URL url = new URL( MockMemoryStreamHandler.MEMORY_PROTOCOL, null, "test" );
        MockMemoryStreamHandler.addContent( url, SAMPLE_RULE_DEFINITION );

        CvRuleReader reader = new CvRuleReader();
        CvMapping mapping = reader.read( url );
        checkContent( mapping );
    }

    @Test
    public void readFile() throws Exception {
        final File file = new File( getTargetDirectory(), "sample.xml" );
        BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
        out.write( SAMPLE_RULE_DEFINITION );
        out.flush();
        out.close();

        CvRuleReader reader = new CvRuleReader();
        CvMapping mapping = reader.read( file );
        checkContent( mapping );
    }


    @Test
    public void readInputStream() throws Exception {
        CvRuleReader reader = new CvRuleReader();
        final MockInputStream is = new MockInputStream();
        is.setBuffer( SAMPLE_RULE_DEFINITION );
        CvMapping mapping = reader.read( is );
        checkContent( mapping );
    }

    @Test
    public void readString() throws Exception {
        CvRuleReader reader = new CvRuleReader();
        CvMapping mapping = reader.read( SAMPLE_RULE_DEFINITION );
        checkContent( mapping );
    }

    private void checkContent( CvMapping mapping ) {
        Assert.assertNotNull( mapping );
        final List<CvMappingRule> rules = mapping.getCvMappingRuleList().getCvMappingRule();

        final CvMapping.CvReferenceList cvRefs = mapping.getCvReferenceList();
        Assert.assertNotNull( cvRefs );
        Assert.assertEquals( 2, cvRefs.getCvReference().size() );

        Assert.assertEquals( 2, rules.size() );
        CvMappingRule rule = rules.iterator().next();
        Assert.assertEquals( 2, rule.getCvTerm().size() );

        // get first CvTerm and check the setting for useTermName
        // <CvTerm termAccession="MI:0001" allowChildren="true" termName="interaction detection method" useTerm="false" cvIdentifier="MI"/>
        CvTerm cvterm = rules.get( 0 ).getCvTerm().get( 0 );
        Assert.assertEquals( "XX:0001", cvterm.getTermAccession() );
        Assert.assertEquals( "A", ( ( CvReference ) cvterm.getCvIdentifierRef() ).getCvIdentifier() );
        Assert.assertEquals( false, cvterm.isAllowChildren() );
        Assert.assertEquals( true, cvterm.isUseTerm() );
        // get second CvTerm and check the default setting for useTermName
        Assert.assertEquals( false, cvterm.isUseTermName() );

        cvterm = rules.get( 1 ).getCvTerm().get( 0 );
        Assert.assertFalse( cvterm.isUseTermName() );
    }
}
