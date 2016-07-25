package psidev.psi.tools.objectRuleReader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.ObjectRuleList;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.Rule;

import java.io.*;
import java.net.URL;
import java.util.Iterator;

/**
 * ObjectRuleReader Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class ObjectRuleReaderTest {

    public static final String SAMPLE_XML =
            "<objectRuleList>\n" +
            "    <rule class=\"net.sf.psi.validator.rules.Rule1\" />\n" +
            "    <rule class=\"net.sf.psi.validator.rules.Rule2\" />\n" +
            "</objectRuleList>";

    private File getTargetDirectory() {
        String outputDirPath = ObjectRuleReaderTest.class.getResource( "/" ).getFile();
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
        MockMemoryStreamHandler.addContent( url, SAMPLE_XML );

        ObjectRuleReader reader = new ObjectRuleReader();
        ObjectRuleList cfg = reader.read( url );
        checkContent( cfg );
    }

    @Test
    public void readFile() throws Exception {
        final File file = new File( getTargetDirectory(), "sample.xml" );
        BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
        out.write( SAMPLE_XML );
        out.flush();
        out.close();

        ObjectRuleReader reader = new ObjectRuleReader();
        ObjectRuleList cfg = reader.read( file );
        checkContent( cfg );
    }


    @Test
    public void readInputStream() throws Exception {
        ObjectRuleReader reader = new ObjectRuleReader();
        final MockInputStream is = new MockInputStream();
        is.setBuffer( SAMPLE_XML );
        ObjectRuleList cfg = reader.read( is );
        checkContent( cfg );
    }

    @Test
    public void readString() throws Exception {
        ObjectRuleReader reader = new ObjectRuleReader();
        ObjectRuleList cfg = reader.read( SAMPLE_XML );
        checkContent( cfg );
    }

    private void checkContent( ObjectRuleList config ) {
        Assert.assertNotNull( config );

        Assert.assertEquals( 2, config.getRule().size() );
        final Iterator<Rule> it = config.getRule().iterator();

        Rule or = it.next();
        Assert.assertNotNull( or );
        Assert.assertEquals( "net.sf.psi.validator.rules.Rule1", or.getClazz());

        or = it.next();
        Assert.assertNotNull( or );
        Assert.assertEquals( "net.sf.psi.validator.rules.Rule2", or.getClazz());
    }
}
