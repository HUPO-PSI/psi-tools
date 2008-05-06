package psidev.psi.tools.ontologyConfigReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSourceList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;


/**
 * Reader for the Ontology Configuration schema.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class OntologyConfigReader {

    public static final Log log = LogFactory.getLog( OntologyConfigReader.class );

    ////////////////////////
    // Private methods

    private Unmarshaller getUnmarshaller() throws JAXBException {

        // create a JAXBContext capable of handling classes generated into the jaxb package
        JAXBContext jc = JAXBContext.newInstance( "psidev.psi.tools.ontologyCfgReader.mapping.jaxb" );

        // create and return Unmarshaller

        // TODO enable/disable validation use setSchema( s ) on Marshaller
        return jc.createUnmarshaller();
    }

    private CvSourceList unmarshall( URL url ) throws JAXBException, FileNotFoundException {

        if ( url == null ) {
            throw new IllegalArgumentException( "You must give a non null URL." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvSourceList ) u.unmarshal( url );
    }

    private CvSourceList unmarshall( File file ) throws JAXBException, FileNotFoundException {

        if ( file == null ) {
            throw new IllegalArgumentException( "You must give a non null file." );
        }

        if ( !file.exists() ) {
            throw new IllegalArgumentException( "You must give an existing file." );
        }

        if ( !file.canRead() ) {
            throw new IllegalArgumentException( "You must give a readable file." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvSourceList ) u.unmarshal( new FileInputStream( file ) );
    }

    private CvSourceList unmarshall( InputStream is ) throws JAXBException {

        if ( is == null ) {
            throw new IllegalArgumentException( "You must give a non null input stream." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvSourceList ) u.unmarshal( is );
    }

    private CvSourceList unmarshall( String s ) throws JAXBException {

        if ( s == null ) {
            throw new IllegalArgumentException( "You must give a non null String." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvSourceList ) u.unmarshal( new StringReader( s ) );
    }

    //////////////////////////
    // Public methods

    public CvSourceList read( String s ) throws OntologyConfigReaderException {
        try {
            return unmarshall( s );
        } catch ( JAXBException e ) {
            throw new OntologyConfigReaderException( e );
        }
    }

    public CvSourceList read( File file ) throws OntologyConfigReaderException {
        try {
            return unmarshall( file );
        } catch ( JAXBException e ) {
            throw new OntologyConfigReaderException( e );
        } catch ( FileNotFoundException e ) {
            throw new OntologyConfigReaderException( e );
        }
    }

    public CvSourceList read( InputStream is ) throws OntologyConfigReaderException {
        try {
            return unmarshall( is );
        } catch ( JAXBException e ) {
            throw new OntologyConfigReaderException( e );
        }
    }

    public CvSourceList read( URL url ) throws OntologyConfigReaderException {
        try {
            return unmarshall( url );
        } catch ( JAXBException e ) {
            throw new OntologyConfigReaderException( e );
        } catch ( FileNotFoundException e ) {
            throw new OntologyConfigReaderException( e );
        }
    }
}
