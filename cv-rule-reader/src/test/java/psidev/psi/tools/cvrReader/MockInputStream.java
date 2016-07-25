/*
 * =================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All
 * rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software
 *    itself, if and wherever such third-party acknowlegements
 *    normally appear.
 *
 * 4. The names "The Jakarta Project", "junitbook", "jia",
 *    "JUnit in Action" and "Apache Software Foundation" must not be
 *    used to endorse or promote products derived from this software
 *    without prior written permission. For written permission,
 *    please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * =================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For
 * more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package psidev.psi.tools.cvrReader;

import junit.framework.AssertionFailedError;

import java.io.InputStream;
import java.io.IOException;

/**
 * Allow to test defined inputs.
 * <br>
 * Code found in the book <b>JUnit in action</b> by <i>Vincent Massol</i>.
 *
 * @author Vincent Massol, Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: MockInputStream.java 2586 2004-02-20 15:57:49Z skerrien $
 */
public class MockInputStream extends InputStream {

    private String buffer;
    private int    position   = 0;
    private int    closeCount = 0;

    /**
     * Set the data that will be read later on from the stream.
     *
     * @param buffer
     */
    public void setBuffer( String buffer ) {
         this.buffer = buffer;
    }

    public int read() throws IOException {
        if( position == this.buffer.length() ) {
            return -1;
        }

        return this.buffer.charAt( this.position++ );
    }

    public void close() throws IOException {
        closeCount++;
        super.close();
    }

    /**
     * Allow the user of that mock object to check after use if it has been closed properly.
     * ie. the close() methodd should have been called one and only once by the user of the Stream.
     *
     * @throws junit.framework.AssertionFailedError if the close() method hasn't been called exactly once.
     */
    public void verify() throws AssertionFailedError {
        if( closeCount != 1 ) {
            throw new AssertionFailedError( "close() has been called "+ closeCount +" time"+
                    (closeCount>1?"s":"")+" but should have been called exactly  once." );
        }
    }
}