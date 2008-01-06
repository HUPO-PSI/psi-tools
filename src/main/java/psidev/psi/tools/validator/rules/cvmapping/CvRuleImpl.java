package psidev.psi.tools.validator.rules.cvmapping;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvReference;
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
 * Implementation of the CV rule that performs check based on XML definition.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * Author: florian
 * @version $Id$
 * Date: 18-Jul-2007
 * Time: 15:52:08
 */
public class CvRuleImpl extends AbstractRule implements CvRule {

    public static final Log log = LogFactory.getLog( CvRuleImpl.class );

    private CvMappingRule cvMappingRule;

    public CvRuleImpl( OntologyManager ontologyManager ) {
        super( ontologyManager );
        cvMappingRule = new CvMappingRule();
    }

    /////////////////
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
     * @return
     * @throws ValidatorException
     */
    public Collection<ValidatorMessage> check( Object object, String prefixXpath ) throws ValidatorException {

        if ( object == null ) {
            throw new ValidatorException( "Cannot validate a null object." );
        }

        log.debug( "Given prefix Xpath: " + prefixXpath );

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        Recommendation level = Recommendation.forName( getRequirementLevel() );

        // given the scope of the XPath expression, transform the elementXpath to it only retreive the objects on which
        // we want to apply the validation

        String scopeXpath = getScopePath();
        String elementXpath = getElementPath();

        if ( prefixXpath != null ) {
            scopeXpath = removeXpathPrefix( scopeXpath, prefixXpath );
            elementXpath = removeXpathPrefix( elementXpath, prefixXpath );
            log.debug( "Updated scope Xpath using prefix '" + prefixXpath + "' to: " + scopeXpath );
            log.debug( "Updated element Xpath using prefix '" + prefixXpath + "' to: " + elementXpath );
        } else {
            // otherwise assume the object is at the root level of the rule and use the elementXpath specified in the rule
            //rootObjectXpath = scopeXpath;
        }
        log.debug( "Xpath to fetch objects to check on: " + scopeXpath );

        // get the elements to check
        List<XPathResult> results = null;
        try {
            results = XPathHelper.evaluateXPath( scopeXpath, object );
            if ( log.isDebugEnabled() ) {
                log.debug( "XPath '" + scopeXpath + "' allowed to fetch " + results.size() + " object(s) from the given " +
                           object.getClass().getSimpleName() + ": " + printObjectAccessions( results ) );
            }

        } catch ( JXPathException e ) {
            messages.add( buildMessage( scopeXpath, level,
                                        "Skip this rule as the XPath expression could not be compiled: '" + scopeXpath + "'" ) );
            return messages;
        }


        if ( results.isEmpty() ) {

            // there's nothing to check on, be quiet.
            log.debug( "Count not find any object using Xpath: " + scopeXpath );

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
            log.debug( "Xpath allowing to retreive the values from the Objects: " + valueXpath );


            // Check the objects one by one
            for ( XPathResult result : results ) {
                Object objectToCheck = result.getResult();

                // Retrieve the values to check against CvTerms specified in the rule
                List<XPathResult> valueResults = null;
                try {
                    valueResults = XPathHelper.evaluateXPath( valueXpath, objectToCheck );

                    if ( log.isDebugEnabled() ) {
                        log.debug( "XPath '" + valueXpath + "' allowed to fetch " + valueResults.size() +
                                   " value(s) from the given " + resultClassName + ": " +
                                   printObjectAccessions( valueResults ) );
                    }

                } catch ( JXPathException e ) {
                    messages.add( buildMessage( valueXpath, level,
                                                "Skip this rule as the XPath expression could not be compiled: '" + valueXpath + "'" ) );
                    return messages;
                }


                final int resultCount = valueResults.size();

                if ( resultCount == 0 ) {

                    // In this case, we generate a message of the appropriate level for each term that the rule was expecting here.
                    // If there are no known terms, then obviously do not generate a message.
                    if ( getCVTerms() != null && getCVTerms().size() > 0 ) {
                        StringBuilder sb = new StringBuilder( 256 );
                        sb.append( "None of the given CvTerms were found at '" + getElementPath() + "' because no values were found:\n" );
                        Iterator<CvTerm> iterator = getCVTerms().iterator();
                        while ( iterator.hasNext() ) {
                            CvTerm cvTerm = iterator.next();
                            sb.append( "  - " ).append( printCvTerm( cvTerm ) );
                            if ( iterator.hasNext() ) {
                                sb.append( "\n" );
                            }
                        }

                        messages.add( buildMessage( elementXpath, level, sb.toString() ) );
                    }

                } else {

                    // TODO extract method here that resurns that Map initialized
                    // Initialize the map that is going to hold the statistics that are later used to determine if there are errors
                    Map<XPathResult, Map<CvTerm, Integer>> result2termCount =
                            new HashMap<XPathResult, Map<CvTerm, Integer>>( valueResults.size() );

                    // check that each match has at least one matching CV term amongst those specified.
                    for ( XPathResult valueResult : valueResults ) {

                        Map<CvTerm, Integer> term2count = new HashMap<CvTerm, Integer>( getCVTerms().size() );
                        result2termCount.put( valueResult, term2count );

                        // for each XPath expression
                        if ( log.isDebugEnabled() ) {
                            log.debug( "Processing value: " + valueResult.getResult() );
                        }

                        // check each specified CvTerm in this CvRule (and potentially child terms)
                        for ( CvTerm cvTerm : getCVTerms() ) {

                            if ( isMatchingCv( cvTerm, valueResult, messages, level, term2count ) ) {
                                if ( log.isDebugEnabled() ) {
                                    log.debug( "Match between '" + valueResult.getResult() + "' and " + printCvTerm( cvTerm ) );
                                }
                            } else {
                                if ( log.isDebugEnabled() ) {
                                    log.debug( "No match between '" + valueResult.getResult() + "' and " + printCvTerm( cvTerm ) );
                                }
                            }
                        } // for
                    } // results

                    if ( log.isDebugEnabled() ) {
                        printMap( result2termCount );
                    }

                    // Now that we have processed all results, lets check if we have any errors

                    // After counting terms, we process the map given the boolean operator set and produce messages accordingly
                    String operator = getCvTermsCombinationLogic();
                    if ( operator != null ) {
                        operator = operator.trim();
                    }

                    final int matchingCvTermCount = calculateMatchingResultCount( result2termCount );
                    log.debug( "Matching term count: " + matchingCvTermCount );
                    boolean xorFired = false;
                    for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> e : result2termCount.entrySet() ) {

                        final String resultValue = ( String ) e.getKey().getResult();
                        final Map<CvTerm, Integer> t2c = e.getValue();

                        // Process repeatability of CvTerms according to their usage
                        final Map<CV, Integer> term2count = calculateCvTermUsage( result2termCount );
                        for ( Map.Entry<CV, Integer> entry2 : term2count.entrySet() ) {
                            final CV cvTerm = entry2.getKey();
                            final Integer count = entry2.getValue();

                            // If the current CvTerm is non repeatable check that the count is < 2
                            if ( !cvTerm.isRepeatable() && count > 1 ) {

                                // TODO We need to give a context for the message: object that was checked on

                                StringBuilder sb = new StringBuilder( 256 );
                                sb.append( "According to the CvMapping, the term '" ).append( cvTerm.getAccession() )
                                        .append( "' wasn't meant to be repeated, yet it appeared " ).append( count )
                                        .append( " times in elements pointed out by the XPath expression: " )
                                        .append( getElementPath() );
                                messages.add( buildMessage( getElementPath(), level, sb.toString() ) );
                            }
                        } //for

                        // Then check if have reach our target given the boolean operator specified on the current CvRule
                        if ( "OR".equalsIgnoreCase( operator ) ) {

                            // if any of the cvTerm got a hit, we are good
                            if ( matchingCvTermCount == 0 ) {
                                StringBuilder sb = new StringBuilder( 256 );
                                // TODO generate a different message if there is a single result available
                                // TODO provide a way to describe the object that was checked on !! otherwise the message we are giving are meaningless !!
                                // class ObjectPrinter<T extends Object> {
                                //       public String print( T object ){...}
                                // }

                                sb.append( "[OR] The result found at: " + elementXpath + " for which the value is '" + resultValue +
                                           "' didn't match any of the " + getCVTerms().size() + " specified CV term" +
                                           ( getCVTerms().size() > 1 ? "s" : "" ) + ":\n" );
                                sb.append( listCvTerms( "  - ", getCVTerms() ) );
                                messages.add( buildMessage( elementXpath, level, sb.toString() ) );
                            }

                        } else if ( "AND".equalsIgnoreCase( operator ) ) {

                            // if all of the cvTerm got at least a hit we are good
                            if ( matchingCvTermCount != getCVTerms().size() ) {
                                log.debug( "Found only " + matchingCvTermCount + " matching terms while we were expecting " + getCVTerms().size() );

                                StringBuilder sb = new StringBuilder( 256 );
                                sb.append( "[AND] Not all of the " + resultCount + " values " + resultClassName + "'s CV terms [" + printObjectAccessions( valueResults ) +
                                           "] found using the Xpath '" + elementXpath + "' matched any of the " + getCVTerms().size() +
                                           " CvTerm(s):\n" )
                                        .append( listCvTerms( "  - ", getCVTerms() ) );
                                messages.add( buildMessage( elementXpath, level, sb.toString() ) );
                            }

                        } else if ( "XOR".equalsIgnoreCase( operator ) ) {

                            // if exactly one cv term got a hit we are good
                            if ( matchingCvTermCount != 1 && !xorFired) {
                                xorFired = true;
                                StringBuilder sb = new StringBuilder( 256 );
                                sb.append( "[XOR] Not exactly one of the " + resultCount + " " + resultClassName + "'s CV terms [" + printObjectAccessions( valueResults ) +
                                           "] found using the Xpath '" + elementXpath + "' matched any of the " + getCVTerms().size() +
                                           " CvTerm(s):\n" )
                                        .append( listCvTerms( "  - ", getCVTerms() ) );
                                messages.add( buildMessage( elementXpath, level, sb.toString() ) );
                            }
                        } else {
                            // This should not happened as the incoming data are validated by XML schema ... so just in case ...
                            throw new UnsupportedOperationException( "CvRule count not handle boolean operator: '" + operator + "'" );
                        }
                    } // for all results
                } // else -- at least 1 result to process
            } // for each object to check upon
        } // else

        return messages;
    }

    public ValidatorMessage buildMessage( String xpath, Recommendation level, String message, Rule rule ) {
        return new ValidatorMessage( message,
                                     convertCvMappingLevel( level ),
                                     new Context( xpath ),
                                     rule );
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

            if ( !accession.equals( cv.accession ) ) return false;
            if ( name != null ? !name.equals( cv.name ) : cv.name != null ) return false;

            return true;
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
     * @param result2termCount
     * @return
     */
    private Map<CV, Integer> calculateCvTermUsage( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        Map<CV, Integer> term2count = new HashMap<CV, Integer>();

        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> e : result2termCount.entrySet() ) {
            Map<CvTerm, Integer> t2c = e.getValue();

            for ( Map.Entry<CvTerm, Integer> entry : t2c.entrySet() ) {
                final CV cvTerm = new CV( entry.getKey() );
                final Integer count = entry.getValue();


                if ( !term2count.containsKey( cvTerm ) ) {
                    term2count.put( cvTerm, 0 );
                }

                final Integer totalCount = term2count.get( cvTerm );
                term2count.put( cvTerm, totalCount + count );
            }
        }

        return term2count;
    }

    /**
     * Pretty print of the result map for debugging purpose.
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

    private String printCvTerm( CvTerm cv ) {

        StringBuilder sb = new StringBuilder( 512 );
//        sb.append( "CvTerm(" );
//        sb.append( '\'' ).append( cv.getTermAccession() ).append( '\'' ).append( ',' ).append( ' ' );
//        sb.append( '\'' ).append( cv.getTermName() ).append( '\'' ).append( ',' ).append( ' ' );
//        sb.append( "allowChildren:" ).append( cv.isAllowChildren() ).append( ',' ).append( ' ' );
//        sb.append( "useTerm:" ).append( cv.isUseTerm() ).append( ',' ).append( ' ' );
//        sb.append( "repeatable:" ).append( cv.isIsRepeatable() );
//        sb.append( ")" );

        if ( cv.isUseTerm() && cv.isAllowChildren() ) {
            sb.append( cv.getTermAccession() + " (" + cv.getTermName() + ")" );
            sb.append( " or any of its children. " );
        } else if ( !cv.isUseTerm() && cv.isAllowChildren() ) {
            sb.append( "Any children term of " + cv.getTermAccession() + " (" + cv.getTermName() + "). " );
        } else if ( cv.isUseTerm() && !cv.isAllowChildren() ) {
            sb.append( "The sole term " + cv.getTermAccession() + " (" + cv.getTermName() + ") " );
            sb.append( "or any of its children. " );
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
     * Checks that the given term (xpResult) is found in the ontology (by identifier or name), if so, update the given
     * map that counts the CvTerms (term2count).
     *
     * @param cvTerm      CvTerm to check against
     * @param xpResult    The accession or name of a term to compare to the CvTerm
     * @param messages    List of messages in case of error
     * @param level       The level of the messages to create
     * @param term2count  To keep count of how many times we have seen specific CvTerms
     * @return true if the term was found.
     * @throws ValidatorException
     */
    protected boolean isMatchingCv( CvTerm cvTerm,
                                    XPathResult xpResult,
                                    Collection<ValidatorMessage> messages,
                                    Recommendation level,
                                    Map<CvTerm, Integer> term2count ) throws ValidatorException {
        boolean isMatching = false;

        String accession = null;
        try {
            accession = ( String ) xpResult.getResult();
        } catch ( ClassCastException cce ) {
            // Message explaining that the xpath doesn't describe a CV term accession
            cce.printStackTrace();
            messages.add( buildMessage( getElementPath(), level,
                                        "The object pointed to by the XPath(" + getElementPath() +
                                        ") was not a CV term accession (String) as " + "expected, instead: " +
                                        xpResult.getResult().getClass().getName() ) );
        }

        // Get all information from the CV term
        String ontologyID = ( ( CvReference ) cvTerm.getCvIdentifierRef() ).getCvIdentifier();
        // Ask the ontologymanager for the required ontology
        if ( !ontologyManager.containsOntology( ontologyID ) ) {
            // Yikes, ontology not found! Major configuration issue, throw Exception!
            throw new ValidatorException( "The requested ontology was not found: " + ontologyID );
        }

        String ruleTermAcc = cvTerm.getTermAccession();
        boolean allowChildren = cvTerm.isAllowChildren();
        boolean useTerm = cvTerm.isUseTerm();
        boolean useTermName = cvTerm.isUseTermName();

        // Get the accession numbers that are valid for this cvTerm.
        Set<String> allowedAccs = ontologyManager.getValidIDs( ontologyID, ruleTermAcc, allowChildren, useTerm );

        // Now we'll see whether we should be checking CV accessions or CV preferred names.
        Set<String> allowedValues = null;
        if ( useTermName ) {
            // We should check on term names rather that accessions, apparently.
            allowedValues = new HashSet<String>( allowedAccs.size() );
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
            // Term found, we populate the map

            Integer count = null;
            if ( !term2count.containsKey( cvTerm ) ) {
                term2count.put( cvTerm, 1 );
            } else {
                count = term2count.get( cvTerm ) + 1;
                term2count.put( cvTerm, count );
            }
            // Flag succesful validation for this term.
            isMatching = true;
        } else {
            // insert 0 in the map
            if ( !term2count.containsKey( cvTerm ) ) {
                term2count.put( cvTerm, 0 );
            }
        }

        return isMatching;
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
        if( prefixXpath.equals( "." ) ) {
            // "." means the current node, this there's nothing to remove
            return xpath;
        }

        if ( ! xpath.startsWith( prefixXpath ) ) {
            throw new IllegalArgumentException( "The given prefix '"+ prefixXpath +"' is not a prefix of '"+xpath+"'" );
        }

        String result = xpath.substring( prefixXpath.length() );
        if(result.length() == 0) {
            // is the prefix is the same as the xpath, then return dot, that is the current element.
            result = ".";
        }
        return result;
    }
}
