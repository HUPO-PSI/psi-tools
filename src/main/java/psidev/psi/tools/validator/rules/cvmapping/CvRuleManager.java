package psidev.psi.tools.validator.rules.cvmapping;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRules;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.xpath.XPathHelper;

import java.util.*;

/**
 * Generic rule that will take care of validating objects against a given CvMapping.
 * <p/>
 * Use the CvMappingRuleFactory to construct this object.
 *
 * @author Samuel Kerrien
 * @version $Id: CvMappingRule.java 668 2007-06-29 16:44:18 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public class CvRuleManager {
    // should be CvMappingRuleList
    public static final Log log = LogFactory.getLog( CvRuleManager.class );

    // is this ever used? -> nowhere else stored
    private CvMappingRules cvMappingRules;

    private OntologyManager ontologyMngr;

    // ToDo: used on this level? does it make sense?
    // not used in PSI-MI, was meant for MS data but can/should be handled by ObjectRules, should work on element level only
    private Map<CvTerm, Integer> nonRepeatableTerms = null;

    private Collection<CvRule> rules;

    //////////////////
    // Constructors

    public CvRuleManager( OntologyManager ontoMngr, CvMappingRules cvMappingRules ) {
        this.ontologyMngr = ontoMngr;
        this.cvMappingRules = cvMappingRules;
        addRules(cvMappingRules.getCvMappingRule());
    }

    ////////////////////////
    // Getters and Setters

    public CvMappingRules getCvMappingRules() {
        return cvMappingRules;
    }

    public void setCvMappingRules( CvMappingRules cvMappingRules) {
        this.cvMappingRules = cvMappingRules;
        addRules(cvMappingRules.getCvMappingRule());
    }

    public Collection<CvRule> getCvRules() {
        return rules;
    }

    //////////////////////
    // Rule

    // ToDo: remove this method or keep for convenience?
    // replacement of the check(object, null) method with check(object, xpath_from_root) ?
    public Collection<ValidatorMessage> check( Object o ) throws ValidatorException {

        if ( o == null ) {
            throw new ValidatorException( "Cannot validate a null object." );
        }

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        for (CvRule rule : rules) {
            messages.addAll(rule.check(o, null));
        }
        return messages;
    }

    /**
     * Checks that the CvMapping is valid. CvTerms and Rules can be pruned along the way and messages explaining it
     * should inform the user. This collection of messages is returned to the user.
     *
     * @return a non null collection of ValidatorMessage.
     * @throws psidev.psi.tools.validator.ValidatorException ...
     */
    public Collection<ValidatorMessage> checkCvMapping() throws ValidatorException {
        // ToDo: check this!
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        if ( rules.size() < 1 ) {
            throw new ValidatorException("checkCvMapping: There are no rules to check! Make sure valid rules have been loaded.");
        }
        for ( Iterator<CvRule> it_rule = rules.iterator(); it_rule.hasNext(); ) {
            CvRule rule = it_rule.next();

            try {
                // try to compile the xpath expression
                // TODO this is where one would want to cache the compiled expression !
                XPathHelper.evaluateXPath( rule.getElementPath(), "" );

                int cvTermCount = rule.getCVTerms().size();
                Iterator<CvTerm> it_cv = rule.getCVTerms().iterator();
                while ( it_cv.hasNext() ) {
                    CvTerm cvTerm = it_cv.next();
                    if ( !isValidCvTerm( cvTerm, rule, messages ) ) {
                        //ToDo: add message that rule has been removed
                        it_cv.remove(); // remove the rule from the cvMappingRules
                    }
                } // cvTerms

                if ( rule.getCVTerms().isEmpty() ) {
                    // no cvterms available, remove the rule
                    it_rule.remove();
                    String msg = "All CvTerm" + ( cvTermCount > 1 ? "s" : "" ) + " (" + cvTermCount + ") of this rule " +
                                 ( cvTermCount > 1 ? "were" : "was" ) + " removed due " +
                                 "to inconsistencies (cf. previous messages). This rule will be removed.";
                    messages.add( rule.buildMessage( rule.getElementPath(),
                                                Recommendation.forName( rule.getRequirementLevel() ),
                                                msg, rule ) );
                }

            } catch ( JXPathException e ) {
                // failed to compile the XPath expression
                it_rule.remove();
                String msg = "The XPath expression could not be compiled: " + rule.getElementPath() +
                                            " . This rule will be removed.";
                messages.add( rule.buildMessage( rule.getElementPath(),
                                            Recommendation.forName( rule.getRequirementLevel() ),
                                            msg, rule ) );
            }
        } // rules


        return messages;
    }

    /**
     * Check if the given CvTerm is valid or not. If not, ValidatorMessage are generated along the way and stored in
     * the given collection.
     *
     * @param cvTerm   the term to check upon
     * @param rule     the rule to which the cvTerm is attached
     * @param messages the non null collection of messages
     * @return true is the cvTerm is valid, false otherwise.
     * @throws ValidatorException if valdation failed.
     */
    private boolean isValidCvTerm( CvTerm cvTerm,
                                   CvRule rule,
                                   Collection<ValidatorMessage> messages ) throws ValidatorException {

        String ontologyID = cvTerm.getCvIdentifier();
        System.out.println("##### DEBUG: Checking cvTerm " + cvTerm.getTermName() + " for ontology: " + ontologyID );

        if ( !ontologyMngr.containsKey( ontologyID ) ) {
            String msg = "The requested ontology wasn't defined: " + ontologyID + ". The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ));
            return false;
        }



//        OntologyTerm ruleTerm = ontology.search( cvTerm.getTermAccession() );
        String ruleTermAcc = cvTerm.getTermAccession();
        Set<String> validAccs = ontologyMngr.getValidIDs( ontologyID, ruleTermAcc, false, true );
        if ( validAccs.size() == 1 && validAccs.contains( ruleTermAcc ) ) {
            // no children, only use the specified term -> there should be only one result (if the term is valid)
        } else {
            String msg = "The rule defines a term " + printSimpleCvTerm( cvTerm ) + " that doesn't " +
                         "exist in that ontology: " + ontologyID + ". The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ) );
            return false;
        }

//        if ( ruleTerm.isObsolete() ) {
        if ( ontologyMngr.isObsoleteID( ontologyID, ruleTermAcc )) {
            // this term should not be in use here
            String msg = "The term " + printSimpleCvTerm( cvTerm ) + " is obsolote in the ontology " +
                         ontologyID + ". The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ) );
            return false;
        }

        validAccs = ontologyMngr.getValidIDs( ontologyID, ruleTermAcc, true, false );
        // if validAccs == 0, then there were no children
        if ( (validAccs.size() == 0) && !cvTerm.isUseTerm() && cvTerm.isAllowChildren() ) {
            // this term doesn't have children yet the cvmapping recommends to use them
            String msg = "The term " + printSimpleCvTerm( cvTerm ) + " doesn't have children in the ontology '" +
                         ontologyID + "', yet the CvMapping recommends to use them. " +
                         "The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ) );
            return false;
        }

        if ( !cvTerm.isUseTerm() && !cvTerm.isAllowChildren() ) {
            // TODO check with Luisa if this makes sense ...
            // don't use the term and also don't use child terms -> doesn't make sense
            String msg = "The term " + printSimpleCvTerm( cvTerm ) + " defines that neither the term nor any of " +
                         "its children can be used. The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ) );
            return false;
        }


        return true;
    }



    ///////////////
    // utilities

    private void addRules(List<CvMappingRule> cvMappingRules) {
        if (rules == null) {
            rules = new ArrayList<CvRule>();
        }
        for (CvMappingRule cvMappingRule : cvMappingRules) {
            CvRuleImpl rule = new CvRuleImpl(ontologyMngr);
            rule.setCvMappingRule(cvMappingRule);
            rule.setNonRepeatableTerms(nonRepeatableTerms);
            rules.add(rule);
        }

    }

    protected String printSimpleCvTerm( CvTerm cv ) {
        StringBuilder sb = new StringBuilder();
        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ' ' );
        sb.append( '(' ).append( cv.getTermAccession() ).append( ')' );
        return sb.toString();
    }


}
