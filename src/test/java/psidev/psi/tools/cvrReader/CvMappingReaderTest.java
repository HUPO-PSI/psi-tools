package psidev.psi.tools.cvrReader;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvReference;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;

import java.util.List;

/**
 * CvMappingReader Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: CvMappingReaderTest.java 669 2007-06-29 16:45:04 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 0.3
 */
public class CvMappingReaderTest {

    @Test
    public void read() throws Exception {

        String testXml = "<CvMapping modelName=\"string\" modelURI=\"http://www.company.org/lala/sonoras\" modelVersion=\"string\">\n" +
                         "\n" +
                         "    <CvReferenceList CvSourceVersion=\"anySimpleType\">\n" +
                         "        <CvReference cvIdentifier=\"A\" cvName=\"string\"/>\n" +
                         "        <CvReference cvIdentifier=\"B\" cvName=\"string\"/>\n" +
                         "    </CvReferenceList>\n" +
                         "\n" +
                         "    <CvMappingRuleList>\n" +
                         "        <CvMappingRule scopePath=\"/a/b/c/d/e\" cvElementPath=\"/a/b/c\" cvTermsCombinationLogic=\"AND\" requirementLevel=\"SHOULD\">\n" +
                         "            <CvTerm cvIdentifierRef=\"A\" termAccession=\"XX:0001\" termName=\"lala\" useTermName=\"false\" useTerm=\"true\" allowChildren=\"false\" isRepeatable=\"true\"/>\n" +
                         "            <CvTerm cvIdentifierRef=\"B\" termAccession=\"YY:0001\" termName=\"foo\" useTermName=\"true\" useTerm=\"false\" allowChildren=\"true\" isRepeatable=\"false\"/>\n" +
                         "        </CvMappingRule>\n" +
                         "        <CvMappingRule scopePath=\"/a/b/c/d/e\" cvElementPath=\"/a/b/c\" cvTermsCombinationLogic=\"AND\" requirementLevel=\"SHOULD\">\n" +
                         "            <CvTerm cvIdentifierRef=\"A\" termAccession=\"XX:0001\" termName=\"lala\" useTermName=\"false\" useTerm=\"true\" allowChildren=\"false\" isRepeatable=\"true\"/>\n" +
                         "        </CvMappingRule>\n" +
                         "    </CvMappingRuleList>\n" +
                         "</CvMapping>";

        CvRuleReader reader = new CvRuleReader();
        CvMapping mapping = reader.read( testXml );
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
        CvTerm cvterm = rules.get(0).getCvTerm().get(0);
        Assert.assertEquals( "XX:0001", cvterm.getTermAccession() );
        Assert.assertEquals( "A", (( CvReference )cvterm.getCvIdentifierRef()).getCvIdentifier() );
        Assert.assertEquals( false, cvterm.isAllowChildren() );
        Assert.assertEquals( true, cvterm.isUseTerm() );
        // get second CvTerm and check the default setting for useTermName
        Assert.assertEquals( false, cvterm.isUseTermName() );

        cvterm = rules.get(1).getCvTerm().get(0);
        Assert.assertFalse( cvterm.isUseTermName() );
    }
}
