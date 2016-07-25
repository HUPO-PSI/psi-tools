package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: BathRoom.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class BathRoom {

    String color;

    Sink sink;
    Shower shower = new Shower();
    BathTub bathTub;

    //////////////////
    // Constructors

    public BathRoom( String color ) {
        this.color = color;
    }

    ///////////////////////////
    // Getters and Setters

    public Sink getSink() {
        return sink;
    }

    public void setSink( Sink sink ) {
        this.sink = sink;
    }

    public Shower getShower() {
        return shower;
    }

    public void setShower( Shower shower ) {
        this.shower = shower;
    }

    public BathTub getBathTub() {
        return bathTub;
    }

    public void setBathTub( BathTub bathTub ) {
        this.bathTub = bathTub;
    }
}
