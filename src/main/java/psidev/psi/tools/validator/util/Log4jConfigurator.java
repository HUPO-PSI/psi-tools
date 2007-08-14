/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.validator.util;

import org.apache.log4j.PropertyConfigurator;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: Log4jConfigurator.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>18-Jan-2006</pre>
 */
public class Log4jConfigurator {
    public static void configure() {
       // BasicConfigurator replaced with PropertyConfigurator.
        PropertyConfigurator.configure( "config/log4j.properties" );
    }
}