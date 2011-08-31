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

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.javascript.archive.JavascriptArtifactManager;
import org.codehaus.mojo.javascript.assembler.AssemblerReaderManager;
import org.codehaus.mojo.javascript.assembler.Assembler;
import org.codehaus.mojo.javascript.assembler.AssemblerReader;
import org.codehaus.mojo.javascript.assembler.AssemblerReaderManager;
import org.codehaus.mojo.javascript.assembler.Script;
import org.codehaus.mojo.javascript.titanium.FileStrip;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;
import java.util.*;

/**
 * Goal that will place in a titanium build directory the file needed
 * to create a titanium package.
 *
 * @phase compile
 * @goal titanium-compile
 * @plexus.component role-hint="titanium"
 */
public class TitaniumCompileMojo extends CompileMojo {
    private static Map<String, String> platformPatterns;

    static {
        platformPatterns = new HashMap<String, String>();
        platformPatterns.put("android", "**/android/**");
        platformPatterns.put("iphone", "**/iphone/**");
    }

    /**
     * The platform for which the code should be compiled.
     * android, iphone, ipad, universal
     *
     * @parameter expression="${platform}"
     * @required
     */
    private String platform;

    /**
     * The name of the script directory.
     * <p>This parameter is optional it defaults to <code>platform-scripts</code></p>
     * @parameter
     */
    private String scriptsDir;

    protected String getScriptsDir() {
        if (scriptsDir == null) {
            scriptsDir = platform + "-scripts";
        }
        return scriptsDir;
    }

    private String getNormalizedPlatform() {
        if (platform.equals("universal") || platform.equals("ipad")) {
            return "iphone";
        } else {
            return platform;
        }
    }
    /**
     * The Titanium tiapp.xml configuration.
     * If not specified a default tiapp.xml file will be generated
     *
     * @parameter
     */
    protected Tiapp tiapp;

    /**
     * The folder for javascripts dependencies
     *
     * @parameter expression="${scripts}" default-value="lib"
     */
    protected String libsDirectory;

    protected File getPlatformOutputDirectory() {
        return new File(outputDirectory, getScriptsDir());
    }


    /**
     * Perform the titanium compilation phase.
     * <p/>
     * <ol>
     * <li>Extract the dependencies in depsDirectory</li>
     * <li>Copy the sourceDirectory files (using excludes and includes) to titaniumDirectory</li>
     * <li>Move files located under "platform" folder outside of the platform folder.
     * Based on the selected platform</li>
     * <li>Assemble the files in one App.js file</li>
     * </ol>
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDirectory = getPlatformOutputDirectory();

        // Add the runtime dependencies to the lib folder
        try
        {
            javascriptArtifactManager.unpack( project, DefaultArtifact.SCOPE_RUNTIME, new File(
                outputDirectory, libsDirectory ), useArtifactId );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack javascript dependencies", e );
        }

        super.execute();
    }

    private String[] getAllPlatformExcludes() {
        List<String> result = new ArrayList<String>();
        if (excludes != null) {
            result.addAll(Arrays.asList(excludes));
        }
        for (Map.Entry<String, String> p : platformPatterns.entrySet()) {
            result.add(p.getValue());
        }
        return result.toArray(new String[result.size()]);
    }

    private String[] getPlatformExcludes() {
        List<String> result = new ArrayList<String>();
        if (excludes != null) {
            result.addAll(Arrays.asList(excludes));
        }
        for (Map.Entry<String, String> p : platformPatterns.entrySet()) {
            if (p.getKey().compareToIgnoreCase(getNormalizedPlatform()) != 0) {
                result.add(p.getValue());
            }
        }

        return result.toArray(new String[result.size()]);
    }

    protected void copyUnmerged(Set merged) throws MojoExecutionException {
        if (includes == null) {
            includes = DEFAULT_INCLUDES;
        }

        if (sourceDirectory.isDirectory()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(sourceDirectory);
            scanner.setExcludes(getAllPlatformExcludes());
            scanner.addDefaultExcludes();

            scanner.setIncludes(includes);
            scanner.scan();

            try {
                String[] files = scanner.getIncludedFiles();
                List<String> genericFiles = new ArrayList<String>();

                for (int i = 0; i < files.length; i++) {
                    String file = files[i];
                    if (merged.contains(file)) {
                        continue;
                    }
                    genericFiles.add(file);
                    File source = new File(sourceDirectory, file);
                    File dest = new File(super.outputDirectory, file);
                    getLog().debug("Copying " + source.getAbsolutePath() + " -> " + dest.getAbsolutePath());
                    dest.getParentFile().mkdir();
                    FileUtils.copyFile(source, dest);
                }

                File platformSpecificFolder = new File(sourceDirectory, getNormalizedPlatform());
                if (platformSpecificFolder.exists() && platformSpecificFolder.isDirectory()) {
                    scanner.setBasedir(platformSpecificFolder);
                    scanner.setExcludes(excludes);
                    scanner.scan();
                    files = scanner.getIncludedFiles();
                    for (int i = 0; i < files.length; i++) {
                        String file = files[i];
                        if (genericFiles.contains(file)) {
                            File source = new File(platformSpecificFolder, file);
                            File dest = new File(super.outputDirectory, file);
                            getLog().debug("Overwriting with platform file " + source.getAbsolutePath() + " -> " + dest.getAbsolutePath());
                            dest.getParentFile().mkdir();
                            FileUtils.copyFile(source, dest);
                        }
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to copy source files to " + outputDirectory,
                        e);
            }
        }
    }

    protected Set assemble(Assembler assembler)
            throws MojoExecutionException {
        Set merged = new HashSet();

        DirectoryScanner scanner = null;
        if (sourceDirectory.isDirectory()) {
            scanner = new DirectoryScanner();
            scanner.setBasedir(sourceDirectory);
            scanner.setExcludes(getAllPlatformExcludes());
            scanner.addDefaultExcludes();
        }

        DirectoryScanner depsScan = null;
        if (depsCount > 0) {
            depsScan = new DirectoryScanner();
            depsScan.setBasedir(depsDirectory);
            depsScan.setExcludes(excludes);
            depsScan.addDefaultExcludes();
        } else {
            getLog().info("No compile time dependency - just assembling local scripts");
        }

        if (scanner == null && depsScan == null) {
            throw new MojoExecutionException("Nothing to compile or assemble ?");
        }

        assembleScripts(assembler, scanner, depsScan, merged);
        return merged;
    }

    protected void assembleScripts(Assembler assembler,
                                   DirectoryScanner scanner,
                                   DirectoryScanner depsScan,
                                   Set merged)
            throws MojoExecutionException {
        for (Iterator<Script> it = assembler.getScripts().iterator(); it.hasNext(); ) {
            Script script = it.next();
            String fileName = script.getFileName();
            getLog().debug("Assembling Script: " + fileName);

            List scriptOrderedIncludes = script.getIncludes();
            PrintWriter writer = null;
            File target = new File(outputDirectory, fileName);
            try {
                target.getParentFile().mkdirs();
                writer = new PrintWriter(target);

                for (Iterator itInc = scriptOrderedIncludes.iterator(); itInc.hasNext(); ) {
                    String includedScript = (String) itInc.next();

                    if ((scanner == null ||
                            addScriptFile(scanner, includedScript, merged, writer) < 1)
                            && depsScan != null) {
                        appendScriptFile(depsDirectory, depsScan, writer, includedScript, null);
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to write merged file " + fileName, e);
            } finally {
                IOUtil.close(writer);
            }
        }
    }

    protected int addScriptFile(DirectoryScanner scanner, String fileName, Set merged, PrintWriter writer) throws IOException {
        scanner.setIncludes(new String[]{fileName});
        scanner.scan();

        String[] filesFound = scanner.getIncludedFiles();

        File baseDir = scanner.getBasedir();
        File platformBaseDir = new File(baseDir, getNormalizedPlatform());

        String[] platformFilesFound = null;
        if (platformBaseDir.exists() && platformBaseDir.isDirectory()) {
            scanner.setBasedir(platformBaseDir);
            scanner.setExcludes(excludes);
            scanner.scan();

            platformFilesFound = scanner.getIncludedFiles();
        }

        for (int i = 0; i < filesFound.length; i++) {
            String genericFile = filesFound[i];
            String platformFile = null;
            File source = null;
            if (platformFilesFound != null) {
                for (int j = 0; j < platformFilesFound.length && platformFile == null; j++) {
                    String pFile = platformFilesFound[j];
                    if (genericFile.equals(pFile)) {
                        platformFile = pFile;
                    }
                }
            }

            if (platformFile != null) {
                source = new File(platformBaseDir, platformFile);
                getLog().debug(" Assembling platform file: " + source.getAbsolutePath());
            } else {
                source = new File(baseDir, genericFile);
                getLog().debug(" Assembling generic file: " + source.getAbsolutePath());
            }

            if (merged != null) {
                merged.add(genericFile);
            }
            IOUtil.copy(new FileReader(source), writer);
            writer.println();
        }

        scanner.setBasedir(baseDir);
        scanner.setExcludes(getAllPlatformExcludes());

        return filesFound.length;
    }
}
