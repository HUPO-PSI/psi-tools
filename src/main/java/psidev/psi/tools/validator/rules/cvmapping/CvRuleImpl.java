package psidev.psi.tools.validator.rules.cvmapping;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.AbstractRule;
import psidev.psi.tools.validator.rules.Rule;
import psidev.psi.tools.validator.xpath.XPathHelper;
import psidev.psi.tools.validator.xpath.XPathResult;

import java.util.*;

/**
 * Author: florian
 * Date: 18-Jul-2007
 * Time: 15:52:08
 */
public class CvRuleImpl extends AbstractRule implements CvRule {

    public static final Log log = LogFactory.getLog( CvRuleImpl.class );

    private CvMappingRule modelElementMap;

    private Map<CvTerm, Integer> nonRepeatableTerms = null;

    public CvRuleImpl(OntologyManager ontologyManager) {
        super(ontologyManager);
        modelElementMap = new CvMappingRule();
    }


    /////////////////
    // Getter + Setter

    public CvMappingRule getCvMappingRule() {
        return modelElementMap;
    }

    public void setCvMappingRule(CvMappingRule modelElementMap) {
        this.modelElementMap = modelElementMap;
    }

    public List<CvTerm> getCVTerms() {
        return modelElementMap.getCvTerm();
    }

    public String getElementPath() {
        return modelElementMap.getElementPath();
    }

    public String getRequirementLevel() {
        return modelElementMap.getRequirementLevel();
    }

    public void setNonRepeatableTerms(Map<CvTerm, Integer> nonRepeatableTerms) {
        this.nonRepeatableTerms = nonRepeatableTerms;
    }

    //////////////////
    // Rule

    public boolean canCheck(String xPath) {
        // if xPath == null assume that we want to use the full xPath of the rule
        // (we do not want to change the xPath that is already stored in the rule)
        if (xPath == null) { return true; }
        // if the specified XPath is part of the XPath from the configuration file and starts at
        // the root level element, assume it points to a valid level in the xml and can be checked
        return getElementPath().startsWith(xPath);
    }

    public Collection<ValidatorMessage> check(Object object, String objectXpath) throws ValidatorException {

        if ( object == null ) {
            throw new ValidatorException( "Cannot validate a null object." );
        }
        if ( nonRepeatableTerms != null ) {
            throw new IllegalStateException( "This rule instance is being run already! " +
                                             "Looks like you're having some multithreading issues." );
        }

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        Recommendation level = Recommendation.forName( getRequirementLevel() );

        // get the xpath to use for the checking, use xpath of the rule if no
        // xpath expression was specified for this object
        String xpath= getElementPath();
        if (objectXpath != null) { // if a xpath for this object was specified, generate the xpath to use for the rule
            xpath = getXpathToUse(getElementPath(), objectXpath);
        } // otherwise assume the object is at the root level of the rule and use the xpath specified in the rule

        if ( log.isDebugEnabled() ) {
            log.debug( "xpath = '" + xpath + "'" );
            log.debug( "object = " + object );
        }

        // get the elements to check
        List<XPathResult> results = null;
        try {
            results = XPathHelper.evaluateXPath( xpath, object );
        } catch ( JXPathException e ) {
            messages.add( buildMessage( xpath, level, "Skip this rule as the XPath expression could not be compiled: " + xpath ) );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Found " + results.size() + " hit(s)." );
        }

        if ( results.isEmpty() ) {

            // We agreed with Luisa that we don't need to display this one.
            messages.add( buildMessage( xpath, level, "The given XPath(" + xpath + ") didn't match any data." ) );

        } else {

            if ( nonRepeatableTerms != null ) {
                throw new IllegalStateException( "This rule instance is being run already! " +
                                                 "Looks like you're having some multithreading issues." );
            }

            // TODO for each XPath, build a list of non repeatable CVs and keep track of it.
            // initialize the map
            nonRepeatableTerms = new HashMap<CvTerm, Integer>();

            // check that each match has at least one matching CV term amongst those specified.
            for ( XPathResult result : results ) {
                // for each XPath expression,
                log.debug( result );

                boolean foundOne = false;

                // check each specified CVTerm in this CvRule (and potential child terms)
                Iterator<CvTerm> it = getCVTerms().iterator();
                while ( it.hasNext() ) {
                    CvTerm cvTerm = it.next();

                    try {
                        String accession = ( String ) result.getResult();
// ToDo: can this be generified to a list of all allowed terms?
// create a list of allowed terms first based on the CvRule (CvMappingRule) and then check all the terms
// -> when creating the rule, is it possible create a list of all valid terms?
// isRepeatable ?? scope?, per term?, per rule?
                        if ( isMatchingCv( cvTerm, accession, messages, level, xpath ) ) {
                            foundOne = true;
                            // TODO break ??
                        } else {
                            if ( log.isDebugEnabled() ) {
//                                System.out.println("No match between '" + accession + "' and " + printCvTerm( cvTerm ));
                                log.debug( "No match between '" + accession + "' and " + printCvTerm( cvTerm ) );
                            }
                        }

                    } catch ( ClassCastException cce ) {
                        // Message explaining that the xpath doesn't describe a CV term accession
                        cce.printStackTrace();
                        messages.add( buildMessage( xpath, level, "The object pointed to by the XPath(" + xpath +
                                                                  ") was not a CV term accession (String) as " +
                                                                  "expected, instead: " +
                                                                  result.getResult().getClass().getName() ) );
                    }
                }

                if ( !foundOne ) {
                    // create a message
                    StringBuilder sb = new StringBuilder();
                    sb.append( "None of the given CvTerms matched the target (" + xpath + ") '" + result.getResult()
                               + "' :\n" );
                    Iterator<CvTerm> iterator = getCVTerms().iterator();
                    while ( iterator.hasNext() ) {
                        CvTerm cvTerm = iterator.next();
                        sb.append( "  - " ).append( printCvTerm( cvTerm ) );
                        if ( iterator.hasNext() ) {
                            sb.append( "\n" );
                        }
                    }

                    messages.add( buildMessage( xpath, level, sb.toString() ) );
                }
            }

            // if any of the non repeatable element was defined more than once, create a message
            for ( Map.Entry<CvTerm, Integer> entry : nonRepeatableTerms.entrySet() ) {
                CvTerm cvTerm = entry.getKey();
                Integer count = entry.getValue();

                // Note: default value of an unspecified isRepeatable is false
                boolean isRepeatable = cvTerm.isIsRepeatable();
                if ( count > 1 && !isRepeatable ) {
                    messages.add( buildMessage( xpath,
                                                level,
                                                "According to the CvMapping, the term '" + cvTerm.getTermAccession() +
                                                "' wasn't meant to be repeated, yet it appeared " + count +
                                                " times in elements pointed out by the XPath expression: " + xpath ) );
                }
            }

            // reset the map
            nonRepeatableTerms.clear();
            nonRepeatableTerms = null;
        }


        return messages;
    }


    ///////////////////
    // utilities

    public ValidatorMessage buildMessage( String xpath, Recommendation level, String message, Rule rule ) {
        return new ValidatorMessage( message,
                                     convertCvMappingLevel( level ),
                                     new Context( xpath ),
                                     rule );
    }

    private ValidatorMessage buildMessage( String xpath, Recommendation level, String message ) {
        return buildMessage(xpath, level, message, this);
    }

    public MessageLevel convertCvMappingLevel( Recommendation level ) {
        switch ( level ) {
            case MAY:
                return MessageLevel.INFO;
            case SHOULD:
                return MessageLevel.WARN;
            case MUST:
                return MessageLevel.ERROR;
            default:
                throw new IllegalArgumentException( "Unknown CvMapping RequirementLevel: " + level );
        }
    }

    protected String printCvTerm( CvTerm cv ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "CvTerm(" );
        sb.append( '\'' ).append( cv.getTermAccession() ).append( '\'' ).append( ',' ).append( ' ' );
        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ',' ).append( ' ' );
        sb.append( "allowChildren:" ).append( cv.isAllowChildren() ).append( ',' ).append( ' ' );
        sb.append( "useTerm:" ).append( cv.isUseTerm() ).append( ',' ).append( ' ' );
        sb.append( "repeatable:" ).append( cv.isIsRepeatable() );
        sb.append( ")" );
        return sb.toString();
    }

    protected String printSimpleCvTerm( CvTerm cv ) {
        StringBuilder sb = new StringBuilder();
        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ' ' );
        sb.append( '(' ).append( cv.getTermAccession() ).append( ')' );
        return sb.toString();
    }

    private void incrementCvTermCount( CvTerm cvTerm ) {
        Integer count = nonRepeatableTerms.get( cvTerm );
        if ( count == null ) {
            count = 1;
        } else {
            count++;
        }
        nonRepeatableTerms.put( cvTerm, count );
    }

    protected boolean isMatchingCv( CvTerm cvTerm, String accession,
                                    Collection<ValidatorMessage> messages, Recommendation level, String xpath ) throws ValidatorException {
        boolean result = false;

        String ontologyID = cvTerm.getCvIdentifier();
        String ruleTermAcc = cvTerm.getTermAccession();
        String ruleTerm = cvTerm.getTermName();
        boolean allowChildren = cvTerm.isAllowChildren();
        boolean useTerm = cvTerm.isUseTerm();
        boolean useTermName = cvTerm.isUseTermName();

        if ( !ontologyManager.containsOntology( ontologyID ) ) {
            throw new ValidatorException( "The requested ontology was not found: " + ontologyID );
        }

//        System.out.println("Using: ontologyID:" + ontologyID + " ruleTermAcc: " + ruleTermAcc + " allowChildren: " + allowChildren + " useTerm: " + useTerm + " on accession: " + accession);
        Set<String> allowedAccs = ontologyManager.getValidIDs( ontologyID, ruleTermAcc, allowChildren, useTerm );
        if (useTermName) { // check on term names rather that accession
            Set<String> allowedNames = new HashSet<String>();
            // get term names for allowed accessions
            for ( String allowedAcc : allowedAccs ) {
                allowedNames.add( ontologyManager.getTermNameByID( ontologyID, allowedAcc ) );
            }
            if ( allowedNames.contains(accession) ) {
                // rule passed
                incrementCvTermCount( cvTerm );  // ToDo: check this! for repeatable terms...
                result = true;
            }
        } else { // check on cv accession (regardless of the actual name)
            if ( allowedAccs.contains( accession ) ) {
                // rule passed, no validation message
                incrementCvTermCount( cvTerm );  // ToDo: check this! for repeatable terms...
                result = true;
            } else {
                // no message since even if the accession is not allowed in these terms, it could be allowed in a different ontology
            }
        }

        return result;
    }

    /**
     * Returns the xpath expression to use with this rule when checking a object other
     * than the representation of the root level element.
     * @param ruleXpath the xpath stored for this rule (specified in the config file)
     * @param objectXpath xpath of this object from the root element of the XML.
     * @return the xpath to use for the checking
     */
    private String getXpathToUse( String ruleXpath, String objectXpath) {
       //ToDo: more detailed checking of xpath (e.g. starts with '/', ...)
       return ruleXpath.substring(objectXpath.length());
    }


}
