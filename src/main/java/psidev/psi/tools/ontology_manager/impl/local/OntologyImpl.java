package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

/**
 * Holder for an Ontology and provide basic search feature.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: OntologyImpl.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>04-Jan-2006</pre>
 */
public class OntologyImpl extends OntologyTemplateImpl<OntologyTermI> implements Ontology {

    public static final Log log = LogFactory.getLog( OntologyImpl.class );
}