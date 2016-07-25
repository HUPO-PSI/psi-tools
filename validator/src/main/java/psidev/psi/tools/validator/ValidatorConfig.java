package psidev.psi.tools.validator;

/**
 * Configuration of the validator
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27-Apr-2010</pre>
 */

public class ValidatorConfig {

    protected String lineSeparator;

    public ValidatorConfig() {
        lineSeparator = System.getProperty( "line.separator" );
    }

    public String getLineSeparator() {
        return lineSeparator;
    }
}
