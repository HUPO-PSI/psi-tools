package psidev.psi.tools.ontology_manager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.impl.local.LocalOntology;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.impl.ols.OlsOntology;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.net.URL;

/**
 * OntologyManager tester.
 *
 * Author: Florian Reisinger
 * Date: 15-Aug-2007
 */
public class OntologyManagerTest {

    private OntologyManager om;

    @Before
    public void setup() throws OntologyLoaderException, IOException {
        String ontoConfig = "ontologies.xml";
        InputStream is = OntologyManager.class.getClassLoader().getResourceAsStream( ontoConfig );
        Assert.assertNotNull( "Could not read ontology configuration file: " + ontoConfig, is );
        om = new OntologyManager(is);
        is.close();
        Assert.assertNotNull( om );
        for ( String id : om.getOntologyIDs() ) {
            System.out.println( id );
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

    @Test
    public void getChildTermsOLS() throws OntologyLoaderException {
        // get child terms of 'alias type' (MI:0300) form the MI ontology
        Set<String> result = om.getValidIDs( "MI", "MI:0300", true, false );
        // should be 9 terms (15. August 2007)
        Assert.assertEquals( "There should be 9 child terms for MI:0300!", 9, result.size() );
    }

    @Test
    public void getChildTermsLocal() throws OntologyLoaderException {
        // get child terms of 'stereoisomerized residue' (MOD:00664) form the MOD ontology
        Set<String> result = om.getValidIDs( "MOD", "MOD:00664", true, false );
        // should be  terms (August 2007)
        Assert.assertEquals( 10, result.size() );
    }

    @Test
    public void noChildTermsOLS() {
        // get child terms of term that does not have children
        Set<String> result = om.getValidIDs( "MI", "MI:0326", true, false );
        // should be no terms (August 2007)
        Assert.assertEquals( "There should be no child terms for MI:0326!", 0, result.size() );

    }

    @Test
    public void noChildTermsLocal() {
        // get child terms of term that does not have children   (isomerization to D-alanine MOD:00198)
        Set<String> result = om.getValidIDs( "MOD", "MOD:00198", true, false );
        // should be no terms (August 2007)
        Assert.assertEquals( "There should be no child terms for MOD:00198!", 0, result.size() );

    }
}