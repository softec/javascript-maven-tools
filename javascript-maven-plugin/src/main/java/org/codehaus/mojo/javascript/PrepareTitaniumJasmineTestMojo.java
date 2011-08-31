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

package org.codehaus.mojo.javascript;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.shade.org.codehaus.plexus.util.FileUtils;
import org.codehaus.mojo.javascript.archive.JavascriptArtifactManager;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import searls.jasmine.io.IOUtilsWrapper;
import searls.jasmine.runner.SpecRunnerTitaniumGenerator;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import java.io.*;
import java.util.*;

/**
 * This Mojo create the titanium project structure
 * required to execute the jasmine tests.
 *
 * @goal prepare-titanium-jasmine-tests
 * @phase test-compile
 * @requiresDependencyResolution test
 */
public class PrepareTitaniumJasmineTestMojo extends AbstractMojo {

    /** default includes pattern */
    protected static final String[] DEFAULT_INCLUDES = { "**/*.js" };
    private static final String JAVASCRIPT_TYPE = "js";

    /**
	 * JavaScript sources (typically vendor/lib dependencies) that need to be loaded
	 * before other sources (and specs) in a particular order, these are relative to the ${sourceDirectory}
	 * directory! Therefore, if jquery.js is in `${sourceDirectory}/vendor`, you would configure:
	 * <code>
	 *  	&lt;preloadSources&gt;
	 *			&lt;source&gt;vendor/z.js&lt;/source&gt;
	 *		&lt;/preloadSources&gt;
	 * </code>
	 * And z.js would load before all the other sources and specs.
	 *
	 * @parameter
	 */
	protected List<String> preloadSources;

    /**
     * The folder for javascripts dependencies
     *
     * @parameter expression="${scripts}" default-value="lib"
     */
    protected String libsDirectory;

    /**
     * The output folder for jasmine spec files.
     * @parameter expression="${specs}" default-value="specs"
     */
    protected String specsDirectory;

    /**
     * The location of the javascript test files
     * @parameter default-value="${project.basedir}${file.separator}src${file.separator}test${file.separator}javascript" expression="${jsunitTestSourceDirectory}"
     */
    protected File jasmineTestSourceDirectory;

    /**
     * <p>The platform for which the code should be packaged.</p>
     * <p>Supported platforms are:</p>
     * <dl>
     *     <dt>android</dt>
     *     <dd>Package for the android platform.</dd>
     *     <dt>iphone</dt>
     *     <dd>Package for the iPhone platform.</dd>
     *     <dt>ipad</dt>
     *     <dd>Package for the iPad platform.</dd>
     *     <dt>universal</dt>
     *     <dd>Package for iPhone and iPad.</dd>
     * </dl>
     *
     * @parameter expression="${platform}"
     * @required
     */
    protected String platform;

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
     * The output directory of the test files.
     * @parameter default-value="${project.build.testOutputDirectory}"
     */
    protected File testOutputDirectory;

    /**
	 * @parameter default-value="${plugin.artifacts}"
	 */
	protected List<Artifact> pluginArtifacts;

    /**
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT
     * RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${maven.test.skip}"
     */
    protected boolean skipTests;

    /**
     * Exclusion pattern.
     * <p>Allow to specify which jasmine spec files should be excluded.</p>
     *
     * @parameter
     */
    protected String[] specExcludes;

    protected File getPlatformTestSourceDirectory() {
        return new File(jasmineTestSourceDirectory, platform);
    }

    protected File getPlatformTestOutputDirectory() {
        return new File(testOutputDirectory, platform);
    }

    protected File getTiProjectDirectory() {
        return new File(outputDirectory, platform);
    }

    protected File getTiProjectResourceDirectory() {
        return new File(getTiProjectDirectory(), "Resources");
    }

    /**
     * Use the artifactId as folder
     *
     * @parameter
     */
    protected boolean useArtifactId;

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
        if(skipTests || !jasmineTestSourceDirectory.exists()) {
            getLog().info("No Jasmine tests, skipping Jasmine tests preparation.");
            return;
        }

        getLog().info("Processing source folder: " + outputDirectory.getAbsolutePath());
        getPlatformTestOutputDirectory().mkdirs();
        File depsDirectory = new File(getPlatformTestOutputDirectory(), "Resources" + File.separator + libsDirectory);
        depsDirectory.mkdirs();

        try
        {
            javascriptArtifactManager.unpack( project, DefaultArtifact.SCOPE_TEST, depsDirectory, useArtifactId );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack javascript dependencies", e );
        }


        // 1. Copy source files
        List<String> sourceFiles = new ArrayList<String>();
        try {
            File appDestDirectory = new File(getPlatformTestOutputDirectory(), "Resources");
            if (getTiProjectResourceDirectory().exists()) {
                appDestDirectory.mkdirs();
                FileUtils.copyDirectoryStructure(getTiProjectResourceDirectory(), appDestDirectory);

                DirectoryScanner scanner = new DirectoryScanner();
                scanner.setBasedir(getTiProjectResourceDirectory());
                scanner.setIncludes(DEFAULT_INCLUDES);
                scanner.setExcludes(new String[] { "**/app.js" });
                scanner.addDefaultExcludes();
                scanner.scan();
                String[] foundFiles = scanner.getIncludedFiles();
                if (foundFiles != null) {
                    for (String fFile : foundFiles) {
                        sourceFiles.add(fFile);
                    }
                }
            } else {
                getLog().info("Titanium resources folder doesn't exist.");
                if (getTiProjectDirectory().exists()) {
                    getLog().info("Trying to copy source script from: " + getTiProjectDirectory().getAbsolutePath());
                    appDestDirectory.mkdirs();
                    FileUtils.copyDirectoryStructure(getTiProjectDirectory(), appDestDirectory);

                    DirectoryScanner scanner = new DirectoryScanner();
                    scanner.setBasedir(getTiProjectDirectory());
                    scanner.setIncludes(DEFAULT_INCLUDES);
                    scanner.setExcludes(new String[] { "**/app.js" });
                    scanner.addDefaultExcludes();
                    scanner.scan();
                    String[] foundFiles = scanner.getIncludedFiles();
                    if (foundFiles != null) {
                        for (String fFile : foundFiles) {
                            sourceFiles.add(fFile);
                        }
                    }
                } else if (outputDirectory.exists()) {
                    getLog().info("Processing output directory: " + outputDirectory.getAbsolutePath());
                    appDestDirectory.mkdirs();
                    FileUtils.copyDirectoryStructure(outputDirectory, appDestDirectory);

                    DirectoryScanner scanner = new DirectoryScanner();
                    scanner.setBasedir(outputDirectory);
                    scanner.setIncludes(DEFAULT_INCLUDES);
                    scanner.setExcludes(new String[] { "**/app.js" });
                    scanner.addDefaultExcludes();
                    scanner.scan();
                    String[] foundFiles = scanner.getIncludedFiles();
                    if (foundFiles != null) {
                        for (String fFile : foundFiles) {
                            sourceFiles.add(fFile);
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            getLog().error("Failed to copy titanium project files to test directory", ioe);
            return;
        }

        // 2. Assemble test scripts
        copySpecFiles();

        // 3. generate app.js file
        createTestAppJs(sourceFiles);

        // 4. Ensure tiapp.xml presence
        try {
            ensureTiApp();
        } catch (Throwable t) {
            throw new MojoExecutionException("Unable to create test application tiapp.xml file");
        }
    }

    protected void ensureTiApp() throws IOException {
        File tiapp = new File(getPlatformTestOutputDirectory(), "tiapp.xml");
        if (!tiapp.exists()) {
            getLog().info("Generating custom tiapp.xml file.");
            IOUtilsWrapper wrapper = new IOUtilsWrapper();
            String content = wrapper.toString("/titanium/tiapp.xml");
            StringTemplate template = new StringTemplate(content, DefaultTemplateLexer.class);
            template.setAttribute("id", project.getGroupId() + "." + project.getArtifactId() + ".test");
            template.setAttribute("name", project.getName() + " Test");
            template.setAttribute("version", project.getVersion());
            template.setAttribute("guid", UUID.randomUUID().toString());
            if (project.getDescription() != null) {
                template.setAttribute("description", project.getDescription());
            } else {
                template.setAttribute("description", "not specified");
            }

            FileWriter writer = new FileWriter(tiapp);
            try {
                IOUtil.copy(template.toString(), writer);
            } finally {
                IOUtil.close(writer);
            }
        }
    }

    protected void copySpecFiles() throws MojoExecutionException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(jasmineTestSourceDirectory);
        scanner.setIncludes(DEFAULT_INCLUDES);
        scanner.setExcludes(specExcludes);
        scanner.addDefaultExcludes();

        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        File testFolder = new File(getPlatformTestOutputDirectory(), "Resources" + File.separator + specsDirectory);
        testFolder.mkdirs();

        try {
            for (String file : files) {
                File srcFile = new File(jasmineTestSourceDirectory, file);
                File destFile = new File(testFolder, file);
                destFile.getParentFile().mkdirs();
                FileUtils.copyFile(srcFile, destFile);
            }
        } catch (IOException ioe) {
            throw new MojoExecutionException("Error while creating jasmine test file script", ioe);
        }
    }

    protected void createTestAppJs(List<String> sourceFiles)
    throws MojoExecutionException {
        File appFile = new File(getPlatformTestOutputDirectory(), "Resources" + File.separatorChar + "app.js");
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(appFile);
            SpecRunnerTitaniumGenerator generator = new SpecRunnerTitaniumGenerator(preloadSources,
                    sourceFiles,
                    new File(getPlatformTestOutputDirectory(), "Resources"),
                    libsDirectory,
                    specsDirectory);
            writer.write(generator.generate(SpecRunnerTitaniumGenerator.ReporterType.TITANIUM));
        } catch (IOException ioe) {
            throw new MojoExecutionException("Error while generating jasmine test application file", ioe);
        }   finally {
            IOUtil.close(writer);
        }
    }
}
