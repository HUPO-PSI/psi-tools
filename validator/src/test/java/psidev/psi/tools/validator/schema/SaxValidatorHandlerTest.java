/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package psidev.psi.tools.validator.schema;

import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * SaxValidatorHandler Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class SaxValidatorHandlerTest {

    @Test
    public void validate() throws Exception {

        InputStream is = SaxValidatorHandlerTest.class.getResourceAsStream( "/xml-samples/17129785_syntaxError.xml" );
        Assert.assertNotNull(is);

        final SaxReport report = SaxValidatorHandler.validate(is);
        Assert.assertNotNull(report);
        Assert.assertFalse( report.isValid() );
        
        Assert.assertEquals( 1, report.getMessages().size() );
        final SaxMessage message = report.getMessages().iterator().next();
        Assert.assertEquals(6, message.getLineNumber());
        Assert.assertTrue( message.getMessage().contains("badTag"));
    }
}
