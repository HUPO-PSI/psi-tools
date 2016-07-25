package net.sf.psi.spe;

import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;

public class Experiment {

    /**
     * Unique identifier for that experiment.
     */
    private int id;

    /**
     * Name of the experiment.
     */
    private String name;

    /**
     * When was the experiment done.
     */
    private Date created;

    private Collection<Molecule> molecules;

    public Experiment( int id ) {
        this.id = id;
        this.molecules = new ArrayList<Molecule>( );
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated( Date created ) {
        this.created = created;
    }

    public Collection<Molecule> getMolecules() {
        return molecules;
    }

    public void setMolecules( Collection<Molecule> molecules ) {
        this.molecules = molecules;
    }

    public void addMolecule( Molecule molecule ) {
        molecules.add( molecule );
        molecule.setExperiment( this );
    }

    public void removeMolecule( Molecule molecule ) {
        final boolean m = molecules.remove( molecule );
        molecule.setExperiment( null );
    }
}
