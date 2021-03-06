package org.codehaus.mojo.javascript;

/*
 * Derivative Work
 * Copyright 2010 SOFTEC sa. All rights reserved.
 *
 * Original Work
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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.javascript.archive.JavascriptArtifactManager;
import org.codehaus.mojo.javascript.assembler.Assembler;
import org.codehaus.mojo.javascript.assembler.AssemblerReader;
import org.codehaus.mojo.javascript.assembler.AssemblerReaderManager;
import org.codehaus.mojo.javascript.assembler.Script;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Goal which assemble javascript sources into the packaging directory. An
 * optional assembler descriptor can be set to configure scripts to be merged.
 * Other scripts are simply copied to the output directory.
 *
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CompileMojo
    extends AbstractMojo
{
    /** default includes pattern */
    protected static final String[] DEFAULT_INCLUDES = { "**/*.js" };

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Location of the source files.
     *
     * @parameter default-value="${basedir}/src/main/javascript"
     */
    protected File sourceDirectory;

    /**
     * The output directory of the assembled js file.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     */
    protected File outputDirectory;

    /**
     * The folder where javascript dependencies are extracted and taken during assembling
     *
     * @parameter default-value="${project.build.directory}/javascript-dependency"
     */
    protected File depsDirectory;

    /**
     * For dependencies, if true, create a folder named by the artifactId while unpacking
     *
     * @parameter
     */
    protected boolean useArtifactId;

    /**
     * Exclusion pattern.
     *
     * @parameter
     */
    protected String[] excludes;

    /**
     * Inclusion pattern.
     *
     * @parameter
     */
    protected String[] includes;

    /**
     * @component
     */
    protected AssemblerReaderManager assemblerReaderManager;

    /**
     * Descriptor for the strategy to assemble individual scripts sources into
     * destination.
     *
     * @parameter default-value="${basedir}/src/assembler/${project.artifactId}.xml"
     */
    protected File descriptor;

    /**
     * Descriptor file format (default or jsbuilder)
     *
     * @parameter
     */
    protected String descriptorFormat;

    /**
     * @component
     */
    protected JavascriptArtifactManager javascriptArtifactManager;

    // Compile-time dependency count
    protected int depsCount = 0;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if (outputDirectory == null) {
            getLog().error("OutputDirectory is null");
            throw new MojoExecutionException("outputDirectory must be specified");
        } else {
            getLog().debug("Creating outputDirectory " + outputDirectory);
            outputDirectory.mkdirs();
        }

        try
        {
            depsCount = getJavascriptArtifactManager().unpack( getProject(), DefaultArtifact.SCOPE_COMPILE,
                depsDirectory, useArtifactId );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack javascript dependencies", e );
        }

        Set merged = assemble();

        copyUnmerged(merged);
    }

    protected void copyUnmerged(Set merged) throws MojoExecutionException{
        if ( includes == null )
        {
            includes = DEFAULT_INCLUDES;
        }

        if( sourceDirectory.isDirectory() ) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( sourceDirectory );
            scanner.setExcludes( excludes );
            scanner.addDefaultExcludes();

            scanner.setIncludes( includes );
            scanner.scan();

            try
            {
                String[] files = scanner.getIncludedFiles();
                for ( int i = 0; i < files.length; i++ )
                {
                    String file = files[i];
                    if ( merged.contains( file ) )
                    {
                        continue;
                    }
                    File source = new File( sourceDirectory, file );
                    File dest = new File( outputDirectory, file );
                    dest.getParentFile().mkdir();
                    FileUtils.copyFile( source, dest );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to copy source files to " + outputDirectory,
                    e );
            }
        }
    }

    /**
     * Honor the assembly rules to build merged scripts from individual ones.
     *
     * @return a set of all script merged, to be skiped from the target
     * directory.
     * @throws MojoExecutionException
     */
    protected Set assemble()
        throws MojoExecutionException
    {
        if ( descriptor == null )
        {
            return Collections.EMPTY_SET;
        }

        if ( !descriptor.exists() )
        {
            if ( descriptor.getName().equals( project.getArtifactId() + ".xml" ) )
            {
                getLog().info( "No default assembler descriptor - just copy scripts" );
                return Collections.EMPTY_SET;
            }
            throw new MojoExecutionException( "The assembler descriptor does not exists : "
                + descriptor );
        }

        if ( descriptorFormat == null )
        {
            descriptorFormat = "default";
            if ( descriptor.getName().toLowerCase().endsWith( ".jsb" ) )
            {
                descriptorFormat = "jsbuilder";
            }
        }

        Assembler assembler;
        try
        {
            AssemblerReader reader = assemblerReaderManager.getAssemblerReader( descriptorFormat );
            assembler = reader.getAssembler( descriptor );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to read the assembler descriptor "
                + descriptor.getAbsolutePath(), e );
        }
        return assemble( assembler );
    }

    protected Set assemble( Assembler assembler )
        throws MojoExecutionException
    {
        Set merged = new HashSet();

        DirectoryScanner scanner = null;
        if( sourceDirectory.isDirectory() ) {
            scanner = new DirectoryScanner();
            scanner.setBasedir( sourceDirectory );
            scanner.setExcludes( excludes );
            scanner.addDefaultExcludes();
        }

        DirectoryScanner depsScan = null;
        if( depsCount > 0 ) {
            depsScan = new DirectoryScanner();
            depsScan.setBasedir( depsDirectory );
            depsScan.setExcludes( excludes );
            depsScan.addDefaultExcludes();
        } else {
            getLog().info( "No compile time dependency - just assembling local scripts" );
        }

        if( scanner == null && depsScan == null ) {
            throw new MojoExecutionException( "Nothing to compile or assemble ?" );
        }

        for ( Iterator iterator = assembler.getScripts().iterator(); iterator.hasNext(); )
        {
            Script script = (Script) iterator.next();
            String fileName = script.getFileName();
            PrintWriter writer = null;
            try
            {
                File target = new File( outputDirectory, fileName );
                target.getParentFile().mkdirs();
                writer = new PrintWriter( target );

                List scriptOrderedIncludes = script.getIncludes();
                for ( Iterator iter = scriptOrderedIncludes.iterator(); iter.hasNext(); )
                {
                    String scriptInclude = (String) iter.next();

                    if ((scanner == null ||
                        appendScriptFile(sourceDirectory, scanner, writer, scriptInclude, merged) < 1)
                        && depsScan != null)
                    {
                        appendScriptFile(depsDirectory, depsScan, writer, scriptInclude, null);
                    }
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to write merged file " + fileName, e );
            }
            finally
            {
                IOUtil.close( writer );
            }
        }
        return merged;
    }

    protected int appendScriptFile(File dir, DirectoryScanner scanner, PrintWriter writer, String scriptInclude, Set merged)
        throws IOException
    {
        scanner.setIncludes( new String[] { scriptInclude } );
        scanner.scan();

        String[] files = scanner.getIncludedFiles();
        for ( int i = 0; i < files.length; i++ )
        {
            String file = files[i];
            File source = new File( dir, file );
            IOUtil.copy( new FileReader( source ), writer );
            writer.println();
            if( merged != null ) {
                merged.add( file );
            }
        }

        return files.length;
    }

    /**
     * @return the project
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * @return the javascript artifact manager
     */
    protected JavascriptArtifactManager getJavascriptArtifactManager()
    {
        return javascriptArtifactManager;
    }
}
