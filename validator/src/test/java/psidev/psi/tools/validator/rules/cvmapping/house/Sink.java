package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Sink.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Sink {

    boolean hasWaterMixer;
    float capacity;

    //////////////////
    // Constructors

    public Sink( boolean hasWaterMixer, float capacity ) {
        this.hasWaterMixer = hasWaterMixer;
        this.capacity = capacity;
    }

    ///////////////////////////
    // Getters and Setters

    public boolean isHasWaterMixer() {
        return hasWaterMixer;
    }

    public void setHasWaterMixer( boolean hasWaterMixer ) {
        this.hasWaterMixer = hasWaterMixer;
    }

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity( float capacity ) {
        this.capacity = capacity;
    }
}
