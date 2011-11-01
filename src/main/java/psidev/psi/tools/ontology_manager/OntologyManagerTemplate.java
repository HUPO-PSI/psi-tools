package psidev.psi.tools.ontology_manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSource;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSourceList;
import psidev.psi.tools.ontologyConfigReader.OntologyConfigReader;
import psidev.psi.tools.ontologyConfigReader.OntologyConfigReaderException;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccessTemplate;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract ontologyManager
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public abstract class OntologyManagerTemplate<T extends OntologyTermI, A extends OntologyAccessTemplate<T>> {

    public static final Log log = LogFactory.getLog(OntologyManager.class);

    /**
     * The Map that holds the Ontologies.
     * The key is the ontology ID and the value is a ontology inplementing the OntologyAccess interface.
     */
    protected Map<String, A> ontologies;

    public static final String CLASSPATH_PREFIX = "classpath:";

    ////////////////////
    // Constructors

    /**
     * Create a new OntologyManager with no configuration (no associated ontologies).
     */
    public OntologyManagerTemplate() {
        ontologies = new HashMap<String, A>();
        if ( log.isDebugEnabled() ) log.info( "Created new unconfigured OntologyManager." );
    }

    /**
     * Creates a new OntologyManager managing the ontologies specified in the config file.
     * This config file has to be defined as per the following XSD:
     * <pre>http://www.psidev.info/files/validator/CvSourceList.xsd</pre>
     *
     * @param configFile configuration file for the manager.
     * @throws psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException if the config file could not be parsed or the loading of a ontology failed.
     */
    public OntologyManagerTemplate( InputStream configFile ) throws OntologyLoaderException {
        ontologies = new HashMap<String, A>();
        loadOntologies( configFile );
        if ( log.isDebugEnabled() ) log.debug( "Successfully created and configured new OntologyManager." );
    }

    ////////////////////
    // public methods

    /**
     * This method will manually add a ontology to the manager.
     *
     * @param ontologyID the ID under which the ontology will be accessible (e.g. 'GO' for "Gene Ontology")
     * @param ontology   the ontology to manage.
     * @return the previous value associated with the specified ontologyID, or null if there was no mapping for the specified ontologyID.
     * @see java.util.HashMap#put(Object, Object)
     */
    public A putOntology( String ontologyID, A ontology ) {
        if ( ontologies.containsKey( ontologyID ) ) {
            if ( log.isWarnEnabled() )log.warn( "Ontology with the ID '" + ontologyID + "' already exists. Overwriting!" );
        }
        return ontologies.put( ontologyID, ontology );
    }

    /**
     * Returns the ontologyIDs of all managed ontologies.
     *
     * @return a Collection of all ontologyIDs.
     * @see java.util.HashMap#keySet()
     */
    public Set<String> getOntologyIDs() {
        return ontologies.keySet();
    }

    /**
     * Returns the ontology for the specified ID.
     *
     * @param ontologyID the ID of a managed ontology.
     * @return the ontology or null if no ontology was found for the specified ID.
     * @see java.util.HashMap#get(Object)
     */
    public A getOntologyAccess( String ontologyID ) {
        return ontologies.get( ontologyID );
    }

    public void setOntologyDirectory( File ontologyDirectory ) {
        OntologyManagerContext.getInstance().setOntologyDirectory( ontologyDirectory );
    }

    /**
     * This method checks if a ontology for the specified ID is stored in the manager
     *
     * @param ontologyID the ID of the ontology to check.
     * @return true if the manager's ontolgoy map contains a ontology for the specified ID
     * @see java.util.HashMap#containsKey(Object)
     */
    public boolean containsOntology( String ontologyID ) {
        return ontologies.containsKey( ontologyID );
    }

    /**
     * Method to load the ontologies from the configuration file.
     *
     * @param configFile a InputStream of the config file that lists the ontologies to manage.
     * @throws OntologyLoaderException if loading failed.
     */
    public void loadOntologies( InputStream configFile ) throws OntologyLoaderException {

        OntologyConfigReader ocr = new OntologyConfigReader();
        final CvSourceList cvSourceList;
        try {
            cvSourceList = ocr.read( configFile );
        } catch ( OntologyConfigReaderException e ) {
            throw new OntologyLoaderException( "Error while reading ontology config file", e );
        }

        if ( cvSourceList != null ) {
            for ( CvSource cvSource : cvSourceList.getCvSource() ) {

                String sourceUri = cvSource.getUri();
                final String id = cvSource.getIdentifier();
                final String name = cvSource.getName();
                final String version = cvSource.getVersion();
                final String format = cvSource.getFormat();
                final String loaderClass = cvSource.getSource();

                URI uri;
                try {

                    if ( sourceUri != null && sourceUri.toLowerCase().startsWith( CLASSPATH_PREFIX ) ) {
                        sourceUri = sourceUri.substring( CLASSPATH_PREFIX.length() );
                        if ( log.isDebugEnabled() ) {
                            log.debug( "Loading ontology from classpath: " + sourceUri );
                        }
                        final URL url = OntologyManager.class.getClassLoader().getResource( sourceUri );
                        if ( url == null ) {
                            throw new OntologyLoaderException( "Unable to load from classpath: " + sourceUri );
                        }
                        uri = url.toURI();
                        if ( log.isDebugEnabled() ) {
                            log.debug( "URI=" + uri.toASCIIString() );
                        }

                    } else {
                        uri = new URI( sourceUri );
                    }

                } catch ( URISyntaxException e ) {
                    throw new IllegalArgumentException( "The specified uri '" + sourceUri + "' " +
                            "for ontology '" + id + "' has an invalid syntax.", e );
                }

                if ( log.isInfoEnabled() ) {
                    log.info( "Loading ontology: name=" + name + ", ID= " + id + ", format=" + format
                            + ", version=" + version + ", uri=" + uri + " using source: " + loaderClass );
                }

                Class loader;
                try {
                    final String lcLoaderClass = loaderClass.toLowerCase();
                    loader = findLoader(loaderClass, lcLoaderClass);
                    Constructor c = loader.getConstructor();
                    A oa = ( A ) c.newInstance();
                    oa.setOntologyDirectory( OntologyManagerContext.getInstance().getOntologyDirectory() );
                    oa.loadOntology( id, name, version, format, uri );
                    ontologies.put( id, oa );
                } catch ( Exception e ) {
                    throw new OntologyLoaderException( "Failed loading ontology source: " + loaderClass, e );
                }
            }
        }
    }

    protected abstract Class findLoader(String loaderClass, String lcLoaderClass) throws ClassNotFoundException ;

    /**
     *
     * @return false if one of the OntologyAccess instances doesn't have an up-to-date ontology uploaded.
     * @throws OntologyLoaderException
     */
    public boolean isUpToDate() throws OntologyLoaderException {

        for (Map.Entry<String, A> entry : this.ontologies.entrySet()){
            if (entry.getValue() != null){
                if (!entry.getValue().isOntologyUpToDate()){
                    return false;
                }
            }
        }

        return true;
    }
}
