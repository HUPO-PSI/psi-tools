package psidev.psi.tools.ontology_manager.impl.local;

import static org.junit.Assert.fail;
import org.junit.*;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/**
 * LocalOntology Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class LocalOntologyTest {

    private OntologyManager manager;

    @Before
    public void setup() throws OntologyLoaderException {
        if ( manager == null ) {
            final InputStream config = LocalOntologyTest.class.getResourceAsStream( "/local-ontologies.xml" );
            manager = new OntologyManager( config );
        }
    }

    @After
    public void cleanup() throws Exception {
        manager = null;
    }

    ////////////
    // Tests

    @Test
    public void getValidTerms() throws OntologyLoaderException {
        final OntologyAccess mod = manager.getOntologyAccess( "MOD" );
        final Set<OntologyTermI> terms = mod.getValidTerms( "MOD:00647", true, false );
        Assert.assertEquals( 3, terms.size() );
    }

    @Test
    public void isObsolete() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = mi.getTermForAccession( "MI:0205" );
        Assert.assertTrue(mi.isObsolete( term ));

        final OntologyTermI term2 = mi.getTermForAccession( "MI:0001" );
        Assert.assertFalse(mi.isObsolete( term2 ));
    }

    @Test
    public void isObsolete_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = new OntologyTermImpl( "MI:xxxx", "bogus term" );
        Assert.assertFalse(mi.isObsolete( term ));
    }

    @Test
    @Ignore
    public void setOntologyDirectory() throws Exception {
        fail( "Not yet implemented." );
    }

    @Test
    public void getTermForAccession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );
        final OntologyTermI term = mi.getTermForAccession( "MI:0013" );
        Assert.assertNotNull( term );
        Assert.assertEquals( "MI:0013", term.getTermAccession() );
        Assert.assertEquals( "biophysical", term.getPreferredName() );
    }

    @Test
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
        Assert.assertEquals( 2, children.size() );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0409", "dna footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0814", "protease accessibility laddering" ) ) );
    }

    @Test
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
        Assert.assertEquals( children.toString(), 7, children.size() );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0409", "dna footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0602", "chemical footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0603", "dimethylsulphate footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0604", "potassium permanganate footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0605", "enzymatic footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0606", "DNase I footprinting" ) ) );
        Assert.assertTrue( children.contains( new OntologyTermImpl( "MI:0814", "protease accessibility laddering" ) ) );
    }

    @Test
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

    @Test
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

    @Test
    public void getAllParents_unknown_accession() throws Exception {
        final OntologyAccess mi = manager.getOntologyAccess( "MI" );

        final OntologyTermImpl term = new OntologyTermImpl( "MI:xxxx", "bogus term" );

        final Set<OntologyTermI> parents = mi.getAllParents( term );
        Assert.assertNotNull( parents );
        Assert.assertEquals( 0, parents.size() );
    }

    private void printTerms( Collection<OntologyTermI> terms ) {
        for ( OntologyTermI term : terms ) {
            System.out.println( term );
        }
    }
}
