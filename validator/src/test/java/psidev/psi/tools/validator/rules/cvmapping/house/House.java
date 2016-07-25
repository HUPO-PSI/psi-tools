package psidev.psi.tools.validator.rules.cvmapping.house;

import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO comment a that class header
 *
 * @author Samuel Kerrien
 * @version $Id: House.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class House {

    Kitchen kitchen;
    Collection<BedRoom> bedrooms = new ArrayList<BedRoom>( );

    Garage garage;
    BathRoom bathroom;

    //////////////////
    // Constructors

    public House() {}

    ///////////////////////
    // Getters and Setters

    public Garage getGarage() {
        return garage;
    }

    public void setGarage( Garage garage ) {
        this.garage = garage;
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    public void setKitchen( Kitchen kitchen ) {
        this.kitchen = kitchen;
    }

    public BathRoom getBathroom() {
        return bathroom;
    }

    public void addBedroom( BedRoom bedRoom ) {
        bedrooms.add( bedRoom );
    }

    public void setBathroom( BathRoom bathroom ) {
        this.bathroom = bathroom;
    }

    public Collection<BedRoom> getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms( Collection<BedRoom> bedrooms ) {
        this.bedrooms = bedrooms;
    }
}
