/*
 * Copyright 2011 SOFTEC sa. All rights reserved.
 *
 * This source code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Luxembourg
 * License.
 *
 * To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-nc-nd/3.0/lu/
 * or send a letter to Creative Commons, 171 Second Street,
 * Suite 300, San Francisco, California, 94105, USA.
 */

package org.codehaus.mojo.javascript.titanium;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;

/**
 * Class to strip lines from a file.
 */
public class FileStrip {

    /**
     * A list of special token to recognize lines to be removed from scripts (debugging
     * code).
     */
    private String[] strips;

    /**
     * A special token to recognize lines to be removed from scripts (debugging
     * code).
     */
    private String strip;

    /**
     * The folder where stripped files will be created.
     */
    private File strippedDirectory;

    /**
     * Retrieve the special token to recognize lines to be removed from scripts.
     * @return The strip token. May be null.
     */
    public String getStrip() {
        return strip;
    }

    public void setStrip(String strip) {
        this.strip = strip;
    }

    public File getStrippedDirectory() {
        return strippedDirectory;
    }

    public void setStrippedDirectory(File strippedDirectory) {
        this.strippedDirectory = strippedDirectory;
    }

    public String[] getStrips() {
        return strips;
    }

    public void setStrips(String[] strips) {
        this.strips = strips;
    }

    public FileStrip(File strippedDirectory, String strip) {
        this(strippedDirectory, strip, null);
    }

    public FileStrip(File strippedDirectory, String[] strips) {
        this(strippedDirectory, null, strips);
    }

    public FileStrip(File strippedDirectory, String strip, String[] strips) {
        this.strippedDirectory = strippedDirectory;
        this.strip = strip;
        this.strips = strips;
    }


    /**
     * Strip the specified file.
     * @param name The name of the destination file.
     * Will be generated in the {@link #strippedDirectory} folder.
     * @param file The file to strip.
     * @return The stripped file.
     * @throws MojoExecutionException When an error occurs while stripping the file.
     */
    public File strip(String name, File file) throws MojoExecutionException {
        return stripDebugs(name, file);
    }

    private File stripDebugs( String name, File file )
        throws MojoExecutionException
    {
        if ( strip == null && (strips == null || strips.length == 0))
        {
            return file;
        }

        File stripped = new File( getStrippedDirectory(), name );
        stripped.getParentFile().mkdirs();
        if ( file.equals( stripped ) )
        {
            try
            {
                File temp = File.createTempFile( "stripped", ".js" );
                stripDebugs(file, temp);
                FileUtils.copyFile(temp, file);
                temp.delete();
                return file;
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error creating temp file for stripping", e );
            }
        }
        else
        {
            stripDebugs(file, stripped);
            return stripped;
        }
    }

    private void stripDebugs( File file, File stripped )
            throws MojoExecutionException
    {
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            PrintWriter writer = new PrintWriter( stripped );
            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                String trimmed = line.trim();
                boolean stripLine = (strip != null && trimmed.startsWith( strip ));
                if( strips != null ) {
                    for( int i=0, len = strips.length; !stripLine && i < len; i++ ) {
                        stripLine = trimmed.startsWith( strips[i] );
                    }
                }
                if ( !stripLine )
                {
                    writer.println( line );
                }
            }
            IOUtil.close(reader);
            IOUtil.close( writer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to strip debug code in " + file, e );
        }
    }


}
