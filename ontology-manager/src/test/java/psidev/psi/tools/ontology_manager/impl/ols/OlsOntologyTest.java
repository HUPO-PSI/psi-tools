package psidev.psi.tools.ontology_manager.impl.ols;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.OntologyUtils;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * OlsOntology Tester.
 *
 * @author Florian Reisinger
 * Date: 20-Aug-2007
 */
public class OlsOntologyTest {

    private static OntologyManager manager;

    static {
        final InputStream config = OlsOntologyTest.class.getResourceAsStream( "/ols-ontologies.xml" );
        try {
            manager = new OntologyManager( config );
        } catch (OntologyLoaderException e) {
            e.printStackTrace();
        }
    }

    ////////////
    // Tests

    @Test
    public void getValidTerms() throws OntologyLoaderException {
        final OntologyAccess mod = manager.getOntologyAccess( "GO" );
        // GO:0055044 has 7 children (OLS 17 July 2008) = 7 valid terms
        final Set<OntologyTermI> terms = mod.getValidTerms( "GO:0055044", true, false );
        Assert.assertEquals( 7, terms.size() );
        // inclusive the query term itself = 8 valid terms
        final Set<OntologyTermI> terms2 = mod.getValidTerms( "GO:0055044", true, true );
        Assert.assertEquals( 8, terms2.size() );
        // the query term only = 1 valid term
        final Set<OntologyTermI> terms3 = mod.getValidTerms( "GO:0055044", false, true );
        Assert.assertEquals( 1, terms3.size() );
        // neither the query term itself nor its child terms = 0 valid term
        final Set<OntologyTermI> terms4 = mod.getValidTerms( "GO:0055044", false, false );
        Assert.assertTrue( terms4.isEmpty() );
        //fails?
        Assert.assertNotNull(mod.getTermForAccession("GO:0005575"));
    }

    @Test
    public void getValidTerms_so() throws OntologyLoaderException {
        final OntologyAccess mod = manager.getOntologyAccess( "SO" );
//        OntologyTermI parent = mod.getTermForAccession("SO:0000001"); // no good for testing!
        // SO:0000336 has 9 children (OLS 10 Aug 2011) = 9 valid terms
        OntologyTermI parent = mod.getTermForAccession("SO:0000336");
        Set<OntologyTermI> terms = mod.getAllChildren(parent);
        Assert.assertEquals( 14, terms.size() );
    }

    @Test
    public void getMiTermSynonyms() throws OntologyLoaderException {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final Set<OntologyTermI> terms = mi.getValidTerms( "MI:0018", false, true );
        Assert.assertEquals( 1, terms.size() );
        final OntologyTermI y2h = terms.iterator().next();

        // OLS check: 8 synonyms (17 May 2016)
        Assert.assertEquals( 8, y2h.getNameSynonyms().size() );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "classical two hybrid" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "Gal4 transcription regeneration" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "yeast two hybrid" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "two-hybrid" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "2 hybrid" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "2-hybrid" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "2h" ) );
        Assert.assertTrue( y2h.getNameSynonyms().contains( "2H" ) );

    }
    
    @Test
    public void getMiTermSynonyms0217() throws OntologyLoaderException {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final Set<OntologyTermI> terms = mi.getValidTerms( "MI:0217", false, true );
        Assert.assertEquals( 1, terms.size() );
        
        final OntologyTermI phosphorylation = mi.getTermForAccession("MI:0217");
        assertEquals(1, phosphorylation.getNameSynonyms().size());
        
        // different approach
        Collection<String> names;
        names = OntologyUtils.getTermNames(terms);
        assertTrue(names.contains("phosphorylation"));
        assertTrue(names.contains("phosphorylation reaction"));
    }

    @Test
    public void getMiChildTermSynonyms0190() throws OntologyLoaderException {
        OntologyAccess mi = manager.getOntologyAccess( "MI" );
        Set<OntologyTermI> terms = mi.getValidTerms( "MI:0190", true, false );
        Assert.assertFalse(terms.isEmpty());
        
        // different approach
        Collection<String> names;
        names = OntologyUtils.getTermNames(terms);
        // preferred name
        assertTrue(names.contains("phosphorylation reaction"));
        
        // how about synonym?
        assertTrue(names.contains("phosphorylation"));
    }
    
    @Test
    public void getModTermSynonyms() throws OntologyLoaderException {
        final OntologyAccess mod = manager.getOntologyAccess( "MOD" );
        final Set<OntologyTermI> terms = mod.getValidTerms( "MOD:00007", false, true );
        Assert.assertEquals( 1, terms.size() );
        final OntologyTermI term = terms.iterator().next();

        Assert.assertEquals( 3, term.getNameSynonyms().size() );
        Assert.assertTrue( term.getNameSynonyms().contains( "Delta:S(-1)Se(1)" ) );
        Assert.assertTrue( term.getNameSynonyms().contains( "Se(S)Res" ) );
        Assert.assertTrue( term.getNameSynonyms().contains( "Selenium replaces sulphur" ) );
    }

    @Test
    public void illegalQuery() throws Exception {
        final OntologyAccess mod = manager.getOntologyAccess( "GO" );
        Set<OntologyTermI> result = mod.getValidTerms( null, false, false );
        Assert.assertTrue("Did not expect results for accession 'null'!", (result.size() == 0));
    }

    @Test
    public void isObsolete() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = mi.getTermForAccession( "MI:0205" );
        Assert.assertTrue(mi.isObsolete( term ));

        final OntologyTermI term2 = mi.getTermForAccession( "MI:0001" );
        Assert.assertFalse(mi.isObsolete( term2 ));
    }

    @Test(expected=IllegalStateException.class)
    public void isObsolete_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = new OntologyTermImpl( "MI:xxxx", "bogus term" );
        Assert.assertFalse(mi.isObsolete( term ));
    }

    @Test
    public void getTermForAccession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = mi.getTermForAccession( "MI:0013" );
        Assert.assertNotNull( term );
        Assert.assertEquals( "MI:0013", term.getTermAccession() );
        Assert.assertEquals( "biophysical", term.getPreferredName() );
    }

    @Test(expected=IllegalStateException.class)
    public void getTermForAccession_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = mi.getTermForAccession( "MI:xxxx" );
        Assert.assertNull( term );
    }

    //////////////////
    // Children

    @Test
    public void getDirectChildren() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermI term = mi.getTermForAccession( "MI:0417" ); // footprinting
        Assert.assertNotNull( term );

        final Set<OntologyTermI> children = mi.getDirectChildren( term );
        Assert.assertNotNull( children );
        Assert.assertEquals( 3, children.size() );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0605", "enzymatic footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0602", "chemical footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:1191", "ultraviolet (uv) footprinting" ) ) );
    }

    @Test(expected=IllegalStateException.class)
    public void getDirectChildren_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermI term = new OntologyTermImpl( "MI:xxxx", "bogus term" );

        final Set<OntologyTermI> children = mi.getDirectChildren( term );
        Assert.assertNotNull( children );
        Assert.assertEquals( 0, children.size() );
    }

    @Test
    public void getAllChildren() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermI term = mi.getTermForAccession( "MI:0417" ); // footprinting
        Assert.assertNotNull( term );

        final Set<OntologyTermI> children = mi.getAllChildren( term );
        Assert.assertNotNull( children );
        Assert.assertEquals( children.toString(), 12, children.size() );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0901", "isotope label footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0602", "chemical footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0603", "dimethylsulphate footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0604", "potassium permanganate footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0605", "enzymatic footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0606", "DNase I footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:1191", "ultraviolet (uv) footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:1190", "hydroxy radical footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:1189", "methylation interference assay" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:1183", "nuclease footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0814", "protease accessibility laddering" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:1352", "uracil interference assay" ) ) );
    }

    @Test(expected=IllegalStateException.class)
    public void getAllChildren_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermI term = new OntologyTermImpl( "MI:xxxx", "bogus term" );

        final Set<OntologyTermI> children = mi.getAllChildren( term );
        Assert.assertNotNull( children );
        Assert.assertEquals( 0, children.size() );
    }

    ///////////////////
    // Parents

    @Test
    public void getDirectParents() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermI term = mi.getTermForAccession( "MI:0013" );
        Assert.assertNotNull( term );

        final Set<OntologyTermI> parents = mi.getDirectParents( term );
        Assert.assertNotNull( parents );
        Assert.assertEquals( 1, parents.size() );
        Assert.assertTrue( parents.contains( new OntologyTermImpl( "MI:0045", "experimental interaction detection" ) ) );
    }

    @Test(expected=IllegalStateException.class)
    public void getDirectParents_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermImpl term = new OntologyTermImpl( "MI:xxxx", "bogus term" );

        final Set<OntologyTermI> parents = mi.getDirectParents( term );
        Assert.assertNotNull( parents );
        Assert.assertEquals( 0, parents.size() );
    }

    @Test
    public void getAllParents() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermI term = mi.getTermForAccession( "MI:0013" );
        Assert.assertNotNull( term );

        final Set<OntologyTermI> parents = mi.getAllParents( term );
        Assert.assertNotNull( parents );
        Assert.assertEquals( 3, parents.size() );
        Assert.assertTrue( parents.contains( new OntologyTermImpl( "MI:0045", "experimental interaction detection" ) ) );
        Assert.assertTrue( parents.contains( new OntologyTermImpl( "MI:0001", "interaction detection method" ) ) );
        Assert.assertTrue( parents.contains( new OntologyTermImpl( "MI:0000", "molecular interaction" ) ) );
    }

    @Test(expected=IllegalStateException.class)
    public void getAllParents_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermImpl term = new OntologyTermImpl( "MI:xxxx", "bogus term" );

        final Set<OntologyTermI> parents = mi.getAllParents( term );
        Assert.assertNotNull( parents );
        Assert.assertEquals( 0, parents.size() );
    }

    ///////////////////
    // Various

    @Test
    public void cacheSpeed() {
        // get child terms of 'alias type' (MI:0300) from the MI ontology
        OntologyAccess mi = manager.getOntologyAccess("MI");
        OntologyTermI term =  mi.getTermForAccession( "MI:0300" ); // accession not used before

        // run first time -> uncached
        long start = System.currentTimeMillis();
        Set<OntologyTermI> result_uc = mi.getDirectParents( term ); // first run will fill cache
        long stop = System.currentTimeMillis();
        long time_uncached = stop - start;
        // run second time -> now cached
        start = System.currentTimeMillis();
        Set<OntologyTermI> result_c = mi.getDirectParents( term );
        stop = System.currentTimeMillis();
        long time_cached = stop - start;

//        System.out.println("Comparing performance between cached and uncached method:");
//        System.out.println("uncached: " + time_uncached + "(ms) and cached: " + time_cached + "(ms).");
        Assert.assertEquals("Cached and uncached query have to return the same result!", result_uc, result_c );
        // ToDo: in "mvn clean test" runs on the same speed -> why?
        // This is not a deterministic behavious as many factor can interfere with your running system. better not check on this in a unit test.
//        Assert.assertFalse("Cached and uncached query should not take the same time to execute!", time_uncached == time_cached );
    }
}
