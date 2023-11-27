package psidev.psi.tools.validator.rules.cvmapping;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvReference;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.OntologyUtils;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;
import psidev.psi.tools.validator.*;
import psidev.psi.tools.validator.rules.AbstractRule;
import psidev.psi.tools.validator.rules.Rule;
import psidev.psi.tools.validator.util.XpathValidator;
import psidev.psi.tools.validator.xpath.XPathHelper;
import psidev.psi.tools.validator.xpath.XPathResult;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Implementation of the CV rule that performs check based on XML definition.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class CvRuleImpl extends AbstractRule implements CvRule {

    public static final Log log = LogFactory.getLog( CvRuleImpl.class );

    private CvMappingRule cvMappingRule;

    private static final String cvScope = "cv-only";

    private MappingRuleStatus status = MappingRuleStatus.NOT_CHECKED;

    public CvRuleImpl( OntologyManager ontologyManager ) {
        super( ontologyManager );
        cvMappingRule = new CvMappingRule();
        setScope(cvScope);
    }

    ////////////////////
    // Getter + Setter

    public CvMappingRule getCvMappingRule() {
        return cvMappingRule;
    }

    public void setCvMappingRule( CvMappingRule mappingRule ) {
        this.cvMappingRule = mappingRule;
    }

    public List<CvTerm> getCVTerms() {
        return cvMappingRule.getCvTerm();
    }

    public String getElementPath() {
        return cvMappingRule.getCvElementPath();
    }

    public String getScopePath() {
        return cvMappingRule.getScopePath();
    }

    public String getRequirementLevel() {
        return cvMappingRule.getRequirementLevel();
    }

    public String getCvTermsCombinationLogic() {
        return cvMappingRule.getCvTermsCombinationLogic();
    }

    public MappingRuleStatus getStatus() {
        return status;
    }

    public void resetStatus() {
        this.status = MappingRuleStatus.NOT_CHECKED;
    }

    public String getId() {
        return cvMappingRule.getId();
    }

    public String getName() {
        return cvMappingRule.getName();
    }

    //////////////////
    // Rule

    public boolean canCheck( String xPath ) {
        // if xPath == null assume that we want to use the full xPath of the rule
        // (we do not want to change the xPath that is already stored in the rule)
        if ( xPath == null ) {
            return true;
        }
        // if the specified XPath is part of the XPath from the configuration file and starts at
        // the root level element, assume it points to a valid level in the xml and can be checked
        return getElementPath().startsWith( xPath );
    }

    /**
     * @param object      the object on which we will apply the validation
     * @param prefixXpath the xpath that describe the object given as parameter.
     * @return a Collection of ValidatorMessages
     * @throws ValidatorException
     */
    public Collection<ValidatorMessage> check( Object object, String prefixXpath ) throws ValidatorException {

        /*            element
        *    _______________________
        *    / a / b / c / d / e / f
        *    ________
        *     prefix ________
        *             scope  _______
        *                     value
        */

        if ( object == null ) {
            throw new ValidatorException( "Cannot validate a null object." );
        }

        Collection<ValidatorMessage> messages = new ArrayList<>();
        if( status.equals( MappingRuleStatus.INVALID_XPATH )) {
            // do not run the rule as it is not valid.
            return messages;
        }

        if ( log.isDebugEnabled() ) log.debug( "Given prefix Xpath: " + prefixXpath );

        Recommendation level = Recommendation.forName( getRequirementLevel() );

        // given the scope of the XPath expression, transform the elementXpath to it only retreive the objects on which
        // we want to apply the validation

        String scopeXpath = getScopePath();
        String elementXpath = getElementPath();

        if ( prefixXpath != null ) {
            // if the user has provided us with a prefix, we update the XPath available in the Rule (i.e. removing the prefix)
            scopeXpath = removeXpathPrefix( scopeXpath, prefixXpath );
            elementXpath = removeXpathPrefix( elementXpath, prefixXpath );
            if ( log.isDebugEnabled() ) {
                log.debug( "Updated scope Xpath using prefix '" + prefixXpath + "' to: " + scopeXpath );
                log.debug( "Updated element Xpath using prefix '" + prefixXpath + "' to: " + elementXpath );
            }
        }

        if ( log.isDebugEnabled() ) log.debug( "Xpath to fetch objects to check on: " + scopeXpath );

        // get the elements to check
        List<XPathResult> results = Collections.EMPTY_LIST;
        try {
            results = XPathHelper.evaluateXPath( scopeXpath, object );
            if ( log.isDebugEnabled() ) {
                log.debug( "XPath '" + scopeXpath + "' allowed to fetch " + results.size() + " object(s) from the given " +
                           object.getClass().getSimpleName() + ": " + printObjectAccessions( results ) );
            }

        } catch ( JXPathException e ) {
            messages.add( buildMessage( scopeXpath, level,
                                        "Skip this rule as the XPath expression could not be compiled: '" + scopeXpath + "'", results, object) );
            return messages;
        }


        if ( results.isEmpty() ) {

            // then check if the XPath expression if valid.
            if( ! status.equals( MappingRuleStatus.VALID_RULE  )) {
                // here we check the root
                XpathValidator validator = new XpathValidator( elementXpath );
                String msg = validator.validate( object );
                if( msg != null ) {
                    messages.add( new ValidatorMessage( msg,
                                                        MessageLevel.ERROR,
                                                        new Context( "Flaw in the rule definition: " + getCvMappingRule().getId()),
                                                        this) );

                    status = MappingRuleStatus.INVALID_XPATH;

                    return messages; // abort the rule as itx xpath is not valid.
                } else {
                    status = MappingRuleStatus.VALID_XPATH;
                }
            }

        } else {

            String resultClassName = results.iterator().next().getResult().getClass().getSimpleName();
            if ( log.isDebugEnabled() ) {
                log.debug( "Found " + results.size() +
                           ( results.isEmpty() ? " hit" : " hits of type " + resultClassName ) );
            }

            // Process all objects

            // First, build the Xpath allowing to fetch the values from the object contained in the results
            // that is substract the scopeXpath from the elementXpath
            // example:  /garage/bikes/@color
            //           /garage/bikes         <-- this is the prefix
            String valueXpath = removeXpathPrefix( elementXpath, scopeXpath );
            if ( log.isDebugEnabled() )
                log.debug( "Xpath allowing to retreive the values from the Objects: " + valueXpath );

            // Check the objects one by one
            for ( XPathResult result : results ) {
                Object objectToCheck = result.getResult();

                checkSingleObject( objectToCheck, elementXpath, valueXpath, messages, level, object );

                if( status.equals( MappingRuleStatus.INVALID_XPATH ) ) {
                    return messages;
                }
            }
        } // else


        // if there are no messages yet, then there were no problems with the validation
        // (e.g. this rule could not detect any problems with the element)
        // We can (depending on the settings) report that the validation of this element
        // accounting to the definition of this rule was successful.
        if (Validator.isValidationSuccessReporting() && messages.isEmpty()) {
            String identifier = getBestIdentifier(object);
            Context context = new Context("Checked element identifier: " + identifier);

            ValidatorMessage successMsg = new ValidatorMessage("Element OK.", MessageLevel.SUCCESS, context, this);
            messages.add(successMsg);
        }
        return messages;
    }

    /**
     * Method to try to retrieve the best identifier for the provided Object.
     * This will look for a 'getId' or 'getName' method and try to invoke it
     * on the object to retrieve the identifier.
     * If no id is found the name is returned. If no name is found either,
     * simply the canonical class name is returned.
     * Note: the 'best' identifier is not an Object ID in terms of Java objects,
     * but rather an identifier in the sense of the XML based element represented
     * by this object.
     *
     * @param o the object to inspect for an identifier.
     * @return the best identifier describing this object.
     */
    private String getBestIdentifier(Object o) {
        if (o == null) { return null; }

        Class oClass = o.getClass();
        Method[] methods = oClass.getMethods();
        String identifier = null; // placeholder for the identifier we want to find
        for (Method method : methods) {
            // we are interested in an ID, so if we find one, overwrite whatever we may have and stop the search
            if (method.getName().equalsIgnoreCase("getId")) {
                try {
                    identifier = (String)method.invoke(o);
                } catch (Exception e) {
                    log.debug("Could not invoke getId method for object of type: " + oClass.getCanonicalName(), e);
                }
                if (identifier != null) {
                    // we have found an id for the object, so we can stop the search
                    // there is a slight chance the the name if returned instead of the id (in the case, where
                    // the getId method invocation fails, but a previous getName invocation succeeded)
                    // however, we ignore this case
                    break;
                }
            } else if (method.getName().equalsIgnoreCase("getName")) {
                // if we have not found an id yet, assume the name as identifier, if there is one
                // (until it is overwritten by the real id, if we can find one)
                try {
                    identifier = (String)method.invoke(o);
                } catch (Exception e) {
                    log.debug("Could not invoke getName method for object of type: " + oClass.getCanonicalName(), e);
                }
            } // else go on searching
        }

        if (identifier == null) {
            identifier = oClass.getCanonicalName();
        }
        // return whatever identifier we could find
        return identifier;
    }

    /**
     * Runs the check on a given object and potentially create messages if necessary.
     *
     * @param objectToCheck the object we are checking on.
     * @param elementXpath  the path that led to this element (for error reporting purpose).
     * @param valueXpath    the Xpath expression allowing to fetch the values on the objectToCheck.
     * @param messages      list of message that eventually will be returned to the user.
     * @param level         level of the messages to generate
     * @param o             the parent object on what the rule is applied to
     * @throws ValidatorException if the provided Xpath could not be compiled.
     */
    private void checkSingleObject( Object objectToCheck,
                                    String elementXpath,
                                    String valueXpath,
                                    Collection<ValidatorMessage> messages,
                                    Recommendation level,
                                    Object o) throws ValidatorException {
        
        String resultClassName = objectToCheck.getClass().getSimpleName();

        // 1. from the objectToCheck retrieve the values to be checked against the CvTerms of the rule
        List<XPathResult> valueResults = Collections.EMPTY_LIST;
        try {
            valueResults = XPathHelper.evaluateXPath( valueXpath, objectToCheck );

            if( ! valueResults.isEmpty() ) {
                status = MappingRuleStatus.VALID_RULE;
            } else {
                // then check if the XPath expression if valid.
                if( ! status.equals( MappingRuleStatus.VALID_RULE  )) {
                    // here we check the root
                    XpathValidator validator = new XpathValidator( valueXpath );
                    String msg = validator.validate( objectToCheck );
                    if( msg != null ) {
                        messages.add( new ValidatorMessage( msg,
                                                            MessageLevel.ERROR,
                                                            new Context( "Flaw in the rule definition: " +  getCvMappingRule().getId() ),
                                                            this) );

                        status = MappingRuleStatus.INVALID_XPATH;

                        return; // abort the rule as itx xpath is not valid.
                    } else {
                        status = MappingRuleStatus.VALID_XPATH;
                    }
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug( "XPath '" + valueXpath + "' allowed to fetch " + valueResults.size() +
                           " value(s) from the given " + resultClassName + ": " +
                           printObjectAccessions( valueResults ) );
            }

        } catch ( JXPathException e ) {
            messages.add( buildMessage( valueXpath, level,
                                        "Skip this rule as the XPath expression could not be compiled: '" + valueXpath + "'", valueResults, o ) );
            return;
        }


        // 2. examine the retrieved terms
        final int resultCount = valueResults.size();

        if ( resultCount == 0 ) {

            // No value found, generate a message of the appropriate level for each cv term that the rule was expecting here.
            // If there are no known terms, then obviously do not generate a message.
            if ( getCVTerms() != null && getCVTerms().size() > 0 ) {
                StringBuilder sb = new StringBuilder( 256 );
                sb.append("None of the given CvTerms were found at '")
                        .append(getElementPath())
                        .append("' because no values were found:\n");
                Iterator<CvTerm> iterator = getCVTerms().iterator();
                while ( iterator.hasNext() ) {
                    CvTerm cvTerm = iterator.next();
                    sb.append( "  - " ).append( printCvTerm( cvTerm ) );
                    if ( iterator.hasNext() ) {
                        sb.append( "\n" );
                    }
                }

                messages.add( buildMessage( elementXpath, level, sb.toString(), valueResults, o ) );
            }

        } else {

            // Initialize the map that is going to hold the statistics that are later used to determine if there are errors
            Map<XPathResult, Map<CvTerm, Integer>> result2termCount = checkValuesAgainstCvTerms( valueResults, messages, level );

            // Now that we have processed all results, lets check if we have any errors

            // After counting terms, we process the map given the boolean operator set and produce messages accordingly
            String operator = getCvTermsCombinationLogic();
            if ( operator != null ) {
                operator = operator.trim();
            }

            // Calculates how many terms in valueResults have at least one CV match.
            final int matchingCvTermCount = calculateMatchingResultCount( result2termCount );

            // computes CV usage statistics for checking on repeatability
            final Map<CV, Integer> term2count = calculateCvTermUsage( result2termCount );

            // Process repeatability of CvTerms according to their usage
            for ( Map.Entry<CV, Integer> entry2 : term2count.entrySet() ) {
                final CV cvTerm = entry2.getKey();
                final Integer count = entry2.getValue();

                // If the current CvTerm is non repeatable check that the count is < 2
                if ( !cvTerm.isRepeatable() && count > 1 ) {

                    // TODO We need to give a context for the message: object that was checked on

                    StringBuilder sb = new StringBuilder( 256 );
                    sb.append( "According to the CvMapping, the term '" ).append( cvTerm.getAccession() )
                            .append( "' wasn't meant to be repeated, yet it appeared " )
                            .append( count )
                            .append( " times in elements pointed out by the XPath expression: " )
                            .append( getElementPath() );
                    messages.add( buildMessage( getElementPath(), level, sb.toString(), valueResults, o ) );
                }
            } //for


            if ( log.isDebugEnabled() ) log.debug( "Matching term count: " + matchingCvTermCount );

            // ToDo: should we not use the term2count here instead of the matchingCvTermCount?
            // ToDo: the actual matching terms are not interesting for the boolean logic, only the CvTerms of the rule
            // ToDo: we should use the term2count map and check if: at leat one (OR), all (AND) or only one (XOR) terms had matches  
            // Then check if have reach our target given the boolean operator specified on the current CvRule
            if ( "OR".equalsIgnoreCase( operator ) ) {

                // The boolean combination logic (OR)requires that at least one of the CvTerms in the Rule has to be matched.
                // So we check for each CvTerm associated with this rule if we have at least one match:
                boolean match = false;
                for (CV cv : term2count.keySet()) {
                    if (term2count.get(cv) > 0) {
                        match = true;
                    }
                }

                // if any of the cvTerm got a hit, we are good
                if ( !match ) {
                    StringBuilder sb = new StringBuilder( 256 );
                    // TODO provide a way to describe the object that was checked on !! otherwise the message we are giving are meaningless !!
                    // class ObjectPrinter<T extends Object> {
                    //       public String print( T object ){...}
                    // }

                    sb.append( "The result found at: " )
                            .append( elementXpath )
                            .append( " for which the values " )
                            .append( valueResults.size() > 1 ? "are " : "is " )
                            .append( " '" )
                            .append(printObjectAccessions(valueResults))
                            .append("' didn't match ")
                            .append((getCVTerms().size() > 1 ? "any of the " : "the ") )
                            .append(getCVTerms().size())
                            .append(" specified CV term")
                            .append(getCVTerms().size() > 1 ? "s" : "")
                            .append(":\n")
                            .append( listCvTerms( "  - ", getCVTerms() ) );
                    
                    messages.add( buildMessage( elementXpath, level, sb.toString(), valueResults, o ) );
                }

            } else if ( "AND".equalsIgnoreCase( operator ) ) {

                // The boolean combination logic (AND) requires that all of the CvTerms in the Rule have to be matched.
                // So we check all CvTerms associated with this rule and if there is at least one without match, the rule failed
                boolean match = true;
                for ( CV cv : term2count.keySet() ) {
                    if ( term2count.get(cv) < 1 ) {
                        match = false;
                    }
                }

                // if all of the cvTerm got at least a hit we are good
                if ( !match ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Found only " + matchingCvTermCount + " matching terms while we were expecting " + getCVTerms().size() );
                    }

                    StringBuilder sb = new StringBuilder( 256 );
                    sb.append("Not all of the ")
                            .append(resultCount)
                            .append(" values ")
                            .append(resultClassName)
                            .append("'s CV terms [")
                            .append(printObjectAccessions(valueResults))
                            .append("] found using the Xpath '")
                            .append(elementXpath)
                            .append("' matched any of the ")
                            .append(getCVTerms().size())
                            .append(" CvTerm(s):\n")
                            .append( listCvTerms( "  - ", getCVTerms() ) );

                    messages.add( buildMessage( elementXpath, level, sb.toString(), valueResults, o ) );
                }

            } else if ( "XOR".equalsIgnoreCase( operator ) ) {

                // The boolean combination logic (XOR) requires that only one of the CvTerms in the Rule can be matched.
                // So we check all CvTerms associated with this rule and if there is more than one match (or none), the rule failed
                int match = 0;
                for ( CV cv : term2count.keySet() ) {
                    if ( term2count.get(cv) > 0 ) {
                        match++;
                    }
                }

                // if exactly one cv term got a hit we are good
                if ( match != 1 ) {
                    StringBuilder sb = new StringBuilder( 256 );
                    sb.append("Not exactly one of the ")
                            .append(resultCount).append(" ")
                            .append(resultClassName)
                            .append("'s CV terms [")
                            .append(printObjectAccessions(valueResults))
                            .append("] found using the Xpath '")
                            .append(elementXpath)
                            .append("' matched any of the ")
                            .append(getCVTerms().size())
                            .append(" CvTerm(s):\n")
                            .append( listCvTerms( "  - ", getCVTerms() ) );
                    messages.add( buildMessage( elementXpath, level, sb.toString(), valueResults, o ) );
                }
            } else {
                // This should not happened as the incoming data are validated by XML schema ... so just in case ...
                throw new UnsupportedOperationException( "CvRule count not handle boolean operator: '" + operator + "'" );
            }
        }
    }

    /**
     * Creates the map that is going to hold the statistics of usage of CvTerms in the list of provided results.
     *
     * @param valueResults values that have been extracted from the scope objects.
     * @param messages     list of message that eventually will be returned to the user.
     * @param level        level of the messages to generate
     * @return a non null map holding the usage of CvTerm in the list of provided values.
     */
    private Map<XPathResult, Map<CvTerm, Integer>> checkValuesAgainstCvTerms( final Collection<XPathResult> valueResults,
                                                                              final Collection<ValidatorMessage> messages,
                                                                              final Recommendation level ) {

        Map<XPathResult, Map<CvTerm, Integer>> result2termCount =
                new HashMap<>(valueResults.size());

        // check that each match (term used in the XML) has at least one matching CV term amongst those specified.
        for ( XPathResult valueResult : valueResults ) { // for each term used in the XML

            Map<CvTerm, Integer> term2count = new HashMap<>(getCVTerms().size());
            result2termCount.put( valueResult, term2count );

            // for each XPath expression
            if ( log.isDebugEnabled() ) {
                log.debug( "Processing value: " + valueResult.getResult() );
            }

            boolean hasMatch = false;
            // check each specified CvTerm in this CvRule (and potentially child terms)
            for ( CvTerm cvTerm : getCVTerms() ) {

                // Note: isMatchingCv is updating the term2count map
                if ( isMatchingCv( cvTerm, valueResult, messages, level, term2count ) ) {
                    hasMatch = true;
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Match between '" + valueResult.getResult() + "' and " + printCvTerm( cvTerm ) );
                    }
                } else {
                    if ( log.isDebugEnabled() ) {
                        log.debug( "No match between '" + valueResult.getResult() + "' and " + printCvTerm( cvTerm ) );
                    }
                }
            } // for

            // try a WhiteList hack to find terms that were used in a location were we have a CvRule,
            // but did not match any terms defined by any CvRule for this location
            // ToDo: check that, especially with rules which define terms that should not be used!
            ValidatorCvContext vc = ValidatorCvContext.getInstance();
            if (hasMatch) {
                // the current term has at least one match in this CvRule,
                // so add it to the set of recognised terms
                vc.addRecognised( getElementPath(), (String) valueResult.getResult() );
                // if it was not recognised by a previous rule, then we have
                // to remove it from the notRecognised set
                vc.removeNotRecognised( getElementPath(), (String) valueResult.getResult() );
            } else {
                // this term was not matched by any CvTerm specified in the
                // current rule, so we add it to the notRecognised terms, but
                // only if it is not already a recognised term (from previous rules)
                if ( !vc.isRecognised(getElementPath(), (String) valueResult.getResult() ) ) {
                    vc.addNotRecognised( getElementPath(), (String) valueResult.getResult() );
                }

            }


        } // results

        if ( log.isDebugEnabled() ) {
            printMap( result2termCount );
        }

        return result2termCount;
    }

    /**
     * Checks that the given term (xpResult) is found in the ontology (by identifier or name).
     * If so, update the given map that counts the CvTerms (term2count).
     *
     * @param cvTerm     CvTerm to check against
     * @param xpResult   The accession or name of a term to compare to the CvTerm
     * @param messages   List of messages in case of error
     * @param level      The level of the messages to create
     * @param term2count To keep count of how many times we have seen specific CvTerms
     * @return true if the term was found.
     */
    private boolean isMatchingCv( CvTerm cvTerm,
                                  XPathResult xpResult,
                                  Collection<ValidatorMessage> messages,
                                  Recommendation level,
                                  Map<CvTerm, Integer> term2count ) {
        boolean isMatching = false;

        String accession = null;
        try {
            accession = ( String ) xpResult.getResult();
        } catch ( ClassCastException cce ) {
            // Message explaining that the xpath doesn't describe a CV term accession
            messages.add( buildMessage( getElementPath(), level,
                                        "The object pointed to by the XPath(" + getElementPath() +
                                        ") was not a CV term accession (String) as expected, instead: " +
                                        xpResult.getResult().getClass().getName() ) );
        }

        // Get all information from the CV term
        String ontologyID = ( ( CvReference ) cvTerm.getCvIdentifierRef() ).getCvIdentifier();

        String ruleTermAcc = cvTerm.getTermAccession();
        boolean allowChildren = cvTerm.isAllowChildren();
        boolean useTerm = cvTerm.isUseTerm();
        boolean useTermName = cvTerm.isUseTermName();

        // Get the accession numbers that are valid for this cvTerm.
        // Note: the ontologyID is checkd on by the CvRuleManager.checkCvMapping()
        Collection<OntologyTermI> allowedTerms = ontologyManager.getOntologyAccess(ontologyID).getValidTerms( ruleTermAcc, allowChildren, useTerm );

        // Now we'll see whether we should be checking CV accessions or CV preferred names.
        Collection<String> allowedValues;
        if ( useTermName ) {
            // We should check on term names rather that accessions.
            // Note that the names are the preferred names.
            allowedValues = OntologyUtils.getTermNames(allowedTerms);
        } else {
            // The allowed values in this case are the actual accession numbers.
            // Note that the names are ignored now. Accession has precedence.
            allowedValues = OntologyUtils.getAccessions(allowedTerms);
        }

        // Check whether the value found is in the allowed values (be they terms or accessions).
        if ( allowedValues.contains( accession ) ) {
            // Term found, we populate the map

            Integer count;
            if ( !term2count.containsKey( cvTerm ) ) {
                term2count.put( cvTerm, 1 );
            } else {
                count = term2count.get( cvTerm ) + 1;
                term2count.put( cvTerm, count );
            }
            // Flag successful validation for this term.
            isMatching = true;
        } else {
            // insert 0 in the map (if it does not already contain some values for this term)
            if ( !term2count.containsKey( cvTerm ) ) {
                term2count.put( cvTerm, 0 );
            }
        }

        return isMatching;
    }

    public ValidatorMessage buildMessage( String xpath, Recommendation level, String message, Rule rule ) {
        return new ValidatorMessage( message,
                                     convertCvMappingLevel( level ),
                                     new Context( xpath ),
                                     rule );
    }

    public ValidatorMessage buildMessage( String xpath, Recommendation level, String message, Rule rule, List<XPathResult> results, Object o) {
        return buildMessage(xpath, level, message, rule);
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

    ///////////////////
    // utilities

    /**
     * Calculate how many CvTerm have at least one match.
     *
     * @param result2termCount the map containing the association of cv term and their count of match.
     * @return count of how many CvTerm have at least one match.
     */
    private int calculateMatchingResultCount( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        int matchingResultCount = 0;

        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> e : result2termCount.entrySet() ) {
            Map<CvTerm, Integer> t2c = e.getValue();
            int matchingCvTermCount = 0;

            for ( Map.Entry<CvTerm, Integer> entry : t2c.entrySet() ) {
                final Integer count = entry.getValue();
                if ( count > 0 ) {
                    matchingCvTermCount++;
                }
            }

            if ( matchingCvTermCount > 0 ) {
                matchingResultCount++;
            }
        }

        return matchingResultCount;
    }

    private class CV {
        private String name;
        private String accession;
        boolean isRepeatable;

        private CV( CvTerm cvTerm ) {
            this.accession = cvTerm.getTermAccession();
            this.name = cvTerm.getTermName();
            this.isRepeatable = cvTerm.isIsRepeatable();
        }

        public String getName() {
            return name;
        }

        public String getAccession() {
            return accession;
        }

        public boolean isRepeatable() {
            return isRepeatable;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append( "CV" );
            sb.append( "{accession='" ).append( accession ).append( '\'' );
            sb.append( ", name='" ).append( name ).append( '\'' );
            sb.append( '}' );
            return sb.toString();
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;

            CV cv = ( CV ) o;

            return accession.equals(cv.accession) && !(name != null ? !name.equals(cv.name) : cv.name != null);
        }

        @Override
        public int hashCode() {
            int result;
            result = ( name != null ? name.hashCode() : 0 );
            result = 31 * result + accession.hashCode();
            return result;
        }
    }

    /**
     * Calculate for a given result2termCount,
     *
     * @param result2termCount
     * @return
     */
    private Map<CV, Integer> calculateCvTermUsage( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        Map<CV, Integer> term2count = new HashMap<>();

        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> e : result2termCount.entrySet() ) {
            Map<CvTerm, Integer> t2c = e.getValue();

            for ( Map.Entry<CvTerm, Integer> entry : t2c.entrySet() ) {
                final CV cvTerm = new CV( entry.getKey() );
                final Integer count = entry.getValue();

                if ( !term2count.containsKey( cvTerm ) ) {
                    term2count.put( cvTerm, 0 );
                }

                if ( count > 0 ) {
                    final Integer totalCount = term2count.get( cvTerm );
                    term2count.put( cvTerm, totalCount + count );
                }
            }
        }

        return term2count;
    }

    /**
     * Pretty print of the result map for debugging purpose.
     * @param result2termCount
     */
    private void printMap( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        log.debug( "===============================================================" );
        log.debug( "Printing Map<XPathResult, Map<CvTerm, Integer>>..." );
        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> entry : result2termCount.entrySet() ) {
            XPathResult result = entry.getKey();
            Map<CvTerm, Integer> term2count = entry.getValue();

            log.debug( "XPathResult: " + result.getResult() );
            if ( term2count.isEmpty() ) {
                log.debug( "     No association" );
            } else {
                for ( Map.Entry<CvTerm, Integer> entry2 : term2count.entrySet() ) {
                    final CvTerm t = entry2.getKey();
                    final Integer count = entry2.getValue();
                    log.debug( "      " + printSimpleCvTerm( t ) + " --> " + count );
                }
            }
        }
        log.debug( "===============================================================" );
    }

    private String printObjectAccessions( List<XPathResult> results ) {
        StringBuilder sb = new StringBuilder( 128 );
        for ( Iterator<XPathResult> iterator = results.iterator(); iterator.hasNext(); ) {
            XPathResult result = iterator.next();
            sb.append( '\'' ).append( result.getResult() ).append( '\'' );
            if ( iterator.hasNext() ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    private String listCvTerms( String prefix, Collection<CvTerm> terms ) {
        StringBuilder sb = new StringBuilder( 256 );
        Iterator<CvTerm> iterator = terms.iterator();
        while ( iterator.hasNext() ) {
            CvTerm cvTerm = iterator.next();
            sb.append( prefix ).append( printCvTerm( cvTerm ) );
            if ( iterator.hasNext() ) {
                sb.append( "\n" );
            }
        }
        return sb.toString();
    }

    private ValidatorMessage buildMessage( String xpath, Recommendation level, String message ) {
        return buildMessage( xpath, level, message, this );
    }

    protected ValidatorMessage buildMessage( String xpath, Recommendation level, String message, List<XPathResult> pathResults, Object o ) {
        return buildMessage( xpath, level, message, this, pathResults, o );
    }

    private String printCvTerm( CvTerm cv ) {

        StringBuilder sb = new StringBuilder( 512 );

        if ( cv.isUseTerm() && cv.isAllowChildren() ) {
            sb.append(cv.getTermAccession()).append(" (").append(cv.getTermName()).append(")");
            sb.append( " or any of its children. " );
        } else if ( !cv.isUseTerm() && cv.isAllowChildren() ) {
            sb.append("Any children term of ").append(cv.getTermAccession())
                    .append(" (").append(cv.getTermName()).append("). ");
        } else if ( cv.isUseTerm() && !cv.isAllowChildren() ) {
            sb.append("The sole term ").append(cv.getTermAccession())
                    .append(" (").append(cv.getTermName()).append(") ")
                    .append( "or any of its children. " );
        } else {
            throw new IllegalStateException( "Either the term itself of its children have to be allowed" );
        }

        if ( cv.isIsRepeatable() ) {
            sb.append( "The term can be repeated. " );
        } else {
            sb.append( "A single instance of this term can be specified. " );
        }

        if ( cv.isUseTermName() ) {
            sb.append( "The matching value has to be the name of the term, not its identifier." );
        } else {
            sb.append( "The matching value has to be the identifier of the term, not its name." );
        }

        return sb.toString();
    }

    private String printSimpleCvTerm( CvTerm cv ) {
        StringBuilder sb = new StringBuilder();
        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ' ' );
        sb.append( '(' ).append( cv.getTermAccession() ).append( ')' );
        return sb.toString();
    }

    /**
     * Returns the xpath expression to use with this rule when checking a object other
     * than the representation of the root level element.
     *
     * @param xpath       the xpath stored for this rule (specified in the config file)
     * @param prefixXpath xpath of this object from the root element of the XML.
     * @return the xpath to use for the checking
     */
    private String removeXpathPrefix( String xpath, String prefixXpath ) {
        //ToDo: more detailed checking of xpath (e.g. starts with '/', ...)
//        if ( log.isDebugEnabled() ) {
//            log.debug( "\""+ xpath +"\".substring(\""+ prefixXpath +"\".length())" );
//            log.debug( "\""+ xpath +"\".substring(\""+ prefixXpath.length() +"\")" );
//        }
        if ( prefixXpath.equals( "." ) ) {
            // "." means the current node, this there's nothing to remove
            return xpath;
        }

        if ( !xpath.startsWith( prefixXpath ) ) {
            throw new IllegalArgumentException( "The given prefix '" + prefixXpath + "' is not a prefix of '" + xpath + "'" );
        }

        String result = xpath.substring( prefixXpath.length() );
        if ( result.length() == 0 ) {
            // is the prefix is the same as the xpath, then return dot, that is the current element.
            result = ".";
        }
        return result;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer( 256 );

        sb.append("[Rule: ID=");
        sb.append(this.getId());
        if (this.getName() != null && this.getName().trim().length() > 0) {
            sb.append("Name=").append(this.getName());
        }
        sb.append("]");

        return sb.toString();
    }
}
