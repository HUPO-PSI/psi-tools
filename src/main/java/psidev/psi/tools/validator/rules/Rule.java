package psidev.psi.tools.validator.rules;

import java.util.Collection;

/**
 * Author: florian
 * Date: 18-Jul-2007
 * Time: 11:55:58
 */
public interface Rule {

    String getName();

    String getDescription();

    Collection<String> getHowToFixTips();

    String toString();
}
