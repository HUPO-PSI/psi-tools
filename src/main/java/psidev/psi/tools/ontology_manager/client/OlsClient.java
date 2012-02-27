package psidev.psi.tools.ontology_manager.client;

import uk.ac.ebi.ols.soap.Query;
import uk.ac.ebi.ols.soap.QueryServiceLocator;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Client of OLS webservice.
 *
 * This client was added because the ols 1.18 did not contain the webservice anymore.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/11/11</pre>
 */

public class OlsClient {

    private QueryServiceLocator queryService;
    private static final String wsdlURL = "http://www.ebi.ac.uk/ontology-lookup/OntologyQuery.wsdl";

    private static final String ontologyQueryURL = "http://www.ebi.ac.uk/ontology-lookup/OntologyQuery";
    private static final String ontologyQueryName = "QueryService";

    public OlsClient() throws MalformedURLException, ServiceException {
        queryService = new QueryServiceLocator(wsdlURL, new QName(ontologyQueryURL, ontologyQueryName));
    }

    public QueryServiceLocator getQueryService() {
        return queryService;
    }

    public Query getQuery() throws ServiceException {
        return queryService.getOntologyQuery();
    }

    public String getTermById(String accession, String ontologyId) throws RemoteException{
        try {
            return getQuery().getTermById(accession, ontologyId);
        } catch ( Exception e ) {
            throw new RemoteException( "RemoteException while trying to connect to OLS." );
        }
    }

    public Map getTermMetadata(String termAccession, String ontologyId) throws RemoteException{
        final Map metadata;
        try {
            metadata = getQuery().getTermMetadata(termAccession, ontologyId);

            return metadata;

        } catch ( Exception e ) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public Map getTermXrefs(String termAccession, String ontologyId) throws RemoteException{
        final Map metadata;
        try {
            metadata = getQuery().getTermXrefs(termAccession, ontologyId);

            return metadata;

        } catch ( Exception e ) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public Map getRootTerms(String ontologyId) throws RemoteException{

        try {
            Map roots = getQuery().getRootTerms(ontologyId);

            return roots;
        } catch ( Exception e ) {
            throw new RemoteException( "RemoteException while trying to connect to OLS." );
        }
    }

    public boolean isObsolete(String termAccession, String ontologyId) throws RemoteException{
        boolean retVal;
        try {
            retVal = getQuery().isObsolete(termAccession, ontologyId);
        } catch ( Exception e ) {
            throw new RemoteException( "RemoteException while trying to connect to OLS." );
        }
        return retVal;
    }

    public Map getTermParents(String termAccession, String ontologyId) throws RemoteException{

        final Map metadata;
        try {
            metadata = getQuery().getTermParents(termAccession, ontologyId);

            return metadata;

        } catch ( Exception e ) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public Map getTermChildren(String termAccession, String ontologyId, int level, int[] relationships) throws RemoteException{

        final Map metadata;
        try {
            metadata = getQuery().getTermChildren(termAccession, ontologyId, level, relationships);

            return metadata;

        } catch ( Exception e ) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public String getOntologyLoadDate(String ontologyId) throws RemoteException{
        try {
            String dateString = getQuery().getOntologyLoadDate(ontologyId);

            return dateString;

        } catch (Exception e) {
            throw new RemoteException("We can't access the date of the last ontology update.", e);
        }
    }
}
