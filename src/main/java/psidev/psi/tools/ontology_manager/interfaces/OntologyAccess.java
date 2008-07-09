package psidev.psi.tools.ontology_manager.interfaces;

import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;

import java.net.URI;
import java.util.Set;
import java.io.File;

/**
 * Author: florian
 * Date: 07-Aug-2007
 * Time: 14:10:02
 */
public interface OntologyAccess {

    void loadOntology( String ontologyID, String name, String version, String format, URI uri) throws OntologyLoaderException;

    void setOntologyDirectory( File directory );

    public Set<OntologyTermI> getValidTerms( String accession, boolean allowChildren, boolean useTerm );

    public OntologyTermI getTermForAccession( String accession );

    public boolean isObsolete( OntologyTermI term );

    public Set<OntologyTermI> getDirectParents( OntologyTermI term );

    public Set<OntologyTermI> getDirectChildren( OntologyTermI term );

    public Set<OntologyTermI> getAllParents( OntologyTermI term );

    public Set<OntologyTermI> getAllChildren( OntologyTermI term );
}