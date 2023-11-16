package psidev.psi.tools.objectRuleReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import psidev.psi.tools.objectRuleReader.mapping.jaxb.ObjectRuleList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;


/**
 * Reader for the Object Rule schema.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class ObjectRuleReader {

    public static final Log log = LogFactory.getLog( ObjectRuleReader.class );

    ////////////////////////
    // Private methods

    private Unmarshaller getUnmarshaller() throws JAXBException {

        // create a JAXBContext capable of handling classes generated into the jaxb package
        JAXBContext jc = JAXBContext.newInstance( "psidev.psi.tools.objectRuleReader.mapping.jaxb" );

        // create and return Unmarshaller
        final Unmarshaller unmarshaller = jc.createUnmarshaller();

        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

        final URL url = this.getClass().getClassLoader().getResource("object-rule.xsd");
        if (url == null) {
            throw new IllegalStateException("Could not find object-rule.xsd, the jar file seems corrupted!");
        }

        Schema schema;
        try {
            schema = sf.newSchema(url);
        } catch ( SAXException e ) {
            throw new JAXBException( "Error creating schema instance from schema " + url, e );
        }

        unmarshaller.setSchema(schema);

        return unmarshaller;
    }

    private ObjectRuleList unmarshall( URL url ) throws JAXBException, FileNotFoundException {

        if ( url == null ) {
            throw new IllegalArgumentException( "You must give a non null URL." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( ObjectRuleList ) u.unmarshal( url );
    }

    private ObjectRuleList unmarshall( File file ) throws JAXBException, FileNotFoundException {

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
        return ( ObjectRuleList ) u.unmarshal( new FileInputStream( file ) );
    }

    private ObjectRuleList unmarshall( InputStream is ) throws JAXBException {

        if ( is == null ) {
            throw new IllegalArgumentException( "You must give a non null input stream." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( ObjectRuleList ) u.unmarshal( is );
    }

    private ObjectRuleList unmarshall( String s ) throws JAXBException {

        if ( s == null ) {
            throw new IllegalArgumentException( "You must give a non null String." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( ObjectRuleList ) u.unmarshal( new StringReader( s ) );
    }

    //////////////////////////
    // Public methods

    public ObjectRuleList read( String s ) throws ObjectRuleReaderException {
        try {
            return unmarshall( s );
        } catch ( JAXBException e ) {
            throw new ObjectRuleReaderException( e );
        }
    }

    public ObjectRuleList read( File file ) throws ObjectRuleReaderException {
        try {
            return unmarshall( file );
        } catch ( JAXBException e ) {
            throw new ObjectRuleReaderException( e );
        } catch ( FileNotFoundException e ) {
            throw new ObjectRuleReaderException( e );
        }
    }

    public ObjectRuleList read( InputStream is ) throws ObjectRuleReaderException {
        try {
            return unmarshall( is );
        } catch ( JAXBException e ) {
            throw new ObjectRuleReaderException( e );
        }
    }

    public ObjectRuleList read( URL url ) throws ObjectRuleReaderException {
        try {
            return unmarshall( url );
        } catch ( JAXBException e ) {
            throw new ObjectRuleReaderException( e );
        } catch ( FileNotFoundException e ) {
            throw new ObjectRuleReaderException( e );
        }
    }
}
