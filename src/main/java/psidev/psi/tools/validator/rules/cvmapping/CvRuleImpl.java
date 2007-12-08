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
 * Author: florian
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

    public Collection<ValidatorMessage> check( Object object, String objectXpath ) throws ValidatorException {

        if ( object == null ) {
            throw new ValidatorException( "Cannot validate a null object." );
        }

        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

        Recommendation level = Recommendation.forName( getRequirementLevel() );

        // get the xpath to use for checking, use xpath of the rule if no
        // xpath expression was specified for this object
        String xpath = getElementPath();
        if ( objectXpath != null ) {
            // if an xpath for this object was specified, generate the xpath to use for the rule
            xpath = getXpathToUse( getElementPath(), objectXpath );
        } else {
            // otherwise assume the object is at the root level of the rule and use the xpath specified in the rule
            objectXpath = getElementPath();
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "xpath = '" + xpath + "'" );
            log.debug( "object xpath= " + object + "'" );
        }

        // get the elements to check
        List<XPathResult> results = null;
        try {
            results = XPathHelper.evaluateXPath( xpath, object );
            if ( log.isDebugEnabled() ) {
                log.debug( "XPath '" + xpath + "' allowed to fetch " + results.size() + " object(s) from the given " +
                           object.getClass().getSimpleName() + ": " + printObjectAccessions( results ) );
            }

        } catch ( JXPathException e ) {
            messages.add( buildMessage( xpath, level,
                                        "Skip this rule as the XPath expression could not be compiled: '" + xpath + "'" ) );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Found " + results.size() +
                       ( results.isEmpty() ? " hit" : " hits of type " +
                                                      results.iterator().next().getResult().getClass().getSimpleName() ) );
        }

        final int resultCount = results.size();

        if ( resultCount == 0 ) {

            // In this case, we generate a message of the appropriate level for
            // each term that the rule was expecting here.
            // If there are no known terms, then obvisouly do not generate a message.
            if ( getCVTerms() != null && getCVTerms().size() > 0 ) {
                StringBuilder sb = new StringBuilder( 256 );
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

            final String resultClassName = object.getClass().getSimpleName();

            // initialize the map that is going to hold the statistics that are later used to determine if there are errors
            Map<XPathResult, Map<CvTerm, Integer>> result2termCount = new HashMap<XPathResult, Map<CvTerm, Integer>>();

            // check that each match has at least one matching CV term amongst those specified.
            for ( XPathResult result : results ) {

                Map<CvTerm, Integer> term2count = new HashMap<CvTerm, Integer>();
                result2termCount.put( result, term2count );

                // for each XPath expression
                if ( log.isDebugEnabled() ) {
                    log.debug( "Processing result: " + result.getResult() );
                }

                // check each specified CVTerm in this CvRule (and potentially child terms)
                for ( CvTerm cvTerm : getCVTerms() ) {

                    if ( !isMatchingCv( cvTerm, result, messages, level, objectXpath, term2count ) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug( "No match between '" + result.getResult() + "' and " + printCvTerm( cvTerm ) );
                        }
                    } else {
                        if ( log.isDebugEnabled() ) {
                            log.debug( "Match between '" + result.getResult() + "' and " + printCvTerm( cvTerm ) );
                        }
                    }
                }
            } // results

            if ( log.isDebugEnabled() ) {
                printMap( result2termCount );
            }

            // Now that we have processed all results, lets check if we have any errors

            // First report on repeatability of CvTerms
            final Map<CvTerm, Integer> term2count = calculateCvTermUsage( result2termCount );
            for ( Map.Entry<CvTerm, Integer> entry2 : term2count.entrySet() ) {
                final CvTerm cvTerm = entry2.getKey();
                final Integer count = entry2.getValue();

                // If the current CvTerm is non repeatable check that the count is < 2
                if ( !cvTerm.isIsRepeatable() && count > 1 ) {

                    // TODO We need to give a context for the message: object that was checked on

                    StringBuilder sb = new StringBuilder( 256 );
                    sb.append( "According to the CvMapping, the term '" ).append( cvTerm.getTermAccession() )
                            .append( "' wasn't meant to be repeated, yet it appeared " ).append( count )
                            .append( " times in elements pointed out by the XPath expression: " )
                            .append( getElementPath() );
                    messages.add( buildMessage( getElementPath(), level, sb.toString() ) );
                }
            }

            // After counting terms, we process the map given the boolean operator set and produce messages accordingly
            String operator = getCvTermsCombinationLogic();
            if ( operator != null ) {
                operator = operator.trim();
            }

            // Then check if have reach our target given the boolean operator specified on the current CvRule
            final int matchingResultCount = calculateMatchingResultCount( result2termCount );

            if ( "OR".equalsIgnoreCase( operator ) ) {

                // if any of the cvTerm got a hit, we are good
                if ( matchingResultCount == 0 ) {
                    StringBuilder sb = new StringBuilder( 256 );
                    // TODO generate a different message if there is a single result available
                    // TODO provide a way to describe the object that was checked on !! otherwise the message we are giving are meaningless !!
                    // class ObjectPrinter<T extends Object> {
                    //       public String print( T object ){...}
                    // }

                    sb.append( "[OR] None of the " + resultCount + " " + resultClassName + "'s CV terms [" + printObjectAccessions( results ) +
                               "] found using the Xpath '" + xpath + "' matched any of the " + getCVTerms().size() +
                               " CvTerm(s):\n" )
                            .append( listCvTerms( "  - ", getCVTerms() ) );
                    messages.add( buildMessage( xpath, level, sb.toString() ) );
                }

            } else if ( "AND".equalsIgnoreCase( operator ) ) {

                // if all of the cvTerm got at least a hit we are good
                if ( matchingResultCount != getCVTerms().size() ) {
                    StringBuilder sb = new StringBuilder( 256 );
                    sb.append( "[AND] Not of the " + resultCount + " " + resultClassName + "'s CV terms [" + printObjectAccessions( results ) +
                               "] found using the Xpath '" + xpath + "' matched any of the " + getCVTerms().size() +
                               " CvTerm(s):\n" )
                            .append( listCvTerms( "  - ", getCVTerms() ) );
                    messages.add( buildMessage( xpath, level, sb.toString() ) );
                }

            } else if ( "XOR".equalsIgnoreCase( operator ) ) {

                // if exactly one cvterm got a hit we are good
                if ( matchingResultCount != 1 ) {
                    StringBuilder sb = new StringBuilder( 256 );
                    sb.append( "[XOR] Not exactly one of the " + resultCount + " " + resultClassName + "'s CV terms [" + printObjectAccessions( results ) +
                               "] found using the Xpath '" + xpath + "' matched any of the " + getCVTerms().size() +
                               " CvTerm(s):\n" )
                            .append( listCvTerms( "  - ", getCVTerms() ) );
                    messages.add( buildMessage( xpath, level, sb.toString() ) );
                }
            } else {
                // This should not happened as the incoming data are validated by XML schema ... so just in case ...
                throw new UnsupportedOperationException( "CvRule count not handle boolean operator: '" + operator + "'" );
            }
        }

        return messages;
    }

    private Map<CvTerm, Integer> calculateCvTermUsage( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        Map<CvTerm, Integer> term2count = new HashMap<CvTerm, Integer>();

        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> e : result2termCount.entrySet() ) {
            Map<CvTerm, Integer> t2c = e.getValue();

            for ( Map.Entry<CvTerm, Integer> entry : term2count.entrySet() ) {
                final CvTerm cvTerm = entry.getKey();
                final Integer count = entry.getValue();


                if( !term2count.containsKey( cvTerm ) ) {
                     term2count.put( cvTerm, 0 );
                }

                final Integer totalCount = term2count.get( cvTerm );
                term2count.put( cvTerm, totalCount + count );
            }
        }

        return term2count;
    }

    private int calculateMatchingResultCount( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        int matchingResultCount = 0;
        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> entry : result2termCount.entrySet() ) {
            Map<CvTerm, Integer> term2count = entry.getValue();

            int matchingCvTermCount = 0;
            for ( Map.Entry<CvTerm, Integer> entry2 : term2count.entrySet() ) {
                final Integer count = entry2.getValue();

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

    private void printMap( Map<XPathResult, Map<CvTerm, Integer>> result2termCount ) {
        for ( Map.Entry<XPathResult, Map<CvTerm, Integer>> entry : result2termCount.entrySet() ) {
            XPathResult result = entry.getKey();
            Map<CvTerm, Integer> term2count = entry.getValue();

            log.debug( "After checking on Result: " + result.getResult() + " here is the state of the Map<CvTerm, Integer>" );
            for ( Map.Entry<CvTerm, Integer> entry2 : term2count.entrySet() ) {
                final CvTerm t = entry2.getKey();
                final Integer count = entry2.getValue();
                log.debug( "[" + count + "] " + printCvTerm( t ) );
            }
        }
    }

    private String printObjectAccessions( List<XPathResult> results ) {
        StringBuilder sb = new StringBuilder( 128 );
        for ( Iterator<XPathResult> iterator = results.iterator(); iterator.hasNext(); ) {
            XPathResult result = iterator.next();
            sb.append( result.getResult() );
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

    ///////////////////
    // utilities

    public ValidatorMessage buildMessage( String xpath, Recommendation level, String message, Rule rule ) {
        return new ValidatorMessage( message,
                                     convertCvMappingLevel( level ),
                                     new Context( xpath ),
                                     rule );
    }

    private ValidatorMessage buildMessage( String xpath, Recommendation level, String message ) {
        return buildMessage( xpath, level, message, this );
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

        // TODO print this in text mode

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

    protected boolean isMatchingCv( CvTerm cvTerm, XPathResult xpResult,
                                    Collection<ValidatorMessage> messages,
                                    Recommendation level,
                                    String objectXPath,
                                    Map<CvTerm, Integer> term2count ) throws ValidatorException {
        boolean result = false;

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

        // Get all information from the CV term.
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
        //boolean repeatable = cvTerm.isIsRepeatable();

        // The following variables and complicated processing is only necessary for
        // non-repeatable elements.
//        String relativeScope = null;
//
//            // TODO issue: now we always have a scope whever we have a repeatable term or not
//
//            // Find the scope for the repeat. Anything repeated within this
//            // xpath scope will flag an error, everything repeated outside will not.
//
//            String scope = getScopePath();
//
//            // Transform the scope into a relative scope, based on the objects original xpath.
//            String tempObjectXpath = objectXPath;
//            if(!scope.endsWith("/") && objectXPath.endsWith("/")) {
//                tempObjectXpath = tempObjectXpath.substring(0, tempObjectXpath.length()-1);
//            }
//            relativeScope = getXpathToUse(scope, tempObjectXpath);

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
            // Term found, everybody happy, no validation message necessary.
            // We do however need to populate the Map 

            Integer count = null;
            if ( !term2count.containsKey( cvTerm ) ) {
                term2count.put( cvTerm, 0 );
            }
            count = term2count.get( cvTerm );
            term2count.put( cvTerm, count + 1 );

            // Flag succesful validation for this term.
            result = true;
        }

        return result;
    }

    /**
     * The asPath can look like this:
     * <p/>
     * /elementNameA[2]/elementNameB[1]/accession
     * <p/>
     * while our relativeScope looks like:
     * <p/>
     * elementNameA/
     * <p/>
     * So we need to go through the asPath, allowing for absence or presence
     * of the '[]' at each point, to retrieve the actual value of the
     * asPath for our relative scope.
     *
     * @param asPath        String with the 'asPath', something like:
     *                      '/elementNameA[2]/elementNameB[1]/accession'.
     * @param relativeScope String with the 'relative scope', something like:
     *                      'elementNameA/'
     * @return String  with the asPath for our relative scope.
     */
    private String parseIndex( String asPath, String relativeScope ) {
        StringBuffer sb = new StringBuffer();

        // If the current relative scope is empty, it means that
        // the object we're validating is exactly the scope.
        // So always just return '1' in that case. In any other case,
        // processing is required.
        if ( !relativeScope.equals( "" ) ) {
            // Removing leading '/' on asPath.
            if ( asPath.startsWith( "/" ) ) {
                // Removing leading '/' on asPath.
                asPath = asPath.substring( 1 );
            }
            // Removing leading '/' on relativeScope.
            if ( relativeScope.startsWith( "/" ) ) {
                // Removing leading '/' on relativeScope.
                relativeScope = relativeScope.substring( 1 );
            }

            // Split asPath and relativescope on '/'.
            String[] asPathArray = asPath.split( "/" );
            String[] relScopeArray = relativeScope.split( "/" );

            // Try each asPath entry, removing the potential '[]'
            // until we have exhausted the elements of our relative scope array.
            for ( int i = 0; i < asPathArray.length; i++ ) {
                String asPathPart = asPathArray[i];
                String count = null;
                int startSqBracket = -1;
                if ( ( startSqBracket = asPathPart.indexOf( "[" ) ) > 0 ) {
                    count = asPathPart.substring( startSqBracket + 1, asPathPart.lastIndexOf( "]" ) );
                    asPathPart = asPathPart.substring( 0, startSqBracket );
                }
                // See if we have this asPathPart in our relative scope array at this index.
                if ( !asPathPart.equals( relScopeArray[i] ) ) {
                    throw new IllegalArgumentException( "The relative scope you specified ('" + relativeScope + "') was not contained in the asPath you specified ('" + asPath + "')!" );
                }
                // See if we have now exhausted our relative scope array.
                if ( relScopeArray.length >= ( i + 1 ) ) {
                    // All done. This is the count we need, though it could be 'null'.
                    sb.append( asPathPart );
                    if ( count != null ) {
                        sb.append( '[' ).append( count ).append( ']' );
                    }
                    sb.append( '/' );
                    if ( relScopeArray.length == ( i + 1 ) ) {
                        break;
                    }
                }
            }
        }

        // Sanity check. Our constructed String must be a substring of the asPath.
        String result = sb.toString();
        if ( !result.equals( "" ) && asPath.indexOf( result ) != 0 ) {
            throw new IllegalStateException( "The obtained context for this scope '" + result + "' does not match with the start of the asPath '" + asPath + "'!" );
        }

        return result;
    }

    /**
     * Returns the xpath expression to use with this rule when checking a object other
     * than the representation of the root level element.
     *
     * @param ruleXpath   the xpath stored for this rule (specified in the config file)
     * @param objectXpath xpath of this object from the root element of the XML.
     * @return the xpath to use for the checking
     */
    private String getXpathToUse( String ruleXpath, String objectXpath ) {
        //ToDo: more detailed checking of xpath (e.g. starts with '/', ...)
//        if ( log.isDebugEnabled() ) {
//            log.debug( "\""+ ruleXpath +"\".substring(\""+ objectXpath +"\".length())" );
//            log.debug( "\""+ ruleXpath +"\".substring(\""+ objectXpath.length() +"\")" );
//        }
        return ruleXpath.substring( objectXpath.length() );
    }
}
