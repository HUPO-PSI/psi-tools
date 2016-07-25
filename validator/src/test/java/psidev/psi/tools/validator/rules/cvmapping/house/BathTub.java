package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: BathTub.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class BathTub {

    String color;
    float capacity;

    public BathTub( String color, float capacity ) {
        this.color = color;
        this.capacity = capacity;
    }

    public String getColor() {
        return color;
    }

    public void setColor( String color ) {
        this.color = color;
    }

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity( float capacity ) {
        this.capacity = capacity;
    }
}
