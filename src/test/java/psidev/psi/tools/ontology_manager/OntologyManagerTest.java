package psidev.psi.tools.ontology_manager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.impl.local.LocalOntology;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.impl.ols.OlsOntology;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * OntologyManager tester.
 *
 * Author: Florian Reisinger
 * Date: 15-Aug-2007
 */
public class OntologyManagerTest {

    private OntologyManager om;
    private File ontologyDirectory;

    @Before
    public void setup() throws OntologyLoaderException, IOException {
        ontologyDirectory = new File(getTargetDirectory(), "downloaded-ontologies");
        final String ontoConfig = "ontologies.xml";
        InputStream is = OntologyManager.class.getClassLoader().getResourceAsStream( ontoConfig );
        Assert.assertNotNull( "Could not read ontology configuration file: " + ontoConfig, is );
        om = new OntologyManager();
        om.setOntologyDirectory(ontologyDirectory);
        om.loadOntologies(is);
        is.close();
        Assert.assertNotNull( om );
//        for ( String id : om.getOntologyIDs() ) {
//            System.out.println( id );
//        }
    }

    private File getTargetDirectory() {
        String outputDirPath = OntologyManagerTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }

    @Test
    public void ontologyDirectory() {
        Assert.assertTrue(ontologyDirectory.exists());
    }

    @Test
    public void isUpToDate() {
        try {
            Assert.assertTrue(om.isUpToDate());
        } catch (OntologyLoaderException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
    }

    @Test
    public void ontologyLoading() {
        Collection<String> ontologyIDs = om.getOntologyIDs();
        Assert.assertEquals( "ontologies.xml specifies only 2 ontologies.", 3, ontologyIDs.size() );
        Assert.assertTrue( ontologyIDs.contains( "MI" ) );
        Assert.assertTrue( ontologyIDs.contains( "MOD" ) );
        Assert.assertTrue( ontologyIDs.contains( "MS" ) );

        OntologyAccess oa1 = om.getOntologyAccess( "MI" );
        Assert.assertNotNull( oa1 );
        // ontologies.xml defines a OlsOntology for 'MI'
        Assert.assertTrue( oa1 instanceof OlsOntology );

        OntologyAccess oa2 = om.getOntologyAccess( "MOD" );
        Assert.assertNotNull( oa2 );
        // ontologies.xml defines a LocalOntology for 'MOD'
        Assert.assertTrue( oa2 instanceof LocalOntology);

        OntologyAccess oa3 = om.getOntologyAccess( "MS" );
        Assert.assertNotNull( oa3 );
        // ontologies.xml defines a LocalOntology for 'MOD'
        Assert.assertTrue( oa3 instanceof LocalOntology);
    }
}
