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

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Utitlity class to manage the android emulator Thread.
 */
public class AndroidEmulatorThread extends Thread {
    /**
     * The process builder containing the command to launch to execute the emulator.
     */
    private ProcessBuilder processBuilder;

    /**
     * The maven logger.
     */
    private Log log;

    /**
     * The emulator process.
     */
    private Process process;

    /**
     * Create a new instance.
     * @param processBuilder The ProcessBuilder to use to execute the emulator.
     * @param log The maven logger.
     */
    public AndroidEmulatorThread(ProcessBuilder processBuilder, Log log) {
        this.processBuilder = processBuilder;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            //processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("Error while starting thread", e);
            process = null;
        }

        if (process != null) {
            outputProcessLog();
        }

        System.out.println("process execution finished");
    }

    /**
     * Check if the emulator process is running.
     * @return true if the emulator process is running,
     * false otherwise.
     */
    public boolean isRunning() {
        if (process == null) {
            return false;
        }
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException ise) {
            return true;
        }
    }

    /**
     * Output the emulator process log.
     */
    private void outputProcessLog() {
        BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        while (isRunning()) {
            try {
                fetchRow(outReader, System.out);
                fetchRow(errReader, System.err);
            } catch (IOException e) {
                System.err.println("Error while reading process input");
            }
        }

        try {
            fetchRow(outReader, System.out);
            fetchRow(errReader, System.err);
        } catch (IOException e) {
            System.err.println("Error while reading process input");
        }
    }

    /**
     * Fetch a row from a stream and
     * output it to the specified output.
     * @param reader The reader from which to read a row.
     * @param outStream The output stream.
     * @throws IOException If an I/O exception occurs.
     */
    private void fetchRow(BufferedReader reader, PrintStream outStream) throws IOException {
        if (reader.ready()) {
            String line = reader.readLine();
            if (line != null) {
                outStream.println(line);
            }
        }
    }
}
