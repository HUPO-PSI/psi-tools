package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * Object allowing to build complex houses.
 *
 * @author Samuel Kerrien
 * @version $Id: HouseFactory.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class HouseFactory {

    public static House buildSimpleHouse() {
        House house = new House();

        Kitchen kitchen = new Kitchen( new Sink( false, 5.0f ), new Bin( 60.0f ) );
        kitchen.setNote( "PSI:1000010" );
        house.setKitchen( kitchen );

        BathRoom br = new BathRoom( "light blue" );
        br.setBathTub( new BathTub( "white", 200.0f ) );
        br.setSink( new Sink( false, 15.0f ) );
        br.setShower( new Shower( ) );
        house.setBathroom( br );

        house.addBedroom( new BedRoom( "blue" ) );
        house.addBedroom( new BedRoom( "pink" ) );
        house.addBedroom( new BedRoom( "white" ) );
        
        return house;
    }
}