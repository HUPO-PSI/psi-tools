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
package net.sf.psi.spe.validator;

import net.sf.psi.spe.Experiment;
import net.sf.psi.spe.Modification;
import net.sf.psi.spe.Molecule;
import net.sf.psi.spe.MoleculeType;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class ValidationXpathTest {

    @Test
    public void xxx() throws Exception {

        Experiment experiment = new Experiment( 2 );
        experiment.setName( "proteomics-exp-2" );
        Molecule p1 = new Molecule( "P12345", new MoleculeType( "SPE:0326", "protein" ) );
        Molecule p2 = new Molecule( "Q98765", new MoleculeType( "SPE:0326", "protein" ) );
        p2.addModification( new Modification( "MOD:00850", "unnatural residue" ) );
        experiment.addMolecule( p1 );
        experiment.addMolecule( p2 );


        String xpath = "molecules/modifIcations/id";

        Object currentObject = experiment;
        final String[] xpathElements = xpath.split( "/" );

        if ( !( currentObject instanceof Collection ) ) {
            currentObject = Arrays.asList( currentObject );
        }
        check( ( Collection ) currentObject, xpathElements, 0 );
    }

    private boolean check( Collection<Object> currentObjects, String[] xpathElements, int idx ) throws IntrospectionException,
                                                                                                       IllegalAccessException,
                                                                                                       InvocationTargetException {
        final String xpathElement = xpathElements[idx];
        for ( Object currentObject : currentObjects ) {

            System.out.println( "Looking for " + xpathElement + " in " + currentObject.getClass().getSimpleName() );
            final Method method;
            try {
                PropertyDescriptor pd = new PropertyDescriptor( xpathElement, currentObject.getClass() );
                method = pd.getReadMethod();
            } catch ( IntrospectionException e ) {
                System.out.println( "(Exception thrown) Could not find property '" + xpathElement + "' under object type: " + currentObject.getClass().getName() );
                return false;
            }
            if ( method != null && xpathElements.length > (idx+1) ) {
                currentObject = method.invoke( currentObject );
                
                if ( !( currentObject instanceof Collection ) ) {
                    currentObject = Arrays.asList( currentObject );
                }
                check( ( Collection ) currentObject, xpathElements, idx + 1 );

            } else {
                System.out.println( "Could not find property '" + xpathElement + "' under object type: " + currentObject.getClass().getName() );
                return false;
                // error
            }
        } // for

        return true;
    }
}
