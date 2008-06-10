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
package net.sf.psi.spe.validator.rules;

import net.sf.psi.spe.Molecule;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks on the correctness of the molecule's sequence according to their type.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0.0
 */

public class MoleculeSequenceRule extends ObjectRule<Molecule> {

    Pattern AA_PATERN = Pattern.compile( "[ABCDEFGHIKLMNPQRSTVWYZ]+" );
    Pattern DNA_PATERN = Pattern.compile( "[ACGT]+" );
    Pattern RNA_PATERN = Pattern.compile( "[ACGU]+" );
    
    public static final String SMALL_MOLECULE = "SPE:0328";
    public static final String PROTEIN = "SPE:0326";
    public static final String NUCLEIC_ACID = "SPE:0318";
    public static final String RIBONUCLIEC_ACID = "SPE:0320";

    public MoleculeSequenceRule( OntologyManager ontologyManager ) {
        super( ontologyManager );
    }

    public boolean canCheck( Object object ) {
        return ( object instanceof Molecule );
    }

    public Collection<ValidatorMessage> check( Molecule molecule ) throws ValidatorException {

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        if ( molecule.hasSequence() ) {

            final String sequence = molecule.getSequence();
            final String type = molecule.getType().getName();
            final String typeId = molecule.getType().getId();

            Set<String> termIds = ontologyManager.getValidIDs( "SPE", PROTEIN, true, true ); // protein
            if ( termIds.contains( typeId ) ) {
                if ( !AA_PATERN.matcher( sequence ).matches() ) {
                    messages.add( new ValidatorMessage( "Molecule '" + molecule.getName() + "' of type '" + type +
                                                        "' has a non valid amino acid sequence: " + sequence,
                                                        MessageLevel.WARN ) );
                    return messages;
                }
            }

            termIds = ontologyManager.getValidIDs( "SPE", NUCLEIC_ACID, true, true ); // nucleic acid
            if ( termIds.contains( typeId ) ) {
                messages.add( new ValidatorMessage( "Molecule '" + molecule.getName() + "' of type '" + type +
                                                    "' has a non valid nucleic acid sequence: "  + sequence,
                                                    MessageLevel.WARN ) );
                return messages;
            }


            termIds = ontologyManager.getValidIDs( "SPE", RIBONUCLIEC_ACID, true, true ); // ribonucleic acid
            if ( termIds.contains( typeId ) ) {
                messages.add( new ValidatorMessage( "Molecule '" + molecule.getName() + "' of type '" + type +
                                                    "' has a non valid ribonucleic acid sequence: " + sequence,
                                                    MessageLevel.WARN ) );
                return messages;
            }

            if ( SMALL_MOLECULE.equals( typeId ) ) {
                messages.add( new ValidatorMessage( "Molecule '" + molecule.getName() + "' of type '" + type +
                                                    "' should not have a sequence: " + sequence,
                                                    MessageLevel.WARN ) );
                return messages;
            }

        } else {
            messages.add( new ValidatorMessage( "Molecule '" + molecule.getName() + "' doesn't have a sequence.",
                                                MessageLevel.INFO ) );
        }

        return messages;
    }
}