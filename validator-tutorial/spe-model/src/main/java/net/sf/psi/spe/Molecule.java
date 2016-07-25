package net.sf.psi.spe;

import java.util.Collection;
import java.util.ArrayList;

public class Molecule {

    /**
     * Experiment in which this molecule was used.
     */
    private Experiment experiment;

    /**
     * Name of the molecule.
     */
    private String name;

    /**
     * Type of the molecule.
     */
    private MoleculeType type;

    /**
     * Sequence of the molecule (if applicable).
     */
    private String sequence;

    /**
     * Modification that may have affected the molecule.
     */
    private Collection<Modification> modifications;

    public Molecule( String name, MoleculeType type ) {
        this.name = name;
        this.type = type;
        this.modifications = new ArrayList<Modification>( );
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public MoleculeType getType() {
        return type;
    }

    public void setType( MoleculeType type ) {
        this.type = type;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment( Experiment experiment ) {
        this.experiment = experiment;
    }

    public Collection<Modification> getModifications() {
        return modifications;
    }

    public void setModifications( Collection<Modification> modifications ) {
        this.modifications = modifications;
    }

    public void addModification(Modification modification) {
        modifications.add( modification );
    }

    public void removeModification(Modification modification) {
        modifications.remove( modification );
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence( String sequence ) {
        if( sequence == null || sequence.trim().length()==0 ) {
            throw new IllegalArgumentException( "You must give a non null/empty sequence" );
        }
        this.sequence = sequence;
    }

    public boolean hasSequence() {
        return sequence != null;
    }
}
