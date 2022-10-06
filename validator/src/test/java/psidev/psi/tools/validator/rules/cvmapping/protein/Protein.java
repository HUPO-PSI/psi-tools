/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
package psidev.psi.tools.validator.rules.cvmapping.protein;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class Protein {

    private String name;
    private Collection<CrossReference> references;
    private Collection<Modification> modifications;
    private String sequence;

    public Protein( String name, CrossReference... references ) {
        this.name = name;
        this.references = new ArrayList<>();
        this.references.addAll( Arrays.asList( references ) );
    }

    public Protein( String name, Modification... modifications ) {
        this.name = name;
        this.modifications = new ArrayList<>();
        this.modifications.addAll( Arrays.asList( modifications ) );
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Collection<CrossReference> getReferences() {
        return references;
    }

    public void setReferences( Collection<CrossReference> references ) {
        this.references = references;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence( String sequence ) {
        this.sequence = sequence;
    }

    public Collection<Modification> getModifications() {
        return modifications;
    }

    public void setModifications( Collection<Modification> modifications ) {
        this.modifications = modifications;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "Protein" );
        sb.append( "{name='" ).append( name ).append( '\'' );
        sb.append( ", references=" ).append( references );
        sb.append( ", sequence='" ).append( sequence ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
