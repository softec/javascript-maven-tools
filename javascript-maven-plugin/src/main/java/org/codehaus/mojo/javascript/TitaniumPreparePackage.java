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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * This goal strips and compress the source scripts in order to
 * create the distribution package.
 *
 * @phase package
 * @goal titanium-prepare-package
 */
public class TitaniumPreparePackage extends AbstractCompressMojo {
    /**
     * <p>The package execution mode.</p>
     * <p>Allow the execution of the package on an emulator/device.</p>
     * <p>Values are:</p>
     * <dl>
     *   <dt>none</dt>
     *   <dd>Do not execute. (Default value)</dd>
     *   <dt>virtual</dt>
     *   <dd>Execute on an emulator whose settings are specified in
     *   {@link TitaniumPackageMojo#virtualDevice}.</dd>
     *   <dt>device</dt>
     *   <dd>Execute on a connected device.</dd>
     * </dl>
     *
     * @parameter default-value="none" expression="${executeMode}"
     */
    protected String executeMode;

    /**
     * The platform for which the code should be compiled.
     * android, iphone, ipad, universal
     *
     * @parameter expression="${platform}"
     * @required
     */
    protected String platform;

    /**
     * Force compression even if not in "none" executeMode.
     *
     * @parameter default-value="false" expression="${forceCompress}"
     */
    protected boolean forceCompress;

    /**
     * optional extension for the compressed artifact. Example "compressed"
     *
     * @parameter
     */
    private String scriptClassifier;

    /**
     * The intput directory for the source javascript files.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File scriptsDirectory;

    /**
     * The output directory of the compressed javascript files.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File compressedDirectory;

    /**
     * The output directory of the compressed javascript files.
     *
     * @parameter default-value="${project.build.directory}/stripped"
     */
    private File strippedDirectory;

    /**
     * The name of the script directory.
     * <p>This parameter is optional it defaults to <code>platform-scripts</code></p>
     * @parameter
     */
    private String scriptsDir;

    /**
     * The name of the stripped directory.
     * <p>This parameter is optional, it defaults to <code>platform-stripped</code></p>
     * @parameter
     */
    private String strippedDirName;

    protected String getScriptsDir() {
        if (scriptsDir == null) {
            scriptsDir = platform + "-scripts";
        }
        return scriptsDir;
    }

    protected String getStrippedDirName() {
        if (strippedDirName == null) {
            strippedDirName = platform + "-stripped";
        }
        return strippedDirName;
    }
    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getExtension()
     */
    public String getExtension()
    {
        return scriptClassifier;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getOutputDirectory()
     */
    protected File getOutputDirectory()
    {
        return new File(compressedDirectory, platform + File.separator + "Resources");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getOutputDirectory()
     */
    protected File getStrippedDirectory()
    {
        return new File(strippedDirectory, getStrippedDirName());
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getSourceDirectory()
     */
    protected File getSourceDirectory()
    {
        return new File(scriptsDirectory, getScriptsDir());
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (!getSourceDirectory().exists()) {
            getLog().info("Skipping prepare package as source directory doesn't exist");
            return;
        }
        if (forceCompress || executeMode.equals("none")) {
            super.execute();
        } else {
            getLog().info("Skipping compress and strip, performing simple scripts copy");
            try {
                copy();
            } catch (IOException ioe) {
                throw new MojoExecutionException("Error while copying scripts");
            }
        }
    }

    protected void copy() throws IOException {
        DirectoryScanner scanner = getDirectoryScanner();
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        getOutputDirectory().mkdirs();

        for (String file : files) {
            File sourceFile = new File(scanner.getBasedir(), file);
            File destFile = new File(getOutputDirectory(), file);
            FileUtils.copyFile(sourceFile, destFile);
        }
    }
}
