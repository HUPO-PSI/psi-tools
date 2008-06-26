package psidev.psi.tools.validator.rules;

import java.util.Collection;

/**
 * Definition of a rule.
 *
 * @author: florian
 * @since 2.0.0
 */
public interface Rule {

    String getName();

    String getDescription();

    Collection<String> getHowToFixTips();

    String toString();
}
