package psidev.psi.tools.validator.rules.cvmapping;

import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.rules.Rule;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.validator.cvmapping.jaxb.ModelElementMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Author: florian
 * Date: 18-Jul-2007
 * Time: 12:02:05
 */
public interface CvRule extends Rule {

    public boolean canCheck(String xPath );

    public Collection<ValidatorMessage> check( Object object, String xPath ) throws ValidatorException;

    
    public String getElementPath();

    public List<ModelElementMap.CVTerm> getCVTerms();

    public String getRequirementLevel();

    public void setNonRepeatableTerms(Map<ModelElementMap.CVTerm, Integer> nonRepeatableTerms);

    public MessageLevel convertCvMappingLevel( Recommendation level );

    public ValidatorMessage buildMessage( String xpath, Recommendation level, String message, Rule rule );
}
