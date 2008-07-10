package psidev.psi.tools.ontology_manager.impl.ols;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Author: Florian Reisinger
 * Date: 07-Aug-2007
 */
public class OlsOntology implements OntologyAccess {

    public static final Log log = LogFactory.getLog( OlsOntology.class );

    protected static GeneralCacheAdministrator admin;
    private static String cacheConfig = "olsontology-oscache.properties";
    static Query query;
    String ontologyID;
    Set<String> rootAccs;

    // methods that use the cache, use the method name to as part of the cahce key
    private final byte GET_VALID_IDS = 1;
    private final byte IS_OBSOLETE_ID = 2;
    private final byte GET_TERM_NAME_BY_ID = 3;
    private final byte GET_DIRECT_PARENTS_IDS = 4;
    private final byte GET_CHILD_TERMS = 5;
    private final byte GET_CHILDREN = 6;
    private final byte GET_DIRECT_PARENTS = 7;
    private final byte IS_OBSOLETE = 8;
    private final byte GET_TERM_FOR_ACCESSION = 9;

    private final boolean useByteCodeGroup = true;

    public OlsOntology() throws OntologyLoaderException {
        log.info("Creating new OlsOntology...");
        
        // preparing cache
        if ( admin == null ) {
            log.info( "Setting up cache administrator..." );
            Properties cacheProps;
            InputStream is = this.getClass().getClassLoader().getResourceAsStream( cacheConfig );
            cacheProps = new Properties();
            try {
                cacheProps.load( is );
            } catch (IOException e) {
                log.error( "Failed to load cache configuration properties: " + cacheConfig, e );
            }
            // ToDo: fail over with default settings ?
            if ( cacheProps.isEmpty() ) {
                log.warn( "Using default cache configuration!" );
                admin = new GeneralCacheAdministrator();
            } else {
                log.info( "Using custom cache configuration from file: " + cacheConfig );
                admin = new GeneralCacheAdministrator( cacheProps );
            }
        } else {
            log.info( "Cache administrator already set-up." );
        }

        // preparing OLS access
        if ( query == null ) {
            log.info("Creating new OLS query client.");
            try {
                QueryService locator = new QueryServiceLocator();
                query = locator.getOntologyQuery();
            } catch (Exception e) {
                log.error( "Exception setting up OLS query client!", e );
                throw new OntologyLoaderException( "Exception setting up OLS query client!", e );
            }
        } else {
            log.info("Reusing statically created OLS query client.");
        }
    }


    public void loadOntology( String ontologyID, String name, String version, String format, URI uri ) {
        this.ontologyID = ontologyID;
        try {
            Map roots = query.getRootTerms( ontologyID );
            rootAccs = new HashSet<String>();
            rootAccs.addAll(roots.keySet());
        } catch (RemoteException e) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        log.info("Successfully created OlsOntology from values: ontology=" + ontologyID + " name=" + name
                + " version=" + version + " format=" + format + " location=" + uri);
    }

    // This has issues finding all the child terms if the tree changes relationship types -> use getValidIDs2 
    public Set<String> getValidIDsOld( String id, boolean allowChildren, boolean useTerm ) {
        Set<String> terms = new HashSet<String>();
        try {
            if ( useTerm ) {
                String result = query.getTermById( id, ontologyID );
                // check if the id returns a valid term name - if not, the id is not valid for this ontology
                if ( result.equalsIgnoreCase(id) ) { // OLS returns the (unchanged) id if no matching term is found
                    log.warn( "The Term ID '" + id + "' was not found in ontology '" + ontologyID + "'." );
                } else {
                    // id is valid for this ontology
                    log.debug( "Found valid id: " + id + " in ontology: " + ontologyID );
                    terms.add(id);
                }
            }
            if ( allowChildren ) { // get all children
                int[] relationshipTypes = new int[4];
                relationshipTypes[0] = 1;
                relationshipTypes[1] = 2;
                relationshipTypes[2] = 3;
                relationshipTypes[3] = 4;

                Map resultMap = query.getTermChildren(id, ontologyID, -1, relationshipTypes );
//                Map resultMap = query.getTermChildren(id, ontologyID, -1, null );
                if ( resultMap == null ) { // should not happen, but just in case ...
                    // ToDo: not sure what is returned from OLS when term has no children (suppose empty Map)
                } else {
                    log.debug( "Found " + resultMap.keySet().size()
                            + " child terms of id: " + id + " in ontology: " + ontologyID );
                    terms.addAll(resultMap.keySet());
                }
            }
        } catch (RemoteException e) {
            log.error( "RemoteException while trying to connect to OLS.", e );
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return terms;
    }

    protected Set<String> getValidIDs2( String id, boolean allowChildren, boolean useTerm ) {
        Set<String> terms = new HashSet<String>();
        try {
            if ( useTerm ) {
                String result = query.getTermById( id, ontologyID );
                // check if the id returns a valid term name - if not, the id is not valid for this ontology
                if ( result.equalsIgnoreCase(id) ) { // OLS returns the (unchanged) id if no matching term is found
                    log.warn( "The Term ID '" + id + "' was not found in ontology '" + ontologyID + "'." );
                } else {
                    // id is valid for this ontology
                    terms.add(id);
                }
            }
            if ( allowChildren ) { // get all children
                Set<String> children = getAllChildTerms( id );
                    log.debug( "Found " + children.size()
                            + " child terms of id: " + id + " in ontology: " + ontologyID );
                    terms.addAll( children );
            }
        } catch (RemoteException e) {
            log.error( "RemoteException while trying to connect to OLS.", e );
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return terms;
    }

    public Set<String> getValidIDs( String queryTerm, boolean allowChildren, boolean useTerm ) {
        Set<String> terms; // to store the results

        // create unique string for this query
        String queryGroup = "getValidIDs_query";
        String myKey;
        if (useByteCodeGroup) {
            myKey = GET_VALID_IDS + '_' + ontologyID + '_' + queryTerm + '_' + allowChildren + '_' + useTerm;
        } else {
            myKey = queryGroup + "_" + ontologyID + "_" + queryTerm + "_" + allowChildren + "_" + useTerm;
        }
        String[] groups = { queryGroup };
        try {
            // Get from the cache
            terms = (Set<String>) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                //terms = this.getValidIDs2( queryTerm, allowChildren, useTerm );
                terms = this.getValidIDsOld(queryTerm, allowChildren, useTerm );
                log.debug( "Storing uncached terms with key: " + myKey );
                // store in the cache
                admin.putInCache(myKey, terms, groups);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return terms;
    }

    protected boolean isObsoleteIDUncached( String id ) {
        boolean result;
        try {
            // OLS does return false if the term does not exist! -> check the existence of the term first
            String s = query.getTermById( id, ontologyID );
            if ( s.equalsIgnoreCase( id ) ) {
                // term not in database (if instead of the term name the accession is returned)
                throw new IllegalStateException( "Checking obsolete on term '" + id
                        + "' which does not exist in '" + ontologyID + "'!" );
            }
            result =  query.isObsolete( id, ontologyID );
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return result;
    }

    public boolean isObsoleteID( String id ) {
        Boolean result;

        // create unique string for this query
        String queryGroup = "isObsoleteID_query";
        String myKey;
        if (useByteCodeGroup) {
            myKey = IS_OBSOLETE_ID + '_' + ontologyID + '_' + id;
        } else {
            myKey = queryGroup + "_" + ontologyID + "_" + id;
        }
        String[] groups = { queryGroup };
        try {
            // Get from the cache
            result = (Boolean) admin.getFromCache(myKey);
            log.debug( "Using cached term for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.isObsoleteIDUncached( id );
                log.debug( "Storing uncached term with key: " + myKey );
                // Store in the cache
                admin.putInCache(myKey, result, groups);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }

    protected String getTermNameByIDUncached( String id ) {
        String result;
        try {
            result =  query.getTermById( id, ontologyID );
        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException while trying to query OLS for: "
                    + id + " in ontology: " + ontologyID);
        }
        return result;
    }

    public String getTermNameByID( String id ) {
        String result;
        
        // create unique string for this query
        String queryGroup = "getTermNameByID_query";
        String myKey;
        if (useByteCodeGroup) {
            myKey = GET_TERM_NAME_BY_ID + '_' + ontologyID + '_' + id;
        } else {
            myKey = queryGroup + "_" + ontologyID + "_" + id;
        }
        String[] groups = { queryGroup };
        try {
            // Get from the cache
            result = (String) admin.getFromCache(myKey);
            log.debug( "Using cached term name for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.getTermNameByIDUncached( id );
                log.debug( "Storing uncached term name with key: " + myKey );
                // Store in the cache
                admin.putInCache(myKey, result, groups);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }

    protected Set<String> getDirectParentsIDsUncached( String id ) {
        Set<String> result;
        try {
            result = (query.getTermParents( id, ontologyID )).keySet();
        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException while trying to connect to OLS.");
        }
        return result;
    }

    public Set<String> getDirectParentsIDs( String id ) {
        Set<String> result;

        // create unique string for this query
        String queryGroup = "getDirectParentsIDs_query";
        String myKey;
        if (useByteCodeGroup) {
            myKey = GET_DIRECT_PARENTS_IDS + '_' + ontologyID + '_' + id;
        } else {
            myKey = queryGroup + "_" + ontologyID + "_" + id;
        }
        String[] groups = { queryGroup };
        try {
            // Get from the cache
            result = (Set<String>) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.getDirectParentsIDsUncached( id );
                log.debug( "Storing uncached terms with key: " + myKey );
                // Store in the cache
                admin.putInCache(myKey, result, groups);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }

    public void setOntologyDirectory(File directory) {
        // not applicable
        log.info("setOntologyDirectory does not have any effect on the OlsOntology.");
    }

    public Set<String> getAllChildTerms( String id ) throws RemoteException {
        Set<String> retVal = new TreeSet<String>();
        appendChildTerms(retVal, getChildTerms( id ));
        return retVal;
    }

    private void appendChildTerms(Set<String> set, Set<String> children) throws RemoteException {
        for(String child : children){
            if (!set.contains(child)) {
                set.add(child);
                appendChildTerms(set, getChildTerms( child ));
            }
        }
    }

    protected Set<String> getChildTermsUncached( String id ) throws RemoteException {
        Map<String, String> result;
        result = query.getTermChildren( id, ontologyID, 1, null );
        return result.keySet();
    }

    private Set<String> getChildTerms( String id ) throws RemoteException {
        Set<String> result;

        // create unique string for this query
        String queryGroup = "getChildTerms_query";
        String myKey;
        if (useByteCodeGroup) {
            myKey = GET_CHILD_TERMS + '_' + ontologyID + '_' + id;
        } else {
            myKey = queryGroup + "_" + ontologyID + "_" + id;
        }
        String[] groups = { queryGroup };
        try {
            // Get from the cache
            result = (Set<String>) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.getChildTermsUncached( id );
                log.debug( "Storing uncached terms for key: " + myKey );
                // Store in the cache
                admin.putInCache(myKey, result, groups);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }


    /**
     * Method that is used by the validator to determine a Set of Ontology terms that are valid terms
     * for a particular rule. E.g. according to the flags, this can be the term corresponding to the
     * provided accession or its children or both.
     *
     * @param accession the accession (ID) of a ontology term.
     * @param allowChildren flag weather or not to allow child terms of the specified accession.
     * @param useTerm flag weather or not to use the given accession as one of the valid terms.
     * @return a Set of OntologyTerms that are valid (in terms of the validator).
     */
    public Set<OntologyTermI> getValidTerms(String accession, boolean allowChildren, boolean useTerm) {
        Set<OntologyTermI> validTerms = new HashSet<OntologyTermI>();
        OntologyTermI term = getTermForAccession( accession );
        if ( term != null ) {
            if ( useTerm ) {
                validTerms.add( term );
            }
            if ( allowChildren ) {
                validTerms.addAll( getChildren( term, -1 ) );
            }
        }
        return validTerms;
    }

    public OntologyTermI getTermForAccessionUncached( String accession ) {
        String termName;
        try {
            termName = query.getTermById( accession, ontologyID );
        } catch (RemoteException e) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        // check the result! ols returns the input accession if no matching entry was found
        OntologyTermI term;
        if ( termName != null && termName.length() > 0 && !termName.equals( accession ) ) {
            term = new OntologyTermImpl(accession, termName);
        } else {
            term = null;
        }

        return term;
    }
    public OntologyTermI getTermForAccession( String accession ) {
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = GET_TERM_FOR_ACCESSION + '_' + ontologyID + '_' + accession;

        OntologyTermI result;
        // try to get the result from the cache
        try {
            result = (OntologyTermI) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.getTermForAccessionUncached( accession );
                log.debug( "Storing uncached terms for key: " + myKey );
                admin.putInCache(myKey, result);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }

    /**
     * This method will use a OLS query to check weather the specified
     * ontoloy term is obsolete or not.
     * Note: this is the uncached version which will always use OLS
     * (remote sevice calls).
     *
     * @param term the ontology term to check for being obsolete.
     * @return true if the term is flagged obolete, false otherwise.
     */
    public boolean isObsoleteUncached(OntologyTermI term) {
        boolean retVal;
        try {
            retVal = query.isObsolete( term.getTermAccession(), ontologyID );
        } catch (RemoteException e) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return retVal;
    }
    /**
     * This method will check weather the specified ontoloy term is
     * obsolete or not.
     * Note: this is the cached version which will first lookup the cache
     * and only invoke the uncached method if no cached entry was found.
     *
     * @param term the ontology term to check for being obsolete.
     * @return true if the term is flagged obolete, false otherwise.
     */
    public boolean isObsolete(OntologyTermI term) {
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = IS_OBSOLETE + '_' + ontologyID + '_' + term.getTermAccession();

        boolean result;
        // try to get the result from the cache
        try {
            result = (Boolean) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.isObsoleteUncached( term );
                log.debug( "Storing uncached terms for key: " + myKey );
                admin.putInCache(myKey, result);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }


    public Set<OntologyTermI> getDirectParentsUncached(OntologyTermI term) {
        Map results;
        try {
            results = query.getTermParents( term.getTermAccession(), ontologyID );
        } catch (RemoteException e) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return olsMap2TermSet( results );
    }
    public Set<OntologyTermI> getDirectParents(OntologyTermI term) {
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = GET_DIRECT_PARENTS + '_' + ontologyID + '_' + term.getTermAccession();

        Set<OntologyTermI> result;
        // try to get the result from the cache
        try {
            result = (Set<OntologyTermI>) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.getDirectParentsUncached( term );
                log.debug( "Storing uncached terms for key: " + myKey );
                admin.putInCache(myKey, result);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }
    public Set<OntologyTermI> getAllParents(OntologyTermI term) {
        Set<OntologyTermI> allParents = new HashSet<OntologyTermI>();
        addParents(term, allParents);
        return allParents;
    }
    private void addParents(OntologyTermI term, Set<OntologyTermI> parents) {
        Set<OntologyTermI> dps = getDirectParents(term);
        for (OntologyTermI dp : dps) {
            // if the parent is not already contained in the list: add it
            if ( !parents.contains(dp)  ) {
                parents.add(dp);
                // only if it is not a root term, look for more parents
                if ( !rootAccs.contains(dp.getTermAccession()) ) {
                    addParents(dp, parents);
                }
            }
        }
    }


    /**
     * Method to retrieve child terms of the specified ontology term.
     * Note: this method is uncached.
     *
     * @param term the ontology term to get the child terms for.
     * @param level up to which level in depth to search (note: -1 will get ALL children)
     * @return a Set containing the child terms of the specified term.
     */
    public Set<OntologyTermI> getChildrenUncached(OntologyTermI term, int level ) {
        int[] relationshipTypes = { 1, 2, 3, 4 };
        Map results;
        try {
            results = query.getTermChildren( term.getTermAccession(), ontologyID, level, relationshipTypes );
        } catch (RemoteException e) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return olsMap2TermSet( results );
    }
    /**
     * Method to retrieve only the direct child terms of the specified
     * ontology term. This method uses the getChildren(OntologyTermI, int)
     * method with a level of 1 to retrieve the direct children on the
     *  specified term.
     *
     * @param term the ontology term to get the child terms for.
     * @return a Set containing the direct child terms of the specified term.
     */
    public Set<OntologyTermI> getDirectChildren(OntologyTermI term) {
        return getChildren( term, 1);
    }
    /**
     * Method to retrieve child terms of the specified ontology term.
     * Note: this method is cached.
     *
     * @param term the ontology term to get the child terms for.
     * @param level up to which level in depth to search for children (note: -1 will get ALL children)
     * @return a Set containing the child terms of the specified term.
     */
    public Set<OntologyTermI> getChildren(OntologyTermI term, int level ) {
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = GET_CHILDREN + '_' + ontologyID + '_' + term.getTermAccession() + '_' + level;

        Set<OntologyTermI> result;
        // try to get the result from the cache
        try {
            result = (Set<OntologyTermI>) admin.getFromCache(myKey);
            log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException nre) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.getChildrenUncached( term, level );
                log.debug( "Storing uncached terms for key: " + myKey );
                admin.putInCache(myKey, result);
                updated = true;
            } finally {
                if (!updated) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate(myKey);
                }
            }
        }
        return result;
    }
    /**
     * Method to retrieve all child terms of the specified ontology term.
     * This method uses the getChildren(OntologyTermI, int) method with
     * a level of -1 to retrieve all the children on the specified term.
     *
     * @param term the ontology term to get the child terms for.
     * @return a Set containing all the child terms of the specified term.
     */
    public Set<OntologyTermI> getAllChildren(OntologyTermI term) {
        return getChildren( term, -1 );
    }

    /**
     * Method to convert the Map returned by the OLS query into a Set of OntologyTerms.
     * The OLS Map is supposed to contain a ontology term accession (String) as key
     * and a preferred name (String) as value for each ontology term entry.
     * In the conversion, each accession - preferred name -pair is combined in one
     * OntologyTermI compliant Object and all such objects are collected in the result set.
     *
     * @param results the Map returned my a OLS query.
     * @return a Set of OntologyTermI objects representing the result contained in the Map.
     */
    private Set<OntologyTermI> olsMap2TermSet( Map results ) {
        Set<OntologyTermI> terms = new HashSet<OntologyTermI>();
        for ( Object o : results.keySet() ) {
            Object v = results.get( o );
            if (o instanceof String && v instanceof String ) {
                terms.add( new OntologyTermImpl( (String)o, (String)v ) );
            } else {
                throw new IllegalStateException( "OLS query returned unexpected result!" +
                        " Expected Map with key and value of class String," +
                        " but found key class: " + o.getClass().getName() +
                        " and value class: " + v.getClass().getName() );
            }
        }
        return terms;
    }



    public static void main(String[] args) throws OntologyLoaderException, URISyntaxException {

        OlsOntology ols = new OlsOntology();
        ols.loadOntology( "GO", "", "", "", new URI("foo") );

        String acc = "GO:0003675";
        System.out.println("Querying for term: " + acc);
        OntologyTermI term = ols.getTermForAccession( acc );
        System.out.println( "Is obsolete? " + ols.isObsolete( term ));

        acc = "GO:0030288";
        System.out.println("Querying for term: " + acc);
        term = ols.getTermForAccession( acc );
        System.out.println( "Is obsolete? " + ols.isObsolete( term ));

        Set<OntologyTermI> parents = ols.getDirectParents( term );
        System.out.println( "Has parents: " + parents.size() );
        Set<OntologyTermI> allParents = ols.getAllParents( term );
        System.out.println( "All parents: " + allParents.size() );
        for (OntologyTermI allParent : allParents) {
            System.out.println(allParent);
        }

        Set<OntologyTermI> valid = ols.getValidTerms( "GO:0030288", true, true );
        System.out.println( "Valid terms: " + valid.size() );


    }

}
