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
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.javascript.titanium.TitaniumUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Titanium package abstract base class;
 */
public abstract class AbstractTitaniumPackageMojo extends AbstractMojo {
    /**
     * <p>Parameter designed to pick <code>-DandroidBuilder</code>
     * in case there is no pom with an <code>&lt;titaniumSettings&gt;</code> configuration tag.</p>
     * <p>Coresponds to {@link TitaniumSettings#androidBuilder}</p>
     * @parameter expression="${androidBuilder}"
     * @readonly
     */
    private File androidBuilderPath;

    /**
     * <p>Parameter designed to pick <code>-DiosBuilder</code>
     * in case there is no pom with an <code>&lt;titaniumSettings&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link TitaniumSettings#iosBuilder}</p>
     * @parameter expression="${iosBuilder}"
     * @readonly
     */
    private File iosBuilderPath;

    /**
     * <p>Parameter designed to pick <code>-DandroidSDK</code>
     * in case there is no pom with <code>&lt;titaniumSettings&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link TitaniumSettings#androidSDK}</p>
     * @parameter expression="${androidSDK}"
     * @readonly
     */
    private File androidSDKPath;

    /**
     * <p>Parameter designed to pick <code>-Dsign.keystore</code>
     * in case there is no pom with <code>&lt;titaniumSettings&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link TitaniumSettings#keystore}</p>
     * @parameter expression="${sign.keystore}"
     * @readonly
     */

    private String titaniumSettingsKeystore;

    /**
     * <p>Parameter designed to pick <code>-Dsign.password</code>
     * in case there is no pom with <code>&lt;titaniumSettings&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link TitaniumSettings#keystorePassword}</p>
     * @parameter expression="${sign.password}"
     * @readonly
     */
    private String titaniumSettingsKeystorePassword;

    /**
     * <p>Parameter designed to pick <code>-Dsign.alias</code>
     * in case there is no pom with <code>&lt;titaniumSettings&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link TitaniumSettings#keystoreAlias}</p>
     * @parameter expression="${sign.alias}"
     * @readonly
     */
    private String titaniumSettingsKeystoreAlias;

    /**
     * <p>Parameter designed to pick <code>-DvirtualDevice.androidAPI</code>
     * in case there is no pom with <code>&lt;virtualDevice&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link VirtualDevice#androidAPI}</p>
     * @parameter expression="${virtualDevice.androidAPI}"
     * @readonly
     */
    private String virtualDeviceAndroidAPI;

    /**
     * <p>Parameter designed to pick <code>-DvirtualDevice.iosVersion</code>
     * in case there is no pom with <code>&lt;virtualDevice&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link VirtualDevice#iosVersion}</p>
     * @parameter expression="${virtualDevice.iosVersion}"
     * @readonly
     */
    private String virtualDeviceIosVersion;

    /**
     * <p>Parameter designed to pick <code>-DvirtualDevice.skin</code>
     * in case there is no pom with <code>&lt;virtualDevice&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link VirtualDevice#skin}</p>
     * @parameter expression="${virtualDevice.skin}"
     * @readonly
     */
    private String virtualDeviceSkin;

    /**
     * <p>Parameter designed to pick <code>-DvirualDevice.family</code>
     * in case there is no pom with <code>&lt;virtualDevice&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link VirtualDevice#family}</p>
     * @parameter expression="${virtualDevice.family}"
     * @readonly
     */
    private String virtualDeviceFamily;

    /**
     * <p>Parameter designed to pick <code>-DvirtualDevice.wait</code>
     * in case there is no pom with <code>&lt;virtualDevice&gt;</code> configuration tag.</p>
     * <p>Corresponds to {@link VirtualDevice#wait}</p>
     * @parameter expression="${virtualDevice.wait}"
     * @readonly
     */
    private Long virtualDeviceWait;

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
     * <p>The version of the platform for which the code should be compiled.</p>
     * <p>This is the version of the library to use to compile the application.
     * It's possible to specify another android API for the android virtual device.
     * See {@link VirtualDevice#androidAPI}.</p>
     *
     * @parameter expression="${androidAPI}"
     */
    protected String androidAPI;

    /**
     * The version of the platform for which the code should be compiled.
     *
     * @parameter expression="${iosVersion}"
     */
    protected String iosVersion;

    /**
     * The titanium SDK version to use.
     *
     * @parameter expression="${titaniumVersion}"
     * @required
     */
    protected String titaniumVersion;

    /**
     * <p>The titanium settings.</p>
     * <p>Contains various information needed to execute a titanium build.</p>
     * <p>Here's the list of the titaniumSettings parameters:</p>
     * <dl>
     *     <dt>{@link TitaniumSettings#androidBuilder}</dt>
     *     <dd>The titanium android builder.py file location.
     *     Optional.
     *     If not specified it tries to retrieve the builder based on {@link #titaniumVersion}</dd>
     *     <dt>{@link TitaniumSettings#iosBuilder}</dt>
     *     <dd>The titanium iOS builder.py location.
     *     Optional.
     *     If not specified, it tries to retrieve the builder based on {@link #titaniumVersion}</dd>
     *     <dt>{@link TitaniumSettings#androidSDK}</dt>
     *     <dd>The android SDK location.
     *     This parameter is optional, by default the android SDK is retrieved based on the environment
     *     variable ANDROID_HOME.</dd>
     *     <dt>{@link TitaniumSettings#keystore}</dt>
     *     <dd>The android keystore to use to sign the application.
     *     Optional. If not specified the Titanium keystore is used.</dd>
     *     <dt>{@link TitaniumSettings#keystorePassword}</dt>
     *     <dd>The android keystore password.
     *     Optional. If not specified the default titanium keystore password is used.</dd>
     *     <dt>{@link TitaniumSettings#keystoreAlias}</dt>
     *     <dd>The android keystore key alias.
     *     Optional. The alias of the key to use to sign the application.
     *     If not specified the default titanium alias is used.</dd>
     *     <dt>{@link TitaniumSettings#iosDevelopmentProvisioningProfile}</dt>
     *     <dd>The iOS development provisioning profile.
     *     This profile is use when {@link #executeMode} is <code>virtual</code>
     *     or <code>device</code>.</dd>
     *     <dt>{@link TitaniumSettings#iosDistributionProvisioningProfile}</dt>
     *     <dd>The iOS distribution provisioning profile.
     *     This profile is used when {@link #executeMode} is <code>none</code>.</dd>
     *     <dt>{@link TitaniumSettings#iosDevelopmentCertificate}</dt>
     *     <dd>The iOS development certificate.
     *     This certificate is used when {@link #executeMode} is <code>virtual</code>
     *     or <code>device</code>.</dd>
     *     <dt>{@link TitaniumSettings#iosDistributionCertificate}</dt>
     *     <dd>The iOS distribution certificate.
     *     This certificate is used when {@link #executeMode} is <code>none</code>.</dd>
     * </dl>
     * @parameter
     */
    protected TitaniumSettings titaniumSettings;

    /**
     * <p>Virtual device configuration.</p>
     * <p>When {@link #executeMode} is virtual,
     * the parameters in virtualDevice are used to configure the
     * android emulator or iphone simulator.</p>
     * <p>VirtualDevice has the following parameters:</p>
     * <dl>
     *     <dt>{@link VirtualDevice#androidAPI}</dt>
     *     <dd>The version on which the virtual device should run.
     *     If not specified, the latest android API version will be used.
     *     Regardless of the global androidAPI value.</dd>
     *     <dt>{@link VirtualDevice#iosVersion}</dt>
     *     <dd>The ios version of the virtual device.
     *     If not specified the latest available version will be used.
     *     Regardless of the global parameter value.</dd>
     *     <dt>{@link VirtualDevice#skin}</dt>
     *     <dd>The skin of the android emulator.
     *     Defaults to HVGA for version less than 10 and to WXGA for version greater than 10</dd>
     *     <dt>{@link VirtualDevice#family}</dt>
     *     <dd>The iOS device family.
     *     Valid values are <code>iphone</code> or <code>ipad</code>.</dd>
     *     <dt>{@link VirtualDevice#wait}</dt>
     *     <dd>How much miliseconds to wait after launching emulator before
     *     installing the android application.</dd>
     * </dl>
     * @see VirtualDevice
     * @parameter
     */
    protected VirtualDevice virtualDevice;

    /**
     * <p>The package execution mode.</p>
     * <p>Allow the execution of the package on an emulator/device.</p>
     * <p>Values are:</p>
     * <dl>
     *   <dt>none</dt>
     *   <dd>Do not execute. (Default value)</dd>
     *   <dt>virtual</dt>
     *   <dd>Execute on an emulator whose settings are specified in {@link #virtualDevice}.</dd>
     *   <dt>device</dt>
     *   <dd>Execute on a connected device.</dd>
     * </dl>
     *
     * @parameter default-value="none" expression="${executeMode}"
     */
    protected String executeMode;

    /**
     * The output directory of the packaged titanium files.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     */
    protected File outputDirectory;

    /**
     * The ios Development certificate.
     * @parameter expression="${iosDevelopmentCertificate}"
     * @readonly
     */
    protected String iosDevelopmentCertificate;

    /**
     * The ios Distribution certificate.
     * @parameter expression="${iosDistributionCertificate}"
     * @readonly
     */
    protected String iosDistributionCertificate;


    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactResolver artifactResolver;

    /**
     * @parameter default-value="${localRepository}"
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     */
    protected List remoteRepositories;

    /**
     * The iOS development provisioning profile.
     * @parameter expression="${iosDevelopmentProvisioningProfile}"
     * @readonly
     */
    protected String iosDevelopmentProvisioningProfile;

    /**
     * The ios distribution provisioning profile.
     * @parameter expression="${iosDistributionProvisioningProfile}"
     * @readonly
     */
    protected String iosDistributionProvisioningProfile;


    /**
     * <p>Retrieve the ios platform version.</p>
     * <p>If the ios platform version is not specified, retrieve one
     * from the XCode installation.</p>
     * @return The iOS platform version.
     */
    protected String getIosVersion() {
        if (iosVersion == null) {
            iosVersion = TitaniumUtils.getLatestIosPlatformVersion();
        }
        return iosVersion;
    }

    /**
     * <p>Retrieve the android API to use when building the application.</p>
     * <p>If the android API is not specified, the latest one is retrieved based on the
     * Android SDK location.</p>
     * @return The android API level.
     * @throws org.apache.maven.plugin.MojoExecutionException
     */
    protected String getAndroidAPI() throws MojoExecutionException {
        if (androidAPI == null) {
            androidAPI = TitaniumUtils.getLatestAndroidPlatformVersion(getTitaniumSettings().getAndroidSdk());
        }
        return androidAPI;
    }

    /**
     * Retrieve the titanium project folder for the specified platform.
     * @return A File representing the titanium project folder.
     */
    protected File getTiProjectDirectory() {
        return new File(outputDirectory, platform);
    }

    /**
     * <p>Retrieve the TitaniumSettings object.</p>
     * <p>If {@link #titaniumSettings} is null, a new TitaniumSettings
     * is constructed based on the specified expressions.</p>
     * @return An initialized TitaniumSettings object.
     * @see #androidSDKPath
     * @see #iosBuilderPath
     * @see #androidBuilderPath
     * @see #titaniumSettingsKeystore
     * @see #titaniumSettingsKeystorePassword
     * @see #titaniumSettingsKeystoreAlias
     */
    protected TitaniumSettings getTitaniumSettings() {
        if (titaniumSettings == null) {
            titaniumSettings = new TitaniumSettings();
        }
        if (androidSDKPath != null) {
            titaniumSettings.setAndroidSdk(androidSDKPath);
        }
        if (iosBuilderPath != null) {
            titaniumSettings.setIosBuilder(iosBuilderPath);
        }
        if (androidBuilderPath != null) {
            titaniumSettings.setAndroidBuilder(androidBuilderPath);
        }
        if (titaniumSettingsKeystore != null) {
            titaniumSettings.setKeystore(titaniumSettingsKeystore);
        }
        if (titaniumSettingsKeystoreAlias != null) {
            titaniumSettings.setKeystoreAlias(titaniumSettingsKeystoreAlias);
        }
        if (titaniumSettingsKeystorePassword != null) {
            titaniumSettings.setKeystorePassword(titaniumSettingsKeystorePassword);
        }
        if (iosDevelopmentCertificate != null) {
            titaniumSettings.setIosDevelopmentCertificate(iosDevelopmentCertificate);
        }
        if (iosDistributionCertificate != null) {
            titaniumSettings.setIosDistributionCertificate(iosDistributionCertificate);
        }
        if (iosDevelopmentProvisioningProfile != null) {
            titaniumSettings.setIosDevelopmentProvisioningProfile(iosDevelopmentProvisioningProfile);
        }
        if (iosDistributionProvisioningProfile != null) {
            titaniumSettings.setIosDistributionProvisioningProfile(iosDistributionProvisioningProfile);
        }
        return titaniumSettings;
    }

    /**
     * <p>Retrieve the VirtualDevice object.</p>
     * <p>If {@link #virtualDevice} is null, a new VirtualDevice
     * is constructed based on the specified expressions.</p>
     * @return An initialized VirtualDevice object.
     * @see #virtualDeviceAndroidAPI
     * @see #virtualDeviceFamily
     * @see #virtualDeviceIosVersion
     * @see #virtualDeviceSkin
     * @see #virtualDeviceWait
     */
    protected VirtualDevice getVirtualDevice() {
        if (virtualDevice == null) {
            virtualDevice = new VirtualDevice();
        }
        if (this.virtualDeviceAndroidAPI != null) {
            virtualDevice.setAndroidAPI(this.virtualDeviceAndroidAPI);
        }
        if (this.virtualDeviceFamily != null) {
            virtualDevice.setFamily(this.virtualDeviceFamily);
        }
        if (this.virtualDeviceIosVersion != null) {
            virtualDevice.setIosVersion(this.virtualDeviceIosVersion);
        }
        if (this.virtualDeviceSkin != null) {
            virtualDevice.setSkin(this.virtualDeviceSkin);
        }
        if (this.virtualDeviceWait != null) {
            virtualDevice.setWait(this.virtualDeviceWait);
        }
        return virtualDevice;
    }

    /**
     * <p>Check the titanium pom settings.</p>
     * <p>Mainly check if the {@link #androidAPI} or the {@link #iosVersion} parameters are valid.</p>
     * @return true if the pom settings are not valid.
     * @throws MojoExecutionException If an error occurs.
     */
    protected boolean checkPomSettings() throws MojoExecutionException {
        if (platform.equals("android")) {
            if (!TitaniumUtils.isAndroidVersionValid(getTitaniumSettings().getAndroidSdk(), getAndroidAPI())) {
                try {
                    List<Integer> availApis = TitaniumUtils.getAvailableAndroidPlatformVersions(getTitaniumSettings().getAndroidSdk());
                    StringBuilder availPlatforms = new StringBuilder();
                    for (int i=0; i<availApis.size(); i++) {
                        if (i>0) {
                            availPlatforms.append(", ");
                        }
                        availPlatforms.append(availApis.get(i).toString());
                    }
                    String errorMsg = MessageFormat.format("The ios version {0} is not valid. Valid versions are ${1}.",
                            getAndroidAPI(), availPlatforms.toString());
                    getLog().error(errorMsg);
                } catch (Throwable t) {
                    getLog().error("The specified android API is not valid");
                }
                return false;
            }
        } else if (platform.equals("iphone") || platform.equals("ipad") || platform.equals("universal")) {
            if (!TitaniumUtils.isIphoneVersionValid(getIosVersion())) {
                try {
                    List<String> platforms = TitaniumUtils.listAvailableIosPlatformVersions();
                    StringBuilder availPlatforms = new StringBuilder();
                    if (platforms != null) {
                        for(int i=0; i<platforms.size(); i++) {
                            if (i>0) {
                                availPlatforms.append(", ");
                            }
                            availPlatforms.append(platforms.get(i));
                        }
                    }
                    String errorMsg = MessageFormat.format("The ios version {0} is not valid. Valid versions are ${1}.",
                            getIosVersion(), availPlatforms.toString());
                    getLog().error(errorMsg);
                } catch (Throwable t) {}
                return false;
            }
        }

        return true;
    }

    protected void downloadTitaniumFromRepositories(String tiVersion, File targetDir) throws ArtifactResolutionException, ArtifactNotFoundException, IOException {
        final String tiGroupId = "org.appcelerator.titanium";
        final String tiArtifactId = "titanium-mobile";

        String classifier = TitaniumUtils.getOsClassifier();
        Artifact artifact = artifactFactory.createArtifactWithClassifier(tiGroupId, tiArtifactId, tiVersion, "zip", classifier);

        artifactResolver.resolve(artifact, remoteRepositories, localRepository);

        File file = artifact.getFile();

        extractZipFile(file, targetDir);
    }

    protected File resolveAndroidBuilder() throws MojoExecutionException {
        File destTiFolder = new File(outputDirectory, "titanium_mobile");

        File builderFile = getTitaniumSettings().getAndroidBuilder(titaniumVersion);
        if (builderFile == null) {
            try {
                builderFile = new File(destTiFolder, "mobilesdk" + File.separator
                    + TitaniumUtils.getOsClassifier() + File.separatorChar
                    + titaniumVersion + File.separatorChar
                    + "android" + File.separatorChar + "builder.py");

                if (!builderFile.exists()) {
                    getLog().info("Downloading Titanium SDK from repository...");
                    downloadTitaniumFromRepositories(titaniumVersion, destTiFolder);
                }
                if ( !builderFile.exists()) {
                    getLog().error("Unable to retrieve android builder from extracted artifact");
                    builderFile = null;
                    throw new MojoExecutionException("Unable to retrieve android builder from extracted artifact");
                } else {
                    getLog().info("Using artifact android builder: " + builderFile.getAbsolutePath());
                }
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Unable to resolve Titanium SDK artifact", e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException("Unable to find the titanium SDK artifact to download for version " + titaniumVersion, e);
            } catch (IOException e) {
                throw new MojoExecutionException("An error occured while extracting the titanium mobile SDK", e);
            }
        }

        return builderFile;
    }

    protected File resolveIOSBuilder() throws MojoExecutionException {
        File destTiFolder = new File(outputDirectory, "titanium_mobile");

        File builderFile = getTitaniumSettings().getIosBuilder(titaniumVersion);
        if (builderFile == null) {
            builderFile = new File(destTiFolder, "mobilesdk" + File.separator
                + TitaniumUtils.getOsClassifier() + File.separator
                + titaniumVersion + File.separator
                + "iphone" + File.separator + "builder.py");

            if (!builderFile.exists()) {
                getLog().info("Downloading Titanium SDK from repository...");
                try {
                    downloadTitaniumFromRepositories(titaniumVersion, destTiFolder);
                } catch (ArtifactResolutionException e) {
                    throw new MojoExecutionException("Unable to resolve Titanium SDK artifact", e);
                } catch (ArtifactNotFoundException e) {
                    throw new MojoExecutionException("Unable to find the titanium SDK artifact to download for version " + titaniumVersion, e);
                } catch (IOException e) {
                    throw new MojoExecutionException("An error occured while extracting the titanium mobile SDK", e);
                }
            }
            if ( !builderFile.exists()) {
                getLog().error("Unable to retrieve ios builder from extracted artifact");
                builderFile = null;
                throw new MojoExecutionException("Unable to retrieve ios builder from extracted artifact");
            } else {
                getLog().info("Using artifact ios builder: " + builderFile.getAbsolutePath());
            }

        }

        return builderFile;
    }

    protected void extractZipFile(File zipFile, File destFolder) throws IOException {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];

        ZipInputStream zis = null;
        ZipEntry zipEntry;

        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));

            while ((zipEntry = zis.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                File newFile = new File(destFolder, entryName);
                if (newFile.getParentFile() != null) {
                    newFile.getParentFile().mkdirs();
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(newFile);
                    int n;

                    while ((n = zis.read(buffer, 0, BUFFER_SIZE)) > -1) {
                        fos.write(buffer, 0, n);
                    }
                } finally {
                    IOUtil.close(fos);
                }
                zis.closeEntry();

                newFile.setReadable(true);
                newFile.setWritable(true);
                if (getFileExtension(newFile).equals("py")
                        || newFile.getName().compareToIgnoreCase("iphonesim") == 0) {
                    newFile.setExecutable(true);
                }
            }
        } finally {
            IOUtil.close(zis);
        }
    }

    private String getFileExtension(final File file) {
        int mid = file.getName().lastIndexOf(".");
        if (mid != -1) {
            return file.getName().substring(mid+1, file.getName().length());
        } else {
            return "";
        }
    }
}
