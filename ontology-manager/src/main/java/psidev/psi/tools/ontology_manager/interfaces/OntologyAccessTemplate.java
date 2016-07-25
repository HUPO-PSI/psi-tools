package psidev.psi.tools.ontology_manager.interfaces;

import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;

import java.io.File;
import java.net.URI;
import java.util.Set;

/**
 * A template for ontology access
 *
 * NOTE : OntologyAccess was the original interface. This interface had been created to add more flexibility when we want to use
 * extensions of OntologyTermI
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public interface OntologyAccessTemplate<T extends OntologyTermI> {

    /**
     * Load ontology data.
     *
     * @param ontologyID the identifier of this ontology.
     * @param name       the name of the ontology
     * @param version    the version of the ontology
     * @param format     the format of the ontology
     * @param uri        the URI of the ontology
     * @throws OntologyLoaderException
     */
    void loadOntology( String ontologyID, String name, String version, String format, URI uri ) throws OntologyLoaderException;

    /**
     * For the OntologyAccess implementation that supports it, sets a directory that can be used as cache for
     * downloaded ontology files.
     *
     * @param directory location of the cache.
     */
    void setOntologyDirectory( File directory );

    /**
     * This method builds a set of all allowed terms based on the specified parameters.
     *
     * @param accession     the accession number of the term wanted.
     * @param allowChildren whether child terms are allowed.
     * @param useTerm       whether to include the specified term ID in the set of allowed IDs.
     * @return a non null set of allowed ontology terms for the specified parameters.
     */
    public Set<T> getValidTerms( String accession, boolean allowChildren, boolean useTerm );

    /**
     * Search a term by accession number.
     *
     * @param accession the accession to be searched for.
     * @return an ontology term or null if not found.
     */
    public T getTermForAccession( String accession );

    /**
     * Is the given ontology term obsolete.
     *
     * @param term the term of interrest.
     * @return true of the term is obsolete, false otherwise.
     */
    public boolean isObsolete( T term );

    /**
     * Provides the direct parents of the given terms in a non null set.
     *
     * @param term the term for which we want the direct parents.
     * @return a non null set of ontology terms.
     */
    public Set<T> getDirectParents( T term );

    /**
     * Provides the direct children of the given terms in a non null set.
     *
     * @param term the term for which we want the direct children.
     * @return a non null set of ontology terms.
     */
    public Set<T> getDirectChildren( T term );

    /**
     * Provides all parents of the given terms in a non null set.
     *
     * @param term the term for which we want all parents.
     * @return a non null set of ontology terms.
     */
    public Set<T> getAllParents( T term );

    /**
     * Provides all children of the given terms in a non null set.
     *
     * @param term the term for which we want all children.
     * @return a non null set of ontology terms.
     */
    public Set<T> getAllChildren( T term );

    /**
     *
     * @return false if a new update of the ontology has been done recently and the date of the last ontology upload is before the date of the last ontology update
     * @throws psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException
     */
    public boolean isOntologyUpToDate() throws OntologyLoaderException;

    /**
     * Note: Implementations of OntologyAccess should by default handle synonyms, so this method
     * should return 'true' on freshly instantiated OntologyAccess implementations.
     *
     * @return a boolean denoting whether the OntologyAccess handles synonyms for ontology terms.
     */
    public boolean isUseTermSynonyms();

    /**
     * Set this flag to 'false' if the OntologyAccess does not need
     * to handle synonyms for ontology terms.
     *
     * @param useTermSynonyms flag to toggle handling of ontology term synonyms.
     */
    public void setUseTermSynonyms(boolean useTermSynonyms);
}
