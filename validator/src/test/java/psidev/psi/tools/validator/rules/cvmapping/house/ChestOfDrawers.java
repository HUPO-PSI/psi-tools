package psidev.psi.tools.validator.rules.cvmapping.house;

import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO commenta that class header
 *
 * @author Samuel Kerrien
 * @version $Id: ChestOfDrawers.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since specify the maven artifact version
 */
public class ChestOfDrawers {
    Collection<Drawer> drawers = new ArrayList<Drawer>( );

    public Collection<Drawer> getDrawers() {
        return drawers;
    }

    public void setDrawers( Collection<Drawer> drawers ) {
        this.drawers = drawers;
    }
}
