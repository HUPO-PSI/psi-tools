/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.validator.preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.validator.ValidatorException;

import java.io.File;
import java.io.IOException;

/**
 * Repository for user preferences.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-Jun-2006</pre>
 */
public class UserPreferences {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( UserPreferences.class );

    /**
     * User's home directory
     */
    public static final String USER_HOME = System.getProperty( "user.home" );

    /**
     * Temporary directory.
     */
    public static final String TEMP_DIR = System.getProperty( "java.io.tmpdir", "tmp" );

    /**
     * preference directories.
     */
    public static final String VALIDATOR_DIRECTORY = ".validator";

    /**
     * Validator's properties file
     */
    public static final String PROPERTIES_FILENAME = "validator.properties";

    //////////////////////////
    // Instance variables

    /**
     * Should we run a SAX validation before to run the semantic validation.
     */
    private boolean saxValidationEnabled = false;

    /**
     * Do we keep the downloaded ontology files on disk.
     */
    private boolean keepDownloadedOntologiesOnDisk = true;

    /**
     * Where the validator will store its temporary files and configuration.
     */
    private File workDirectory;

    //////////////////////////////////
    // Getters and Setters

    public void setSaxValidationEnabled( boolean saxValidationEnabled ) {
        this.saxValidationEnabled = saxValidationEnabled;
    }

    public boolean isSaxValidationEnabled() {
        return saxValidationEnabled;
    }

    public File getWorkDirectory() {

        if ( workDirectory == null ) {
            // give the user's home directory
            return getUserHomeDirectory();
        }

        return workDirectory;
    }

    public boolean isKeepDownloadedOntologiesOnDisk() {
        return keepDownloadedOntologiesOnDisk;
    }

    public void setKeepDownloadedOntologiesOnDisk( boolean keepDownloadedOntologiesOnDisk ) {
        this.keepDownloadedOntologiesOnDisk = keepDownloadedOntologiesOnDisk;
    }

    public void setWorkDirectory( File workDirectory ) {

        if ( workDirectory != null ) {

            if ( ! workDirectory.exists() ) {
                log.warn( workDirectory.getAbsolutePath() + " doesn't exist, trying now to create it." );

                boolean success = workDirectory.mkdirs();
                if ( success ) {
                    log.info( "Directory created successfully." );
                    this.workDirectory = workDirectory;
                } else {
                    log.error( "Could not create " + workDirectory.getAbsolutePath() );
                    log.error( "The default user directory will be used." );
                }

            } else {
                // directory exists
                if ( workDirectory.canRead() && workDirectory.canWrite() ) {
                    this.workDirectory = workDirectory;
                } else {
                    log.error( "The specified work directory (" + workDirectory.getAbsolutePath() +
                               ") is not read/write. We will use the user home directory instead." );

                    // leaving it to null will make sure the user's home directory gets used.
                }
            }
        } else {
            log.warn( "The given file was null, user's home directory will be used." );
        }
    }

    /**
     * Returns the user's home directory.
     * @return user home directory.
     */
    private File getUserHomeDirectory() {
        String path = USER_HOME;
        File home = new File( path );
        return home;
    }

    /**
     * Create a directory for the validator either in the user's home directory or if it cannot, in the system's temp
     * directory.
     *
     * @return the created directory, never null.
     */
    public File getValidatorDirectory() throws ValidatorException {

        String path = getWorkDirectory().getAbsolutePath() + File.separator + VALIDATOR_DIRECTORY;

        File dir = new File( path );

        if ( ! dir.exists() ) {

            if ( !dir.mkdirs() ) {
                throw new ValidatorException( "Cannot create Validator's home directory: " + dir.getAbsolutePath() );
            }

            return dir;

        } else {

            if ( dir.canWrite() ) {
                return dir;
            } else {
                // TODO log error
                dir = null;
                path = TEMP_DIR + File.separator + VALIDATOR_DIRECTORY;
                dir = new File( path );

                if ( ! dir.exists() ) {

                    if ( !dir.mkdirs() ) {
                        throw new ValidatorException( "Cannot create Validator's home directory: " + dir.getAbsolutePath() );
                    }

                    return dir;

                } else {

                    if ( dir.canWrite() ) {
                        return dir;
                    } else {
                        // TODO log error
                        throw new ValidatorException( "Cannot create Validator's home directory, pleast check your config." );
                    }
                }
            }
        }
    }

    public File getPropertiesFile() throws ValidatorException{

        File configDir = getValidatorDirectory();

        if ( configDir == null ) {
            return getUserHomeDirectory();
        }

        File props = new File( configDir.getAbsolutePath() + File.separator + PROPERTIES_FILENAME );
        if ( ! props.exists() ) {
            try {
                if ( ! props.createNewFile() ) {
                    log.warn( "Error - Could not create file " + props.getAbsolutePath() );
                    return getUserHomeDirectory();
                }
            } catch ( IOException e ) {
                log.warn( "Error - Could not create file " + props.getAbsolutePath(), e );
            }
        } else {
            log.info( "Found " + props.getAbsolutePath() );
        }

        return props;
    }
}