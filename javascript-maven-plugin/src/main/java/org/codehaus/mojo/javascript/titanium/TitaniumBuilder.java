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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.javascript.AndroidEmulatorThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to issue titanium builder calls.
 */
public class TitaniumBuilder {

    /**
     * The builder file.
     */
    private File androidBuilder;
    private File iosBuilder;
    private File androidSdk;

    /**
     * Create a new TitaniumBuilder instance.
     * @param androidBuilder The location of the android builder file.
     * @param iosBuilder The location of the iphone/ipad builder file.
     * @param androidSdk The location of the android SDK home folder.
     */
    public TitaniumBuilder(File androidBuilder, File iosBuilder, File androidSdk) {
        this.androidBuilder = androidBuilder;
        this.iosBuilder = iosBuilder;
        this.androidSdk = androidSdk;
    }

    /**
     * Launch the android emulator and start the simulator after.
     *
     * @param projectName The project name.
     * @param tiProjectDirectory The directory containing the titanium project files.
     * @param appId The Titanium application id.
     * <p>Must use the <code>com.company.name</code> format and not containing -.</p>
     * @param androidAPI The API to use to build the application.
     * @param androidDeviceAPI The API the android emulator should use.
     * @param androidDeviceSkin The skin of the android emulator to use.
     * @param androidDeviceWait The interval between the emulator launch and the simulator launch.
     * In milliseconds.
     * @param log The Maven logger
     * @throws MojoFailureException When the builder process return an error.
     * @throws MojoExecutionException When an error occurs during the call.
     * @throws IOException  When an error occurs during the call.
     * @throws InterruptedException  When an error occurs during the call.
     */
    public void launchOnAndroidEmulator(String projectName,
                                        File tiProjectDirectory,
                                        String appId,
                                        String androidAPI,
                                        String androidDeviceAPI,
                                        String androidDeviceSkin,
                                        Long androidDeviceWait,
                                        Log log) throws MojoFailureException, MojoExecutionException, IOException, InterruptedException {
        if (TitaniumUtils.createAvd(androidSdk, androidDeviceAPI,
                androidDeviceSkin,
                log)) {
            log.info("AVD created for "
                    + androidDeviceAPI
                    + " " + androidDeviceSkin);
        }
        boolean isEmulatorRunning = false;
        try {
            isEmulatorRunning = TitaniumUtils.isAndroidEmulatorRunning(androidSdk);
        } catch (Throwable t) {
            log.error("Unable to retrieve launched emulators", t);
        }
        if (!isEmulatorRunning) {
            log.info("Launching emulator");
            ProcessBuilder emulatorBuilder = createAndroidBuilderProcess("emulator", projectName,
                    tiProjectDirectory.getAbsolutePath(),
                    appId,
                    androidDeviceAPI, androidDeviceSkin);
            AndroidEmulatorThread emulatorThread = new AndroidEmulatorThread(emulatorBuilder, log);
            emulatorThread.start();
            log.info("Waiting for emulator start ("
                    + androidDeviceWait + " ms.)");

            Thread.sleep(androidDeviceWait);
        } else {
            log.info("Skipping emulator launching.");
        }
        log.info("Launching simulator");
        ProcessBuilder simulatorBuilder = createAndroidBuilderProcess("simulator", projectName,
                tiProjectDirectory.getAbsolutePath(),
                appId,
                androidAPI, androidDeviceSkin);
        //simulatorBuilder.redirectErrorStream(true);
        Process simulator = simulatorBuilder.start();
        logProcess(simulator, log, null, true);
        log.info("Waiting for simulator end");
        simulator.waitFor();
        log.info("done");
        if (simulator.exitValue() != 0) {
            throw new MojoFailureException("The titanium builder failed");
        }
    }


    /**
     * Create an android ProcessBuilder.
     * @param command The builder command.
     * @param projectName The name of the project
     * @param tiProjectDirectory The titanium project directory.
     * @param appId The titanium application id.
     * @param androidAPI The android API.
     * @param skin The skin.
     * @return The ProcessBuilder to use to launch the process.
     * @throws MojoExecutionException When an error occured during the processbuilder creation.
     */
    public ProcessBuilder createAndroidBuilderProcess(String command, String projectName,
                                                       String tiProjectDirectory,
                                                       String appId,
                                                       String androidAPI,
                                                       String skin)
            throws MojoExecutionException {

        if (androidBuilder == null) {
            throw new MojoExecutionException("Unable to retrieve the android builder");
        }
        if (androidSdk == null) {
            throw new MojoExecutionException("Unable to retrieve the android SDK");
        }

        if (command.equals("install")) {
            return new ProcessBuilder(androidBuilder.getAbsolutePath(),
                    command,
                    projectName,
                    androidSdk.getAbsolutePath(),
                    tiProjectDirectory,
                    appId,
                    androidAPI);
        } else {
            return new ProcessBuilder(androidBuilder.getAbsolutePath(),
                    command,
                    projectName,
                    androidSdk.getAbsolutePath(),
                    tiProjectDirectory,
                    appId,
                    androidAPI,
                    skin);

        }
    }

    /**
     * Create an android distribute ProcessBuilder.
     * @param outputDirectory The outputDirectory location.
     * @param projectName The name of the project.
     * @param tiProjectDirectory The titanium project directory.
     * @param appId The titanium application id.
     * @param keystore The keystore.
     * @param keystorePassword The keystore password
     * @param keystoreAlias The keystore alias.
     * @param androidAPI The android API
     * @param skin The skin
     * @return The ProcessBuilder to use to create the android distribute process.
     * @throws MojoExecutionException When an error occured during the processbuilder creation.
     */
    public ProcessBuilder createAndroidDistributeBuilderProcess(File outputDirectory,
                                                                 String projectName,
                                                                 String tiProjectDirectory,
                                                                 String appId,
                                                                 String keystore,
                                                                 String keystorePassword,
                                                                 String keystoreAlias,
                                                                 String androidAPI,
                                                                 String skin)
            throws MojoExecutionException {

        if (androidBuilder == null) {
            throw new MojoExecutionException("Unable to retrieve the android builder");
        }
        if (androidSdk == null) {
            throw new MojoExecutionException("Unable to retrieve the android SDK");
        }

        File targetDir = new File(outputDirectory, "android-bin");
        targetDir.mkdirs();

        return new ProcessBuilder(androidBuilder.getAbsolutePath(),
                "distribute",
                projectName,
                androidSdk.getAbsolutePath(),
                tiProjectDirectory,
                appId,
                keystore,
                keystorePassword,
                keystoreAlias,
                targetDir.getAbsolutePath(),
                androidAPI,
                skin);
    }

    /**
     * Launch the specified titanium project on an iOs device.
     * @param iosVersion The iOS version to use to build the application.
     * @param tiProjectDirectory The titanium project directory.
     * @param appId The titanium application identifier.
     * @param projectName The project name.
     * @param appuuid The project uuid
     * @param distName The project dist name.
     * @param family The family to use when building the project.
     * @param log The Maven logger.
     * @throws MojoFailureException When the builder process return an error.
     * @throws MojoExecutionException When an error occured while launching on the device.
     */
    public void launchIphoneDevice(String iosVersion,
                                   File tiProjectDirectory,
                                   String appId,
                                   String projectName,
                                   String appuuid,
                                   String distName,
                                   String family,
                                   Log log) throws MojoExecutionException, MojoFailureException {
        if (iosBuilder == null) {
            throw new MojoExecutionException("Unable to retrieve the iphone builder");
        }

        ProcessBuilder pb = new ProcessBuilder(iosBuilder.getAbsolutePath(),
                "install",
                iosVersion,
                tiProjectDirectory.getAbsolutePath(),
                appId,
                projectName,
                appuuid,
                distName,
                family);
        // The first call may fail
        log.info("ProcessBuilder: " + getProcessBuilderString(pb));
        boolean relaunch = false;
        int result;
        try {
            StringBuilder logContent = new StringBuilder();
            Process p = pb.start();
            TitaniumBuilder.logProcess(p, log, logContent, false);
            p.waitFor();
            result = p.exitValue();
            if (failOnTiapp(tiProjectDirectory.getAbsolutePath(), logContent.toString())) {
                relaunch = true;
            }
        } catch (Throwable t) {
            throw new MojoExecutionException("Error while building iphone", t);
        }
        if (relaunch) {
            log.warn("Relaunching builder as it failed on missing tiapp.xml");
            try {
                Process p = pb.start();
                TitaniumBuilder.logProcess(p, log);
                p.waitFor();
                result = p.exitValue();
            } catch (Throwable t) {
                throw new MojoExecutionException("Error while building iphone", t);
            }
        }
        if (result != 0) {
            throw new MojoFailureException("The titanium builder failed");
        }
    }

    /**
     * Launch a titanium project on an iOs simulator.
     * @param iosVersion The ios version to use to build the application.
     * @param tiProjectDirectory The titanium project directory.
     * @param projectName The name of the project.
     * @param family The family to use to build the application.
     * @param simulatorFamily family The family of the simulator ("universal" translates to "iphone").
     * @param log The maven logger.
     * @throws MojoFailureException When the builder process return an error.
     * @throws MojoExecutionException When an error occurs during the simulator process.
     */
    public void launchIphoneEmulator(String iosVersion,
                                     String tiProjectDirectory,
                                     String projectName,
                                     String family,
                                     String simulatorFamily,
                                     Log log) throws MojoFailureException, MojoExecutionException {
        if (iosBuilder == null) {
            throw new MojoExecutionException("Unable to retrieve the iphone builder");
        }

        ProcessBuilder pb = new ProcessBuilder(iosBuilder.getAbsolutePath(),
                "simulator",
                iosVersion,
                tiProjectDirectory,
                "appid", //project.getGroupId() + "." + project.getArtifactId(),
                projectName,
                family,
                simulatorFamily);
        //pb.directory(tiProjectDir);

        // The first call may fail
        log.info("ProcessBuilder: " + getProcessBuilderString(pb));
        boolean relaunch = false;
        int result;
        try {
            StringBuilder logContent = new StringBuilder();
            Process p = pb.start();
            TitaniumBuilder.logProcess(p, log, logContent, false);
            p.waitFor();
            result = p.exitValue();
            if (failOnTiapp(tiProjectDirectory, logContent.toString())) {
                relaunch = true;
            }
        } catch (Throwable t) {
            throw new MojoExecutionException("Error while building iphone", t);
        }
        if (relaunch) {
            log.warn("Relaunching builder as it failed on missing tiapp.xml");
            try {
                Process p = pb.start();
                TitaniumBuilder.logProcess(p, log);
                p.waitFor();
                result = p.exitValue();
            } catch (Throwable t) {
                throw new MojoExecutionException("Error while building iphone", t);
            }
        }
        if (result != 0) {
            throw new MojoFailureException("The titanium build failed");
        }
    }

    /**
     * Launch the titanium ios builder distribute command and log its output.
     * @param iosVersion The ios version.
     * @param tiProjectDirectory The titanium project directory.
     * @param appId The application identifier.
     * @param projectName The project name.
     * @param appuuid The application uuid.
     * @param distName The distribution name.
     * @param targetDir The folder where the resulting file will be created.
     * @param family The device family.
     * @param log The maven logger.
     * @throws MojoFailureException When the builder process return an error.
     * @throws MojoExecutionException When an error occurs while creating the distribution.
     */
    public void launchIphoneDistribute(String iosVersion,
                                       File tiProjectDirectory,
                                       String appId,
                                       String projectName,
                                       String appuuid,
                                       String distName,
                                       File targetDir,
                                       String family,
                                       Log log) throws MojoExecutionException, MojoFailureException {
        if (iosBuilder == null) {
            throw new MojoExecutionException("Unable to retrieve the iphone builder");
        }
        log.info("iphoneBuilder: " + iosBuilder.getAbsolutePath());

        targetDir.mkdirs();
        ProcessBuilder pb = new ProcessBuilder(iosBuilder.getAbsolutePath(),
                "distribute",
                iosVersion,
                tiProjectDirectory.getAbsolutePath(),
                appId,
                projectName,
                appuuid,
                distName,
                targetDir.getAbsolutePath(),
                family).redirectErrorStream(true);

        log.info("ProcessBuilder: " + getProcessBuilderString(pb));
        boolean relaunch = false;
        int result;
        try {
            StringBuilder logContent = new StringBuilder();

            Process p = pb.start();
            TitaniumBuilder.logProcess(p, log, logContent, true);
            p.waitFor();
            result = p.exitValue();
            if (failOnTiapp(tiProjectDirectory.getAbsolutePath(), logContent.toString())) {
                relaunch = true;
            }
        } catch (Throwable t) {
            throw new MojoExecutionException("Error while building iphone", t);
        }

        if (relaunch) {
            log.warn("Relaunching builder as it failed on missing tiapp.xml");
            try {
                Process p = pb.start();
                TitaniumBuilder.logProcess(p, log, null, true);
                p.waitFor();
                result = p.exitValue();
            } catch (Throwable t) {
                throw new MojoExecutionException("Error while building iphone", t);
            }
        }
        if (result != 0) {
            throw new MojoFailureException("The titanium builder failed");
        }
    }


    /**
     * <p>Read a line from the reader and log it to the specified logger.</p>
     * <p>This method doesn't block while no data is available.
     * If no data is present when the method is called, nothing is outputed to the log</p>
     * @param log The logger to use to log the message.
     * @param reader The reader from which a line should be read.
     * @param defaultLogLevel The default log level to use to log message when the
     * log level is not present in the parsed line.
     * @return the log level used to log the line.
     */
    public static String logProcessLine(Log log, BufferedReader reader, String defaultLogLevel) {
        try {
            defaultLogLevel = readAndAppendLine(reader, log, null, defaultLogLevel);
        } catch (IOException ioe) {
            log.error("Error while processing input", ioe);
        }
        return defaultLogLevel;
    }

    /**
     * <p>Log the process output and error stream to the specified logger.</p>
     * <p>This method will start by read and log lines from the output and error stream
     * while the process is running.</p>
     * @param p The process to log.
     * @param log The logger where the messages should be outputted.
     */
    public static void logProcess(Process p, Log log) {
        logProcess(p, log, null, false);
    }

    /**
     * <p>Log the process to the specified logger.</p>
     * @param p The process to log.
     * @param log The logger where the message should be outputted.
     * @param builder A StringBuilder where each line will be appended.
     * May be null.
     * @param skipErrStream true if the error stream should not be logged.
     */
    public static void logProcess(Process p, Log log, StringBuilder builder, boolean skipErrStream) {
        BufferedReader inReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String defaultInLevel = null;
        String defaultErrLevel = null;
        try {
            while(isProcessRunning(p)) {
                defaultInLevel = readAndAppendLine(inReader, log, builder, defaultInLevel);
                if (!skipErrStream) {
                    defaultErrLevel = readAndAppendLine(errReader, log, builder, defaultErrLevel);
                }
            }
            readAndAppendLine(inReader, log, builder, defaultInLevel);
            if (!skipErrStream) {
                readAndAppendLine(errReader, log, builder, defaultErrLevel);
            }
        } catch (IOException ioe) {
            log.error("Error while processing builder input", ioe);
        }
    }

    /**
     * Read a line from a reader and append it the the specified logger and builder.
     * @param reader The reader from which the line should be read.
     * @param log The logger where the line should be appended. May be null.
     * @param builder The builder where the line should be appended. May be null.
     * @param defaultLogLevel The default log level to use to log message when
     * the log level is not present on the line.
     * @throws IOException If an I/O exception occurs.
     * @return The log level used to log the message or the defaultLogLevel if no log specified
     */
    private static String readAndAppendLine(BufferedReader reader, Log log, StringBuilder builder, String defaultLogLevel)
    throws IOException {
        if (reader.ready()) {
            String line = reader.readLine();
            if (line != null) {
                if (log != null) {
                    defaultLogLevel = parseTitaniumBuilderLine(line, log, defaultLogLevel);
                }
                if (builder != null) {
                    builder.append(line);
                    builder.append("\n");
                }
            }
        }
        return defaultLogLevel;
    }


    /**
     * Check if a process is running.
     * @param p The process to check.
     * @return true if the process is running, false otherwise.
     */
    public static boolean isProcessRunning(final Process p) {
        boolean isRunning = true;
        try {
            int status = p.exitValue();
            isRunning = false;
        } catch(IllegalThreadStateException e) {}
        return isRunning;
    }

    /**
     * Parse a line and output to the specified logger using the appropriate level.
     * @param line The line to parse.
     * @param log The logger.
     * @param defaultLogLevel The log level to use if the parsed line doesn't contain
     * the log level info.
     * @return the log level used to log the line.
     */
    private static String parseTitaniumBuilderLine(String line, Log log, String defaultLogLevel) {
        final Pattern pattern = Pattern.compile("\\[(TRACE|INFO|DEBUG|WARN|ERROR)\\] (.+)");
        final Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String type = matcher.group(1);
            String msg = matcher.group(2);
            if (type.equals("TRACE") || type.equals("DEBUG")) {
                log.debug(msg);
                return "DEBUG";
            } else if (type.equals("INFO")) {
                log.info(msg);
                return "INFO";
            } else if (type.equals("ERROR")) {
                log.error(msg);
                return "ERROR";
            } else if (type.equals("WARN")){
                log.warn(msg);
                return "WARN";
            } else {
                log.debug(msg);
                return "DEBUG";
            }
        } else {
            if (defaultLogLevel != null) {
                defaultLogLevel = defaultLogLevel.toUpperCase();
                if (defaultLogLevel.equals("DEBUG") || defaultLogLevel.equals("TRACE")) {
                    log.debug(line);
                } else if (defaultLogLevel.equals("INFO")) {
                    log.info(line);
                } else if (defaultLogLevel.equals("ERROR")) {
                    log.error(line);
                } else if (defaultLogLevel.equals("WARN")) {
                    log.warn(line);
                } else {
                    log.debug(line);
                    defaultLogLevel = "DEBUG";
                }
                return defaultLogLevel;
            } else {
                log.debug(line);
                return "DEBUG";
            }
        }
    }

    /**
     * Check if the iOS build process failed due to a missing tiapp.xml at an incorrect location.
     * @param tiProjectDirectory The titanium project folder.
     * @param logContent The log containing the error cause.
     * @return true if the process failed due to a missing tiapp.xml
     */
    private boolean failOnTiapp(String tiProjectDirectory, String logContent) {
        File tiWrongFile = new File(new File(tiProjectDirectory), "build" + File.separator + "iphone" + File.separator + "tiapp.xml");

        StringBuilder sb = new StringBuilder();
        sb.append("IOError: [Errno 2] No such file or directory: u'");
        sb.append(tiWrongFile.getAbsolutePath());
        sb.append("'");
        return (logContent.contains(sb.toString()));
    }

    /**
     * Retrieve a String representation of a ProcessBuilder.
     * @param pb The ProcessBuilder.
     * @return A String representing the ProcessBuilder.
     */
    private static String getProcessBuilderString(ProcessBuilder pb) {
        StringBuilder sb = new StringBuilder();

        List<String> commands = pb.command();
        if (pb.directory() != null) {
            sb.append(pb.directory().getAbsolutePath() + ": ");
        }
        for (int i=0; i<commands.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            if (commands.get(i) == null) {
                sb.append("NULL");
            } else {
                sb.append(commands.get(i).replaceAll(" ", "\\\\ "));
            }
        }

        return sb.toString();
    }
}
