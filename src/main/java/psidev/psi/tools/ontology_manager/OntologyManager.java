package psidev.psi.tools.ontology_manager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.impl.local.LocalOntology;
import psidev.psi.tools.ontology_manager.impl.ols.OlsOntology;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontologyConfigReader.OntologyConfigReader;
import psidev.psi.tools.ontologyConfigReader.OntologyConfigReaderException;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSourceList;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Author: Florian Reisinger
 * Date: 02-Aug-2007
 */
public class OntologyManager {

    public static final Log log = LogFactory.getLog( OntologyManager.class );

    /**
     * The Map that holds the Ontologies.
     * The key is the ontology ID and the value is a ontology inplementing the OntologyAccess interface.
     */
    Map<String, OntologyAccess> ontologies;

    private File ontologyDirectory = null;
    ////////////////////
    // Constructors

    /**
     * Create a new OntologyManager with no configuration (no associated ontologies).
     */
    public OntologyManager() {
        ontologies = new HashMap<String, OntologyAccess>();
        log.info( "Created new unconfigured OntologyManager." );
    }

    /**
     * Creates a new OntologyManager managing the ontologies specified in the config file.
     * //ToDo: description of the config file
     * @param configFile configuration file for the manager.
     * @throws OntologyLoaderException if the config file could not be parsed or the loading of a ontology failed.
     */
    public OntologyManager( InputStream configFile ) throws OntologyLoaderException {
        ontologies = new HashMap<String, OntologyAccess>();
        loadOntologies( configFile );
        log.info( "Successfully created and configured new OntologyManager." );
    }

    ////////////////////
    // Getter & Setter

    /**
     * This method will manually add a ontology to the manager.
     * @param ontologyID the ID under which the ontology will be accessible (e.g. 'GO' for "Gene Ontology")
     * @param ontology the ontology to manage.
     * @return the previous value associated with the specified ontologyID, or null if there was no mapping for the specified ontologyID.
     * @see java.util.HashMap#put(Object, Object)
     */
    public OntologyAccess putOntology( String ontologyID, OntologyAccess ontology  ) {
        if ( ontologies.containsKey(ontologyID) ) {
            // overwrite but return the value that will be overwritten
            log.warn("Ontology with the ID '" + ontologyID + "' already exists. Overwriting!");
        }
        return ontologies.put(ontologyID, ontology);
    }

    /**
     * Returns the ontologyIDs of all managed ontologies.
     * @return a Collection of all ontologyIDs.
     * @see java.util.HashMap#keySet()
     */
    public Set<String> getOntologyIDs() {
        return ontologies.keySet();
    }

    /**
     * Returns the ontology for the specified ID.
     * @param ontologyID the ID of a managed ontology.
     * @return the ontology or null if no ontology was found for the specified ID.
     * @see java.util.HashMap#get(Object)
     */
    public OntologyAccess getOntologyAccess( String ontologyID ) {
        return ontologies.get( ontologyID );
    }

    public void setOntologyDirectory(File ontologyDirectory) {
        this.ontologyDirectory = ontologyDirectory;
    }

    ////////////////////
    // Utilities

    public static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Keywords that when specified in the cvSource's source gets converted in a specific implementation of OntologyAccess.
     */
    public static final Map<String, Class> keyword2class = new HashMap<String, Class>( );
    static {
        keyword2class.put( "ols", OlsOntology.class );
        keyword2class.put( "file", LocalOntology.class );
    }

    /**
     * Method to load the ontologies from the configuration file.
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

                log.info( "Loading ontology: name=" + name + ", ID= " + id + ", format=" + format
                          + ", version=" + version + ", uri=" + uri + " using source: " + loaderClass );
                Class loader;
                try {
                    final String lcLoaderClass = loaderClass.toLowerCase();
                    if( keyword2class.containsKey( lcLoaderClass ) ) {
                        loader = keyword2class.get( lcLoaderClass );
                        if ( log.isDebugEnabled() ) {
                            log.debug( "the source '"+ loaderClass +"' was converted to Class: " + loader );
                        }
                    } else {
                        loader = Class.forName( loaderClass );
                    }
                    Constructor c = loader.getConstructor();
                    OntologyAccess oa = ( OntologyAccess ) c.newInstance();
                    oa.setOntologyDirectory( ontologyDirectory );
                    oa.loadOntology( id, name, version, format, uri );
                    ontologies.put( id, oa );
                } catch ( Exception e ) {
                    throw new OntologyLoaderException( "Failed loading ontology source: " + loaderClass, e );
                }
            }
        }
    }

    ////////////////////
    // Methods

    /**
     * This method checks if a ontology for the specified ID is stored in the manager
     * @param ontologyID the ID of the ontology to check.
     * @return true if the manager's ontolgoy map contains a ontology for the specified ID
     * @see java.util.HashMap#containsKey(Object)
     */
    public boolean containsOntology( String ontologyID ) {
        return ontologies.containsKey( ontologyID );
    }

    /**
     * This method builds a set of all allowed term IDs based on the specified parameters.
     * A IllegalArgumentException is thrown if no ontology with the specified ontologyID exists.
     * ToDo: description
     * @param ontologyID the ontology to use.
     * @param queryTerm the ontology term ID to use.
     * @param allowChildren whether child terms are allowed.
     * @param useTerm whether to include the specified term ID in the set of allowed IDs.
     * @return a set of allowed ontology term IDs for the specified parameters..
     */
    public Set<String> getValidIDs( String ontologyID, String queryTerm, boolean allowChildren, boolean useTerm ) {
        Set<String> terms;

        if ( ontologies.containsKey(ontologyID) ) {
            OntologyAccess ontology = ontologies.get(ontologyID);
            terms = ontology.getValidIDs( queryTerm, allowChildren, useTerm );
        } else {
            throw new IllegalArgumentException("No ontology with the ID '" + ontologyID + "' exists.");
        }

        return terms;
    }

    /**
     * This method checks if a ontology term is obsolete.
     * A IllegalArgumentException is thrown if no ontology with the specified ontologyID exists.
     * A IllegalStateException may be thrown if this method is run on a term ID that does not
     * exist in the specified ontology.
     * @param ontologyID the ontology to look up.
     * @param termID the ontology term ID to check.
     * @return true if the term is obsolete, false if the term is not obsolete.
     */
    public boolean isObsoleteID( String ontologyID, String termID ) {
        if ( ontologies.containsKey(ontologyID) ) {
            return ontologies.get(ontologyID).isObsoleteID( termID );
        } else {
            throw new IllegalArgumentException("No ontology with the ID '" + ontologyID + "' exists.");
        }
    }

    /**
     * This method retrieves the term name for a specified term ID.
     * A IllegalArgumentException is thrown if no ontology with the specified ontologyID exists.
     * @param ontologyID the ontology to look up.
     * @param id the ontology term ID.
     * @return the name of the term with the specified ID.
     */
    public String getTermNameByID( String ontologyID, String id ) {
        if ( ontologies.containsKey(ontologyID) ) {
            return ontologies.get(ontologyID).getTermNameByID( id );
        } else {
            throw new IllegalArgumentException("No ontology with the ID '" + ontologyID + "' exists.");
        }
    }

    // ToDo: add isValidOntologyAccession( String acc )
    // otherwise: call getValidIDs(ontologyID, acc, false, true) should return only the acc
}
