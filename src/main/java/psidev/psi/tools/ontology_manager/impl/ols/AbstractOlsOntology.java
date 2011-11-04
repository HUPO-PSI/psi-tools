package psidev.psi.tools.ontology_manager.impl.ols;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.client.OlsClient;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccessTemplate;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Abstract ols ontology
 *
 * NOTE : OlsOntology was the original class but did not offer flexibility for using different extensions of OntologyTermI.
 * That is why this template abstract class has been created.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public abstract class AbstractOlsOntology<T extends OntologyTermI> implements OntologyAccessTemplate<T>{
    public static final Log log = LogFactory.getLog(OlsOntology.class);

    protected GeneralCacheAdministrator admin; // the cache
    protected static final String cacheConfig = "olsontology-oscache.properties";
    protected boolean useTermSynonyms = true; // flag whether term synonyms should be recorded
    protected OlsClient olsClient;
    protected String ontologyID;
    protected Set<String> rootAccs;

    protected Date lastOntologyUpload;

    // methods that use the cache, use the method name to as part of the cache key
    protected final byte GET_VALID_IDS = 1;
    protected final byte IS_OBSOLETE_ID = 2;
    protected final byte GET_TERM_NAME_BY_ID = 3;
    protected final byte GET_DIRECT_PARENTS_IDS = 4;
    protected final byte GET_CHILD_TERMS = 5;
    protected final byte GET_CHILDREN = 6;
    protected final byte GET_DIRECT_PARENTS = 7;
    protected final byte IS_OBSOLETE = 8;
    protected final byte GET_TERM_FOR_ACCESSION = 9;
    protected final byte GET_METADATA_FOR_ACCESSION = 10;
    protected final byte GET_XREF_FOR_ACCESSION = 11;

    public AbstractOlsOntology() throws OntologyLoaderException {
        log.info( "Creating new OlsOntology..." );

        // preparing cache
        lastOntologyUpload = new Date(System.currentTimeMillis());

        // set up the cache for the ontology terms
        initCache();

        // preparing OLS access
        log.info( "Creating new OLS query client." );
        try {
            olsClient = new OlsClient();
        } catch ( Exception e ) {
            log.error( "Exception setting up OLS query client!", e );
            throw new OntologyLoaderException( "Exception setting up OLS query client!", e );
        }
    }

    /**
     * This will read the cache configuration file and initialise the cache.
     */
    private void initCache() {
        log.info( "Setting up cache administrator..." );
        Properties cacheProps;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream( cacheConfig );
        cacheProps = new Properties();
        try {
            cacheProps.load( is );
        } catch ( IOException e ) {
            log.error( "Failed to load cache configuration properties: " + cacheConfig, e );
        }
        if ( cacheProps.isEmpty() ) {
            log.warn( "Using default cache configuration!" );
            admin = new GeneralCacheAdministrator();
        } else {
            log.info( "Using custom cache configuration from file: " + cacheConfig );
            admin = new GeneralCacheAdministrator( cacheProps );
        }
    }

    /**
     * @return true if synonyms for ontology terms are taken into account, false if not.
     */
    public boolean isUseTermSynonyms() {
        return useTermSynonyms;
    }

    /**
     * @param useTermSynonyms flag to toggle handling of ontology term synonyms.
     */
    public void setUseTermSynonyms(boolean useTermSynonyms) {
        // if we did not record the term synonyms before, but now we are supposed to,
        // then we have to reset the cache, so all terms get loaded again including synonyms.
        if (!isUseTermSynonyms() && useTermSynonyms) {
            admin.destroy();
            initCache();
        }
        // in the other cases (using synonyms or switching off synonyms) we don't need to touch the cache
        this.useTermSynonyms = useTermSynonyms;
    }

    public void loadOntology( String ontologyID, String name, String version, String format, URI uri ) {
        this.ontologyID = ontologyID;
        try {
            Map roots = olsClient.getRootTerms( ontologyID );
            rootAccs = new HashSet<String>();
            rootAccs.addAll( roots.keySet() );
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        log.info( "Successfully created OlsOntology from values: ontology=" + ontologyID + " name=" + name
                + " version=" + version + " format=" + format + " location=" + uri );
    }

    /**
     * This method is not applicable for this implementation of the OntologyAccess interface.
     * @param directory irrelevant.
     */
    public void setOntologyDirectory( File directory ) {
        // not applicable
        log.info( "setOntologyDirectory does not have any effect on the OlsOntology." );
    }


    /**
     * Method that is used by the validator to determine a Set of Ontology terms that are valid terms
     * for a particular rule. E.g. according to the flags, this can be the term corresponding to the
     * provided accession or its children or both.
     *
     * @param accession     the accession (ID) of a ontology term.
     * @param allowChildren flag weather or not to allow child terms of the specified accession.
     * @param useTerm       flag weather or not to use the given accession as one of the valid terms.
     * @return a Set of OntologyTerms that are valid (in terms of the validator).
     */
    public Set<T> getValidTerms( String accession, boolean allowChildren, boolean useTerm ) {
        Set<T> validTerms = new HashSet<T>();
        T term = getTermForAccession( accession );
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

    /**
     * This method is used to create a full OntologyTermI
     * from the given accession only (using the OLS service).
     * Note: this is the uncached method version.
     *
     * @param accession the ontology term accession for which to look up the term.
     * @return the OntologyTermI for the specified accession.
     */
    public T getTermForAccessionUncached( String accession ) {
        // if we don't even have a valid input, there is no point in trying to query OLS
        if (accession == null) { return null; }

        String termName;
        try {
            termName = olsClient.getTermById(accession, ontologyID);
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }

        // check the result! ols returns the input accession if no matching entry was found
        T term;
        if ( termName != null && termName.length() > 0 && !termName.equals( accession ) ) {
            term = createNewOntologyTerm( accession, termName );
            if (useTermSynonyms) {
                fetchTermSynonyms( term );
            }
        } else {
            term = null;
        }

        return term;
    }

    protected abstract T createNewOntologyTerm(String identifier, String name);

    private void fetchTermSynonyms( T term ) {
        if (term == null) { return; }
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        final String myKey = GET_METADATA_FOR_ACCESSION + '_' + ontologyID + '_' + term.getTermAccession();
        Map metadata = getAllTermSynonyms(term.getTermAccession());

        for ( Object k : metadata.keySet() ) {
            final String key = (String) k;
            // That's the only way OLS provides synonyms, all keys are different so we are fishing out keywords :(
            if( key != null && (key.contains( "synonym" ) || key.contains( "Alternate label" )) ) {
                String value = (String) metadata.get( k );
                if( value != null ) {
                    term.getNameSynonyms().add(value.trim());
                }
            }
        }
    }

    private Map getTermMetadataUncached(String termAccession){
        if (termAccession == null) { return null; }

        final Map metadata;
        try {
            metadata = olsClient.getTermMetadata(termAccession, ontologyID);

            return metadata;

        } catch ( Exception e ) {
            if ( log.isWarnEnabled() ) {
                log.warn( "Error while loading term synonyms from OLS for term: " + termAccession, e );
            }
        }

        return null;
    }

    public Map getAllTermSynonyms( String termAccession ) {
        if (termAccession == null) { return null; }
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        final String myKey = GET_METADATA_FOR_ACCESSION + '_' + ontologyID + '_' + termAccession;
        Map metadata;

        try {
            metadata = (Map) getFromCache(myKey);
            if ( log.isDebugEnabled() ) log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException e) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                metadata = this.getTermMetadataUncached( termAccession );
                if ( log.isDebugEnabled() ) log.debug( "Storing uncached terms for key: " + myKey );
                putInCache( myKey, metadata );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    cancelUpdate( myKey );
                }
            }
        }

        return metadata;
    }

    public Map getAllTermXrefs( String termAccession ) {
        if (termAccession == null) { return null; }
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        final String myKey = GET_XREF_FOR_ACCESSION + '_' + ontologyID + '_' + termAccession;
        Map xrefs;

        try {
            xrefs = (Map) getFromCache(myKey);
            if ( log.isDebugEnabled() ) log.debug( "Using cached terms for key: " + myKey );
        } catch (NeedsRefreshException e) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                xrefs = this.getAllTermXrefsUncached(termAccession);
                if ( log.isDebugEnabled() ) log.debug( "Storing uncached terms for key: " + myKey );
                putInCache( myKey, xrefs );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    cancelUpdate( myKey );
                }
            }
        }

        return xrefs;
    }

    private Map getAllTermXrefsUncached(String termAccession){
        if (termAccession == null) { return null; }

        final Map xrefs;
        try {
            xrefs = olsClient.getTermXrefs(termAccession, ontologyID);

            return xrefs;

        } catch ( Exception e ) {
            if ( log.isWarnEnabled() ) {
                log.warn( "Error while loading term synonyms from OLS for term: " + termAccession, e );
            }
        }

        return null;
    }

    /**
     * The method is synchronized to avoid concurrent access/modification when accessing the cache
     * @param myKey : the key to find in the cache
     * @return the object associated with this key in the cache.
     * @throws NeedsRefreshException : the key doesn't exist in the cache or is outdated
     */
    private synchronized Object getFromCache( String myKey ) throws NeedsRefreshException {
        return admin.getFromCache( myKey );
    }

    /**
     * The method will put the key associated with an object in the cache of this OlsOntology
     * The method is synchronized to avoid concurrent access/modification when accessing the cache
     * @param myKey : the key to put in the cache
     * @param result : the associated object to put in the cache
     */
    private synchronized void putInCache( String myKey, Object result ) {
        admin.putInCache( myKey, result );
    }

    /**
     * Cancel any update for this key in the cache
     * The method is synchronized to avoid concurrent access/modification when accessing the cache
     * @param myKey : the key to find in the cache
     */
    private synchronized void cancelUpdate(String myKey){
        admin.cancelUpdate( myKey );
    }

    /**
     * This method is used to create a full OntologyTermI
     * from the given accession via using the OLS service.
     * Note: this is method uses a cache.
     *
     * @param accession the ontology term accession for which to look up the term.
     * @return the OntologyTermI for the specified accession.
     */
    public T getTermForAccession( String accession ) {
        if (accession == null) { return null; }
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        final String myKey = GET_TERM_FOR_ACCESSION + '_' + ontologyID + '_' + accession;

        T result;
        // try to get the result from the cache
        try {
            result = (T) getFromCache(myKey);
            if ( log.isDebugEnabled() ) log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.getTermForAccessionUncached( accession );
                if ( log.isDebugEnabled() ) log.debug( "Storing uncached terms for key: " + myKey );
                putInCache( myKey, result );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    cancelUpdate( myKey );
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
    public boolean isObsoleteUncached( T term ) {
        boolean retVal;
        try {
            retVal = olsClient.isObsolete( term.getTermAccession(), ontologyID );
        } catch ( RemoteException e ) {
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
    public boolean isObsolete( T term ) {
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = IS_OBSOLETE + '_' + ontologyID + '_' + term.getTermAccession();

        boolean result;
        // try to get the result from the cache
        try {
            result = ( Boolean ) getFromCache( myKey );
            log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.isObsoleteUncached( term );
                log.debug( "Storing uncached terms for key: " + myKey );
                putInCache( myKey, result );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    cancelUpdate( myKey );
                }
            }
        }
        return result;
    }

    /**
     * This method looks up the direct parents of the given OntologyTermI.
     * Note: this method is uncached and operates directly on OLS.
     *
     * @param term the OntologyTermI for which to look up its direct parents.
     * @return a Set of OntologyTermIs of the direct parents of the given term or null if the term is invalid.
     */
    public Set<T> getDirectParentsUncached( T term ) {
        if (term == null) { return null; }
        Map results;
        try {
            results = olsClient.getTermParents( term.getTermAccession(), ontologyID );
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        // OLS returns a empty map if no parents are found for the query, so we
        // will always retrun a Set (empty or non-empty) if no exception is thrown.
        return olsMap2TermSet( results );
    }

    /**
     * This method looks up the direct parents of the given OntologyTermI.
     * Note: this method is cached and only operates directly on OLS if
     * the query has not been performed before.
     *
     * @param term the OntologyTermI for which to look up its direct parents.
     * @return a Set of OntologyTermIs of the direct parents of the given term.
     */
    public Set<T> getDirectParents( T term ) {
        if (term == null) { return null; }
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = GET_DIRECT_PARENTS + '_' + ontologyID + '_' + term.getTermAccession();

        Set<T> result;
        // try to get the result from the cache
        try {
            result = ( Set<T> ) getFromCache( myKey );
            log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.getDirectParentsUncached( term );
                log.debug( "Storing uncached terms for key: " + myKey );
                putInCache( myKey, result );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    cancelUpdate( myKey );
                }
            }
        }
        return result;
    }

    /**
     * This method looks up all parents of the given OntologyTermI.
     * Note: this method is cached and uses the #getDirectParents method
     * recursively until the root terms of the ontology are reached.
     *
     * @param term the OntologyTermI for which to look up its direct parents.
     * @return a Set of OntologyTermIs of the direct parents of the given term.
     */
    public Set<T> getAllParents( T term ) {
        Set<T> allParents = new HashSet<T>();
        addParents( term, allParents );
        return allParents;
    }

    /**
     * Helper method for the recursive call of the #getAllParents method.
     *
     * @param term the OntologyTermI for which to get the parents.
     * @param parents Set of OntologyTermIs to which to add the found parents.
     */
    private void addParents( T term, Set<T> parents ) {
        if (term == null) { return; }
        Set<T> dps = getDirectParents( term );
        for ( T dp : dps ) {
            // if the parent is not already contained in the list: add it
            if ( !parents.contains( dp ) ) {
                parents.add( dp );
                // only if it is not a root term, look for more parents
                if ( !rootAccs.contains( dp.getTermAccession() ) ) {
                    addParents( dp, parents );
                }
            }
        }
    }

    /**
     * Method to retrieve child terms of the specified ontology term.
     * Note: this method is uncached.
     *
     * @param term  the ontology term to get the child terms for.
     * @param level up to which level in depth to search (note: -1 will get ALL children)
     * @return a Set containing the child terms of the specified term or null if the term is invalid.
     */
    public Set<T> getChildrenUncached( T term, int level ) {
        if (term == null) { return null; }
        int[] relationshipTypes = new int[] {1, 2, 3, 4};
        Map results;
        try {
            results = olsClient.getTermChildren( term.getTermAccession(), ontologyID, level, relationshipTypes );
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        // OLS returns a empty map if no children are found for the query, so we
        // will always retrun a Set (empty or non-empty) if no exception is thrown.
        return olsMap2TermSet( results );
    }

    /**
     * Method to retrieve only the direct child terms of the specified
     * ontology term. This method uses the getChildren(OntologyTermI, int)
     * method with a level of 1 to retrieve the direct children on the
     * specified term.
     *
     * @param term the ontology term to get the child terms for.
     * @return a Set containing the direct child terms of the specified term.
     */
    public Set<T> getDirectChildren( T term ) {
        return getChildren( term, 1 );
    }

    /**
     * Method to retrieve child terms of the specified ontology term.
     * Note: this method is cached.
     *
     * @param term  the ontology term to get the child terms for.
     * @param level up to which level in depth to search for children (note: -1 will get ALL children)
     * @return a Set containing the child terms of the specified term or null if the term is invalid.
     */
    public synchronized Set<T> getChildren( T term, int level ) {
        if (term == null) { return null; }
        // create a unique string for this query
        // generate from from method specific ID, the ontology ID and the input parameter
        String myKey = GET_CHILDREN + '_' + ontologyID + '_' + term.getTermAccession() + '_' + level;

        Set<T> result;
        // try to get the result from the cache
        try {
            result = ( Set<T> ) admin.getFromCache( myKey );
            log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            // if not found in cache, use uncached method and store result in cache
            try {
                result = this.getChildrenUncached( term, level );
                log.debug( "Storing uncached terms for key: " + myKey );
                admin.putInCache( myKey, result );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate( myKey );
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
    public Set<T> getAllChildren( T term ) {
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
    private Set<T> olsMap2TermSet( Map results ) {
        Set<T> terms = new HashSet<T>();
        for ( Object o : results.keySet() ) {
            Object v = results.get( o );
            if ( o instanceof String && v instanceof String ) {
                final T term = createNewOntologyTerm( ( String ) o, ( String ) v );
                if (useTermSynonyms) {
                    fetchTermSynonyms( term );
                }
                terms.add( term );
            } else {
                throw new IllegalStateException( "OLS query returned unexpected result!" +
                        " Expected Map with key and value of class String," +
                        " but found key class: " + o.getClass().getName() +
                        " and value class: " + v.getClass().getName() );
            }
        }
        return terms;
    }

    // This has issues finding all the child terms if the tree changes relationship types -> use getValidIDs2
    @Deprecated
    public Set<String> getValidIDsOld( String id, boolean allowChildren, boolean useTerm ) {
        Set<String> terms = new HashSet<String>();
        try {
            if ( useTerm ) {
                String result = olsClient.getTermById( id, ontologyID );
                // check if the id returns a valid term name - if not, the id is not valid for this ontology
                if ( result.equalsIgnoreCase( id ) ) { // OLS returns the (unchanged) id if no matching term is found
                    log.warn( "The Term ID '" + id + "' was not found in ontology '" + ontologyID + "'." );
                } else {
                    // id is valid for this ontology
                    log.debug( "Found valid id: " + id + " in ontology: " + ontologyID );
                    terms.add( id );
                }
            }
            if ( allowChildren ) { // get all children
                int[] relationshipTypes = new int []  {1, 2, 3, 4};

                Map resultMap = olsClient.getTermChildren( id, ontologyID, -1, relationshipTypes );
//                Map resultMap = query.getTermChildren(id, ontologyID, -1, null );
                if ( resultMap == null ) { // should not happen, but just in case ...
                } else {
                    log.debug( "Found " + resultMap.keySet().size()
                            + " child terms of id: " + id + " in ontology: " + ontologyID );
                    terms.addAll( resultMap.keySet() );
                }
            }
        } catch ( RemoteException e ) {
            log.error( "RemoteException while trying to connect to OLS.", e );
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return terms;
    }

    @Deprecated
    protected Set<String> getValidIDs2( String id, boolean allowChildren, boolean useTerm ) {
        Set<String> terms = new HashSet<String>();
        try {
            if ( useTerm ) {
                String result = olsClient.getTermById( id, ontologyID );
                // check if the id returns a valid term name - if not, the id is not valid for this ontology
                if ( result.equalsIgnoreCase( id ) ) { // OLS returns the (unchanged) id if no matching term is found
                    log.warn( "The Term ID '" + id + "' was not found in ontology '" + ontologyID + "'." );
                } else {
                    // id is valid for this ontology
                    terms.add( id );
                }
            }
            if ( allowChildren ) { // get all children
                Set<String> children = getAllChildTerms( id );
                log.debug( "Found " + children.size()
                        + " child terms of id: " + id + " in ontology: " + ontologyID );
                terms.addAll( children );
            }
        } catch ( RemoteException e ) {
            log.error( "RemoteException while trying to connect to OLS.", e );
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return terms;
    }

    @Deprecated
    public synchronized Set<String> getValidIDs( String queryTerm, boolean allowChildren, boolean useTerm ) {
        Set<String> terms; // to store the results

        // create unique string for this query
        String queryGroup = "getValidIDs_query";
        String myKey = queryGroup + "_" + ontologyID + "_" + queryTerm + "_" + allowChildren + "_" + useTerm;

        String[] groups = {queryGroup};
        try {
            // Get from the cache
            terms = ( Set<String> ) admin.getFromCache( myKey );
            log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                //terms = this.getValidIDs2( queryTerm, allowChildren, useTerm );
                terms = this.getValidIDsOld( queryTerm, allowChildren, useTerm );
                log.debug( "Storing uncached terms with key: " + myKey );
                // store in the cache
                admin.putInCache( myKey, terms, groups );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate( myKey );
                }
            }
        }
        return terms;
    }

    @Deprecated
    protected boolean isObsoleteIDUncached( String id ) {
        boolean result;
        try {
            // OLS does return false if the term does not exist! -> check the existence of the term first
            String s = olsClient.getTermById( id, ontologyID );
            if ( s.equalsIgnoreCase( id ) ) {
                // term not in database (if instead of the term name the accession is returned)
                throw new IllegalStateException( "Checking obsolete on term '" + id
                        + "' which does not exist in '" + ontologyID + "'!" );
            }
            result = olsClient.isObsolete( id, ontologyID );
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return result;
    }

    @Deprecated
    public synchronized boolean isObsoleteID( String id ) {
        Boolean result;

        // create unique string for this query
        String queryGroup = "isObsoleteID_query";
        String myKey = queryGroup + "_" + ontologyID + "_" + id;

        String[] groups = {queryGroup};
        try {
            // Get from the cache
            result = ( Boolean ) admin.getFromCache( myKey );
            log.debug( "Using cached term for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.isObsoleteIDUncached( id );
                log.debug( "Storing uncached term with key: " + myKey );
                // Store in the cache
                admin.putInCache( myKey, result, groups );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    admin.cancelUpdate( myKey );
                }
            }
        }
        return result;
    }

    @Deprecated
    protected String getTermNameByIDUncached( String id ) {
        String result;
        try {
            result = olsClient.getTermById( id, ontologyID );
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to query OLS for: "
                    + id + " in ontology: " + ontologyID );
        }
        return result;
    }

    @Deprecated
    public synchronized String getTermNameByID( String id ) {
        String result;

        // create unique string for this query
        String queryGroup = "getTermNameByID_query";
        String myKey = queryGroup + "_" + ontologyID + "_" + id;

        String[] groups = {queryGroup};
        try {
            // Get from the cache
            result = ( String ) admin.getFromCache( myKey );
            log.debug( "Using cached term name for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.getTermNameByIDUncached( id );
                log.debug( "Storing uncached term name with key: " + myKey );
                // Store in the cache
                admin.putInCache( myKey, result, groups );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    admin.cancelUpdate( myKey );
                }
            }
        }
        return result;
    }

    @Deprecated
    protected Set<String> getDirectParentsIDsUncached( String id ) {
        Set<String> result;
        try {
            result = ( olsClient.getTermParents( id, ontologyID ) ).keySet();
        } catch ( RemoteException e ) {
            throw new IllegalStateException( "RemoteException while trying to connect to OLS." );
        }
        return result;
    }

    @Deprecated
    public synchronized Set<String> getDirectParentsIDs( String id ) {
        Set<String> result;

        // create unique string for this query
        String queryGroup = "getDirectParentsIDs_query";
        String myKey = queryGroup + "_" + ontologyID + "_" + id;

        String[] groups = {queryGroup};
        try {
            // Get from the cache
            result = ( Set<String> ) admin.getFromCache( myKey );
            log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.getDirectParentsIDsUncached( id );
                log.debug( "Storing uncached terms with key: " + myKey );
                // Store in the cache
                admin.putInCache( myKey, result, groups );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the cached content could not be rebuilt
                    admin.cancelUpdate( myKey );
                }
            }
        }
        return result;
    }

    @Deprecated
    public Set<String> getAllChildTerms( String id ) throws RemoteException {
        Set<String> retVal = new TreeSet<String>();
        appendChildTerms( retVal, getChildTerms( id ) );
        return retVal;
    }

    @Deprecated
    private void appendChildTerms( Set<String> set, Set<String> children ) throws RemoteException {
        for ( String child : children ) {
            if ( !set.contains( child ) ) {
                set.add( child );
                appendChildTerms( set, getChildTerms( child ) );
            }
        }
    }

    @Deprecated
    protected Set<String> getChildTermsUncached( String id ) throws RemoteException {
        Map<String, String> result;
        result = olsClient.getTermChildren( id, ontologyID, 1, null );
        return result.keySet();
    }

    @Deprecated
    private synchronized Set<String> getChildTerms( String id ) throws RemoteException {
        Set<String> result;

        // create unique string for this query
        String queryGroup = "getChildTerms_query";
        String myKey = queryGroup + "_" + ontologyID + "_" + id;

        String[] groups = {queryGroup};
        try {
            // Get from the cache
            result = ( Set<String> ) admin.getFromCache( myKey );
            log.debug( "Using cached terms for key: " + myKey );
        } catch ( NeedsRefreshException nre ) {
            boolean updated = false;
            try {
                // result of this query not in cache, get it from the un-cached method
                result = this.getChildTermsUncached( id );
                log.debug( "Storing uncached terms for key: " + myKey );
                // Store in the cache
                admin.putInCache( myKey, result, groups );
                updated = true;
            } finally {
                if ( !updated ) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    admin.cancelUpdate( myKey );
                }
            }
        }
        return result;
    }

    /**
     * put in the cache all the terms of the ontology
     */
    public void preLoadAllOntologyTerms(){
        for (String accession : this.rootAccs){
            T term = getTermForAccessionUncached(accession);

            preLoadAllChildrenOf(term);
        }
    }

    /**
     * Put in cache the ontology term with all its children
     * @param termToPreLoad : the ontology term to put in cache with all its children
     */
    private void preLoadAllChildrenOf(T termToPreLoad){
        Set<T> children = getAllChildren(termToPreLoad);

        for (T child : children){
            getTermForAccessionUncached(child.getTermAccession());
        }
    }

    /**
     *
     * @return true if the date of the last ontology upload is after the date of the last OLS update
     * @throws OntologyLoaderException
     */
    public boolean isOntologyUpToDate() throws OntologyLoaderException {

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = olsClient.getOntologyLoadDate(ontologyID);
            Date lastUpdate = dateFormat.parse(dateString);

            if (lastOntologyUpload.after(lastUpdate)){
                return true;
            }

        } catch (RemoteException e) {
            throw new OntologyLoaderException("We can't access the date of the last ontology update.", e);
        } catch (ParseException e) {
            throw new OntologyLoaderException("The date of the last ontology update cannot be parsed.", e);
        }
        return false;
    }
}
