package psidev.psi.tools.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.CvRuleReader;
import psidev.psi.tools.cvrReader.CvRuleReaderException;
import psidev.psi.tools.objectRuleReader.ObjectRuleReader;
import psidev.psi.tools.objectRuleReader.ObjectRuleReaderException;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.Import;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.ImportRuleList;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.ObjectRuleList;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.Rule;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.validator.preferences.UserPreferences;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRuleManager;
import psidev.psi.tools.validator.util.ValidatorReport;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * <b>Semantic XML Validator</b>.
 * <p/>
 * Validates a XML document against a set of rules. </p>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Matthias Oesterheld
 * @version $Id: Validator.java 668 2007-06-29 16:44:18 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public abstract class Validator {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( Validator.class );

    protected ValidatorContext validatorContext;

    /**
     * User preferences.
     * <p/>
     * Initialise to the default values.
     */
    protected UserPreferences userPreferences = new UserPreferences();

    protected OntologyManager ontologyMngr;

    /**
     * The set of rules specific to that Validator.
     * List of ObjectRuleS
     */
    private Set<ObjectRule> rules = new HashSet<ObjectRule> ();

    /**
     * Contains the URL for the rules to import
     */
    private HashMap<String, String> urlsForTheImportedRules = new HashMap<String, String>();

    /**
     * The type of the file to import in the object-rule config file is a resource
     */
    private static final String RESOURCE = "resource";

    /**
     * The type of the file to import in the object-rule config file is a local file
     */
    private static final String LOCAL_FILE = "file";

    /**
     * The type of the file to import in the object-rule config file is a file
     */
    private static final String FILE = "url";

    /**
     * List holding the CvRuleS.
     */
    private CvRuleManager cvRuleManager;

    //////////////////////
    // Constructor

    public Validator( InputStream ontoConfig, InputStream cvRuleConfig, InputStream objectRuleConfig ) throws ValidatorException, OntologyLoaderException {
        this( ontoConfig, cvRuleConfig );

        // if specified, load objectRules
        setObjectRules( objectRuleConfig );
    }

    public Validator( InputStream ontoConfig, InputStream cvRuleConfig ) throws ValidatorException, OntologyLoaderException {
        // load the ontologies
        this( ontoConfig );

        // if specified, load cvRules
        if ( cvRuleConfig != null ) {
            try {
                setCvMappingRules( cvRuleConfig );
            } catch ( CvRuleReaderException e ) {
                throw new ValidatorException( "CvMappingException while trying to load the CvRules.", e );
            }
        }
    }

    public Validator( InputStream ontoConfig ) throws OntologyLoaderException {
        // load the ontologies
        setOntologyManager( ontoConfig );
    }

    ////////////////////////
    // Getters and Setters

    public OntologyManager getOntologyMngr() {
        return ontologyMngr;
    }

    public void setOntologyManager( InputStream ontoConfig ) throws OntologyLoaderException {
        ontologyMngr = new OntologyManager( ontoConfig );
    }

    public CvRuleManager getCvRuleManager() {
        return cvRuleManager;
    }

    public ValidatorContext getValidatorContext() {
        return validatorContext;
    }

    /**
     * Set a cvMapping file and build the corresponding cvRuleManager.
     *
     * @param cvIs InputStream form the configuration file defining the CV Mapping to be applied as rule.
     * @throws CvRuleReaderException if one cannot parse the given file.
     */
    public void setCvMappingRules( InputStream cvIs ) throws CvRuleReaderException {
        CvRuleReader reader = new CvRuleReader();
        cvRuleManager = new CvRuleManager( ontologyMngr, reader.read( cvIs ) );
    }

    public Set<ObjectRule> getObjectRules() {
        return rules;
    }

    /**
     *
     * @param className
     * @return  true if the className matches a the class name of one of the ObjectRules in the list of instantiated rules.
     */
    private ObjectRule isTheRuleAlreadyInstantiated(String className){

        for (ObjectRule rule : this.rules){
            if (rule.getClass().getName().equals(className)){
                return rule;
            }
        }
        return null;
    }

    /**
     * Instantiates the appropriate Rule from the jaxb Rule 'rule' and add it to the list of rules.
     * @param rule
     * @throws ValidatorException
     */
    private void addRule(Rule rule, String name) throws ValidatorException {
        String className = null;
        Class ruleClass = null;
        try {
            className = rule.getClazz();
            ObjectRule alreadyImportedRule = isTheRuleAlreadyInstantiated(className);
            if (alreadyImportedRule == null){
                ruleClass = Class.forName( className );
                Constructor c = ruleClass.getConstructor( OntologyManager.class );
                ObjectRule r = ( ObjectRule ) c.newInstance( ontologyMngr );

                if (name != null){
                    r.setName(name);
                }

                this.rules.add( r );
                if ( log.isInfoEnabled() ) {
                    log.info( "Added rule: " + r.getClass() );
                }
            }
            else{
                log.info( "The rule " + className + " has already been added with a label " + alreadyImportedRule.getName() + " and will not be reimported with a label " + name);
            }

        } catch (Exception e) {
            throw new ValidatorException( "Error instantiating rule (" + className + ")", e );
        }

    }

    /**
     * Load a file from a URL
     * @param urlName
     * @throws FileNotFoundException
     * @throws ValidatorException
     * @throws IOException
     */
    private void loadFileFrom(String urlName) throws ValidatorException, IOException {

        URL url = new URL(urlName);

        InputStream is = url.openStream();
        setObjectRules(is);
    }

    /**
     * Look if this file is a local file
     * @param urlName
     * @throws FileNotFoundException
     * @throws ValidatorException
     * @throws IOException
     */
    private boolean isALocalFile(String urlName) {

        File file = new File(urlName);

        if (file.exists()){
            return true;
        }
        return false;
    }

    /**
     * Load a local file
     * @param urlName
     * @throws FileNotFoundException
     * @throws ValidatorException
     * @throws IOException
     */
    private void loadLocalFileFrom(String urlName) throws ValidatorException, IOException {

        File file = new File(urlName);

        if (file.exists()){
            FileInputStream is = new FileInputStream(file);

            setObjectRules(is);
        }
    }

    /**
     * First look for a resource file of Validator, then a local file and finally a file on internet.
     * @param urlName
     * @throws ValidatorException
     */
    private void importRulesFromFile(String urlName, String typeOfImport) throws ValidatorException{
        try {
            boolean isImportDone = false;

            if (typeOfImport != null){

                if (typeOfImport.toLowerCase().equals(RESOURCE)){
                    //URL url = Validator.class.getClassLoader().getResource( urlName );
                    URL url = this.getClass().getResource( urlName );

                    if (url != null){
                        File file = new File(url.toURI());

                        if (file.exists()){
                            FileInputStream is = new FileInputStream(file);
                            setObjectRules(is);
                            isImportDone = true;
                        }
                        else {
                            log.warn(" The file to import is a resource (" + typeOfImport + ") but doesn't exist. Try to load this url as a local file and if not, try to read the url on internet.");
                        }
                    }
                    else{
                        log.warn(" The file to import is a resource (" + typeOfImport + ") but was not found. Try to load this url as a local file and if not, try to read the url on internet.");
                    }
                }
                else if (typeOfImport.toLowerCase().equals(LOCAL_FILE)){
                    if (isALocalFile(urlName)){
                        loadLocalFileFrom(urlName);
                        isImportDone = true;
                    }
                    else {
                        log.warn(" The file to import is a local file (" + typeOfImport + ") but was not found. Try to read the url on internet.");
                    }
                }
                else if (typeOfImport.toLowerCase().equals(FILE)){
                    loadFileFrom(urlName);
                    isImportDone = true;
                }
                else {
                    log.warn(" The type of the file to import " + typeOfImport + " is not known. You can choose 'resource' (resource of the validator), 'file' (local file on your machine), or 'url' (look on internet)." +
                            " First we will try to load this file as a resource. If not found, we will look the local files and then we will try on internet.");
                }
            }
            else {
                log.warn(" The type of the file to import " + typeOfImport + " is not precised. You can choose 'resource' (resource of the validator), 'file' (local file on your machine), or 'url' (look on internet)." +
                        " First we will try to load this file as a resource. If not found, we will look the local files and then we will try on internet.");
            }

            if (!isImportDone){
                //URL url = Validator.class.getClassLoader().getResource( urlName );
                URL url = this.getClass().getResource( urlName );

                if (url != null){
                    File file = new File(url.toURI());

                    if (file.exists()){
                        FileInputStream is = new FileInputStream(file);
                        setObjectRules(is);
                    }
                    else {
                        if (isALocalFile(urlName)){
                            loadLocalFileFrom(urlName);
                        }
                        else {
                            loadFileFrom(urlName);
                        }
                    }
                }
                else{
                    if (isALocalFile(urlName)){
                        loadLocalFileFrom(urlName);
                    }
                    else {
                        loadFileFrom(urlName);
                    };
                }

            }
        } catch (MalformedURLException e) {
            throw new ValidatorException("The URL " + urlName + " is malformed and can't be read",e);
        }
        catch (IOException e){
            throw new ValidatorException("The URL " + urlName + " can't be read",e);
        }
        catch (URISyntaxException e){
            throw new ValidatorException("The URI " + urlName + " doesn't have a valid syntax",e);
        }
    }

    /**
     * Parse the configuration file and update the list of Rule of the current Validator.
     * <p/>
     * Each Rule is initialised with a Map of Ontologies that have been read from the config file.
     *
     * @param configFile the configuration file.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public void setObjectRules( InputStream configFile ) throws ValidatorException {
        // set -> replace whatever there might have been

        String name = null;

        if( configFile != null ) {
            ObjectRuleReader reader = new ObjectRuleReader();
            try {
                final ObjectRuleList rules = reader.read( configFile );
                name = rules.getName();

                for ( Rule rule : rules.getRule() ) {
                    addRule(rule, name);
                }

                ImportRuleList rulesToImport = rules.getImportRuleList();
                if(rulesToImport != null){
                    for (Import importedRules : rulesToImport.getImport()){
                        String linkToRules = importedRules.getRules();
                        String typeOfImport = importedRules.getType();

                        if (!this.urlsForTheImportedRules.containsKey(linkToRules)){
                            importRulesFromFile(linkToRules, typeOfImport);
                            this.urlsForTheImportedRules.put(linkToRules, name);
                        }
                        else{
                            log.warn("The " + name != null ? name : "" + " rules from the url " + linkToRules + " have already been imported in a previous file (name = " + this.urlsForTheImportedRules.get(linkToRules) + "). We cannot do the import twice.");
                        }
                    }
                }
            } catch ( ObjectRuleReaderException e ) {
                throw new ValidatorException( "Error during the parsing of "+ configFile.toString(), e );
            }
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug( "No Object rules were configured in this validator." );
            }
        }
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences( UserPreferences userPreferences ) {
        this.userPreferences = userPreferences;
    }

    ////////////////////////////////////
    // Validation against object rule

    /**
     * Validates a collection of objects against all the (object) rules.
     *
     * @param col collection of objects to check on.
     * @return collection of validator messages.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public Collection<ValidatorMessage> validate( Collection<?> col ) throws ValidatorException {
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        for ( ObjectRule rule : rules ) {
            messages.addAll( validate( col, rule ) );
        }
        return messages;
    }

    /**
     * Validates a single object against all the (object) rules.
     *
     * @param objectToCheck objects to check on.
     * @return collection of validator messages.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public Collection<ValidatorMessage> validate( Object objectToCheck ) throws ValidatorException {
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        for ( ObjectRule rule : rules ) {
            if ( rule.canCheck( objectToCheck ) ) { // apply only if rule can handle this object
                messages.addAll( rule.check( objectToCheck ) );
            }
        }
        return messages;
    }

    /**
     * Validates a single object against a given (object) rules.
     *
     * @param objectToCheck objects to check on.
     * @return collection of validator messages.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public Collection<ValidatorMessage> validate( Object objectToCheck, ObjectRule rule ) throws ValidatorException {
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        if ( rule.canCheck( objectToCheck ) ) { // apply only if rule can handle this object
            messages.addAll( rule.check( objectToCheck ) );
        }
        return messages;
    }

    /**
     * Validates a collection of objects against a single (object) rule.
     *
     * @param col  collection of objects to check on.
     * @param rule the Rule to check on
     * @return collection of validator messages.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    private Collection<ValidatorMessage> validate( Collection<?> col, ObjectRule rule ) throws ValidatorException {
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        for ( Object aCol : col ) {
            if ( rule.canCheck( aCol ) ) { // apply only if rule can handle this object
                messages.addAll( rule.check( aCol ) );
            }
        }
        return messages;
    }

    //////////////////////////
    // CvMapping validation

    /**
     * Run a check on the CvMappingRules to ensure syntactically correct rules will be used for the CvMapping check.
     *
     * @return collection of validator messages.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public Collection<ValidatorMessage> checkCvMappingRules() throws ValidatorException {
        if ( cvRuleManager != null ) {
            return cvRuleManager.checkCvMapping();
        } else {
            log.warn( "The CvRuleManager has not been set up yet." );
            return new ArrayList<ValidatorMessage>();
        }
    }

    /**
     * Run a check on the CvMapping for a given Collection of Objects.
     *
     * @param col   collection of objects to check on.
     * @param xPath the xpath from the XML root to the object that is to be checked.
     * @return collection of validator messages describing the validation results.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public Collection<ValidatorMessage> checkCvMapping( Collection<?> col, String xPath ) throws ValidatorException {
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        // Run cv mapping check
        if ( cvRuleManager != null ) {
            for ( CvRule rule : cvRuleManager.getCvRules() ) {
                for ( Object o : col ) {
                    if ( rule.canCheck( xPath ) ) {
                        messages.addAll( rule.check( o, xPath ) );
                    }
                    // else: rule does not apply
                }
            }
        } else {
            log.error( "The CvRuleManager has not been set up yet." );
        }
        return messages;
    }

    /**
     * Run a check on the CvMapping for a given Object.
     *
     * @param o     Object to check.
     * @param xPath the xpath from the XML root to the object that is to be checked.
     * @return collection of validator messages describing the validation results.
     * @throws ValidatorException Exception while trying to validate the input.
     */
    public Collection<ValidatorMessage> checkCvMapping( Object o, String xPath ) throws ValidatorException {
        Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
        // Run cv mapping check
        if ( cvRuleManager != null ) {
            for ( CvRule rule : cvRuleManager.getCvRules() ) {
                if ( rule.canCheck( xPath ) ) {
                    messages.addAll( rule.check( o, xPath ) );
                }
                // else: rule does not apply
            }
        } else {
            log.error( "The CvRuleManager has not been set up yet." );
        }
        return messages;
    }

    public ValidatorReport getReport() {
        return new ValidatorReport( cvRuleManager.getCvRules() );
    }

    //////////////////////////
    // resetting validation

    public void resetCvRuleStatus() {
        Collection<CvRule> cvRules = this.cvRuleManager.getCvRules();
        for (CvRule cvRule : cvRules) {
            cvRule.resetStatus();
        }
    }
}