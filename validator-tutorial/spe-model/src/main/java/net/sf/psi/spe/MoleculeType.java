package net.sf.psi.spe;

public class MoleculeType {

    /**
     * Identifier of the molecule type.
     */
    private String id;

    /**
     * Name of the molecule type.
     */
    private String name;

    public MoleculeType( String id, String name ) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }
}
