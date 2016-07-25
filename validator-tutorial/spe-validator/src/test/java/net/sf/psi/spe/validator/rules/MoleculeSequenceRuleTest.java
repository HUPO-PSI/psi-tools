package net.sf.psi.spe.validator.rules;

import net.sf.psi.spe.Molecule;
import net.sf.psi.spe.MoleculeType;
import net.sf.psi.spe.validator.SPEValidator;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.tools.validator.ValidatorMessage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

/**
 * MoleculeSequenceRule Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0.0
 */
public class MoleculeSequenceRuleTest {

    @Test
    public void no_sequence() throws Exception {
        File ontologyFile = new File( MoleculeSequenceRuleTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( MoleculeSequenceRuleTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( MoleculeSequenceRuleTest.class.getResource( "/object-rules.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0328", "small molecule" ) );

        final Collection<ValidatorMessage> messages = validator.validate( p1 );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( "Expected to find a single message about missing sequence in the protein", 1, messages.size() );
    }

    @Test
    public void valid_protein() throws Exception {
        File ontologyFile = new File( MoleculeSequenceRuleTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( MoleculeSequenceRuleTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( MoleculeSequenceRuleTest.class.getResource( "/object-rules.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0326", "protein" ) );
        p1.setSequence( "SSWWAHVEMGPPDPILGVTEAYKRDTNSKK" );

        final Collection<ValidatorMessage> messages = validator.validate( p1 );

        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void small_molecule_with_protein_sequence() throws Exception {
        File ontologyFile = new File( MoleculeSequenceRuleTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( MoleculeSequenceRuleTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( MoleculeSequenceRuleTest.class.getResource( "/object-rules.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0328", "small molecule" ) );
        p1.setSequence( "SSWWAHVEMGPPDPILGVTEAYKRDTNSKK" );

        final Collection<ValidatorMessage> messages = validator.validate( p1 );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( "Expected a single message as the given small molecule has a protein sequence", 1, messages.size() );
    }

    @Test
    public void dna_with_protein_sequence() throws Exception {
        File ontologyFile = new File( MoleculeSequenceRuleTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( MoleculeSequenceRuleTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( MoleculeSequenceRuleTest.class.getResource( "/object-rules.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Molecule p1 = new Molecule( "P12345", new MoleculeType( MoleculeSequenceRule.NUCLEIC_ACID, "dna" ) );
        p1.setSequence( "SSWWAHVEMGPPDPILGVTEAYKRDTNSKK" );

        final Collection<ValidatorMessage> messages = validator.validate( p1 );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( "Expected a single message as the given DNA molecule has a protein sequence", 1, messages.size() );
    }

    @Test
    public void rnai_with_protein_sequence() throws Exception {
        File ontologyFile = new File( MoleculeSequenceRuleTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( MoleculeSequenceRuleTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( MoleculeSequenceRuleTest.class.getResource( "/object-rules.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Molecule p1 = new Molecule( "RNAi", new MoleculeType( "SPE:0610", "rnai" ) );
        p1.setSequence( "ATGCATGCATGC" );

        final Collection<ValidatorMessage> messages = validator.validate( p1 );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( "Expected a single message as the given RNAi molecule has a DNA sequence", 1, messages.size() );
    }

    @Test
    public void dna_with_invalid_chars() throws Exception {
        File ontologyFile = new File( MoleculeSequenceRuleTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( MoleculeSequenceRuleTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( MoleculeSequenceRuleTest.class.getResource( "/object-rules.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Molecule p1 = new Molecule( "dbaX", new MoleculeType( MoleculeSequenceRule.NUCLEIC_ACID, "dna" ) );
        p1.setSequence( "ATGC/ATGCATGC" );

        final Collection<ValidatorMessage> messages = validator.validate( p1 );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( "Expected a single message as the given DNA molecule has invalid character in its sequence", 1, messages.size() );
    }
}
