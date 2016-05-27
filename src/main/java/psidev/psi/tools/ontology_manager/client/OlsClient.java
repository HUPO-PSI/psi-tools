package psidev.psi.tools.ontology_manager.client;

import org.hsqldb.lib.Collection;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigDev;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
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

    private OLSClient olsClient;

    public OlsClient() throws MalformedURLException, ServiceException {
        this.olsClient = new OLSClient(new OLSWsConfigDev());
    }

    public String getTermById(String accession, String ontologyId) throws RemoteException{
        try {
            Identifier identifier = new Identifier(accession, Identifier.IdentifierType.OBO);
            return olsClient.getTermById(identifier, ontologyId).getLabel();
        } catch ( Exception e ) {
            throw new RemoteException( "RemoteException while trying to connect to OLS." );
        }
    }

    public Map getTermMetadata(String termAccession, String ontologyId) throws RemoteException{
        final Map metadata;
        try {
            Identifier identifier = new Identifier(termAccession, Identifier.IdentifierType.OBO);
            metadata = olsClient.getMetaData(identifier, ontologyId);
            if (metadata != null && !metadata.isEmpty()) {
                for (Object key : metadata.keySet()) {
                    if (metadata.get(key) == null) {
                        metadata.remove(key);
                    }
                    if (metadata.get(key) instanceof String && metadata.get(key).equals("")) {
                        metadata.remove(key);
                    }
                    if (metadata.get(key) instanceof Collection && ((Collection) metadata.get(key)).isEmpty()) {
                        metadata.remove(key);
                    }
                }
            }
            return metadata;

        } catch ( Exception e ) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public Map getTermXrefs(String termAccession, String ontologyId) throws RemoteException{
        final Map metadata;
        try {
            Identifier identifier = new Identifier(termAccession, Identifier.IdentifierType.OBO);
            metadata = olsClient.getTermXrefs(identifier, ontologyId);

            return metadata;

        } catch ( Exception e ) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public Map getRootTerms(String ontologyId) throws RemoteException{
        HashMap roots = new HashMap();
        try {
            List<Term> result = olsClient.getRootTerms(ontologyId);
            for(Term term : result){
                roots.put(term.getTermOBOId().getIdentifier(), term);
            }
            return roots;
        } catch ( Exception e ) {
            throw new RemoteException( "RemoteException while trying to connect to OLS." );
        }
    }

    public boolean isObsolete(String termAccession, String ontologyId) throws RemoteException{
        boolean retVal;
        try {
            retVal = olsClient.isObsolete(termAccession, ontologyId);
        } catch ( Exception e ) {
            throw new RemoteException( "RemoteException while trying to connect to OLS." );
        }
        return retVal;
    }

    public Map getTermParents(String termAccession, String ontologyId) throws RemoteException {

        final Map metadata = new HashMap();
        try {
            Identifier identifier = new Identifier(termAccession, Identifier.IdentifierType.OBO);
            for (Term term : olsClient.getTermParents(identifier, ontologyId, 1)) {
                metadata.put(term.getTermOBOId().getIdentifier(), term.getLabel());
            }
            return metadata;

        } catch (Exception e) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public Map getTermChildren(String termAccession, String ontologyId, int level) throws RemoteException {

        final Map metadata = new HashMap();
        try {
            Identifier identifier = new Identifier(termAccession, Identifier.IdentifierType.OBO);
            for (Term term : olsClient.getTermChildren(identifier, ontologyId, level)) {
                metadata.put(term.getTermOBOId().getIdentifier(), term.getLabel());
            }

            return metadata;

        } catch (Exception e) {
            throw new RemoteException("RemoteException while trying to connect to OLS.", e);
        }
    }

    public String getOntologyLoadDate(String ontologyId) throws RemoteException{
        try {

            return olsClient.getOntology(ontologyId).getLoadedDate();

        } catch (Exception e) {
            throw new RemoteException("We can't access the date of the last ontology update.", e);
        }
    }
}
