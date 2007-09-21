package psidev.psi.tools.ontology_manager.impl.ols;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.impl.ols.OlsOntology;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;

import java.util.Set;
import java.rmi.RemoteException;

/**
 * Author: Florian Reisinger
 * Date: 20-Aug-2007
 */
public class OlsOntologyTest {

    static OlsOntology mi_ols;
    static OlsOntology go_ols;

    @BeforeClass
    public static void setup() throws OntologyLoaderException {
        mi_ols = new OlsOntology();
        mi_ols.loadOntology( "MI", null, null, null, null );
        go_ols = new OlsOntology();
        go_ols.loadOntology( "GO", null, null, null, null );
    }

    @Test
    public void testEmptyStringQuery() {
        String id = ""; // check on empty string as id

        // check getValidIDs
        Set<String> result1 = mi_ols.getValidIDs( id, true, false );
        Assert.assertNotNull(result1);
        Assert.assertEquals( "The result set must be empty, since we don't expect any " +
                "results from empty queries!", 0, result1.size() );

        // check isObsoleteID
        try {
            mi_ols.isObsoleteID( id );
        } catch ( Exception e) {
            // calling isObsoleteID on non existing id causes IllegalStateException
            Assert.assertTrue( e instanceof IllegalStateException );
        }

        // check getTermNameByID
        String result3 = mi_ols.getTermNameByID( id );
        Assert.assertEquals( "", result3);

        // check getDirectParentsIDs
        Set<String> result4 = mi_ols.getDirectParentsIDs( id );
        Assert.assertNotNull( result4 );
        Assert.assertEquals( "The result set must be empty, since we don't expect any " +
                "results from empty queries!", 0, result4.size() );
    }

    @Test
    public void testCacheSpeed() {
        // get child terms of 'alias type' (MI:0300) from the MI ontology
        String id = "MI:0300";
        // run first time -> uncached
        Set<String> result1 = mi_ols.getDirectParentsIDs( id ); // run with cache
        // run second time -> now cached
        long start = System.currentTimeMillis();
        Set<String> result2 = mi_ols.getDirectParentsIDs( id );
        long stop = System.currentTimeMillis();
        long cachedDif = stop - start;

        // run uncached
        start = System.currentTimeMillis();
        Set<String> result3 = mi_ols.getDirectParentsIDsUncached( id );
        stop = System.currentTimeMillis();
        long uncachedDif = stop - start;

        System.out.println("Comparing performance between cached and uncached method...");
        System.out.println("uncached: " + uncachedDif + "(ms) and cached: " + cachedDif + "(ms)");
        Assert.assertEquals( "Cached and uncached results have to be the same!", result1, result2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", result2, result3 );

    }

    @Test
    public void testGetValidIDsWithMI() {
        String id = "MI:0300";
        Set<String> resultA1 = mi_ols.getValidIDs( id, true, false ); // not yet cached
        Set<String> resultA2 = mi_ols.getValidIDs( id, true, false ); // now cached
//        // retrieval of child terms through recursive OLS queries from client: takes too long
//        Set<String> resultA3 = mi_ols.getValidIDs2( id, true, false ); // result without using cache
        // retrieval of child terms on OLS server side: much faster
        Set<String> resultA3 = mi_ols.getValidIDsOld( id, true, false ); // result without using cache
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA1, resultA2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA2, resultA3 );
        Assert.assertEquals( "This set should contain 9 result terms!", 9, resultA1.size() ); // on: 20. Aug. 2007
    }

    @Test
    public void testGetValidIDsWithGO() {
        String id = "GO:0055044";
        Set<String> resultB1 = go_ols.getValidIDs( id, true, false ); // not yet cached
        Set<String> resultB2 = go_ols.getValidIDs( id, true, false ); // now cached
//        // retrieval of child terms through recursive OLS queries from client: takes too long
//        Set<String> resultB3 = go_ols.getValidIDs2( id, true, false ); // result without using cache
        // retrieval of child terms on OLS server side: much faster
        Set<String> resultB3 = go_ols.getValidIDsOld( id, true, false ); // result without using cache
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB1, resultB2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB2, resultB3 );
        Assert.assertEquals( "This set should contain 7 result terms!", 7, resultB1.size() ); // on: 20. Aug. 2007
    }

    @Test
    public void testIsObsoleteWithMI() {
        String idA = "MI:0021"; // obsolete
        boolean resultA1 = mi_ols.isObsoleteID( idA );
        boolean resultA2 = mi_ols.isObsoleteID( idA );
        boolean resultA3 = mi_ols.isObsoleteIDUncached( idA );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA1, resultA2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA2, resultA3 );
        Assert.assertTrue( "Term '" + idA + "'is obsolete!", resultA1 );

        String idB = "MI:0428"; // non obsolete
        boolean resultB1 = mi_ols.isObsoleteID( idB );
        boolean resultB2 = mi_ols.isObsoleteID( idB );
        boolean resultB3 = mi_ols.isObsoleteIDUncached( idB );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB1, resultB2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB2, resultB3 );
        Assert.assertFalse( "Term '" + idB + "' is NOT obsolete!", resultB1 );
    }

    @Test
    public void testIsObsoleteWithGO() {
        String idA = "GO:0015428"; // obsolete term: GO:0015428 : type I protein secretor activity
        boolean resultA1 = go_ols.isObsoleteID( idA );
        boolean resultA2 = go_ols.isObsoleteID( idA );
        boolean resultA3 = go_ols.isObsoleteIDUncached( idA );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA1, resultA2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA2, resultA3 );
        Assert.assertTrue( "Term '" + idA + "'is obsolete!", resultA1 );

        String idB = "GO:0042277"; // non obsolete term: GO:0042277 : peptide binding
        boolean resultB1 = go_ols.isObsoleteID( idB );
        boolean resultB2 = go_ols.isObsoleteID( idB );
        boolean resultB3 = go_ols.isObsoleteIDUncached( idB );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB1, resultB2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB2, resultB3 );
        Assert.assertFalse( "Term '" + idB + "' is NOT obsolete!", resultB1 );
    }

    @Test
    public void testGetTermNameByIDWithMI() {
        String idA = "MI:0616"; // MI accession for term: example
        String resultA1 = mi_ols.getTermNameByID( idA );
        String resultA2 = mi_ols.getTermNameByID( idA );
        String resultA3 = mi_ols.getTermNameByIDUncached( idA );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA1, resultA2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA2, resultA3 );
        Assert.assertEquals( "example", resultA1);

        String idB = "MI:0403"; // MI accession for term: colocalization
        String resultB1 = mi_ols.getTermNameByID( idB );
        String resultB2 = mi_ols.getTermNameByID( idB );
        String resultB3 = mi_ols.getTermNameByIDUncached( idB );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB1, resultB2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB2, resultB3 );
        Assert.assertEquals( "colocalization", resultB1);
    }

    @Test
    public void testGetTermNameByIDWithGO() {
        String resultA1 = go_ols.getTermNameByID( "GO:0005623" ); // GO accession for term: cell
        String resultA2 = go_ols.getTermNameByID( "GO:0005623" );
        String resultA3 = go_ols.getTermNameByIDUncached( "GO:0005623" );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA1, resultA2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultA2, resultA3 );
        Assert.assertEquals( "cell", resultA1);

        String resultB1 = go_ols.getTermNameByID( "GO:0043226" ); // GO accession for term: organelle
        String resultB2 = go_ols.getTermNameByID( "GO:0043226" );
        String resultB3 = go_ols.getTermNameByIDUncached( "GO:0043226" );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB1, resultB2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", resultB2, resultB3 );
        Assert.assertEquals( "organelle", resultB1);
    }

    @Test
    public void testGetDirectParentsIDsWithMI() {
        String idA = "MI:0362"; // MI:0362 = 'inference' has 3 direct parents
        Set<String> result1 = mi_ols.getDirectParentsIDs( idA ); // not yet cached
        Set<String> result2 = mi_ols.getDirectParentsIDs( idA ); // now cached
        Set<String> result3 = mi_ols.getDirectParentsIDsUncached( idA ); // result without using cache
        Assert.assertEquals( "Cached and uncached results have to be the same!", result1, result2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", result2, result3 );
        Assert.assertEquals( "This set should contain 3 result terms!", 3, result1.size() ); // on: 20. Aug. 2007
    }

    @Test
    public void testGetDirectParentsIDsWithGO() {
        String idB = "GO:0044464"; // GO:0044464 = 'cell part' has 2 direct parents
        Set<String> result1 = go_ols.getDirectParentsIDs( idB ); // not yet cached
        Set<String> result2 = go_ols.getDirectParentsIDs( idB ); // now cached
        Set<String> result3 = go_ols.getDirectParentsIDsUncached( idB ); // result without using cache
        Assert.assertEquals( "Cached and uncached results have to be the same!", result1, result2 );
        Assert.assertEquals( "Cached and uncached results have to be the same!", result2, result3 );
        Assert.assertEquals( "This set should contain 2 result terms!", 2, result1.size() ); // on: 20. Aug. 2007
    }

    @Test
    public void testUnknownIDQuery() {
        String id = "GO:0055044"; // check on empty string as id

        // check getValidIDs
        Set<String> result1 = mi_ols.getValidIDs( id, true, false );
        Assert.assertNotNull(result1);
        Assert.assertEquals( "The result set must be empty, since we don't expect any " +
                "results from empty queries!", 0, result1.size() );

        // check isObsoleteID
        try {
            mi_ols.isObsoleteID( id );
        } catch ( Exception e) {
            // calling isObsoleteID on non existing id causes IllegalStateException
            Assert.assertTrue( e instanceof IllegalStateException );
        }

        // check getTermNameByID
        String result3 = mi_ols.getTermNameByID( id );
        // if no name is found, the specified id is returned
        Assert.assertEquals( id, result3);

        // check getDirectParentsIDs
        Set<String> result4 = mi_ols.getDirectParentsIDs( id );
        Assert.assertNotNull( result4 );
        Assert.assertEquals( "The result set must be empty, since we don't expect any " +
                "results from empty queries!", 0, result4.size() );
    }

}
