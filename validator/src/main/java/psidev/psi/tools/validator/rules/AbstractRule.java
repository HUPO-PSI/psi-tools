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
 * @since 1.0
 */
public abstract class AbstractRule implements Rule {

    public static final String NEW_LINE = System.getProperty( "line.separator" );

    ////////////////////////////
    // Instance variables

    /**
     * The name of the rule.
     */
    private String name;

    /**
     * The decription of the rule.
     */
    private String description;

    /**
     * The scope of the rule
     */
    private String scope;

    /**
     * A collection of tips.
     */
    private Collection<String> howToFixTips = new ArrayList<>(1);

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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Collection<String> getHowToFixTips() {
        return howToFixTips;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder( 256 );

        if ( name != null ) {
            sb.append( "Rule name: " ).append( name ).append( NEW_LINE );
        }

        if ( description != null ) {
            sb.append( "Description: " ).append( description ).append( NEW_LINE );
        }

        if ( howToFixTips != null && !howToFixTips.isEmpty() ) {
            sb.append( "Tip" ).append( ( howToFixTips.size() > 1 ? "s" : "" ) ).append( ':' ).append( NEW_LINE );
            for (String tip : howToFixTips) {
                sb.append("\t* ").append(tip).append(NEW_LINE);
            }
        }

        return sb.toString();
    }
}
