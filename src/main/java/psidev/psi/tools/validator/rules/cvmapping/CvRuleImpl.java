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

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        Recommendation level = Recommendation.forName( getRequirementLevel() );

        // get the xpath to use for the checking, use xpath of the rule if no
        // xpath expression was specified for this object
        String xpath= getElementPath();
        if (objectXpath != null) { // if a xpath for this object was specified, generate the xpath to use for the rule
            xpath = getXpathToUse(getElementPath(), objectXpath);
        } else {
            // otherwise assume the object is at the root level of the rule and use the xpath specified in the rule
            objectXpath = getElementPath();
        }

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

            // In this case, we generate a message of the appropriate level for
            // each term that the rule was expecting here.
            // If there are no known terms, then obvisouly do not generate a message.
            if(getCVTerms() != null && getCVTerms().size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append( "None of the given CvTerms were found at '" + xpath + "' because no annotation was found here :\n" );
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

            // This was much too 'insider funny' to delete. Sorry.
            // We agreed with Luisa that we don't need to display this one.
            //messages.add( buildMessage( xpath, level, "The given XPath(" + xpath + ") didn't match any data." ) );
        } else {

            // initialize the map
            HashMap<CvTerm, HashMap<String, Integer>>  nonRepeatableTerms = new HashMap<CvTerm, HashMap<String, Integer>>();

            // check that each match has at least one matching CV term amongst those specified.
            for ( XPathResult result : results ) {
                // for each XPath expression,
                log.debug( result );

                boolean foundOne = false;

                // check each specified CVTerm in this CvRule (and potential child terms)
                Iterator<CvTerm> it = getCVTerms().iterator();
                while ( it.hasNext() ) {
                    CvTerm cvTerm = it.next();

                   if ( isMatchingCv( cvTerm, result, messages, level, objectXpath, nonRepeatableTerms ) ) {
                        foundOne = true;
                        // Note that we can not break here as soon as we found one match,
                        // since we need to check for potentially unwanted repeats of terms
                        // ('isRepeatable' on term is 'false' in those cases).
                    } else {
                        if ( log.isDebugEnabled() ) {
                            log.debug( "No match between '" + result.getResult() + "' and " + printCvTerm( cvTerm ) );
                        }
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
            for ( Map.Entry<CvTerm, HashMap<String, Integer>> entry : nonRepeatableTerms.entrySet() ) {
                CvTerm cvTerm = entry.getKey();
                HashMap<String, Integer> termCounts = entry.getValue();

                for ( Map.Entry<String, Integer> innerEntry : termCounts.entrySet() ) {
                    String index = innerEntry.getKey();
                    Integer count = innerEntry.getValue();
                    
                    // Note: default value of an unspecified isRepeatable is false
                    boolean isRepeatable = cvTerm.isIsRepeatable();
                    if ( !isRepeatable && count > 1 ) {
                        messages.add( buildMessage( getElementPath(),
                                                    level,
                                                    "According to the CvMapping, the term '" + cvTerm.getTermAccession() +
                                                    "' wasn't meant to be repeated, yet it appeared " + count +
                                                    " times in elements pointed out by the XPath expression: " + getElementPath() ) );
                    }
                }
            }
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

    /**
     *
     * @param cvTerm
     * @param scopeIndex
     * @param nonRepeatableTerms
     */
    private void incrementCvTermCount( CvTerm cvTerm, String scopeIndex, HashMap<CvTerm, HashMap<String, Integer>> nonRepeatableTerms ) {
        HashMap<String, Integer> counts = nonRepeatableTerms.get( cvTerm );
        if(counts == null) {
            counts = new HashMap<String, Integer>();
        }
        Integer count = counts.get(scopeIndex);
        if ( count == null ) {
            count = 1;
        } else {
            count++;
        }
        counts.put(scopeIndex, count);
        nonRepeatableTerms.put( cvTerm, counts );
    }

    protected boolean isMatchingCv( CvTerm cvTerm, XPathResult xpResult,
                                    Collection<ValidatorMessage> messages, Recommendation level, String objectXPath, HashMap<CvTerm, HashMap<String, Integer>> nonRepeatableTerms) throws ValidatorException {
        boolean result = false;

        String accession = null;
        try {
            accession = (String)xpResult.getResult();
        } catch ( ClassCastException cce ) {
            // Message explaining that the xpath doesn't describe a CV term accession
            cce.printStackTrace();
            messages.add( buildMessage( getElementPath(), level, "The object pointed to by the XPath(" + getElementPath() + ") was not a CV term accession (String) as " + "expected, instead: " + xpResult.getResult().getClass().getName() ) );
        }

        // Get all information from the CV term.
        String ontologyID = cvTerm.getCvIdentifier();
        String ruleTermAcc = cvTerm.getTermAccession();
        String ruleTerm = cvTerm.getTermName();
        boolean allowChildren = cvTerm.isAllowChildren();
        boolean useTerm = cvTerm.isUseTerm();
        boolean useTermName = cvTerm.isUseTermName();
        boolean repeatable = cvTerm.isIsRepeatable();
        // The following variables and complicated processing is only necessary for
        // non-repeatable elements.
        String relativeScope = null;
        if(!repeatable) {
            // Find the scope for the repeat. Anything repeated within this
            // xpath scope will flag an error, everything repeated outside will not.

            String scope = cvTerm.getScope();

            // Transform the scope into a relative scope, based on the objects original
            // xpath.
            String tempObjectXpath = objectXPath;
            if(!scope.endsWith("/") && objectXPath.endsWith("/")) {
                tempObjectXpath = tempObjectXpath.substring(0, tempObjectXpath.length()-1);
            }
            relativeScope = getXpathToUse(scope, tempObjectXpath);
        }

        // Ask the ontologymanager for the required ontology
        if ( !ontologyManager.containsOntology( ontologyID ) ) {
            // Yikes, ontology not found! Major configuration issue, throw Exception!
            throw new ValidatorException( "The requested ontology was not found: " + ontologyID );
        }

        // Get the accession numbers that are valid for this cvTerm.
        Set<String> allowedAccs = ontologyManager.getValidIDs( ontologyID, ruleTermAcc, allowChildren, useTerm );

        // Now we'll see whether we should be checking CV accessions or CV preferred names.
        Set<String> allowedValues = null;
        if (useTermName) {
            // We should check on term names rather that accessions, apparently.
            allowedValues = new HashSet<String>();
            // So get the term names for the allowed accessions.
            for ( String allowedAcc : allowedAccs ) {
                // For each allowed accession, find the preferred term name and use this.
                allowedValues.add( ontologyManager.getTermNameByID( ontologyID, allowedAcc ) );
            }
        } else {
            // The allowed values in this case are the actual accession numbers.
            // Note that the names are ignored now. Accession has precedence.
            allowedValues = allowedAccs;
        }

        // Check whether the value found is in the allowed values (be they terms or accessions).
        if ( allowedValues.contains( accession ) ) {
            // Term found, everybody happy, no validation message necessary.
            // We do however need to populate the Map that checks for repeats of
            // non-repeatable terms.
            if(!repeatable) {
                // OK, find out the element index for this particular occurrence.
                String asPath = xpResult.asPath();

                // The asPath can look like this:
                //
                //      /elementNameA[2]/elementNameB[1]/accession
                //
                // while our relativeScope looks like:
                //
                //      elementNameA/
                //
                // So we need to go through the asPath, allowing for absence or presence
                // of the '[]' at each point, to retrieve the actual value between the
                // '[]' for our relative scope, keeping in mind that it can turn out to be 'null'!
                String scopeIndex = parseIndex(asPath, relativeScope);

                // Increment the count for this term, using the appropriate scope index.
                incrementCvTermCount( cvTerm , scopeIndex, nonRepeatableTerms);
            }
            // Flag succesful validation for this term.
            result = true;
        }

        return result;
    }

    /**
     * The asPath can look like this:
     *
     *      /elementNameA[2]/elementNameB[1]/accession
     *
     * while our relativeScope looks like:
     *
     *      elementNameA/
     *
     * So we need to go through the asPath, allowing for absence or presence
     * of the '[]' at each point, to retrieve the actual value of the
     * asPath for our relative scope.
     *
     * @param asPath    String with the 'asPath', something like:
     *                  '/elementNameA[2]/elementNameB[1]/accession'.
     * @param relativeScope String with the 'relative scope', something like:
     *                  'elementNameA/'
     * @return  String  with the asPath for our relative scope.
     */
    private String parseIndex(String asPath, String relativeScope) {
        StringBuffer sb = new StringBuffer("");

        // If the current relative scope is empty, it means that
        // the object we're validating is exactly the scope.
        // So always just return '1' in that case. In any other case,
        // processing is required.
        if(!relativeScope.equals("")) {
            // Removing leading '/' on asPath.
            if(asPath.startsWith("/")) {
                // Removing leading '/' on asPath.
                asPath = asPath.substring(1);
            }
            // Removing leading '/' on relativeScope.
            if(relativeScope.startsWith("/")) {
                // Removing leading '/' on relativeScope.
                relativeScope = relativeScope.substring(1);
            }

            // Split asPath and relativescope on '/'.
            String[] asPathArray = asPath.split("/");
            String[] relScopeArray = relativeScope.split("/");

            // Try each asPath entry, removing the potential '[]'
            // until we have exhausted the elements of our relative scope array.
            for (int i = 0; i < asPathArray.length; i++) {
                String asPathPart = asPathArray[i];
                String count = null;
                int startSqBracket = -1;
                if((startSqBracket = asPathPart.indexOf("[")) > 0) {
                    count = asPathPart.substring(startSqBracket+1, asPathPart.lastIndexOf("]"));
                    asPathPart = asPathPart.substring(0, startSqBracket);
                }
                // See if we have this asPathPart in our relative scope array at this index.
                if(!asPathPart.equals(relScopeArray[i])) {
                    throw new IllegalArgumentException("The relative scope you specified ('" + relativeScope + "') was not contained in the asPath you specified ('" + asPath + "')!");
                }
                // See if we have now exhausted our relative scope array.
                if(relScopeArray.length >= (i+1)) {
                    // All done. This is the count we need, though it could be 'null'.
                    sb.append(asPathPart);
                    if(count != null) {
                        sb.append("[" + count + "]");
                    }
                    sb.append("/");
                    if(relScopeArray.length == (i+1)) {
                        break;
                    }
                }
            }
        }

        // Sanity check. Our constructed String must be a substring of the asPath.
        String result = sb.toString();
        if(!result.equals("") && asPath.indexOf(result) != 0) {
            throw new IllegalStateException("The obtained context for this scope '" + result + "' does not match with the start of the asPath '" + asPath + "'!");
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
