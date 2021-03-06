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

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorMessage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

public class ValidatorRuleTest {

    @Test
    public void check_RuleObject_Loading_FromValidatorResource_ok() throws Exception {

        File ontologyFile = new File( ValidatorRuleTest.class.getResource( "ontology-config.xml" ).getFile() );
        File objectRules = new File( ValidatorRuleTest.class.getResource("/xmlRuleSets/object-rules.xml").getFile() );


        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                null,
                new FileInputStream( objectRules ) );

        Object experiment = new Object( );

        final Collection<ValidatorMessage> messages = validator.validate( experiment );

        System.out.println( "Validation run collected " + messages.size() + " message(s):" );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( 4, validator.getObjectRules().size() );
    }

    @Test
    public void check_RuleObject_Loading_InstantiatedOntologyManager_ok() throws Exception {
        OntologyManager ontologyManager = new OntologyManager(ValidatorRuleTest.class.getResource( "ontology-config.xml" ).openStream());

        SPEValidator validator = new SPEValidator( ontologyManager, null, null);

        Object experiment = new Object( );

        final Collection<ValidatorMessage> messages = validator.validate( experiment );

        Assert.assertNotNull(validator.getOntologyMngr());
        Assert.assertTrue(messages.isEmpty());
    }

    @Test
    public void check_RuleObject_Loading_FromValidatorResource_With_several_imports() throws Exception {

        File ontologyFile = new File( ValidatorRuleTest.class.getResource( "ontology-config.xml" ).getFile() );
        File objectRules = new File( ValidatorRuleTest.class.getResource("/xmlRuleSets/object-rules-4.xml").getFile() );


        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                null,
                new FileInputStream( objectRules ) );

        Object experiment = new Object( );

        final Collection<ValidatorMessage> messages = validator.validate( experiment );

        System.out.println( "Validation run collected " + messages.size() + " message(s):" );
        for ( ValidatorMessage message : messages ) {
            System.out.println( message );
        }

        Assert.assertEquals( 4, validator.getObjectRules().size() );
    }

    @Test
    public void check_RuleObject_Loading_FromValidatorResource_With_several_imports_one_exclusion() throws Exception {

        File ontologyFile = new File( ValidatorRuleTest.class.getResource( "ontology-config.xml" ).getFile() );
        File objectRules = new File( ValidatorRuleTest.class.getResource("/xmlRuleSets/object-rules-5.xml").getFile() );


        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                null,
                new FileInputStream( objectRules ) );

        Assert.assertEquals( 3, validator.getObjectRules().size() );
    }

    @Test
    public void check_RuleObject_Loading_FromValidatorResource_With_several_imports_one_exclusion_one_included() throws Exception {

        File ontologyFile = new File( ValidatorRuleTest.class.getResource( "ontology-config.xml" ).getFile() );
        File objectRules = new File( ValidatorRuleTest.class.getResource("/xmlRuleSets/object-rules-6.xml").getFile() );


        SPEValidator validator = new SPEValidator( new FileInputStream( ontologyFile ),
                null,
                new FileInputStream( objectRules ) );

        Assert.assertEquals( 3, validator.getObjectRules().size() );
    }

}



