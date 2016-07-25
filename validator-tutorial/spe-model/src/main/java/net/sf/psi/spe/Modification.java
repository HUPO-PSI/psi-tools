package net.sf.psi.spe;

public class Modification {

    /**
     * Identifier of the modicication.
     */
    private String id;

    /**
     * Name of the modification.
     */
    private String name;

    public Modification( String id, String name ) {
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
