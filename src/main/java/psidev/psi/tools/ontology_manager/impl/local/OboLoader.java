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
import uk.ac.ebi.ols.loader.parser.OBO2FormatParser;
import uk.ac.ebi.ols.model.interfaces.Term;

import java.io.File;

/**
 * Wrapper class that hides the way OLS handles OBO files.
 * <p/>
 * NOTE : the OboLoader class is now extending AbstractOboLoader. Nothing has changed in the methods of this class which is still using OntologyTermI and Ontology.
 * As we needed some flexibility when using different extension of the basic OntologyTermI, we created a template for OboLoader and for retrocompatibility,
 * this class has been kept and extends AbstractOboLoader<OntologyTermI, Ontology>.
 *
 * @author Samuel Kerrien
 * @version $Id: OboLoader.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>30-Sep-2005</pre>
 */
public class OboLoader extends AbstractOboLoader<OntologyTermI, Ontology> {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(OboLoader.class);

    public OboLoader(File ontologyDirectory) {
        super(ontologyDirectory);
    }

    /////////////////////////////
    // AbstractLoader's methods

    protected void configure(String filePath) {
        /**
         * ensure we get the right logger
         */
        logger = Logger.getLogger(OboLoader.class);

        try {
            parser = new OBO2FormatParser(filePath);
        } catch (Exception e) {
            logger.fatal("Parse failed: " + e.getMessage(), e);
        }

        ONTOLOGY_DEFINITION = "PSI MI";
        FULL_NAME = "PSI Molecular Interactions";
        SHORT_NAME = "PSI-MI";
    }

    @Override
    protected Ontology createNewOntology() {
        return new OntologyImpl();
    }

    @Override
    protected OntologyTermI createNewOntologyTerm(Term t) {
        return new OntologyTermImpl(t.getIdentifier(), t.getName());
    }
}