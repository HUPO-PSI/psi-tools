package psidev.psi.tools.validator.rules.cvmapping;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.validator.cvmapping.jaxb.CVMappingType;
import psidev.psi.validator.cvmapping.jaxb.CVSource;
import psidev.psi.validator.cvmapping.jaxb.ModelElementMap;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.xpath.XPathHelper;
import psidev.psi.tools.ontology_manager.OntologyManager;

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
    private CVMappingType cvMapping;

    private OntologyManager ontologyMngr;

    // ToDo: used on this level? does it make sense?
    // not used in PSI-MI, was meant for MS data but can/should be handled by ObjectRules, should work on element level only
    private Map<ModelElementMap.CVTerm, Integer> nonRepeatableTerms = null;

    private Collection<CvRule> rules;

    //////////////////
    // Constructors

    public CvRuleManager( OntologyManager ontoMngr, CVMappingType cvMapping ) {
        this.ontologyMngr = ontoMngr;
        this.cvMapping = cvMapping;
        addRules(cvMapping.getModelElementMapList().getModelElementMap());
    }

    ////////////////////////
    // Getters and Setters

    public CVMappingType getCvMapping() {
        return cvMapping;
    }

    public void setCvMapping( CVMappingType cvMapping ) {
        this.cvMapping = cvMapping;
        addRules(cvMapping.getModelElementMapList().getModelElementMap());
    }

    public Collection<CvRule> getCvRules() {
        return rules;
    }

    //////////////////////
    // Rule

/**
//    protected MessageLevel convertCvMappingLevel( Recommendation level ) {
//        switch ( level ) {
//            case MAY:
//                return MessageLevel.INFO;
//            case SHOULD:
//                return MessageLevel.WARN;
//            case MUST:
//                return MessageLevel.ERROR;
//            default:
//                throw new IllegalArgumentException( "Unknown CvMapping RequirementLevel: " + level );
//        }
//    }

//    protected boolean isMatchingCv( ModelElementMap.CVTerm cvTerm, String accession,
//                                    Collection<ValidatorMessage> messages, Recommendation level, String xpath ) throws ValidatorException {
//
//        CVSource source = ( CVSource ) cvTerm.getCvRef();
//        Ontology ontology = getOntology( source.getCvIdentifier() );
//        if ( ontology == null ) {
//            throw new ValidatorException( "The requested ontology wasn't defined: " + source.getCvIdentifier() );
//        }
//
//        boolean allowChildren = cvTerm.isAllowChildren();
//        boolean useTerm = cvTerm.isUseTerm();
//
//        OntologyTerm ruleTerm = ontology.search( cvTerm.getTermAccession() );
//        if ( ruleTerm == null ) {
//            throw new ValidatorException( "This rule defines the term " + printSimpleCvTerm( cvTerm ) + " that doesn't " +
//                                          "exist in that ontology: " + cvTerm.getCvRef() );
//        }
//
//        OntologyTerm targetTerm = ontology.search( accession );
//        if ( targetTerm == null ) {
//            if ( log.isDebugEnabled() ) {
//                log.debug( "Could not find term " + printSimpleCvTerm( cvTerm ) + " in ontology '" + cvTerm.getCvRef() + "', return false." );
//            }
//            return false;
//        }
//
//        if ( false == useTerm && false == allowChildren && accession.equals( cvTerm.getTermAccession() ) ) {
//        }
//
//        // the term is part of the ontology, now check if it matches the range defined in cvTerm
//        if ( false == useTerm && accession.equals( cvTerm.getTermAccession() ) ) {
//            if ( log.isDebugEnabled() ) {
//                log.debug( "useTerm was set to false but the xpath pointed to the same accession '" + accession + "', return false." );
//            }
//            StringBuilder sb = new StringBuilder( 256 );
//            sb.append( "The term " + printSimpleCvTerm( cvTerm ) + " is not supposed to be used in " + xpath + "." );
//            if ( allowChildren ) {
//                sb.append( " Instead you could use one of its children term." );
//            } else {
//                sb.append( " Furthermore, its children are not allowed either." );
//            }
//            messages.add( buildMessage( xpath, level, sb.toString() ) );
//
//            return false;
//        }
//
//        if ( true == useTerm && accession.equals( cvTerm.getTermAccession() ) ) {
//            if ( log.isDebugEnabled() ) {
//                log.debug( "useTerm was set to true but the xpath pointed to the same accession '" + accession + "', return true." );
//            }
//            incrementCvTermCount( cvTerm );
//            return true;
//        }
//
//        if ( allowChildren && targetTerm.isChildOf( ruleTerm ) ) {
//            incrementCvTermCount( cvTerm );
//            return true;
//        } else if ( allowChildren ) {
//            // that is it was not a child of
//            StringBuilder sb = new StringBuilder();
//            sb.append( "The term found in the data file '" ).append( accession ).append( "'" );
//            sb.append( " is not a child of term in mapping file '" ).append( printSimpleCvTerm( cvTerm ) ).append( "'." );
//            messages.add( buildMessage( xpath, level, sb.toString() ) );
//        } else if ( !allowChildren ) {
//            // that is targetTerm.isChildOf( ruleTerm ) was true but allow children was false
//            // TODO should we create an error
//        }
//
//        return false;
//    }

//    private void incrementCvTermCount( ModelElementMap.CVTerm cvTerm ) {
//        Integer count = nonRepeatableTerms.get( cvTerm );
//        if ( count == null ) {
//            count = Integer.valueOf( 1 );
//        } else {
//            count++;
//        }
//        nonRepeatableTerms.put( cvTerm, count );
//    }

//    protected String printCvTerm( ModelElementMap.CVTerm cv ) {
//        StringBuilder sb = new StringBuilder();
//        sb.append( "CvTerm(" );
//        sb.append( '\'' ).append( cv.getTermAccession() ).append( '\'' ).append( ',' ).append( ' ' );
//        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ',' ).append( ' ' );
//        sb.append( "allowChildren:" ).append( cv.isAllowChildren() ).append( ',' ).append( ' ' );
//        sb.append( "useTerm:" ).append( cv.isUseTerm() ).append( ',' ).append( ' ' );
//        sb.append( "repeatable:" ).append( cv.isIsRepeatable() );
//        sb.append( ")" );
//        return sb.toString();
//    }

//    protected String printSimpleCvTerm( ModelElementMap.CVTerm cv ) {
//        StringBuilder sb = new StringBuilder();
//        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ' ' );
//        sb.append( '(' ).append( cv.getTermAccession() ).append( ')' );
//        return sb.toString();
//    }

//    public Collection<ValidatorMessage> check( Object object ) throws ValidatorException {
//
//        if ( object == null ) {
//            throw new ValidatorException( "Cannot validate a null object." );
//        }
//
//        if ( nonRepeatableTerms != null ) {
//            throw new IllegalStateException( "This rule instance is being run already! " +
//                                             "Looks like you're having some multithreading issues." );
//        }
//
//        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
//
//        List<ModelElementMap> rules = cvMapping.getModelElementMapList().getModelElementMap();
//        for ( ModelElementMap rule : rules ) {
//
//            Recommendation level = Recommendation.forName( rule.getRequirementLevel() );
//
//            String xpath = rule.getElementPath();
//            if ( log.isDebugEnabled() ) {
//                log.debug( "xpath = '" + xpath + "'" );
//                log.debug( "object = " + object );
//            }
//            List<XPathResult> results = null;
//            try {
//                results = XPathHelper.evaluateXPath( xpath, object );
//            } catch ( JXPathException e ) {
//                messages.add( buildMessage( xpath, level, "Skip this rule as the XPath expression could not be compiled: " + xpath ) );
//                continue;
//            }
//
//            if ( log.isDebugEnabled() ) {
//                log.debug( "Found " + results.size() + " hit(s)." );
//            }
//
//            if ( results.isEmpty() ) {
//
//                // We agreed with Luisa that we don't need to display this one.
//                messages.add( buildMessage( xpath, level, "The given XPath(" + xpath + ") didn't match any data." ) );
//
//            } else {
//
//                if ( nonRepeatableTerms != null ) {
//                    throw new IllegalStateException( "This rule instance is being run already! " +
//                                                     "Looks like you're having some multithreading issues." );
//                }
//
//                // TODO for each XPath, build a list of non repeatable CVs and keep track of it.
//                // initialize the map
//                nonRepeatableTerms = new HashMap<ModelElementMap.CVTerm, Integer>();
//
//                // check that each match has at least one matching CV term amongst those specified.
//                for ( XPathResult result : results ) {
//                    // for each XPath expression,
//                    log.debug( result );
//
//                    boolean foundOne = false;
//
//                    Iterator<ModelElementMap.CVTerm> it = rule.getCVTerm().iterator();
//                    while ( it.hasNext() ) {
//                        ModelElementMap.CVTerm cvTerm = it.next();
//
//                        try {
//                            String accession = ( String ) result.getResult();
//                            if ( isMatchingCv( cvTerm, accession, messages, level, xpath ) ) {
//                                foundOne = true;
//                                // TODO break ??
//                            } else {
//                                if ( log.isDebugEnabled() ) {
//                                    log.debug( "No match between '" + accession + "' and " + printCvTerm( cvTerm ) );
//                                }
//                            }
//
//                        } catch ( ClassCastException cce ) {
//                            // Message explaining that the xpath doesn't describe a CV term accession
//                            cce.printStackTrace();
//                            messages.add( buildMessage( xpath, level, "The object pointed to by the XPath(" + xpath +
//                                                                      ") was not a CV term accession (String) as " +
//                                                                      "expected, instead: " +
//                                                                      result.getResult().getClass().getName() ) );
//                        }
//                    }
//
//                    if ( !foundOne ) {
//                        // create a message
//                        StringBuilder sb = new StringBuilder();
//                        sb.append( "None of the given CvTerms matched the target (" + xpath + ") '" + result.getResult()
//                                   + "' :\n" );
//                        Iterator<ModelElementMap.CVTerm> iterator = rule.getCVTerm().iterator();
//                        while ( iterator.hasNext() ) {
//                            ModelElementMap.CVTerm cvTerm = iterator.next();
//                            sb.append( "  - " ).append( printCvTerm( cvTerm ) );
//                            if ( iterator.hasNext() ) {
//                                sb.append( "\n" );
//                            }
//                        }
//
//                        messages.add( buildMessage( xpath, level, sb.toString() ) );
//                    }
//                }
//
//                // if any of the non repeatable element was defined more than once, create a message
//                for ( Map.Entry<ModelElementMap.CVTerm, Integer> entry : nonRepeatableTerms.entrySet() ) {
//                    ModelElementMap.CVTerm cvTerm = entry.getKey();
//                    Integer count = entry.getValue();
//
//                    // Note: default value of an unspecified isRepeatable is false
//                    boolean isRepeatable = cvTerm.isIsRepeatable();
//                    if ( count > 1 && !isRepeatable ) {
//                        messages.add( buildMessage( xpath,
//                                                    level,
//                                                    "According to the CvMapping, the term '" + cvTerm.getTermAccession() +
//                                                    "' wasn't meant to be repeated, yet it appeared " + count +
//                                                    " times in elements pointed out by the XPath expression: " + xpath ) );
//                    }
//                }
//
//                // reset the map
//                nonRepeatableTerms.clear();
//                nonRepeatableTerms = null;
//            }
//        }
//
//        return messages;
//    }
*/

    // replacement of the old check method (with default value: complete xpath from root)
    // ToDo: remove this method ?
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
                Iterator<ModelElementMap.CVTerm> it_cv = rule.getCVTerms().iterator();
                while ( it_cv.hasNext() ) {
                    ModelElementMap.CVTerm cvTerm = it_cv.next();
                    if ( !isValidCvTerm( cvTerm, rule, messages ) ) {
                        //ToDo: add message that rule has been removed
                        it_cv.remove(); // remove the rule from the cvMapping
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
    private boolean isValidCvTerm( ModelElementMap.CVTerm cvTerm,
                                   CvRule rule,
                                   Collection<ValidatorMessage> messages ) throws ValidatorException {

        // ToDo: change cv file: get ontology id form CvTerm instead of CvSource!! (because the CvSource will disappear!
        CVSource source = ( CVSource ) cvTerm.getCvRef();
        String ontologyID = source.getCvIdentifier();
        System.out.println("##### DEBUG: Checking cvTerm " + cvTerm.getTermName() + " for ontology: " + ontologyID );

        if ( !ontologyMngr.containsKey( ontologyID ) ) {
            String msg = "The requested ontology wasn't defined: " + source.getCvIdentifier() + ". The CvTerm will be removed.";
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
                         "exist in that ontology: " + cvTerm.getCvRef() + ". The CvTerm will be removed.";
            messages.add( rule.buildMessage( rule.getElementPath(),
                                        Recommendation.forName( rule.getRequirementLevel() ),
                                        msg, rule ) );
            return false;
        }

//        if ( ruleTerm.isObsolete() ) {
        if ( ontologyMngr.isObsoleteID( ontologyID, ruleTermAcc )) {
            // this term should not be in use here
            String msg = "The term " + printSimpleCvTerm( cvTerm ) + " is obsolote in the ontology " +
                         source.getCvIdentifier() + ". The CvTerm will be removed.";
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
                         source.getCvIdentifier() + "', yet the CvMapping recommends to use them. " +
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

    private void addRules(List<ModelElementMap> mems) {
        if (rules == null) {
            rules = new ArrayList<CvRule>();
        }
        for (ModelElementMap mem : mems) {
            CvRuleImpl rule = new CvRuleImpl(ontologyMngr);
            rule.setModelElementMap(mem);
            rule.setNonRepeatableTerms(nonRepeatableTerms);
            rules.add(rule);
        }

    }

    protected String printSimpleCvTerm( ModelElementMap.CVTerm cv ) {
        StringBuilder sb = new StringBuilder();
        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ' ' );
        sb.append( '(' ).append( cv.getTermAccession() ).append( ')' );
        return sb.toString();
    }


}
