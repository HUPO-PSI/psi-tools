package psidev.psi.tools.cvrReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRules;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;


/**
 * Reader for the CV Mapping schema.
 *
 * @author Samuel Kerrien
 * @version $Id: CvMappingReader.java 669 2007-06-29 16:45:04 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 2007-06-23
 */
public class CvRuleReader {

    public static final Log log = LogFactory.getLog( CvRuleReader.class );

    ////////////////////////
    // Private methods

    private Unmarshaller getUnmarshaller() throws JAXBException {

        // create a JAXBContext capable of handling classes generated into the jaxb package
        JAXBContext jc = JAXBContext.newInstance( "psidev.psi.tools.cvrReader.mapping.jaxb" );

        // create and return Unmarshaller

        // TODO enable/disable validation use setSchema( s ) on Marshaller
        return jc.createUnmarshaller();
    }

    private CvMappingRules unmarshall( URL url ) throws JAXBException, FileNotFoundException {

        if ( url == null ) {
            throw new IllegalArgumentException( "You must give a non null URL." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvMappingRules ) u.unmarshal( url );
    }

    private CvMappingRules unmarshall( File file ) throws JAXBException, FileNotFoundException {

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
        return ( CvMappingRules ) u.unmarshal( new FileInputStream( file ) );
    }

    private CvMappingRules unmarshall( InputStream is ) throws JAXBException {

        if ( is == null ) {
            throw new IllegalArgumentException( "You must give a non null input stream." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvMappingRules ) u.unmarshal( is );
    }

    private CvMappingRules unmarshall( String s ) throws JAXBException {

        if ( s == null ) {
            throw new IllegalArgumentException( "You must give a non null String." );
        }

        // create an Unmarshaller
        Unmarshaller u = getUnmarshaller();

        // unmarshal an entrySet instance document into a tree of Java content objects composed of classes from the jaxb package.
        return ( CvMappingRules ) u.unmarshal( new StringReader( s ) );
    }

    //////////////////////////
    // Public methods

    public CvMappingRules read( String s ) throws CvRuleReaderException {
        try {
            return unmarshall( s );
        } catch ( JAXBException e ) {
            throw new CvRuleReaderException( e );
        }
    }

    public CvMappingRules read( File file ) throws CvRuleReaderException {
        try {
            return unmarshall( file );
        } catch ( JAXBException e ) {
            throw new CvRuleReaderException( e );
        } catch ( FileNotFoundException e ) {
            throw new CvRuleReaderException( e );
        }
    }

    public CvMappingRules read( InputStream is ) throws CvRuleReaderException {
        try {
            return unmarshall( is );
        } catch ( JAXBException e ) {
            throw new CvRuleReaderException( e );
        }
    }

    public CvMappingRules read( URL url ) throws CvRuleReaderException {
        try {
            return unmarshall( url );
        } catch ( JAXBException e ) {
            throw new CvRuleReaderException( e );
        } catch ( FileNotFoundException e ) {
            throw new CvRuleReaderException( e );
        }
    }
}
