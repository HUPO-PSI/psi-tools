package psidev.psi.tools.ontology_manager.interfaces;

/**
 * Author: Florian Reisinger
 * Date: 09-Jul-2008
 */
public interface OntologyTermI {

    public String getTermAccession();

    public String getPreferredName();

    public void setTermAccession( String accession );

    public void setPreferredName( String preferredName );


}
