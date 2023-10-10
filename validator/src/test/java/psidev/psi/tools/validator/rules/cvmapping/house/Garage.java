package psidev.psi.tools.validator.rules.cvmapping.house;

import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Garage.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Garage {

    float depth;
    float width;
    Car car = null;
    Collection<Bike> bikes = new ArrayList<>();

    //////////////////
    // Constructors

    public Garage( float depth, float width ) {
        this.depth = depth;
        this.width = width;
    }

    ////////////////////////
    // Getters and setters

    public float getDepth() {
        return depth;
    }

    public void setDepth( float depth ) {
        this.depth = depth;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth( float width ) {
        this.width = width;
    }

    public Car getCar() {
        return car;
    }

    public void setCar( Car car ) {
        this.car = car;
    }

    public void addBike( Bike bike ) {
        bikes.add( bike );
    }

    public Collection<Bike> getBikes() {
        return bikes;
    }

    public void setBikes( Collection<Bike> bikes ) {
        this.bikes = bikes;
    }
}
