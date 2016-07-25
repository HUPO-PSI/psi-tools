package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Bin.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Bin {

    float capacity;

    //////////////////
    // Constructors

    public Bin( float capacity ) {
        this.capacity = capacity;
    }

    ///////////////////////////
    // Getters and Setters

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity( float capacity ) {
        this.capacity = capacity;
    }
}
