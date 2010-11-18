package org.codehaus.mojo.javascript.compress;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.dojotoolkit.shrinksafe.Compressor;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.ShellContextFactory;

/**
 * A JS compressor that uses Dojo modified Rhino engine to compress the script.
 * The resulting compressed-js is garanteed to be functionaly equivalent as this
 * is the internal view of the rhino context.
 * 
 * @author <a href="mailto:nicolas@apache.org">nicolas De Loof</a>
 */
public class ShrinksafeCompressor
    implements JSCompressor
{
    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.mojo.javascript.compress.JSCompressor#compress(java.io.File,
     * java.io.File, int, int)
     */
    public void compress( final File input, File compressed, int level, int language )
        throws CompressionException
    {
        PrintStream out = null;
        try
        {
            out = new PrintStream( new FileOutputStream(compressed) );

            final Global global = new Global();
            global.setErr(System.err);
            global.setOut(out);

            final ToolErrorReporter errorReporter = new ToolErrorReporter( false, global.getErr() );
            errorReporter.setIsReportingWarnings( false );

            final ShellContextFactory shellContextFactory = new ShellContextFactory();
            shellContextFactory.setLanguageVersion( language );
            shellContextFactory.setOptimizationLevel( level );
            shellContextFactory.setErrorReporter( errorReporter );
            shellContextFactory.setStrictMode( true );

            global.init(shellContextFactory);

            shellContextFactory.call( new ContextAction()
            {
                public Object run( Context context )
                {
                    Object[] args = new Object[1];
                    global.defineProperty( "arguments", args, ScriptableObject.DONTENUM );
                    try {
                        global.getOut().println(
                            Compressor.compressScript(FileUtils.readFileToString(input), 0, 1, false, null));
                    } catch (IOException ioe) {
					    Context.reportError(ioe.toString());
				    }

                    return null;
                }
            } );
        }
        catch ( Exception e )
        {
            throw new CompressionException( "Failed to create compressed file", e, input );
        }
        finally
        {
            IOUtil.close( out );
        }
    }
}
