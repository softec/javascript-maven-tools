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
 * Goal used to package stripped JavaScript files and package them as a javascript archive
 * attaching this new artifact to the project for beeing installed / deployed with
 * the regular uncompressed js-archive.
 *
 * @goal attach-stripped
 * @phase package
 */
public class AttachStrippedMojo
    extends AbstractPackageMojo
{
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
     * classifier for the compressed artifact
     *
     * @parameter default-value="stripped"
     */
    private String classifier;

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractPackageMojo#getOutputDirectory()
     */
    protected File getOutputDirectory()
    {
        return buildDirectory;
    }

    String getClassifier()
    {
        return classifier;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.mojo.javascript.AbstractPackageMojo#getScriptsDirectory()
     */
    protected File getScriptsDirectory()
    {
        return strippedDirectory;
    }
}
