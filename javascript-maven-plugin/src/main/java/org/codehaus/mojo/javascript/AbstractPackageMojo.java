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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.javascript.archive.JavascriptArchiver;
import org.codehaus.mojo.javascript.archive.Types;

/**
 * Abstract Goal which packages scripts and resources as a javascript archive to be
 * installed / deployed in maven repositories.
 */
public abstract class AbstractPackageMojo
    extends AbstractMojo
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
    MavenProjectHelper projectHelper;

    /**
     * The filename of the js file.
     *
     * @parameter default-value="${project.build.finalName}"
     */
    private String finalName;

    /**
     * Plexus archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" role-hint="javascript"
     * @required
     */
    private JavascriptArchiver archiver;

    /**
     * @parameter
     */
    private File manifest;

    abstract File getOutputDirectory();

    abstract String getClassifier();

    abstract File getScriptsDirectory();

    public void execute()
        throws MojoExecutionException
    {
        File jsarchive = (getClassifier() != null)
            ? new File( getOutputDirectory(), finalName + "-" + getClassifier() + "." + Types.JAVASCRIPT_EXTENSION )
            : new File( getOutputDirectory(), finalName + "." + Types.JAVASCRIPT_EXTENSION );

        try
        {
            if ( manifest != null )
            {
                archiver.setManifest( manifest );
            }
            else
            {
                archiver.createDefaultManifest( project );
            }
            if (getScriptsDirectory() != null && !getScriptsDirectory().exists()) {
                getScriptsDirectory().mkdirs();
            }
            archiver.addDirectory( getScriptsDirectory() );
            String groupId = project.getGroupId();
            String artifactId = project.getArtifactId();
            archiver.addFile( project.getFile(), "META-INF/maven/" + groupId + "/" + artifactId
                + "/pom.xml" );
            archiver.setDestFile( jsarchive );
            archiver.createArchive();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to create the javascript archive", e );
        }

        if ( getClassifier() != null )
        {
            projectHelper.attachArtifact( project, Types.JAVASCRIPT_TYPE, getClassifier(), jsarchive );
        }
        else
        {
            project.getArtifact().setFile( jsarchive );
        }
    }
}
