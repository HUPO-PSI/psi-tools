package psidev.psi.tools.validator.rules.objectRules.mimix;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08-Feb-2010</pre>
 */

public class Rule4 extends ObjectRule<Object> {
    public Rule4(OntologyManager ontologyManager) {
        super(ontologyManager);
    }

    @Override
    public boolean canCheck(Object t) {
        if (t instanceof Object){
            return true;
        }
        return false;
    }

    @Override
    public Collection<ValidatorMessage> check(Object o) throws ValidatorException {
       return new ArrayList<ValidatorMessage>();
    }
}
