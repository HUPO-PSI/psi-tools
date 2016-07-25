/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.psi.spe.validator;

import net.sf.psi.spe.Experiment;
import net.sf.psi.spe.Modification;
import net.sf.psi.spe.Molecule;
import net.sf.psi.spe.MoleculeType;
import psidev.psi.tools.validator.ValidatorMessage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

public class SPEValidatorRunner {

    public static void main( String[] args ) throws Exception {

        File ontologyFile = new File( SPEValidatorRunner.class.getResource( "/ontologies.xml" ).getFile() );
        File mappingRules = new File( SPEValidatorRunner.class.getResource( "/cv-mapping.xml" ).getFile() );
        File objectRules = new File( SPEValidatorRunner.class.getResource( "/object-rules.xml" ).getFile() );


        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                                                   new FileInputStream( mappingRules ),
                                                   new FileInputStream( objectRules ) );

        Experiment experiment = new Experiment( 3 );
//        experiment.setName( "proteomics-exp-3" );
        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0328", "small molecule" ) );
        p1.addModification( new Modification( "BLA:0000X", "natural residue" ) );
        experiment.addMolecule( p1 );

        final Collection<ValidatorMessage> messages = validator.validate( experiment );

        System.out.println( "Validation run collected " + messages.size() + " message(s):" );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

    }
}



