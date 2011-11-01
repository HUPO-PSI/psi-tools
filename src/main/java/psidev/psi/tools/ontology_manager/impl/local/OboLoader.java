/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;
import uk.ac.ebi.ook.loader.parser.OBOFormatParser;

import java.io.File;

/**
 * Wrapper class that hides the way OLS handles OBO files.
 *
 * @author Samuel Kerrien
 * @version $Id: OboLoader.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>30-Sep-2005</pre>
 */
public class OboLoader extends AbstractOboLoader<OntologyTermI, Ontology> {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( OboLoader.class );

    public OboLoader( File ontologyDirectory ) {
        super(ontologyDirectory);
    }

    /////////////////////////////
    // AbstractLoader's methods

    protected void configure() {
        /**
         * ensure we get the right logger
         */
        logger = Logger.getLogger( OboLoader.class );

        parser = new OBOFormatParser();
        ONTOLOGY_DEFINITION = "PSI MI";
        FULL_NAME = "PSI Molecular Interactions";
        SHORT_NAME = "PSI-MI";
    }

    @Override
    protected Ontology createNewOntology() {
        return new OntologyImpl();
    }

    @Override
    protected OntologyTermI createNewOntologyTerm(String identifier, String name) {
        return new OntologyTermImpl( identifier, name );
    }
}