package psidev.psi.tools.ontology_manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.local.LocalOntology;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.impl.ols.OlsOntology;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Central access to configured OntologyAccess.
 *
 * @author Florian Reisinger
 * @Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.0
 */
public class OntologyManager extends OntologyManagerTemplate<OntologyTermI, OntologyAccess> {

    public static final Log log = LogFactory.getLog( OntologyManager.class );

    /**
     * Keywords that when specified in the cvSource's source gets converted in a specific implementation of OntologyAccess.
     */
    protected static final Map<String, Class> keyword2class = new HashMap<String, Class>();

    static {
        keyword2class.put( "ols", OlsOntology.class );
        keyword2class.put( "file", LocalOntology.class );
    }

    ////////////////////
    // Constructors

    /**
     * Create a new OntologyManager with no configuration (no associated ontologies).
     */
    public OntologyManager() {
        super();
    }

    /**
     * Creates a new OntologyManager managing the ontologies specified in the config file.
     * This config file has to be defined as per the following XSD:
     * <pre>http://www.psidev.info/files/validator/CvSourceList.xsd</pre>
     *
     * @param configFile configuration file for the manager.
     * @throws OntologyLoaderException if the config file could not be parsed or the loading of a ontology failed.
     */
    public OntologyManager( InputStream configFile ) throws OntologyLoaderException {
        super(configFile);
    }

    protected Class findLoader(String loaderClass, String lcLoaderClass) throws ClassNotFoundException {
        Class loader;
        if ( keyword2class.containsKey( lcLoaderClass ) ) {
            loader = keyword2class.get( lcLoaderClass );
            if ( log.isDebugEnabled() ) {
                log.debug( "the source '" + loaderClass + "' was converted to Class: " + loader );
            }
        } else {
            loader = Class.forName( loaderClass );
        }
        return loader;
    }
}