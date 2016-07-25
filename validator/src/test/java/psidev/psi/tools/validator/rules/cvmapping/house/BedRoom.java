package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: BedRoom.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class BedRoom {

    String color;

    Bed bed = new Bed();
    ChestOfDrawers chestOfDrawers = new ChestOfDrawers(); 
    Window window;

    //////////////////
    // Constructors

    public BedRoom( String color ) {
        this.color = color;
    }

    ///////////////////////////
    // Getters and Setters

    public String getColor() {
        return color;
    }

    public void setColor( String color ) {
        this.color = color;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed( Bed bed ) {
        this.bed = bed;
    }

    public ChestOfDrawers getChestOfDrawers() {
        return chestOfDrawers;
    }

    public void setChestOfDrawers( ChestOfDrawers chestOfDrawers ) {
        this.chestOfDrawers = chestOfDrawers;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow( Window window ) {
        this.window = window;
    }
}
