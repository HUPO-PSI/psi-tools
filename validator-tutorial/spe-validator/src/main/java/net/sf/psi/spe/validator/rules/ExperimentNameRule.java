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

import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.ontology_manager.OntologyManager;

import java.util.Collection;
import java.util.ArrayList;

import net.sf.psi.spe.Experiment;

/**
 * Rule that checks that an experiment always has a name.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0.0
 */
public class ExperimentNameRule extends ObjectRule<Experiment> {

    public ExperimentNameRule( OntologyManager ontologyManager ) {
        super( ontologyManager );
    }

    public boolean canCheck( Object object ) {
        return ( object instanceof Experiment );
    }

    public Collection<ValidatorMessage> check( Experiment experiment ) throws ValidatorException {

        final String name = experiment.getName();

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>( ); 
        if( name == null || name.trim().length() == 0 ) {
            messages.add( new ValidatorMessage( "Experiment id:"+ experiment.getId() +" doesn't have a name.",
                                                MessageLevel.WARN ) );
        }

        return messages;
    }
}
