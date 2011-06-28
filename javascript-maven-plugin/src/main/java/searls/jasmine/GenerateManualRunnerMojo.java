/*
 * Derivative Work
 * Copyright 2010 SOFTEC sa. All rights reserved.
 *
 * Original Work
 * Copyright 2010 Justin Searls
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.javascript.archive.JavascriptArtifactManager;
import org.codehaus.plexus.archiver.ArchiverException;

import searls.jasmine.runner.SpecRunnerHtmlGenerator;
import searls.jasmine.runner.SpecRunnerHtmlGenerator.ReporterType;

/**
 * @component
 * @goal generateManualRunner
 * @phase generate-test-sources
 * @requiresDependencyResolution test
 */
public class GenerateManualRunnerMojo extends AbstractJasmineMojo {
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


	public void execute() throws MojoExecutionException, MojoFailureException {
		if(sourceDirectory.exists() && jasmineTestSourceDirectory.exists()) {
            try
            {
                javascriptArtifactManager.unpack( project, DefaultArtifact.SCOPE_TEST, new File(
                    jasmineTargetDir, libsDirectory ), useArtifactId );
            }
            catch ( ArchiverException e )
            {
                throw new MojoExecutionException( "Failed to unpack javascript dependencies", e );
            }
            
			getLog().info("Generating runner '"+manualSpecRunnerHtmlFileName+"' in the Jasmine plugin's target directory to open in a browser to facilitate faster feedback.");
			try {
				writeSpecRunnerToSourceSpecDirectory();
			} catch (Exception e) {
				throw new MojoFailureException(e,"JavaScript Test execution failed.","Failed to execute generated SpecRunner.html");
			}
		} else {
            if( jasmineTestSourceDirectory.equals(jsunitTestSourceDirectory) ) {
                getLog().warn("Skipping manual spec runner generation. Check to make sure that both JavaScript directories `"+
                    sourceDirectory.getAbsolutePath()+"` and `"+ jasmineTestSourceDirectory.getAbsolutePath()+"` exist.");
            }
		}
	}

	private void writeSpecRunnerToSourceSpecDirectory() throws IOException {
		SpecRunnerHtmlGenerator htmlGenerator = new SpecRunnerHtmlGenerator(preloadSources, sourceDirectory,
            jasmineTestSourceDirectory, new File(jasmineTargetDir,libsDirectory));
		String runner = htmlGenerator.generate(pluginArtifacts, ReporterType.TrivialReporter);
		
		File destination = new File(jasmineTargetDir,manualSpecRunnerHtmlFileName);
		String existingRunner = loadExistingManualRunner(destination);
		
		if(!StringUtils.equals(runner, existingRunner)) {
			FileUtils.writeStringToFile(destination, runner);
		} else {
			getLog().info("Skipping spec runner generation, because an identical spec runner already exists.");
		}
	}

	private String loadExistingManualRunner(File destination) {
		String existingRunner = null;
		try {
			if(destination.exists()) {
				existingRunner = FileUtils.readFileToString(destination);
			}
		} catch(Exception e) {
			getLog().warn("An error occurred while trying to open an existing manual spec runner. Continuing");
		}
		return existingRunner;
	}

}
