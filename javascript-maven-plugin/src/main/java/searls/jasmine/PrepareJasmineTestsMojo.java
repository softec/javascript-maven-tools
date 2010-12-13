/*
 * Copyright 2010 SOFTEC sa. All rights reserved.
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
package searls.jasmine;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.javascript.archive.JavascriptArtifactManager;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * Goal which copies scripts to the jasmine target directory.
 *
 * @goal prepare-jasmine-tests
 * @phase test-compile
 * @requiresDependencyResolution test
 */
public class PrepareJasmineTestsMojo extends AbstractJasmineMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private JavascriptArtifactManager javascriptArtifactManager;
    
    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if(skipTests || !jasmineTestSourceDirectory.exists()) {
            getLog().info("No Jasmine tests, skipping Jasmine tests preparation.");
            return;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.addDefaultExcludes();
        try
        {
            File destDir;
            String[] files;

            if( sourceDirectory.exists() ) {
                destDir = new File(jasmineTargetDir, srcDirectoryName);
                scanner.setBasedir( sourceDirectory );
                scanner.scan();
                files = scanner.getIncludedFiles();
                for ( int i = 0; i < files.length; i++ )
                {
                    File destFile = new File( destDir, files[i] );
                    destFile.getParentFile().mkdirs();
                    FileUtils.copyFile( new File( sourceDirectory, files[i] ), destFile );
                }
            }

            destDir = new File(jasmineTargetDir, specDirectoryName);
            scanner.setBasedir( jasmineTestSourceDirectory );
            scanner.scan();
            files = scanner.getIncludedFiles();
            for ( int i = 0; i < files.length; i++ )
            {
                File destFile = new File( destDir, files[i] );
                destFile.getParentFile().mkdirs();
                FileUtils.copyFile( new File( jasmineTestSourceDirectory, files[i] ), destFile );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy scripts in " + jasmineTargetDir );
        }

        try
        {
            javascriptArtifactManager.unpack( project, DefaultArtifact.SCOPE_TEST, new File(
                jasmineTargetDir, libsDirectory ), useArtifactId );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack javascript dependencies", e );
        }
    }
}
