/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package psidev.psi.tools.ontology_manager.impl.local.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

/**
 * OboTerm Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: OntologyTermTest.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>01/04/2006</pre>
 */
public class OntologyTermTest {

    @BeforeClass
    public static void setUp() throws Exception {
        buildSimpleDag();
    }

    // Utility

    private static OntologyTerm mi0;
    private static OntologyTerm mi1;
    private static OntologyTerm mi2;
    private static OntologyTerm mi3;
    private static OntologyTerm mi4;
    private static OntologyTerm mi5;
    private static OntologyTerm mi6;

    private static void buildSimpleDag() {

        /*
         *                  MI:0000
         *             /       |     \
         *     MI:0001     MI:0002     MI:0003
         *           \     /               |
         *           MI:0004           MI:0005
         *               |
         *           MI:0006
         *
         */

        mi0 = new OntologyTerm( "MI:0000" );
        mi1 = new OntologyTerm( "MI:0001" );
        mi2 = new OntologyTerm( "MI:0002" );
        mi3 = new OntologyTerm( "MI:0003" );
        mi4 = new OntologyTerm( "MI:0004" );
        mi5 = new OntologyTerm( "MI:0005" );
        mi6 = new OntologyTerm( "MI:0006" );

        // build hierarchy
        mi0.addChild( mi1 );
        mi1.addParent( mi0 );

        mi0.addChild( mi2 );
        mi2.addParent( mi0 );

        mi0.addChild( mi3 );
        mi3.addParent( mi0 );

        mi1.addChild( mi4 );
        mi4.addParent( mi1 );

        mi2.addChild( mi4 );
        mi4.addParent( mi2 );

        mi3.addChild( mi5 );
        mi5.addParent( mi3 );

        mi4.addChild( mi6 );
        mi6.addParent( mi4 );
    }

    /////////////////////
    // Tests

    @Test
    public void getId() {
        OntologyTerm term = new OntologyTerm( "MI" );
        assertEquals( "MI", term.getId() );
    }

    @Test
    public void getShortName() {
        OntologyTerm term = new OntologyTerm( "MI" );
        term.setShortName( "short" );
        assertEquals( "short", term.getShortName() );
    }

    @Test
    public void getFullName() {
        OntologyTerm term = new OntologyTerm( "MI" );
        term.setFullName( "long" );
        assertEquals( "long", term.getFullName() );
    }

    @Test
    public void getParents() {
        Collection parents = mi4.getParents();
        assertEquals( 2, parents.size() );
        assertTrue( parents.contains( mi1 ) );
        assertTrue( parents.contains( mi2 ) );

        parents = mi0.getParents();
        assertTrue( parents.isEmpty() );
    }

    @Test
    public void getChildren() {
        Collection children = mi0.getChildren();
        assertEquals( 3, children.size() );
        assertTrue( children.contains( mi1 ) );
        assertTrue( children.contains( mi2 ) );
        assertTrue( children.contains( mi3 ) );

        children = mi6.getChildren();
        assertTrue( children.isEmpty() );
    }

    @Test
    public void isObsolete() {
        OntologyTerm term = new OntologyTerm( "MI" );
        assertFalse( term.isObsolete() );

        term.setObsolete( true );
        assertTrue( term.isObsolete() );
    }

    @Test
    public void testGetObsoleteMessage() {
        //TODO: Test of getObsoleteMessage should go here...
    }

//    public void testSetId() {
//        //TODO: Test of setId should go here...
//    }
//
//    public void testSetShortName() {
//        //TODO: Test of setShortName should go here...
//    }
//
//    public void testSetFullName() {
//        //TODO: Test of setFullName should go here...
//    }
//
//    public void testAddParent() {
//        //TODO: Test of addParent should go here...
//    }
//
//    public void testAddChild() {
//        //TODO: Test of addChild should go here...
//    }
//
//    public void testSetObsolete() {
//        //TODO: Test of setObsolete should go here...
//    }
//
//    public void testSetObsoleteMessage() {
//        //TODO: Test of setObsoleteMessage should go here...
//    }

    @Test
    public void equals() {
        assertEquals( mi1, mi1 );
        assertNotSame( mi0, mi1 );
    }

    @Test
    public void tostring() {
        OntologyTerm term = new OntologyTerm( "MI" );
        assertNotNull( term.toString() );

        term.setShortName( "" );
        assertNotNull( term.toString() );
        term.setFullName( "" );
        assertNotNull( term.toString() );
        term.setObsolete( true );
        assertNotNull( term.toString() );
    }

    @Test
    public void getAllChildren() {
        Collection childrenOfMi0 = mi0.getAllChildren();
        assertEquals( 6, childrenOfMi0.size() );

        assertFalse( childrenOfMi0.contains( mi0 ) );

        assertTrue( childrenOfMi0.contains( mi1 ) );
        assertTrue( childrenOfMi0.contains( mi2 ) );
        assertTrue( childrenOfMi0.contains( mi3 ) );
        assertTrue( childrenOfMi0.contains( mi4 ) );
        assertTrue( childrenOfMi0.contains( mi5 ) );
        assertTrue( childrenOfMi0.contains( mi6 ) );
    }

    @Test
    public void isChildOf() {

        assertTrue( mi0.isChildOf( mi0 ) );
        assertTrue( mi1.isChildOf( mi0 ) );
        assertTrue( mi2.isChildOf( mi0 ) );
        assertTrue( mi3.isChildOf( mi0 ) );
        assertTrue( mi4.isChildOf( mi0 ) );
        assertTrue( mi5.isChildOf( mi0 ) );
        assertTrue( mi6.isChildOf( mi0 ) );

        assertTrue( mi4.isChildOf( mi1 ) );
        assertTrue( mi4.isChildOf( mi2 ) );

        assertTrue( mi5.isChildOf( mi3 ) );

        assertTrue( mi6.isChildOf( mi4 ) );
        assertTrue( mi6.isChildOf( mi1 ) );
        assertTrue( mi6.isChildOf( mi2 ) );

        // negative cases
        assertFalse( mi0.isChildOf( mi1 ) );
        assertFalse( mi0.isChildOf( mi2 ) );
        assertFalse( mi0.isChildOf( mi3 ) );
        assertFalse( mi0.isChildOf( mi4 ) );
        assertFalse( mi0.isChildOf( mi5 ) );
        assertFalse( mi0.isChildOf( mi6 ) );
    }

    @Test
    public void isParentOf() {
        assertTrue( mi0.isParentOf( mi0 ) );
        assertTrue( mi0.isParentOf( mi1 ) );
        assertTrue( mi0.isParentOf( mi2 ) );
        assertTrue( mi0.isParentOf( mi3 ) );
        assertTrue( mi0.isParentOf( mi4 ) );
        assertTrue( mi0.isParentOf( mi5 ) );
        assertTrue( mi0.isParentOf( mi6 ) );

        assertTrue( mi1.isParentOf( mi4 ) );
        assertTrue( mi1.isParentOf( mi6 ) );

        assertTrue( mi2.isParentOf( mi4 ) );
        assertTrue( mi2.isParentOf( mi6 ) );

        assertTrue( mi3.isParentOf( mi5 ) );

        assertTrue( mi4.isParentOf( mi6 ) );

        // Negative cases
        assertFalse( mi1.isParentOf( mi0 ) );
        assertFalse( mi2.isParentOf( mi0 ) );
        assertFalse( mi3.isParentOf( mi0 ) );
        assertFalse( mi4.isParentOf( mi0 ) );
        assertFalse( mi5.isParentOf( mi0 ) );
        assertFalse( mi6.isParentOf( mi0 ) );
    }

    @Test
    public void hasParent() {
        assertTrue( mi1.hasParent() );
        assertFalse( mi0.hasParent() );
    }

    @Test
    public void hasChildren() {
        assertTrue( mi0.hasChildren() );
        assertFalse( mi6.hasChildren() );
    }
}
