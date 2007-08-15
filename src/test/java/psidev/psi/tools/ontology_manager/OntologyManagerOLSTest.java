package psidev.psi.tools.ontology_manager;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Author: florian
 * Date: 15-Aug-2007
 * Time: 13:49:25
 */
public class OntologyManagerOLSTest {

    static OntologyManager om;

    @BeforeClass
    public static  void setup() throws OntologyLoaderException, IOException {
        String ontoConfig = "ontologies.xml";
        InputStream is = OntologyManager.class.getClassLoader().getResourceAsStream( ontoConfig );
        Assert.assertNotNull( "Could not read ontology configuration file: " + ontoConfig, is );
        om = new OntologyManager(is);
        is.close();
    }

    @Test
    public void getChildTerms() throws OntologyLoaderException {
        // get child terms of 'alias type' (MI:0300) form the MI ontology
        Set<String> result = om.getValidIDs( "MI", "MI:0300", true, false );
        // should be 9 terms (15. August 2007)
        Assert.assertEquals( "There should be 9 child terms for MI:0300!", 9, result.size() );
//        for (String s : result) {
//            System.out.println("term: " + s);
//        }
    }

    @Test
    public void noChildTerms() {
        // get child terms of term that does not have children
        Set<String> result = om.getValidIDs( "MI", "MI:0326", true, false );
        // should be no terms (15. August 2007)
        Assert.assertEquals( "There should be no child terms for MI:0326!", 0, result.size() );

    }


    
}
