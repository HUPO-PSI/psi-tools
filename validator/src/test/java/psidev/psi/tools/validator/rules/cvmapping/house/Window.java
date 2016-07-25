package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Window.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Window {

    boolean doubleGlazing;
    float width;
    float heigth;

    //////////////////
    // Constructors

    public Window( boolean doubleGlazing, float width, float heigth ) {
        this.doubleGlazing = doubleGlazing;
        this.width = width;
        this.heigth = heigth;
    }

    ///////////////////////////
    // Getters and Setters

    public boolean isDoubleGlazing() {
        return doubleGlazing;
    }

    public void setDoubleGlazing( boolean doubleGlazing ) {
        this.doubleGlazing = doubleGlazing;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth( float width ) {
        this.width = width;
    }

    public float getHeigth() {
        return heigth;
    }

    public void setHeigth( float heigth ) {
        this.heigth = heigth;
    }
}
