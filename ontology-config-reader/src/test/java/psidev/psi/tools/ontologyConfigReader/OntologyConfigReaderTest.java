package psidev.psi.tools.ontologyConfigReader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSourceList;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSource;

import java.io.*;
import java.net.URL;
import java.util.Iterator;

/**
 * OntologyConfigReader Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class OntologyConfigReaderTest {

    public static final String SAMPLE_ONTOLOGY_CONFIG =
            "<cvSourceList>\n" +
            "    <cvSource source=\"psidev.psi.tools.ontology_manager.impl.ols.OlsOntology\"\n" +
            "              uri=\"http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/genomic-proteomic/protein/psi-mi.obo\"\n" +
            "              format=\"OBO\" " +
            "              name=\"PSI-MI\" " +
            "              identifier=\"MI\" " +
            "              version=\"1.154\"/>\n" +
            "    <cvSource source=\"OLS\"\n" +
            "              uri=\"http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/genomic-proteomic/protein/psi-mod.obo\"\n" +
            "              format=\"OBO\" " +
            "              name=\"PSI-MOD\" " +
            "              identifier=\"MOD\" " +
            "              version=\"1.260\"/>\n" +
            "</cvSourceList>";

    private File getTargetDirectory() {
        String outputDirPath = OntologyConfigReaderTest.class.getResource( "/" ).getFile();
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
        MockMemoryStreamHandler.addContent( url, SAMPLE_ONTOLOGY_CONFIG );

        OntologyConfigReader reader = new OntologyConfigReader();
        CvSourceList cfg = reader.read( url );
        checkContent( cfg );
    }

    @Test
    public void readFile() throws Exception {
        final File file = new File( getTargetDirectory(), "sample.xml" );
        BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
        out.write( SAMPLE_ONTOLOGY_CONFIG );
        out.flush();
        out.close();

        OntologyConfigReader reader = new OntologyConfigReader();
        CvSourceList cfg = reader.read( file );
        checkContent( cfg );
    }


    @Test
    public void readInputStream() throws Exception {
        OntologyConfigReader reader = new OntologyConfigReader();
        final MockInputStream is = new MockInputStream();
        is.setBuffer( SAMPLE_ONTOLOGY_CONFIG );
        CvSourceList cfg = reader.read( is );
        checkContent( cfg );
    }

    @Test
    public void readString() throws Exception {
        OntologyConfigReader reader = new OntologyConfigReader();
        CvSourceList cfg = reader.read( SAMPLE_ONTOLOGY_CONFIG );
        checkContent( cfg );
    }

    private void checkContent( CvSourceList config ) {
        Assert.assertNotNull( config );

        Assert.assertEquals( 2, config.getCvSource().size() );
        final Iterator<CvSource> it = config.getCvSource().iterator();
        final CvSource source = it.next();
        Assert.assertNotNull( source );
        Assert.assertEquals( "OBO", source.getFormat());
        Assert.assertEquals( "MI", source.getIdentifier());
        Assert.assertEquals( "PSI-MI", source.getName());
        Assert.assertEquals( "psidev.psi.tools.ontology_manager.impl.ols.OlsOntology", source.getSource());
        Assert.assertEquals( "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/genomic-proteomic/protein/psi-mi.obo", source.getUri());
        Assert.assertEquals( "1.154", source.getVersion());
    }
}
