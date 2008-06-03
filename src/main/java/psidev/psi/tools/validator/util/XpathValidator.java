/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package psidev.psi.tools.validator.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * Checks if a specific Xpath expression can be threaded onto an object instance.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class XpathValidator {

    private static final Log log = LogFactory.getLog( XpathValidator.class );

    private String xpath;

    public XpathValidator( String xpath ) {
        if ( xpath == null || xpath.trim().length() == 0 ) {
            throw new IllegalArgumentException( "You must give a non null/empty xpath expression" );
        }
        this.xpath = xpath;
    }

    /**
     * Checks that the given object complies to the given xpath expression.
     *
     * @param o the object instance to check on.
     * @return an error message or null if validation succeeded.
     */
    public String validate( Object o ) {

        Object currentObject = o;
        String localXpath = xpath;
        if ( localXpath.startsWith( "/" ) ) {
            localXpath = localXpath.substring( 1 );
        }

        final String[] xpathElements = localXpath.split( "/" );

        if ( !( currentObject instanceof Collection ) ) {
            currentObject = Arrays.asList( currentObject );
        }
        return check( ( Collection ) currentObject, xpathElements, 0 );
    }

    /**
     * Recursive method that run the check.
     *
     * @param currentObjects a collection of objects to check on
     * @param xpathElements  the whole expath expression tokenized
     * @param idx            the index of the current xpath element
     * @return an error message or null if validation succeeded.
     */
    private String check( Collection<Object> currentObjects, String[] xpathElements, int idx ) {
        String xpathElement = xpathElements[idx];
        for ( Object currentObject : currentObjects ) {

            if( currentObject == null ) {
                // there's nothing to check on
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug( "Looking for " + xpathElement + " in " + currentObject.getClass().getSimpleName() );
            }

            final Method method;
            try {
                if( xpathElement.startsWith( "@" )) {
                    xpathElement = xpathElement.substring( 1 );
                }
                PropertyDescriptor pd = new PropertyDescriptor( xpathElement, currentObject.getClass() );
                method = pd.getReadMethod();
            } catch ( IntrospectionException e ) {
                return "Could not find property '" + xpathElement + "' of the xpath expression '"+xpath+
                       "' (element position: "+(idx+1)+") in the given object of: " +
                       currentObject.getClass().getName();
            }
            if ( method != null && xpathElements.length > ( idx + 1 ) ) {
                try {
                    currentObject = method.invoke( currentObject );
                } catch ( Exception e ) {
                    throw new RuntimeException( "Error invoking method " + currentObject.getClass().getName() + "." +
                                                method.getName() + "()", e );
                }

                if ( !( currentObject instanceof Collection ) ) {
                    currentObject = Arrays.asList( currentObject );
                }
                return check( ( Collection ) currentObject, xpathElements, idx + 1 );
            }
        } // for

        return null; // success
    }
}