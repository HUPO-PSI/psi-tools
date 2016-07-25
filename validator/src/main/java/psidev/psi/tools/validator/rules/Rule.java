package psidev.psi.tools.validator.rules;

import java.util.Collection;

/**
 * Definition of a rule.
 * Minimal set of methods a validator rule has to have.
 *
 * @author: florian
 * @since 2.0.0
 */
public interface Rule {

    /**
     * @return a String that should uniquely identify the rule
     */
    String getId();

    /**
     * @return a short descriptive name for the rule.
     */
    String getName();

    /**
     * @return a description of the rule. Ideally this should contain clues as to what the rules purpose is.
     */
    String getDescription();

    /**
     * @return a collection of Strings that should make it easier
     *         for the user to determine why the rule failed and
     *         tips that can help in solving the problem.
     */
    Collection<String> getHowToFixTips();

    /**
     * @return a String representation of this rule.
     */
    String toString();
}
