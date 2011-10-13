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

package org.codehaus.mojo.javascript.titanium;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.javascript.TitaniumPackageMojo;
import org.codehaus.mojo.javascript.TitaniumSettings;
import org.codehaus.mojo.javascript.VirtualDevice;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Titanium utility class.
 * Contains helper methods related to Titanium.
 */
public class TitaniumUtils {
    /**
     * The android home environment variable name.
     */
    public static final String ENV_ANDROID_HOME = "ANDROID_HOME";

    /**
     * Retrieve the Android SDK home folder based on the ANDROID_HOME environment variable.
     * @return The android SDK home folder.
     * @throws MojoExecutionException When the android environment is not specified.
     */
    public static String getAndroidHome() throws MojoExecutionException {
        final String androidHome = System.getenv(ENV_ANDROID_HOME);
        if (androidHome == null || androidHome.isEmpty()) {
            throw new MojoExecutionException("No Android SDK path could be found. You may configure it by setting the "
            + ENV_ANDROID_HOME + " environment variable.");
        }
        final File androidHomeFile =  new File(androidHome);
        if (!androidHomeFile.exists() || !androidHomeFile.isDirectory()) {
            throw new MojoExecutionException("The android SDK path is not valid");
        }
        return androidHome;
    }

    /**
     * Retrieve the list of the available platforms for the specified Android SDK folder.
     * @param androidSdkHome The Android SDK home folder.
     * @return A list of android API version.
     * @throws MojoExecutionException When the android SDK folder doesn't exist.
     */
    public static List<Integer> getAvailableAndroidPlatformVersions(File androidSdkHome) throws MojoExecutionException {
        if (androidSdkHome == null) {
            throw new MojoExecutionException("No android SDK home folder specified");
        }
        if (!androidSdkHome.exists()) {
            throw new MojoExecutionException("The specified android SDK home folder doesn't exist: "
            + androidSdkHome.getAbsolutePath());
        }
        if (!androidSdkHome.isDirectory()) {
            throw new MojoExecutionException("The specified android sdk location is not a folder: "
            + androidSdkHome.getAbsolutePath());
        }
        File sdk = new File(androidSdkHome, "platforms");
        Pattern pattern = Pattern.compile("android-([0-9]+)");
        List<Integer> result = new ArrayList<Integer>();
        File[] sdkFiles = sdk.listFiles();
        if (sdkFiles != null) {
            for (File file : sdkFiles) {
                if (file.isDirectory() && file.getName().startsWith("android-")) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.find()) {
                        result.add(new Integer(matcher.group(1)));
                    }
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Retrieve the latest android API version of the specified android SDK
     * @param androidSdkHome The android SDK home folder.
     * @return The latest android API version
     * @throws MojoExecutionException When The androidSdkHome is not valid.
     */
    public static String getLatestAndroidPlatformVersion(File androidSdkHome) throws MojoExecutionException {
        List<Integer> platforms = getAvailableAndroidPlatformVersions(androidSdkHome);
        Integer lastVersion = platforms.get(platforms.size() - 1);
        return lastVersion.toString();
    }

    public static boolean isAndroidEmulatorRunning(File androidSdkHome) throws IOException {
        if (androidSdkHome == null) {
            return false;
        }

        File adb = new File(androidSdkHome, "platform-tools" + File.separator + "adb");
        if (!adb.exists()) {
            return false;
        }

        boolean isEmulatorRunning = false;

        ProcessBuilder pb = new ProcessBuilder(adb.getAbsolutePath(), "devices");
        pb.redirectErrorStream(true);
        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("List of devices")) {
                continue;
            }
            else if (line.startsWith("emulator-")) {
                String[] tokens = line.split("\\s");
                String name = tokens[0];
                String status = tokens[1];
                int port = Integer.parseInt(name.substring(name.indexOf("-") + 1));
                if (status.equals("device") && port == 5560) {
                    isEmulatorRunning = true;
                }
            }
        }

        return isEmulatorRunning;

    }

    /**
     * Create a new Android AVD.
     *
     * @param androidSdkHome The android SDK home folder.
     * @param avdId The android API version.
     * @param skin The skin of the emulator.
     * @param log The logging system.
     * @return true if the avd was successfully created, false if the avd already existed.
     * @throws MojoExecutionException When an error occured during the AVD creation.
     */
    public static boolean createAvd(File androidSdkHome,
                                    String avdId, String skin, Log log) throws MojoExecutionException {
        String avdName = "titanium_" + avdId + "_"
                + skin;

        String homeDir = System.getProperty("user.home");
        File avdDir = new File(homeDir, ".android" + File.separator + "avd"
         + File.separator + avdName + ".avd");

        if (!avdDir.exists()) {
            File androidCmd = new File(androidSdkHome, "tools" + File.separator + "android");
            File sdCard = new File(homeDir, ".titanium" + File.separatorChar + avdName + ".sdcard");

            createSdCard(androidSdkHome, sdCard, "64M");
            ProcessBuilder pb = new ProcessBuilder(androidCmd.getAbsolutePath(), "--verbose",
                    "create", "avd",
                    "-n", avdName, "-t", avdId,
                    "-s", skin,
                    "--force",
                    "--sdcard", sdCard.getAbsolutePath());

            try {
                Process p = pb.start();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                //log.info("Start create AVD");
                //TitaniumPackageMojo.logProcess(p, log);
                log.info("Sending no");
                writer.write("no");
                writer.write("\n");
                writer.flush();
                TitaniumBuilder.logProcess(p, log);
                p.waitFor();
            } catch (IOException ioe) {
                throw new MojoExecutionException("Error while creating avd", ioe);
            } catch (InterruptedException ie) {
                throw new MojoExecutionException("Error while creating avd", ie);
            }
            return true;
        }  else {
            return false;
        }
    }

    /**
     * Create a new SD Card.
     *
     * @param androidSdkHome The android SDK home folder.
     * @param sdCard The SD card file name.
     * @param size The size of the SD Card (i.e.: 64M)
     * @throws MojoExecutionException When an error occured while creating the SD card.
     */
    private static void createSdCard(File androidSdkHome, File sdCard, String size)
            throws MojoExecutionException {
        File mkSdCardCmd = new File(androidSdkHome, "tools" + File.separatorChar + "mksdcard");

        ProcessBuilder pb = new ProcessBuilder(mkSdCardCmd.getAbsolutePath(),
                size, sdCard.getAbsolutePath());

        try {
            pb.start().waitFor();
        } catch (Throwable t) {
            throw new MojoExecutionException("Error while creating AVD SDCard", t);
        }
    }

    private static boolean isWindows() {
        return isOs("win");
    }

    private static boolean isMac() {
        return isOs("mac");
    }

    private static boolean isUnix() {
        return (isOs("nix") || isOs("nux"));
    }

    private static boolean isOs(final String osName) {
        final String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf(osName) >= 0);
    }

    public static String getOsClassifier() {
        if (isWindows()) {
            return "win32";
        } else if (isMac()) {
            return "osx";
        } else if (isUnix()) {
            return "linux";
        } else {
            return "";
        }
    }

    public static String getTitaniumSdkPath(String version) {
        final String homeFolder = System.getProperty("user.home");
        String sdkPath = null;
        File sdkFile = null;

        if (isMac()) {
            sdkPath = homeFolder + "/Library/Application Support/Titanium/mobilesdk/osx/" + version + "/";
            sdkFile = new File(sdkPath);
            if (!sdkFile.exists()) {
                sdkPath = "/Library/Application Support/Titanium/mobilesdk/osx/" + version + "/";
                sdkFile = new File(sdkPath);
                if (!sdkFile.exists()) {
                    sdkPath = null;
                }
            }
        } else if (isWindows()) {
            final String userProfileFolder = System.getenv("ALLUSERSPROFILE");
            sdkPath = userProfileFolder + "\\Titanium\\mobilesdk\\win32\\" + version + "\\";
            sdkFile = new File(sdkPath);
            if (!sdkFile.exists()) {
                sdkPath = "C:\\Documents and Settings\\All Users\\Application Data\\Titanium\\mobilesdk\\win32\\" + version + "\\";
                sdkFile = new File(sdkPath);
                if (!sdkFile.exists()) {
                    sdkPath = null;
                }
            }
        } else if (isUnix()) {
            sdkPath = homeFolder + "/.titanium/mobilesdk/linux/" + version + "/";
            sdkFile = new File(sdkPath);
            if (!sdkFile.exists()) {
                sdkPath = null;
            }
        }

        return sdkPath;
    }

    public static List<String> getAvailableTitaniumSdkVersions() {
        String basePath = getTitaniumBaseSdkPath();
        if (basePath == null) {
            return null;
        }

        File baseFolder = new File(basePath);
        File[] files = baseFolder.listFiles();
        if (files != null && files.length > 0) {
            List<String> versions = new ArrayList<String>();
            for (File f : files) {
                if (f.isDirectory()) {
                    versions.add(f.getName());
                }
            }
            if (!versions.isEmpty()) {
                return versions;
            }
        }
        return null;
    }

    public static String getLatestAvailableTitaniumSdkVersion() {
        List<String> versions = getAvailableTitaniumSdkVersions();
        if (versions != null) {
            Collections.sort(versions);
            return versions.get(versions.size() -1);
        }
        return null;
    }

    public static String getTitaniumBaseSdkPath() {
        final String homeFolder = System.getProperty("user.home");
        String sdkPath = null;
        File sdkFile = null;

        if (isMac()) {
            sdkPath = homeFolder + "/Library/Application Support/Titanium/mobilesdk/osx/";
            sdkFile = new File(sdkPath);
            if (!sdkFile.exists()) {
                sdkPath = "/Library/Application Support/Titanium/mobilesdk/osx/";
                sdkFile = new File(sdkPath);
                if (!sdkFile.exists()) {
                    sdkPath = null;
                }
            }
        } else if (isWindows()) {
            final String userProfileFolder = System.getenv("ALLUSERSPROFILE");
            sdkPath = userProfileFolder + "\\Titanium\\mobilesdk\\win32\\";
            sdkFile = new File(sdkPath);
            if (!sdkFile.exists()) {
                sdkPath = "C:\\Documents and Settings\\All Users\\Application Data\\Titanium\\mobilesdk\\win32\\";
                sdkFile = new File(sdkPath);
                if (!sdkFile.exists()) {
                    sdkPath = null;
                }
            }
        } else if (isUnix()) {
            sdkPath = homeFolder + "/.titanium/mobilesdk/linux/";
            sdkFile = new File(sdkPath);
            if (!sdkFile.exists()) {
                sdkPath = null;
            }
        }

        return sdkPath;
    }

    public static String getTitaniumArtifactSdkPath(String version, File targetDir) {
        File sdkPath = new File(targetDir, "mobilesdk" + File.separator
                + TitaniumUtils.getOsClassifier() + File.separator
                + version);

        if (sdkPath.exists()) {
            return sdkPath.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static boolean isAndroidVersionValid(File androidSdkHome, String version)
    throws MojoExecutionException {
        List<Integer> versions = getAvailableAndroidPlatformVersions(androidSdkHome);
        for (Integer v : versions) {
            if (v.toString().equals(version)) {
                return true;
            }
        }
        return false;
    }

    public static void checkVirtualDevice(String platform, TitaniumSettings settings, VirtualDevice virtualDevice) throws MojoExecutionException {
        if (platform.equals("android")) {
            if (virtualDevice.getAndroidAPI() == null) {
                virtualDevice.setAndroidAPI(getLatestAndroidPlatformVersion(settings.getAndroidSdk()));
            } else {
                if (!isAndroidVersionValid(settings.getAndroidSdk(), virtualDevice.getAndroidAPI())) {
                    throw new MojoExecutionException("The specified android version is not present");
                }
            }

            if (virtualDevice.getSkin() == null) {
                if (new Integer(virtualDevice.getAndroidAPI()).intValue() < 10) {
                    virtualDevice.setSkin("HVGA");
                } else {
                    virtualDevice.setSkin("WXGA");
                }
            }
        } else if (platform.equals("iphone")) {
            if (virtualDevice.getFamily() == null) {
                virtualDevice.setFamily("iphone");
            }
        }  else if (platform.equals("ipad")) {
            if (virtualDevice.getFamily() == null) {
                virtualDevice.setFamily("ipad");
            }
        }  else if (platform.equals("universal")) {
            if (virtualDevice.getFamily() == null) {
                virtualDevice.setFamily("iphone");
            }
        }
    }

    public static boolean isIphoneVersionValid(String version) {
        List<String> platforms = listAvailableIosPlatformVersions();
        for (String platform : platforms) {
            if (platform.equals(version)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> listAvailableIosPlatformVersions() {
        List<String> results = new ArrayList<String>();
        try {
            ProcessBuilder pb = new ProcessBuilder("xcodebuild", "-showsdks");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            Pattern pattern = Pattern.compile("-sdk iphoneos(([0-9]+.?)+)");
            while ((line = reader.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                while (m.find()) {
                    results.add(m.group(1));
                }
            }
        } catch (Throwable t) {
        }
        return results;
    }

    public static String getLatestIosPlatformVersion() {
        List<String> platforms = listAvailableIosPlatformVersions();
        if (!platforms.isEmpty()) {
            return platforms.get(0);
        }
        return "4.3";
    }
}
