/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.validator.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;

/**
 * A simple Sax validator that interface with the PSI validator.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class SaxValidatorHandler {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( SaxValidatorHandler.class );

    private static class MyErrorHandler extends DefaultHandler {

        private PrintStream out;

        boolean warning = false;
        boolean error = false;
        boolean fatal = false;

        SaxReport report = new SaxReport();

        //////////////////////
        // Constructors

        public MyErrorHandler( PrintStream out ) {

            if ( out == null ) {
                throw new NullPointerException( "You must give a valid PrintStream" );
            }
            this.out = out;
        }

        public MyErrorHandler( SaxReport report ) {
            if ( report == null ) {
                throw new IllegalArgumentException( "Report must not be null." );
            }

            this.report = report;
        }

        public MyErrorHandler() {
            this.out = System.out;
        }

        //////////////////
        // Getters

        public boolean hasWarning() {
            return warning;
        }

        public boolean hasError() {
            return error;
        }

        public boolean hasFatal() {
            return fatal;
        }

        ///////////////
        // Overriding

        public void warning( SAXParseException e ) throws SAXException {
            warning = true;
            printInfo( e );
        }

        public void error( SAXParseException e ) throws SAXException {
            error = true;
            printInfo( e );
        }

        public void fatalError( SAXParseException e ) throws SAXException {
            fatal = true;
            printInfo( e );
        }

        private void printInfo( SAXParseException e ) {

            if ( report != null ) {

                SaxMessage message = new SaxMessage( e );
                report.addMessage( message );

            } else {
                StringBuilder sb = new StringBuilder( 150 );
                sb.append( "   Public ID: " + e.getPublicId() ).append( "\n" );
                sb.append( "   System ID: " + e.getSystemId() ).append( "\n" );
                sb.append( "   Line number: " + e.getLineNumber() ).append( "\n" );
                sb.append( "   Column number: " + e.getColumnNumber() ).append( "\n" );
                sb.append( "   Message: " + e.getMessage() );

                out.print( sb.toString() );
                log.info( sb.toString() );
            }
        }
    }

    ///////////////////////////
    // User interface

    public static SaxReport validate( InputStream is ) throws IOException, SAXException {
        InputSource inputSource = new InputSource( is );
        return validate( inputSource );
    }

    public static SaxReport validate( File file ) throws IOException, SAXException {
        String filename = file.getAbsolutePath();
        InputSource inputSource = new InputSource( new FileReader( filename ) );
        return validate( inputSource );
    }

    public static SaxReport validate( String xmlString ) throws IOException, SAXException {
        InputSource inputSource = new InputSource( new StringReader( xmlString ) );
        return validate( inputSource );
    }

    public static SaxReport validate( InputSource inputSource ) throws SAXException, IOException {

        String parserClass = SAXParser.class.getName();
        String validationFeature = "http://xml.org/sax/features/validation";
        String schemaFeature = "http://apache.org/xml/features/validation/schema";

        SaxReport report = new SaxReport();
        MyErrorHandler handler = new MyErrorHandler( report );

        XMLReader r = XMLReaderFactory.createXMLReader( parserClass );
        r.setFeature( validationFeature, true );
        r.setFeature( schemaFeature, true );

        r.setErrorHandler( handler );
        r.parse( inputSource );

        if ( handler.hasError() || handler.hasFatal() || handler.hasWarning() ) {

            log.info( "SaxValidatorHandler.validate( is not valid )" );
            report.setValid( false );

            if ( handler.hasError() ) {
                log.info( "SAX Validator found at least 1 error." );
            }

            if ( handler.hasFatal() ) {
                log.info( "SAX Validator found at least 1 fatal error." );
            }

            if ( handler.hasWarning() ) {
                log.info( "SAX Validator found at least 1 warning." );
            }
        } else {
            log.info( "SaxValidatorHandler.validate( is valid )" );
            report.setValid( true );
        }

        return report;
    }
}