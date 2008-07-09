package psidev.psi.tools.ontology_manager.impl;

import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

/**
 * Representation of a cv term.
 *
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.0
 * @version $Id$
 */
public class OntologyTermImpl implements OntologyTermI {

    private String acc;
    
    private String name;

    //////////////////////////
    // Constructors

    public OntologyTermImpl( String acc ) {
        setTermAccession( acc );
    }

    public OntologyTermImpl( String acc, String name ) {
        this( acc );
        this.name = name;
    }

    ///// ///// ///// ///// /////
    // Getter & Setter

    public void setTermAccession( String accession ) {
        if ( acc == null || acc.trim().length()==0) {
            throw new NullPointerException( "You must give a non null/empty term accession" );
        }
        acc = accession;
    }
    public String getTermAccession() {
        return acc;
    }

    public void setPreferredName( String preferredName ) {
        name = preferredName;
    }
    public String getPreferredName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "OntologyTermImpl" );
        sb.append( "{acc='" ).append( acc ).append( '\'' );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        OntologyTermImpl that = ( OntologyTermImpl ) o;

        if ( !acc.equals( that.acc ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = acc.hashCode();
        return result;
    }
}
