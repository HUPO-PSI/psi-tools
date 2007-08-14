package psidev.psi.tools.validator.rules;

import psidev.psi.tools.ontology_manager.OntologyManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The definition of a rule. the check method should contain the logic of that particular rule.
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: AbstractRule.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>26-Dec-2005</pre>
 */
public abstract class AbstractRule implements Rule {

    public static final String NEW_LINE = System.getProperty( "line.separator" );

    ////////////////////////////
    // Instance variables

    // TODO these attributes could be popuplated from the XML file as well.
    /**
     * The name of the rule.
     */
    private String name;

    /**
     * The decription of the rule.
     */
    private String description;

    /**
     * A collection of tips.
     */
    private Collection<String> howToFixTips = new ArrayList<String>( 1 );

    /**
     * Map of needed ontologies
     */
    protected OntologyManager ontologyManager;

    ////////////////////////////
    // Constructor

    public AbstractRule( OntologyManager ontologyManager ) {
        this.ontologyManager = ontologyManager;
    }

    ////////////////////////////
    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void addTip( String tip ) {
        howToFixTips.add( tip );
    }

    public Collection<String> getHowToFixTips() {
        return howToFixTips;
    }


//    /**
//     * Get an Ontology object by its ID as defined in the configuration file.
//     *
//     * @param id identifier of the ontology.
//     *
//     * @return an Ontology or an exception if thrown if the identifier is not known.
//     *
//     * @throws ValidatorException if the id is not known.
//     */
//    public Ontology getOntology( String id ) throws ValidatorException {
//
//        if ( !ontologies.containsKey( id ) ) {
//            throw new ValidatorException( "Unknown ontology identifier: " + id + ", please check your configuration file." );
//        }
//
//        return ontologies.get( id );
//    }

    public String toString() {

        StringBuffer sb = new StringBuffer( 256 );

        if ( name != null ) {
            sb.append( "Rule name: " ).append( name ).append( NEW_LINE );
        }

        if ( description != null ) {
            sb.append( "Description: " ).append( description ).append( NEW_LINE );
        }

        if ( howToFixTips != null && !howToFixTips.isEmpty() ) {
            sb.append( "Tip" ).append( ( howToFixTips.size() > 1 ? "s" : "" ) ).append( ':' ).append( NEW_LINE );
            for ( Iterator<String> iterator = howToFixTips.iterator(); iterator.hasNext(); ) {
                String tip = iterator.next();
                sb.append( "\t* " ).append( tip ).append( NEW_LINE );
            }
        }

        return sb.toString();
    }
}
