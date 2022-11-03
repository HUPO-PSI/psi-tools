package psidev.psi.tools.validator.rules.cvmapping.house;

import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: Drawer.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class Drawer {

    Collection<? extends Object> objects = new ArrayList<>();

    //////////////////
    // Constructors

    public Drawer( Collection<? extends Object> objects ) {
        this.objects = objects;
    }

    /////////////////////////
    // Getters and Setters

    public Collection<? extends Object> getObjects() {
        return objects;
    }

    public void setObjects( Collection<? extends Object> objects ) {
        this.objects = objects;
    }
}
