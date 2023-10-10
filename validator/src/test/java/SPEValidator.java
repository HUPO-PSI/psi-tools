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

import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.validator.Validator;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class SPEValidator extends Validator {

    public SPEValidator( InputStream ontoConfig,
                         InputStream cvRuleConfig,
                         InputStream objectRuleConfig ) throws ValidatorException, OntologyLoaderException {
        super( ontoConfig, cvRuleConfig, objectRuleConfig );
    }

    public SPEValidator( InputStream ontoConfig,
                         InputStream cvRuleConfig ) throws ValidatorException, OntologyLoaderException {
        super( ontoConfig, cvRuleConfig );
    }

    public SPEValidator( InputStream ontoConfig ) throws OntologyLoaderException {
        super( ontoConfig );
    }

    public SPEValidator(OntologyManager ontologyManager, CvMapping cvMapping, Collection<ObjectRule> objectRules){
        super(ontologyManager, cvMapping, objectRules);
    }


    public Collection<ValidatorMessage> validate( Object experiment ) throws ValidatorException {

        if( experiment == null ) throw new IllegalArgumentException( "You must give a non null experiment" );

        final Collection<ValidatorMessage> messages = new ArrayList<>();

        // Run Object Rules
        messages.addAll( super.validate( experiment ) );

        return messages;
    }
}