package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.OntologyManagerContext;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Access to a local ontology in the form of an OBO file.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.0
 */
public class LocalOntology implements OntologyAccess {

    public static final Log log = LogFactory.getLog( LocalOntology.class );

    private Ontology ontology;

    private final String DEFAULT_ONTOLOGY_DIRECTORY = ".downloaded-ontologies";

    private File ontologyDirectory = null;

    private String ontologyID;

    private String md5Signature;

    private int contentSize = -1;

    private URL fileUrl;

    public LocalOntology() {
        log.info( "Creating new LocalOntology..." );
        ontology = null;
    }

    ////////////////////////////
    // OntologyAccess methods

    public void loadOntology( String ontologyID, String name, String version, String format, URI uri ) throws OntologyLoaderException {
        this.ontologyID = ontologyID;

        // first check the format
        if ( "OBO".equals( format ) ) {
            if ( uri == null ) {
                throw new IllegalArgumentException( "The given CvSource doesn't have a URL" );
            } else {
                URL url;
                try {
                    url = uri.toURL();
                } catch ( MalformedURLException e ) {
                    throw new IllegalArgumentException( "The given CvSource doesn't have a valid URL: " + uri );
                }

                // Compute the MD5 signature of the file to load
                this.md5Signature = computeMD5SignatureFor(url);

                // Get the size of the file to load
                this.contentSize = getSizeOfFile(url);

                // We need to store the url to know if an update has been done later
                this.fileUrl = url;

                // parse the URL and load the ontology
                OboLoader loader = new OboLoader( getOntologyDirectory() );
                try {
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Parsing URL: " + url );
                    }

                    ontology = loader.parseOboFile( url );
                } catch ( OntologyLoaderException e ) {
                    throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Successfully created LocalOntology from values: ontology="
                    + ontologyID + " name=" + name + " version=" + version + " format=" + format + " location=" + uri );
        }
    }

    public void setOntologyDirectory( File directory ) {
        if ( directory != null ) ontologyDirectory = directory;
    }

    public Set<OntologyTermI> getValidTerms( String accession, boolean allowChildren, boolean useTerm ) {
        Set<OntologyTermI> collectedTerms = new HashSet<OntologyTermI>();

        final OntologyTermI term = getTermForAccession( accession );
        if ( term != null ) {
            if ( useTerm ) {
                collectedTerms.add( term );
            }

            if ( allowChildren ) {
                collectedTerms.addAll( getAllChildren( term ) );
            }
        }

        return collectedTerms;
    }

    public OntologyTermI getTermForAccession( String accession ) {
        return ontology.search( accession );
    }

    public boolean isObsolete( OntologyTermI term ) {
        return ontology.isObsoleteTerm( term );
    }

    public Set<OntologyTermI> getDirectParents( OntologyTermI term ) {
        return ontology.getDirectParents( term );
    }

    public Set<OntologyTermI> getDirectChildren( OntologyTermI term ) {
        return ontology.getDirectChildren( term );
    }

    public Set<OntologyTermI> getAllParents( OntologyTermI term ) {
        return ontology.getAllParents( term );
    }

    public Set<OntologyTermI> getAllChildren( OntologyTermI term ) {
        return ontology.getAllChildren( term );
    }

    /**
     * Check if the ontology is up to date
     * @return true if the md5 signature is still the same and/or if the size of the file is still the same
     * @throws OntologyLoaderException
     */
    public boolean isOntologyUpToDate() throws OntologyLoaderException {

        if (this.fileUrl != null){
            if (md5Signature != null){
                boolean isMd5UpToDate = checkUpToDateMd5Signature();
                boolean isContentSizeUpToDate = checkUpToDateContentSize();

                if (isMd5UpToDate && isContentSizeUpToDate){
                    return true;
                }
                else if (!isContentSizeUpToDate && isMd5UpToDate && this.contentSize == -1){
                    return true;
                }
                else if (isContentSizeUpToDate && !isMd5UpToDate && this.md5Signature == null){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @return true if the MD5 signature of the file containing the ontologies is still the same
     * @throws OntologyLoaderException
     */
    private boolean checkUpToDateMd5Signature() throws OntologyLoaderException {
        if (md5Signature != null){
            String newMd5Signature = computeMD5SignatureFor(this.fileUrl);

            return md5Signature.equals(newMd5Signature);
        }
        return false;
    }

    /**
     *
     * @return true if the content size of the file containing the ontologies is still the same
     * @throws OntologyLoaderException
     */
    private boolean checkUpToDateContentSize() throws OntologyLoaderException {
        if (this.contentSize != -1){
            int newContentSize = getSizeOfFile(this.fileUrl);
            return newContentSize == this.contentSize;
        }

        return false;
    }

    /**
     * Computes the md5 signature of the URL
     * @param url
     * @return
     * @throws OntologyLoaderException
     */
    private String computeMD5SignatureFor(URL url) throws OntologyLoaderException {
        InputStream is = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            is = url.openStream();
            byte[] buffer = new byte[8192];
            int read = 0;

            while( (read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);

            }

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);

            return output;

        }
        catch(IOException e) {
            throw new OntologyLoaderException("Unable to process file for MD5", e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new OntologyLoaderException("Unable to compute file MD5 signature for the file " + url.getFile(), e);
        } finally {
            try {
                if (is != null){
                    is.close();
                }
            }
            catch(IOException e) {
                throw new OntologyLoaderException("Unable to close input stream for MD5 calculation", e);
            }
        }

    }

    /**
     * Get the size of a file at a given url
     * @param url
     * @return
     * @throws OntologyLoaderException
     */
    private int getSizeOfFile(URL url) throws OntologyLoaderException {
        URLConnection con = null;

        try {
            con = url.openConnection();
            int size = con.getContentLength();

            return size;
        } catch (IOException e) {
            throw new OntologyLoaderException("Unable to open the url", e);
        }
    }

    /**
     * Create a directory for the validator either in the user's home directory or if it cannot, in the system's temp
     * directory.
     *
     * @return the created directory, never null.
     */
    private File getOntologyDirectory() throws OntologyLoaderException {

        if ( ontologyDirectory != null ) {
            if ( !ontologyDirectory.exists() ) {

                if ( !ontologyDirectory.mkdirs() ) {
                    throw new OntologyLoaderException( "Cannot create home directory for ontologies: " + ontologyDirectory.getAbsolutePath() );
                }
                return ontologyDirectory;

            } else {

                if ( ontologyDirectory.canWrite() ) {
                    return ontologyDirectory;
                }
            }

            log.warn( "Could not create or write to specified ontologies directory: " + ontologyDirectory.getAbsolutePath() );
        }

        if( OntologyManagerContext.getInstance().isStoreOntologiesLocally() ) {

            String path = System.getProperty( "user.home" ) + File.separator + DEFAULT_ONTOLOGY_DIRECTORY;
            log.info( "Using default for ontology directory: " + path );

            File dir = new File( path );

            if ( !dir.exists() ) {

                if ( !dir.mkdirs() ) {
                    throw new OntologyLoaderException( "Cannot create home directory for ontologies: " + dir.getAbsolutePath() );
                }

                return dir;

            } else {

                if ( dir.canWrite() ) {
                    return dir;
                } else {
                    // TODO log error
                    dir = null;
                    path = System.getProperty( "java.io.tmpdir", "tmp" ) + File.separator + ontologyDirectory;
                    dir = new File( path );

                    if ( !dir.exists() ) {

                        if ( !dir.mkdirs() ) {
                            throw new OntologyLoaderException( "Cannot create home directory for ontologies: " + dir.getAbsolutePath() );
                        }

                        return dir;

                    } else {

                        if ( dir.canWrite() ) {
                            return dir;
                        } else {
                            // TODO log error
                            throw new OntologyLoaderException( "Cannot create home directory for ontologies, pleast check your config." );
                        }
                    }
                }
            }
        }

        return null;
    }
}