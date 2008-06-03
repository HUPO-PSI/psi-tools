package psidev.psi.tools.validator.rules.cvmapping;

/**
 * Status of a CV mapping rule.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public enum MappingRuleStatus {

    /**
     * The rule hasn't been run yet, we don't know if it can be.
     */
    NOT_CHECKED,

    /**
     * The rule has been successfully run (data has been collected from the data model).
     */
    VALID_RULE,

    /**
     * Run was run successfuly but no data was collected, yet the xpath expression was valid.
     */
    VALID_XPATH,

    /**
     * The rule didn't collect any data and the xpath was found not to match the data model.
     */
    INVALID_XPATH;
}
