package psidev.psi.tools.ontology_manager.impl.ols;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;
import uk.ac.ebi.ook.web.services.client.ConfigurableQueryServiceLocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URI;
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

    // methods that use the cache, use the method name to as part of the cahce key
    private final byte GET_VALID_IDS = 1;
    private final byte IS_OBSOLETE_ID = 2;
    private final byte GET_TERM_NAME_BY_ID = 3;
    private final byte GET_DIRECT_PARENTS_IDS = 4;
    private final byte GET_CHILD_TERMS = 5;

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


}
