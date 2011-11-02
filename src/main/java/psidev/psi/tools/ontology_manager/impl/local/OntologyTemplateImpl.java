package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.util.*;

/**
 * Implementation of OntologyTemplate
 *
 * NOTE : OntologyImpl was the original class but did not offer flexibility for using different extensions of OntologyTermI.
 * That is why this template abstract class has been created.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public abstract class OntologyTemplateImpl<T extends OntologyTermI> implements OntologyTemplate<T>{

    public static final Log log = LogFactory.getLog(OntologyImpl.class);

    ///////////////////////////////
    // Instance variables

    /**
     * Pool of all term contained in that ontology.
     */
    protected Collection<T> ontologyTerms = new ArrayList<T>( 1024 );

    // TODO introduce an interface for querying/updating the relationship
    // TODO replace the hashmap by a Lucene index -> using a different interface !!

    /**
     * Represent the relationship: child -> parents.
     */
    protected final Map<T, Set<T>> parents = new HashMap<T, Set<T>>();

    /**
     * Represent the relationship: parent -> children.
     */
    protected final Map<T, Set<T>> children = new HashMap<T, Set<T>>();

    /**
     * Mapping of all OboTerm by their ID.
     */
    protected Map<String, T> id2ontologyTerm = new HashMap<String, T>( 1024 );

    /**
     * Collection of root terms of that ontology. A root term is defined as follow: term having no parent.
     */
    protected Collection<T> roots = null;

    /**
     * List of all obsolete term found while loading the ontology.
     */
    protected Collection<T> obsoleteTerms = new ArrayList<T>();

    /////////////////////////////
    // Public methods

    /**
     * Add a new Term in the pool. It will be indexed by its ID.
     *
     * @param term the OntologyTerm to add in that Ontology.
     */
    public void addTerm( T term ) {

        ontologyTerms.add( term );
        String id = term.getTermAccession();
        if ( id2ontologyTerm.containsKey( id ) ) {
            OntologyTermI old = id2ontologyTerm.get( id );
            if( log.isWarnEnabled() ) {
                log.error( "WARNING: 2 Objects have the same ID (" + id + "), the old one is being replaced. old: " + old.getPreferredName() + " new: " + term.getPreferredName() );
            }
        }

        id2ontologyTerm.put( id, term );

        flushRootsCache();
    }

    /**
     * Create a relashionship parent to child between two OntologyTerm.
     *
     * @param parentId The parent term.
     * @param childId  The child term.
     */
    public void addLink( String parentId, String childId ) {

        T child = id2ontologyTerm.get( childId );
        T parent = id2ontologyTerm.get( parentId );

        if ( child == null ) {
            throw new NullPointerException( "You must give a non null child" );
        }

        if ( parent == null ) {
            throw new NullPointerException( "You must give a non null parent" );
        }

        if ( !children.containsKey( parent ) ) {
            children.put( parent, new HashSet<T>() );
        }

        if ( !parents.containsKey( child ) ) {
            parents.put( child, new HashSet<T>() );
        }

        children.get( parent ).add( child );
        parents.get( child ).add( parent );

        flushRootsCache();
    }

    /**
     * Remove the Root cache from memory.<br/> That method should be called every time the collection of OntologyTerm is
     * altered.
     */
    protected void flushRootsCache() {
        if ( roots != null ) {
            // flush roots cache
            roots.clear();
            roots = null;
        }
    }

    /**
     * Answer the question: 'Has that ontology any term loaded ?'.
     *
     * @return true is there are any terms loaded, false otherwise.
     */
    public boolean hasTerms() {
        return ontologyTerms.isEmpty();
    }

    /**
     * Search for a OboTerm by its ID.
     *
     * @param id the identifier of the OntologyTerm we are looking for.
     * @return a OntologyTerm or null if not found.
     */
    public T search( String id ) {
        return id2ontologyTerm.get( id );
    }

    public boolean hasParent( T term ) {
        return parents.containsKey( term );
    }

    /**
     * Get the Root terms of the ontology. The way to get it is as follow: pick a term at random, and go to his highest
     * parent.
     *
     * @return a collection of Root term.
     */
    public Collection<T> getRoots() {

        if ( roots != null ) {
            return roots;
        }

        // it wasn't precalculated, then do it here...
        roots = new HashSet<T>();

        for ( Iterator<T> iterator = ontologyTerms.iterator(); iterator.hasNext(); ) {
            T ontologyTerm = iterator.next();

            if ( !hasParent( ontologyTerm ) ) {
                roots.add( ontologyTerm );
            }
        }

        if ( roots.isEmpty() ) {
            return Collections.EMPTY_LIST;
        }

        return roots;
    }

    /**
     * Get all OboTerm.
     *
     * @return all Ontology term found in the Ontology.
     */
    public Collection<T> getOntologyTerms() {
        return Collections.unmodifiableCollection( ontologyTerms );
    }

    public void addObsoleteTerm( T term ) {
        if ( term == null ) {
            throw new IllegalArgumentException( "You must give a non null term" );
        }
        if ( log.isDebugEnabled() ) {
            log.debug( "Adding obsolete term: " + term.getTermAccession() + " " + term.getPreferredName() );
        }
        obsoleteTerms.add( term );
    }

    public boolean isObsoleteTerm( T term ) {
        return obsoleteTerms.contains( term );
    }

    /**
     * Go through the list of all CV Term and select those that are obsolete.
     *
     * @return a non null Collection of obsolete term.
     */
    public Collection<T> getObsoleteTerms() {
        return Collections.unmodifiableCollection( obsoleteTerms );
    }

    public Set<T> getDirectParents( T term ) {
        final Set<T> directParents = parents.get( term );
        if ( directParents == null ) {
            return Collections.EMPTY_SET;
        } else {
            return directParents;
        }
    }

    public Set<T> getDirectChildren( T term ) {
        final Set<T> directChildren = children.get( term );
        if ( directChildren == null ) {
            return Collections.EMPTY_SET;
        } else {
            return directChildren;
        }
    }

    public Set<T> getAllParents( T term ) {
        Set<T> parents = new HashSet<T>();
        getAllParents( term, parents );
        return parents;
    }

    protected void getAllParents( T term, Set<T> parents ) {
        final Collection<T> directParents = getDirectParents( term );
        parents.addAll( directParents );
        for ( T parent : directParents ) {
            getAllParents( parent, parents );
        }
    }

    public Set<T> getAllChildren( T term ) {
        Set<T> children = new HashSet<T>();
        getAllChildren( term, children );
        return children;
    }

    protected void getAllChildren( T term, Set<T> children ) {
         getAllChildren( "", term, children, new HashSet(512) );
    }

    protected void getAllChildren( String prefix, T term, Set<T> children, Set<String> traversedChildren ) {
        if( traversedChildren.contains( term.getTermAccession() ) ) {
//            System.out.println( prefix.replaceAll( " ", "#" )+" > "+ term.getTermAccession() +" / "+ term.getPreferredName() +" )" );
            return;
        } else {
//            System.out.println( prefix +" > "+ term.getTermAccession() +" / "+ term.getPreferredName() +" )" );
        }
        final Collection<T> directChildren = getDirectChildren( term );
        traversedChildren.add( term.getTermAccession() );
        children.addAll( directChildren );
        for ( T child : directChildren ) {
            getAllChildren( prefix+"     ", child, children, traversedChildren );
        }
    }

    /////////////////////////////////
    // Utility - Display methods

    public void print() {
        log.info( ontologyTerms.size() + " terms to display." );
        final Collection<T> roots = getRoots();
        if ( log.isDebugEnabled() ) {
            log.info( this.roots.size() + " root(s) found." );
        }
        for ( T root : roots ) {
            print( root );
        }
    }

    private void print( T term, String indent ) {

        log.info( indent + term.getTermAccession() + "   " + term.getPreferredName() );
        for ( T child : getDirectChildren( term ) ) {
            print( child, indent + "  " );
        }
    }

    public void print( T term ) {
        print( term, "" );
    }
}
