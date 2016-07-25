package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Kitchen.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Kitchen {

    String note;
    Sink sink;
    Bin bin;

    //////////////////
    // Constructors

    public Kitchen( Sink sink, Bin bin ) {
        this.sink = sink;
        this.bin = bin;
    }

    ///////////////////////////
    // Getters and Setters

    public String getNote() {
        return note;
    }

    public void setNote( String note ) {
        this.note = note;
    }

    public Sink getSink() {
        return sink;
    }

    public void setSink( Sink sink ) {
        this.sink = sink;
    }

    public Bin getBin() {
        return bin;
    }

    public void setBin( Bin bin ) {
        this.bin = bin;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "Kitchen" );
        sb.append( "{note='" ).append( note ).append( '\'' );
        sb.append( ", sink=" ).append( sink );
        sb.append( ", bin=" ).append( bin );
        sb.append( '}' );
        return sb.toString();
    }
}
