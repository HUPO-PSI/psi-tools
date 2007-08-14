package psidev.psi.tools.ontology_manager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

/**
 * Author: Florian Reisinger
 * Date: 02-Aug-2007
 */
public class OntologyManager {

    Map<String, OntologyAccess> ontologies;

    ////////////////////
    // Constructor

    public OntologyManager() {
        ontologies = new HashMap<String, OntologyAccess>();
    }

    public OntologyManager( InputStream configFile ) {
        ontologies = new HashMap<String, OntologyAccess>();
        loadOntologies( configFile );
    }

    ////////////////////
    // Getter & Setter

    public void setOntology( String ontologyID, OntologyAccess ontology  ) {
        if ( ontologies.containsKey(ontologyID) ) {
            // ToDo: Warning or Overwrite?
            System.out.println("ERROR: Ontology with the ID '" + ontologyID + "' already exists.");
        } else {
            ontologies.put(ontologyID, ontology);
        }
    }


    ////////////////////
    // Methods

    private void loadOntologies(InputStream configFile) {
        // parse XML
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse( configFile );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // search the document for the needed information
        NodeList ontos = document.getElementsByTagName( "CVSource" );
        for ( int i = 0; i < ontos.getLength(); i++ ) {
//            System.out.println("_______________________________________");
            String loaderClass = ( (Element) ontos.item( i ) ).getAttribute( "loader" );
//            System.out.println( "loader: " + loaderClass );
            String ontologyID = ( (Element) ontos.item( i ) ).getAttribute( "cvIdentifier" );
//            System.out.println( "ontologyID: " + ontologyID );
            String format = ( (Element) ontos.item( i ) ).getAttribute( "cvFormat" );
//            System.out.println( "cvFormat: " + format );
            String version = ( (Element) ontos.item( i ) ).getAttribute( "version" );
//            System.out.println( "version: " + version );
            String name = ( (Element) ontos.item( i ) ).getAttribute( "name" );
//            System.out.println( "cv name: " + name );
            String loc = ( (Element) ontos.item( i ) ).getAttribute( "uri" );
//            System.out.println( "location: " + loc );

            
            URI uri = null;
            try {
                uri = new URI(loc);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            Class loader;
            try {
                loader = Class.forName( loaderClass );
                Constructor c = loader.getConstructor();
                OntologyAccess oa = ( OntologyAccess ) c.newInstance();
                oa.loadOntology(ontologyID, name, version, format, uri);
                ontologies.put(ontologyID, oa);
            } catch (Exception e) {
                //ToDo: Exception handling
                e.printStackTrace();
            }

        }


    }

    // ToDo: add isValidOntologyAccession( String acc )
    // otherwise: call getValidIDs(ontologyID, acc, false, true) should return only the acc


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

    public boolean containsKey( String ontologyID ) {
        if ( ontologies.containsKey( ontologyID )) {
            return true;
        }
        return false;
    }

    public boolean isObsoleteID( String ontologyID, String queryTerm ) {
        if ( ontologies.containsKey(ontologyID) ) {
            return ontologies.get(ontologyID).isObsoleteID( queryTerm );
        } else {
            throw new IllegalArgumentException("No ontology with the ID '" + ontologyID + "' exists.");
        }
    }
    
    // quick testing
    public static void main(String[] args){
        InputStream is = OntologyManager.class.getClassLoader().getResourceAsStream("ontologies.xml");
        if ( is == null ) {
            System.out.println("ERROR: OntologyManager config file not found.");
        } else {
            OntologyManager om = new OntologyManager(is);
            Set<String> result = om.getValidIDs( "MI", "MI:0300", true, false );
            for (String s : result) {
                System.out.println("term: " + s);
            }
        }
    }

}
