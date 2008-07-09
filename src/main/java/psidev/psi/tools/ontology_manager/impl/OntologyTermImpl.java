package psidev.psi.tools.ontology_manager.impl;

import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

/**
 * Author: Florian Reisinger
 * Date: 09-Jul-2008
 */
public class OntologyTermImpl implements OntologyTermI {

    private String acc;
    private String name;

    ///// ///// ///// ///// /////
    // Getter & Setter


    public void setTermAccession( String accession ) {
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
    
}
