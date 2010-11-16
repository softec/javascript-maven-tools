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

/**
 * Goal which packages scripts and resources as a javascript archive to be
 * installed / deployed in maven repositories.
 * 
 * @goal package
 * @phase package
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class PackageMojo
    extends AbstractPackageMojo
{
    /**
     * The output directory of the js file.
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * Optional classifier
     * 
     * @parameter
     */
    private String classifier;

    /**
     * Location of the scripts files.
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File scriptsDirectory;

    File getOutputDirectory()
    {
        return outputDirectory;
    }

    String getClassifier()
    {
        return classifier;
    }

    File getScriptsDirectory()
    {
        return scriptsDirectory;
    }
}
