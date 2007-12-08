package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Car.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Car {

    String color;
    String Make;
    boolean isFast;

    //////////////////
    // Constructors

    public Car( String color, String make, boolean fast ) {
        this.color = color;
        Make = make;
        isFast = fast;
    }

    ///////////////////////////
    // Getters and Setters

    public String getColor() {
        return color;
    }

    public void setColor( String color ) {
        this.color = color;
    }

    public String getMake() {
        return Make;
    }

    public void setMake( String make ) {
        Make = make;
    }

    public boolean isFast() {
        return isFast;
    }

    public void setFast( boolean fast ) {
        isFast = fast;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "Car" );
        sb.append( "{color='" ).append( color ).append( '\'' );
        sb.append( ", Make='" ).append( Make ).append( '\'' );
        sb.append( ", isFast=" ).append( isFast );
        sb.append( '}' );
        return sb.toString();
    }
}
