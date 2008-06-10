package net.sf.psi.spe.validator;

import net.sf.psi.spe.Experiment;
import net.sf.psi.spe.Modification;
import net.sf.psi.spe.Molecule;
import net.sf.psi.spe.MoleculeType;
import net.sf.psi.spe.validator.rules.MoleculeSequenceRule;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.tools.validator.ValidatorMessage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

/**
 * SPEValidator Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class SPEValidatorTest {

    @Test
    public void cvmapping_withoutError() throws Exception {
        File ontologyFile = new File( SPEValidatorTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( SPEValidatorTest.class.getResource( "/cv-mapping.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ) );

        Experiment experiment = new Experiment( 1 );
        experiment.setName( "proteomics-exp-1" );

        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0326", "protein" ) );
        p1.addModification( new Modification( "MOD:00009", "natural residue" ) );

        Molecule p2 = new Molecule( "Q98765", new MoleculeType( "SPE:0326", "protein" ) );
        p2.addModification( new Modification( "MOD:00850", "unnatural residue" ) );

        experiment.addMolecule( p1 );
        experiment.addMolecule( p2 );

        final Collection<ValidatorMessage> messages = validator.checkCvMapping( experiment, "/experiment/" );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( 0, messages.size() );
    }

    @Test
    public void cvmapping_withError() throws Exception {
        File ontologyFile = new File( SPEValidatorTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( SPEValidatorTest.class.getResource( "/cv-mapping.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ) );

        Experiment experiment = new Experiment( 2 );
        experiment.setName( "proteomics-exp-2" );


        // This protein doesn't have a modification, that triggers an error.
        // TODO Is this really wanted, maybe we want to send an error only when there is a modification defined ???
        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0326", "protein" ) );

        Molecule p2 = new Molecule( "Q98765", new MoleculeType( "SPE:0326", "protein" ) );
        p2.addModification( new Modification( "MOD:00850", "unnatural residue" ) );

        experiment.addMolecule( p1 );
        experiment.addMolecule( p2 );

        final Collection<ValidatorMessage> messages = validator.checkCvMapping( experiment, "/experiment/" );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( 1, messages.size() );
    }

    @Test
    public void cvmapping_moleculeType_error() throws Exception {
        File ontologyFile = new File( SPEValidatorTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( SPEValidatorTest.class.getResource( "/cv-mapping.xml" ).getFile() );

        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ) );

        Experiment experiment = new Experiment( 3 );
        experiment.setName( "proteomics-exp-3" );


        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0328", "small molecule" ) );
        p1.addModification( new Modification( "MOD:00009", "natural residue" ) );

        experiment.addMolecule( p1 );

        final Collection<ValidatorMessage> messages = validator.checkCvMapping( experiment, "/experiment/" );

        System.out.println( messages.size() + " message(s) found." );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( "Here we expect to find an error related to the molecule being a small molecule " +
                             "instead of a protein", 1, messages.size() );
    }

    @Test
    public void validate() throws Exception {
        File ontologyFile = new File( SPEValidatorTest.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( SPEValidatorTest.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( SPEValidatorTest.class.getResource( "/object-rules.xml" ).getFile() );


        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );
        
        Experiment experiment = new Experiment( 3 );
//        experiment.setName( "proteomics-exp-3" );
        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0328", "small molecule" ) );
        p1.addModification( new Modification( "BLA:0000X", "natural residue" ) );
        experiment.addMolecule( p1 );

        final Collection<ValidatorMessage> messages = validator.validate( experiment );

        System.out.println( "Validation run collected "+ messages.size()+" message(s):" );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }
    }
}
