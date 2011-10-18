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

package org.codehaus.mojo.javascript;

import java.io.File;

/**
 * Goal used to strip and compress the JavaScript files from the target script directory
 *
 * @goal compress
 * @phase prepare-package
 */
public class CompressMojo
    extends AbstractCompressMojo
{
    /**
     * The output directory of the compressed javascript files.
     *
     * @parameter default-value="${project.build.directory}/compressed"
     */
    private File compressedDirectory;

    /**
     * The output directory of the compressed javascript files.
     *
     * @parameter default-value="${project.build.directory}/stripped"
     */
    private File strippedDirectory;

    /**
     * The output directory of the compressed javascript archive.
     *
     * @parameter default-value="${project.build.directory}"
     */
    private File buildDirectory;

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
     * The compressor to used. Either "shrinksafe", "yahooui" or "jsmin" for default compressor,
	 * or a custom one provided as an artifact in repo org.codehaus.mojo.javascript:[xxx]-compressor.
     * <p>Use "none" to avoid compression.</p>
     *
     * @parameter default-value="jsmin"
     */
    private String compressor;

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
        return compressedDirectory;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getOutputDirectory()
     */
    protected File getStrippedDirectory()
    {
        return strippedDirectory;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getSourceDirectory()
     */
    protected File getSourceDirectory()
    {
        return scriptsDirectory;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractCompressMojo#getCompressorName()
     */
    @Override
    protected String getCompressorName() {
        return compressor;
    }
}
