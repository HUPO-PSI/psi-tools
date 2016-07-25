package psidev.psi.tools.validator.rules.cvmapping.house;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Shower.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Shower {

    Boolean bigEnoughForTwo;

    //////////////////
    // Constructors

    public Shower() {
    }

    public Shower( Boolean bigEnoughForTwo ) {
        this.bigEnoughForTwo = bigEnoughForTwo;
    }

    ///////////////////////////
    // Getters and Setters

    public Boolean getBigEnoughForTwo() {
        return bigEnoughForTwo;
    }

    public void setBigEnoughForTwo( Boolean bigEnoughForTwo ) {
        this.bigEnoughForTwo = bigEnoughForTwo;
    }
}
