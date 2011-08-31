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

import com.sun.servicetag.Installer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.javascript.titanium.TitaniumBuilder;
import org.codehaus.mojo.javascript.titanium.TitaniumUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compile the titanium application based on the selected platform.
 *
 * @author <a href="mailto:olivier.desaive@softec.lu">Olivier Desaive</a>
 * @goal titanium-package
 * @phase package
 * @plexus.component role-hint="titanium"
 */
public class TitaniumPackageMojo extends AbstractTitaniumPackageMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        TitaniumUtils.checkVirtualDevice(platform, getTitaniumSettings() , getVirtualDevice());

        if (!checkPomSettings()) {
            return;
        }

        File builder = null;

        if (platform.compareToIgnoreCase("android") == 0) {
            builder = resolveAndroidBuilder();
            packageAndroid(builder);
        } else if (platform.compareToIgnoreCase("iphone") == 0
                || platform.compareToIgnoreCase("ipad") == 0
                || platform.compareToIgnoreCase("universal") == 0) {
            builder = resolveIOSBuilder();
            packageIphone(builder);
        } else {
            throw new MojoExecutionException("Unsupported platform: " + platform);
        }
    }

    protected void packageAndroid(File builder) throws MojoFailureException, MojoExecutionException {
        checkAndroidAppId();

        TitaniumBuilder tiBuilder = new TitaniumBuilder(builder,
                null,
                titaniumSettings.getAndroidSdk());

        try {
            if (executeMode.compareToIgnoreCase("virtual") == 0) {
                launchOnEmulator(tiBuilder);
            } else if (executeMode.compareToIgnoreCase("device") == 0) {
                launchOnDevice(tiBuilder);
            } else if (executeMode.compareToIgnoreCase("none") == 0) {
                launchDistribute(tiBuilder);
            }

        } catch (MojoFailureException e) {
            throw e;
        } catch (Throwable t) {
            throw new MojoExecutionException("Error while executing android builder", t);
        }
    }

    private boolean checkAndroidAppId() {
        String appId = project.getGroupId() + "." + project.getArtifactId();
        if ( !Pattern.matches("^([a-zA-Z0-9]+\\.[a-zA-Z0-9]+)+$", appId) ) {
            getLog().warn("The appId " + appId + " may not be supported under android. " +
                    "You should ensure that the appid follow com.company.name " +
                    "and doesn't contain the '-' character.");
            return false;
        } else {
            return true;
        }
    }

    private void launchOnEmulator(TitaniumBuilder tiBuilder)
            throws MojoExecutionException, MojoFailureException,
            IOException, InterruptedException {
        tiBuilder.launchOnAndroidEmulator(project.getName(),
                                        getTiProjectDirectory(),
                                        project.getGroupId() + "." + project.getArtifactId(),
                                        getAndroidAPI(),
                                        getVirtualDevice().getAndroidAPI(),
                                        getVirtualDevice().getSkin(),
                                        getVirtualDevice().getWait(),
                                        getLog());
    }

    private void launchOnDevice(TitaniumBuilder tiBuilder) throws MojoFailureException, MojoExecutionException, IOException, InterruptedException {
        ProcessBuilder pb = tiBuilder.createAndroidBuilderProcess("install",
                project.getName(),
                getTiProjectDirectory().getAbsolutePath(),
                project.getGroupId() + "." + project.getArtifactId(),
                getAndroidAPI(),
                virtualDevice.getSkin());
        Process deviceProcess = pb.start();
        getLog().info("Deploying on device ");
        TitaniumBuilder.logProcess(deviceProcess, getLog());
        getLog().info("done");
        if (deviceProcess.exitValue() != 0) {
            throw new MojoFailureException("Titanium builder failed");
        }
    }

    private void launchDistribute(TitaniumBuilder tiBuilder) throws MojoFailureException, MojoExecutionException, IOException, InterruptedException {
        ProcessBuilder pb = tiBuilder.createAndroidDistributeBuilderProcess(outputDirectory,
                project.getName(),
                getTiProjectDirectory().getAbsolutePath(),
                project.getGroupId() + "." + project.getArtifactId(),
                getTitaniumSettings().getKeystore(titaniumVersion, new File(outputDirectory, "titanium_mobile")),
                getTitaniumSettings().getKeystorePassword(),
                getTitaniumSettings().getKeystoreAlias(),
                virtualDevice.getAndroidAPI(),
                virtualDevice.getSkin());
        Process distProcess = pb.start();
        getLog().info("Creating distribution ");
        TitaniumBuilder.logProcess(distProcess, getLog());
        getLog().info("done");
        if (distProcess.exitValue() != 0) {
            throw new MojoFailureException("Titanium builder failed");
        }
    }

    protected void packageIphone(File iosBuilder) throws MojoFailureException, MojoExecutionException {
        getLog().info("Packaging for iPhone");

        TitaniumBuilder tiBuilder = new TitaniumBuilder(null,
                iosBuilder,
                null);


        if (project.getName().toLowerCase().contains("titanium")) {
            getLog().warn("Project may not build for iPhone as it's name contains the word 'titanium'. ");
            getLog().warn("See http://jira.appcelerator.org/browse/TIMOB-1082");
        }

        if (executeMode.equals("none")) {
            launchIphoneDistribute(tiBuilder);
        } else if (executeMode.equals("device")) {
            String appId = project.getGroupId() + "." + project.getArtifactId();
            File tiAppFile = new File(getTiProjectDirectory(), "tiapp.xml");
            try {
                if (tiAppFile.exists()) {
                    Tiapp tiApp = getTitaniumSettings().getTiappFromXML(tiAppFile);
                    if (tiApp != null && tiApp.getProjectId() != null && tiApp.getProjectId().trim().length() > 0) {
                        appId = tiApp.getProjectId();
                    }
                }  else {
                    getLog().warn("Unable to find " + tiAppFile.getAbsolutePath()
                    + ". uuid will be random.");
                }

            } catch (Throwable t) {
                getLog().error("Error while parsing " + tiAppFile.getAbsolutePath()
                + ". The application uuid will be random ", t);
            }
            tiBuilder.launchIphoneDevice(getIosVersion(),
                    getTiProjectDirectory(),
                    appId,
                    project.getName(),
                    getTitaniumSettings().getIosDevelopmentProvisioningProfile(),
                    getTitaniumSettings().getIosDevelopmentCertificate(),
                    getVirtualDevice().getFamily(),
                    getLog());
        } else if (executeMode.equals("virtual")) {
            launchIphoneEmulator(tiBuilder);
        }
    }

    private void launchIphoneEmulator(TitaniumBuilder tiBuilder) throws MojoFailureException, MojoExecutionException {
        tiBuilder.launchIphoneEmulator(getIosVersion(),
                getTiProjectDirectory().getAbsolutePath(),
                project.getName(),
                platform,
                virtualDevice.getFamily(),
                getLog());
    }


    private void launchIphoneDistribute(TitaniumBuilder tiBuilder) throws MojoFailureException, MojoExecutionException {
        String appId = project.getGroupId() + "." + project.getArtifactId();
        File targetDir = new File(outputDirectory, platform + "-bin");

        File tiAppFile = new File(getTiProjectDirectory(), "tiapp.xml");
        try {
            if (tiAppFile.exists()) {
                Tiapp tiApp = getTitaniumSettings().getTiappFromXML(tiAppFile);
                if (tiApp != null && tiApp.getProjectId() != null && tiApp.getProjectId().trim().length() > 0) {
                    appId = tiApp.getProjectId();
                }
            } else {
                getLog().warn("Unable to find " + tiAppFile.getAbsolutePath()
                + ". uuid will be random.");
            }

        } catch (Throwable t) {
            getLog().error("Error while parsing " + tiAppFile.getAbsolutePath()
            + ". The application uuid will be random ");
        }

        tiBuilder.launchIphoneDistribute(getIosVersion(),
                getTiProjectDirectory(),
                appId,
                project.getName(),
                getTitaniumSettings().getIosDistributionProvisioningProfile(),
                getTitaniumSettings().getIosDistributionCertificate(),
                targetDir,
                virtualDevice.getFamily(),
                getLog());
    }


}
