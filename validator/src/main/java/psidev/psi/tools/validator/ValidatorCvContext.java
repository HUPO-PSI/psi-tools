package psidev.psi.tools.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class to keep track of terms that where used in the file to validate (and that are
 * checked by at least one CV mapping rule), but which did not match any Cv mapping terms.
 * Effectively this treats the specified CV mapping file as a white-list, containing all
 * the allowed terms. Everything else is recorded for further reporting/processing.
 *
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 *         Date: 27-Oct-2009
 * @since 2.0.2
 */
public class ValidatorCvContext {

    // ToDo: possible improvements/optimizations:
    // if cv-mapping-rules are sorted by xpath and checked in an according order, then
    // the map of recognised terms would only have to contain the recognised terms of
    // the current xpath (as soon as we move to a new xpath, we could free up the space
    // held by previous values)
    // Also, this should not be a ThreadLocal 'hack', but rather a central mechanism well
    // embedded in the validator framework. For example the validator could take care of
    // this business (maybe delegating to another class). E.g all CvRules would get on
    // creation a reference back to the executing validator and thus could report any/all
    // 'unrecognised' terms back to the validator for further processing. This way a flag
    // set on the validator could define globally (independent of a possible definition on
    // individual CvRule level) if such recording is required or not.



    private Map<String, Set<String>> notRecognisedTerms;
    private Map<String, Set<String>> recognisedTerms;


    public static final Log log = LogFactory.getLog( ValidatorCvContext.class);

    private static ThreadLocal<ValidatorCvContext> instance = new
            ThreadLocal<ValidatorCvContext>() {
                @Override
                protected ValidatorCvContext initialValue() {
                    return new ValidatorCvContext();
                }
            };

    public static ValidatorCvContext getInstance() {
        return instance.get();
    }

    public static void removeInstance() {
        instance.remove();
    }

    private ValidatorCvContext() {
        notRecognisedTerms = new HashMap<>();
        recognisedTerms    = new HashMap<>();
    }



    public void resetRecognised() {
        recognisedTerms = new HashMap<>();
    }

    public boolean isRecognised(String xpath, String term) {
        return recognisedTerms.get(xpath) != null && recognisedTerms.get(xpath).contains(term);
    }

    public void addRecognised(String xpath, String term) {
        if (recognisedTerms.get(xpath) == null) {
            Set<String> newSet = new HashSet<>();
            recognisedTerms.put(xpath, newSet);
        }
        recognisedTerms.get(xpath).add(term);
    }

    public boolean removeRecognised(String xpath, String term) {
        return recognisedTerms.get(xpath) != null && recognisedTerms.get(xpath).remove(term);
    }

    public Set<String> getRecognisedXpath() {
        return recognisedTerms.keySet();
    }

    public Set<String> getRecognisedTerms(String xpath) {
        return recognisedTerms.get(xpath);
    }



    public void resetNotRecognised() {
        notRecognisedTerms = new HashMap<>();
    }

    public boolean isNotRecognised(String xpath, String term) {
        return notRecognisedTerms.get(xpath) != null && notRecognisedTerms.get(xpath).contains(term);
    }

    public void addNotRecognised(String xpath, String term) {
        if (notRecognisedTerms.get(xpath) == null) {
            Set<String> newSet = new HashSet<>();
            notRecognisedTerms.put(xpath, newSet);
        }
        notRecognisedTerms.get(xpath).add(term);
    }

    public boolean removeNotRecognised(String xpath, String term) {
        return notRecognisedTerms.get(xpath) != null && notRecognisedTerms.get(xpath).remove(term);
    }

    public Set<String> getNotRecognisedXpath() {
        return notRecognisedTerms.keySet();
    }

    public Set<String> getNotRecognisedTerms(String xpath) {
        return notRecognisedTerms.get(xpath);
    }



}
