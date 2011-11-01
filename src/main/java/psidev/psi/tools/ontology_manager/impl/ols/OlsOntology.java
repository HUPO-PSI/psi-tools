package psidev.psi.tools.ontology_manager.impl.ols;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

/**
 * Author: Florian Reisinger
 * Date: 07-Aug-2007
 */
public class OlsOntology extends AbstractOlsOntology<OntologyTermI> implements OntologyAccess {

    public static final Log log = LogFactory.getLog( OlsOntology.class );

    public OlsOntology() throws OntologyLoaderException {
        super();
    }

    @Override
    protected OntologyTermI createNewOntologyTerm(String identifier, String name) {
        return new OntologyTermImpl( identifier, name );
    }
}
