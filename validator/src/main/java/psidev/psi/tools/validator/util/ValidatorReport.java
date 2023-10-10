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
package psidev.psi.tools.validator.util;

import psidev.psi.tools.validator.rules.cvmapping.CvRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Collects data gathered during the validation process.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class ValidatorReport {

    private static final String NEW_LINE = System.getProperty( "line.separator" );

    Collection<CvRule> cvRulesNotChecked = new ArrayList<>();
    Collection<CvRule> cvRulesInvalidXpath = new ArrayList<>();
    Collection<CvRule> cvRulesValidXpath = new ArrayList<>();
    Collection<CvRule> cvRulesValid = new ArrayList<>();

    public ValidatorReport( Collection<CvRule> allCvRules ) {
        splitCvRulesByStatus( allCvRules );
    }

    private void splitCvRulesByStatus( Collection<CvRule> allCvRules ) {
        for ( CvRule rule : allCvRules ) {
            switch ( rule.getStatus() ) {
                case INVALID_XPATH:
                    cvRulesInvalidXpath.add( rule );
                    break;
                case NOT_CHECKED:
                    cvRulesNotChecked.add( rule );
                    break;
                case VALID_RULE:
                    cvRulesValid.add( rule );
                    break;
                case VALID_XPATH:
                    cvRulesValidXpath.add( rule );
                    break;
            }
        }
    }

    public Collection<CvRule> getCvRulesNotChecked() {
        return cvRulesNotChecked;
    }

    public Collection<CvRule> getCvRulesInvalidXpath() {
        return cvRulesInvalidXpath;
    }

    public Collection<CvRule> getCvRulesValidXpath() {
        return cvRulesValidXpath;
    }

    public Collection<CvRule> getCvRulesValid() {
        return cvRulesValid;
    }

    private void printRules( StringBuilder sb, String header, Collection<CvRule> rules ) {
        sb.append( header ).append( " (" ).append( rules.size() ).append( ")" ).append( NEW_LINE );
        sb.append( "-----------------------------------------------------" ).append( NEW_LINE );
        for ( CvRule rule : rules ) {
            sb.append( rule.getId() ).append( " - " ).append( rule.getName() ).append( NEW_LINE );
        }
        sb.append( NEW_LINE );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder( 512 );
        printRules( sb, "Valid rules", cvRulesValid );
        printRules( sb, "Rules with valid Xpath that have not collected data", cvRulesValidXpath );
        printRules( sb, "Rules with invalid Xpath", cvRulesInvalidXpath );
        printRules( sb, "Rules that haven't been run", cvRulesNotChecked );

        return sb.toString();
    }
}
