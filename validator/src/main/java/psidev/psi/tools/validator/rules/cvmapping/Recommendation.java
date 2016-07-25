/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.validator.rules.cvmapping;

/**
 * Give recommendation level as defined by the PSI consortium.
 * <p/>
 * Note: MUST > SHOULD > MAY.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: Recommendation.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>24-Jun-2007</pre>
 */
public enum Recommendation {

    ///////////////////////////////////////
    // Declaration of the Enums' values

    /**
     *
     */
    MAY( "MAY" ),

    /**
     *
     */
    SHOULD( "SHOULD" ),

    /**
     *
     */
    MUST( "MUST" );

    /////////////////////////////////
    // Constructor

    private final String name;

    private Recommendation( String name ) {
        this.name = name;
    }

    public static Recommendation forName( String level ) {
        if ( MAY.name.equalsIgnoreCase( level ) ) {
            return MAY;
        } else if ( SHOULD.name.equalsIgnoreCase( level ) ) {
            return SHOULD;
        } else if ( MUST.name.equalsIgnoreCase( level ) ) {
            return MUST;
        } else {
            return null;
        }
    }

    /////////////////////////////
    // Comparison utility

    public boolean isHigher( Recommendation aLevel ) {
        return ( this.compareTo( aLevel ) > 0 );
    }

    public boolean isSame( Recommendation aLevel ) {
        return ( this.compareTo( aLevel ) == 0 );
    }

    public boolean isLower( Recommendation aLevel ) {
        return ( this.compareTo( aLevel ) < 0 );
    }

    /////////////////////////////////
    // Object's overload.

    @Override
    public String toString() {
        return name;
    }
}