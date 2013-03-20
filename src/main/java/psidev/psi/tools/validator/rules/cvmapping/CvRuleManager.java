package psidev.psi.tools.validator.rules.cvmapping;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvReference;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.OntologyUtils;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;
import psidev.psi.tools.validator.MessageLevel;
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

    public static final Log log = LogFactory.getLog( CvRuleManager.class );

    private OntologyManager ontologyMngr;

    private Collection<CvRule> rules;

    private List<CvReference> cvReferences;

    //////////////////
    // Constructors

    public CvRuleManager( OntologyManager ontoMngr, CvMapping cvMappingRules ) {
        if( ontoMngr == null ) {
            throw new IllegalArgumentException( "The given OntologyManager was null, cannot instanciate a CvRuleManager" );
        }
        this.ontologyMngr = ontoMngr;
        addRules(cvMappingRules.getCvMappingRuleList().getCvMappingRule());
        cvReferences = cvMappingRules.getCvReferenceList().getCvReference();
    }

    ////////////////////////
    // Getters and Setters

    public void setCvMappingRules( CvMapping cvMappingRules) {
        addRules(cvMappingRules.getCvMappingRuleList().getCvMappingRule());
    }

    public Collection<CvRule> getCvRules() {
        return rules;
    }

    //////////////////////
    // Rule

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

    public Collection<ValidatorMessage> check( Object o, String contextXpath ) throws ValidatorException {

        if ( o == null ) {
            throw new ValidatorException( "Cannot validate a null object." );
        }

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        for (CvRule rule : rules) {
            messages.addAll(rule.check(o, contextXpath));
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
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        if( cvReferences != null ) {
            for ( CvReference cvReference : cvReferences ) {

                // before anything else, check if the specified ontology was loaded in the ontology manager
                if ( !ontologyMngr.containsOntology( cvReference.getCvIdentifier() ) ) {
                    String msg = "The requested ontology wasn't defined: " + cvReference.getCvIdentifier() + " ("+
                                 cvReference.getCvName()+"). The CvTerm will be removed.";
                    messages.add( new ValidatorMessage( msg, MessageLevel.WARN ) );
                }
            }
        }
        
        if ( rules.size() < 1 ) {
            throw new ValidatorException("checkCvMapping: There are no rules to check! Make sure valid rules have been loaded.");
        }
        for ( Iterator<CvRule> it_rule = rules.iterator(); it_rule.hasNext(); ) {
            CvRule rule = it_rule.next();

            final String elementPath = rule.getElementPath();
            final String scopePath = rule.getScopePath();

            try {
                // try to compile the xpath expression

                if( ! elementPath.startsWith( scopePath )) {
                    it_rule.remove();
                    String msg = "The scope ('"+ scopePath +"') of this rule did not match the element ('"+elementPath+
                                 "') defined. This rule will be removed.";
                    messages.add( rule.buildMessage( elementPath,
                                                     Recommendation.forName( rule.getRequirementLevel() ),
                                                     msg, rule ) );
                } else {

                    // test compile the XPaths
                    // TODO this is where one would want to cache the compiled expression !
                    XPathHelper.evaluateXPath( scopePath, "" );
                    XPathHelper.evaluateXPath( elementPath, "" );

                    int cvTermCount = rule.getCVTerms().size();
                    Iterator<CvTerm> it_cv = rule.getCVTerms().iterator();
                    while ( it_cv.hasNext() ) {
                        CvTerm cvTerm = it_cv.next();
                        if ( !isValidCvTerm( cvTerm, rule, messages ) ) {
                            //ToDo: add message that TERM has been removed
                            it_cv.remove(); // remove the term from the cvMappingRule
                        }
                    } // cvTerms

                    // If no cv terms remaining, remove the rule
                    if ( rule.getCVTerms().isEmpty() ) {
                        it_rule.remove();
                        String msg = "All CvTerm" + ( cvTermCount > 1 ? "s" : "" ) + " (" + cvTermCount + ") of this rule " +
                                     ( cvTermCount > 1 ? "were" : "was" ) + " removed due " +
                                     "to inconsistencies (cf. previous messages). This rule will be removed.";
                        messages.add( rule.buildMessage( elementPath,
                                                         Recommendation.forName( rule.getRequirementLevel() ),
                                                         msg, rule ) );
                    }
                }

            } catch ( JXPathException e ) {
                // failed to compile the XPath expression
                it_rule.remove();
                String msg = "The XPath expression could not be compiled: " + elementPath +
                                            " . This rule will be removed.";
                messages.add( rule.buildMessage( elementPath,
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

        // ToDo: !! RESTRUCTURE THIS !! 

        String ontologyID = (( CvReference ) cvTerm.getCvIdentifierRef()).getCvIdentifier();
        log.debug("Checking cvTerm " + cvTerm.getTermName() + " for ontology: " + ontologyID );

        // before anything else, check if the specified ontology was loaded in the ontology manager
        if ( !ontologyMngr.containsOntology( ontologyID ) ) {
            String msg = "The requested ontology wasn't defined: " + ontologyID + ". The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ));
            return false;
        }

        // check if the specified term is valid in the given ontology
        String ruleTermAcc = cvTerm.getTermAccession();
        OntologyAccess ontoAccess = ontologyMngr.getOntologyAccess(ontologyID);
        Set<OntologyTermI> validTerms = ontoAccess.getValidTerms( ruleTermAcc, false, true );
        Collection<String> validAccs = OntologyUtils.getAccessions(validTerms);
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

        // check if the used term is obsolete
        if ( ontoAccess.isObsolete(ontoAccess.getTermForAccession(ruleTermAcc)) ) {
            // this term should not be in use here
            String msg = "The term " + printSimpleCvTerm( cvTerm ) + " is obsolete in the ontology " +
                         ontologyID + ". The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ) );
            return false;
        }

        // ToDo: restructure this
        // check if the specified term has children, if it was specified to use children and not the term itself
        validTerms = ontoAccess.getDirectChildren( ontoAccess.getTermForAccession(ruleTermAcc) );
        validAccs = OntologyUtils.getAccessions(validTerms);
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

        // check the use 'useTerm' and 'allowChildren'
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

        // ToDo: add check for 'useTermName'?


        // check the use of 'isRepeatable' and 'scope'
        if ( ! cvTerm.isIsRepeatable() ) {
            // first check if a scope is defined
            String scope = rule.getScopePath();
            if ( scope == null ) {
                // not allowed, if the term is not repeatable, there MUST be a scope defined
                // create error message
                String msg = "The CvTerm " + printSimpleCvTerm( cvTerm ) + " defines that the term is NOT repeatable, " +
                             "but there was no scope defined. The CvTerm will be removed.";
                messages.add( rule.buildMessage( rule.getElementPath(),
                                            Recommendation.forName( rule.getRequirementLevel() ),
                                            msg, rule ) );
                return false;
            }
            // then check if the specified scope matches the rule xpath
            String xpath = rule.getElementPath();
            if ( xpath.indexOf(scope) != 0 ) {
                // The scope has to be a substring of the rule xpath and start at the root
                String msg = "The CvTerm " + printSimpleCvTerm( cvTerm ) + " defines that the term is NOT repeatable, " +
                             "but the specified scope is not valid. The CvTerm will be removed.";
                messages.add( rule.buildMessage( rule.getElementPath(),
                                            Recommendation.forName( rule.getRequirementLevel() ),
                                            msg, rule ) );
                return false;
            }
        }

        return true;
    }


    ///////////////
    // utilities

    protected void addRules(List<CvMappingRule> cvMappingRules) {
        if (rules == null) {
            rules = new ArrayList<CvRule>();
        }
        for (CvMappingRule cvMappingRule : cvMappingRules) {
            CvRuleImpl rule = new CvRuleImpl(ontologyMngr);
            rule.setCvMappingRule(cvMappingRule);
            rules.add(rule);
        }
    }

    protected String printSimpleCvTerm( CvTerm cv ) {
        StringBuilder sb = new StringBuilder( 64 );
        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ' ' );
        sb.append( '(' ).append( cv.getTermAccession() ).append( ')' );
        return sb.toString();
    }
}