package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.OntologyManagerContext;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccessTemplate;
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
 * Abstract local ontology
 *
 * NOTE : LocalOntology was the original class but did not offer flexibility for using different extensions of OntologyTermI.
 * That is why this template abstract class has been created. The template could have been simpler
 * (AbstractLocalOntology<T extends OntologyTermI> instead of AbstractLocalOntology<T extends OntologyTermI, A extends OntologyTemplate<T> , O extends AbstractOboLoader<T, A>>)
 * but we needed an absolute retrocompatibility with the LocalOntology class which used Ontology and OboLoader but not OntologyTemplate<OntologyTerm> and AbstractOboLoader<OntologyTerm></>.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/11/11</pre>
 */

public abstract class AbstractLocalOntology<T extends OntologyTermI, A extends OntologyTemplate<T> , O extends AbstractOboLoader<T, A>> implements OntologyAccessTemplate<T> {

    public static final Log log = LogFactory.getLog(LocalOntology.class);

    protected A ontology;

    protected final String DEFAULT_ONTOLOGY_DIRECTORY = ".downloaded-ontologies";

    protected File ontologyDirectory = null;

    protected String ontologyID;

    protected String md5Signature;

    protected int contentSize = -1;

    protected URL fileUrl;

    public AbstractLocalOntology() {
        log.info( "Creating new LocalOntology..." );
        ontology = null;
    }

    ////////////////////////////
    // OntologyAccess methods

    protected abstract O createNewOBOLoader(File ontologyDirectory) throws OntologyLoaderException;

    public void loadOntology( String ontologyID, String name, String version, String format, URI uri ) throws OntologyLoaderException {
        this.ontologyID = ontologyID;

        // first check the format
        if ( "OBO".equals( format ) ) {
            if ( uri == null ) {
                throw new IllegalArgumentException( "The given CvSource doesn't have a URI" );
            } else {

                // parse the URL and load the ontology
                O loader = createNewOBOLoader( getOntologyDirectory() );


                // if we have a local file, we don't have to load from a URL
                // to specify a local file with a URI you have to follow the following syntax:
                //    [scheme:][//authority][path][?query][#fragment]
                // where authority, query and fragment are empty! An example:
                //    file:///C:/tmp/psi-mi.obo
                // note:    ^ = empty path     ^ = no query and no fragment

                if ( uri.getScheme().equalsIgnoreCase("file") ) {
                    File file = new File(uri);
                    if ( !file.exists() ) {
                        throw new IllegalArgumentException("Could not find the file for URI: " + uri + " - Perhaps the syntax of the URI is wrong!");
                    }
                    ontology = loader.parseOboFile(file);
                } else {
                    URL url;
                    try {
                        url = uri.toURL();
                    } catch ( MalformedURLException e ) {
                        throw new IllegalArgumentException( "The given CvSource doesn't have a valid URI: " + uri );
                    }

                    // Compute the MD5 signature of the file to load
                    this.md5Signature = computeMD5SignatureFor(url);

                    // Get the size of the file to load
                    this.contentSize = getSizeOfFile(url);

                    // We need to store the url to know if an update has been done later
                    this.fileUrl = url;

                    try {
                        if ( log.isDebugEnabled() ) {
                            log.debug( "Parsing URL: " + url );
                        }

                        ontology = loader.parseOboFile( url );
                    } catch ( OntologyLoaderException e ) {
                        throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                    }
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

    public Set<T> getValidTerms( String accession, boolean allowChildren, boolean useTerm ) {
        Set<T> collectedTerms = new HashSet<T>();

        final T term = getTermForAccession( accession );
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

    public T getTermForAccession( String accession ) {
        return ontology.search( accession );
    }

    public boolean isObsolete( T term ) {
        return ontology.isObsoleteTerm( term );
    }

    public Set<T> getDirectParents( T term ) {
        return ontology.getDirectParents( term );
    }

    public Set<T> getDirectChildren( T term ) {
        return ontology.getDirectChildren( term );
    }

    public Set<T> getAllParents( T term ) {
        return ontology.getAllParents( term );
    }

    public Set<T> getAllChildren( T term ) {
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

    public boolean isUseTermSynonyms() {
        return true;
    }

    public void setUseTermSynonyms(boolean useTermSynonyms) {
        if (!useTermSynonyms) {
            throw new UnsupportedOperationException("This implementation of the OntologyAccess " +
                    "does not support this feature. Synonym handling cannot be turned off.");
        }
    }

    /**
     *
     * @return true if the MD5 signature of the file containing the ontologies is still the same
     * @throws OntologyLoaderException
     */
    protected boolean checkUpToDateMd5Signature() throws OntologyLoaderException {
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
    protected boolean checkUpToDateContentSize() throws OntologyLoaderException {
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
    protected String computeMD5SignatureFor(URL url) throws OntologyLoaderException {
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
    protected int getSizeOfFile(URL url) throws OntologyLoaderException {
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
    protected File getOntologyDirectory() throws OntologyLoaderException {

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
