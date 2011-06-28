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
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

public abstract class AbstractJasmineMojo extends AbstractMojo {
	/** Properties in order of most-to-least interesting for client projects to override **/
	
	/**
	 * @parameter default-value="${project.basedir}${file.separator}src${file.separator}main${file.separator}javascript" expression="${sourceDirectory}"
	 */
	protected File sourceDirectory;

    /**
     * Base directory for jsunit test.
     *
     * @parameter default-value="${project.basedir}${file.separator}src${file.separator}test${file.separator}javascript" expression="${jsunitTestSourceDirectory}"
     */
    protected File jsunitTestSourceDirectory;

	/**
	 * @parameter default-value="${project.basedir}${file.separator}src${file.separator}test${file.separator}javascript" expression="${jsunitTestSourceDirectory}"
	 */
	protected File jasmineTestSourceDirectory;

	/**
	 * @parameter default-value="js" expression="${packageJavaScriptPath}"
	 */
	protected String packageJavaScriptPath;
	
	/**
	 * JavaScript sources (typically vendor/lib dependencies) that need to be loaded
	 * before other sources (and specs) in a particular order, these are relative to the ${sourceDirectory}
	 * directory! Therefore, if jquery.js is in `${sourceDirectory}/vendor`, you would configure:
	 * 
	 *  	&lt;preloadSources&gt;
	 *			&lt;source&gt;vendor/z.js&lt;/source&gt;
	 *		&lt;/preloadSources&gt;
	 * 
	 * And z.js would load before all the other sources and specs.
	 * 
	 * @parameter
	 */
	protected List<String> preloadSources;
	
	/**
	 * @parameter default-value="${project.build.directory}${file.separator}jasmine"
	 */
	protected File jasmineTargetDir;

    /**
     * The folder for javascripts dependencies
     *
     * @parameter expression="${scripts}" default-value="lib"
     */
    protected String libsDirectory;

    /**
     * Use the artifactId as folder
     *
     * @parameter
     */
    protected boolean useArtifactId;    
	
    /**
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT
     * RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${maven.test.skip}"
     */
    protected boolean skipTests;

    /**
     * Set this to true to ignore a failure during testing. Its use is NOT
     * RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${maven.test.failure.ignore}"
     */
    protected boolean testFailureIgnore;
	
	/**
	 * @parameter default-value="${project.build.directory}${file.separator}${project.build.finalName}"
	 */
	protected File packageDir;
	
	/**
	 * @parameter default-value="SpecRunner.html"
	 */
	protected String specRunnerHtmlFileName;
	
	/**
	 * @parameter default-value="ManualSpecRunner.html"
	 */
	protected String manualSpecRunnerHtmlFileName;
	
	/**
	 * @parameter default-value="spec"
	 */
	protected String specDirectoryName;
	
	/**
	 * @parameter default-value="src"
	 */
	protected String srcDirectoryName;

	/**
	 * @parameter default-value="${project}"
	 */
	protected MavenProject mavenProject;

	/**
	 * @parameter default-value="${plugin.artifacts}"
	 */
	protected List<Artifact> pluginArtifacts;
}
