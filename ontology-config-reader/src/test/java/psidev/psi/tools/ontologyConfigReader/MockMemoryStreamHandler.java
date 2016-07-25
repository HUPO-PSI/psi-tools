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
package psidev.psi.tools.ontologyConfigReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used to mock URLs from memory.
 *
 * @author Aslak Helles&oslash;y
 * @author Konstantin Pribluda
 * @version $Revision$
 */
public class MockMemoryStreamHandler extends URLStreamHandler {

    public static final String MEMORY_PROTOCOL = "memory";

    private static Map<String, String> url2content = new HashMap<String, String>();

    public static void addContent( URL url, String content ) {
        url2content.put( url.toString(), content );
    }

    protected URLConnection openConnection( final URL u ) throws IOException {

        return new URLConnection( u ) {
            public void connect() throws IOException {
            }

            public InputStream getInputStream() throws IOException {
                if ( url2content.containsKey( u.toString() ) ) {
                    return new ByteArrayInputStream( url2content.get( u.toString() ).getBytes() );
                } else {
                    throw new IllegalStateException( "Could not find content for URL: '" + url.toString() + "'" );
                }
            }
        };
    }

    /**
     * this method is part of an really ugly hack around classloading problem in m2 / surefire plugin.
     * This shall be removed after surefire plugin is fixed
     */
    public static void initHandler() {
        try {
            URL.setURLStreamHandlerFactory( new URLStreamHandlerFactory() {

                public URLStreamHandler createURLStreamHandler( String protocol ) {
                    if ( "memory".equals( protocol ) ) {
                        return new MockMemoryStreamHandler();
                    }
                    return null;
                }
            } );
        } catch ( Throwable ex ) {
            // we just ignore this exception, because m2 does not fork properly
            // and URL does not like double handler definitions.
            // blame sun on unflexibility and surefire guys on classloader wrapping
        }
    }
}
