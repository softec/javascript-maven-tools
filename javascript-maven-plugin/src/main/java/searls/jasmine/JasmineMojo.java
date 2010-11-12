/*
 * Derivative Work
 * Copyright 2010 SOFTEC sa. All rights reserved.
 *
 * Original Work
 * Copyright 2010 Justin Searls
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

package searls.jasmine;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import searls.jasmine.format.JasmineResultLogger;
import searls.jasmine.model.JasmineResult;
import searls.jasmine.runner.SpecRunnerExecutor;
import searls.jasmine.runner.SpecRunnerHtmlGenerator;
import searls.jasmine.runner.SpecRunnerHtmlGenerator.ReporterType;

/**
 * @goal jasmine
 * @phase test
 */
public class JasmineMojo extends AbstractJasmineMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
        if(!jasmineTestSourceDirectory.exists()) {
            getLog().info("No Jasmine tests, skipping Jasmine tests execution.");
            return;
        }

        if (skipTests)
        {
            getLog().warn( "Skipping Jasmine tests." );
            return;
        }

        getLog().info("Executing Jasmine Tests");
        JasmineResult result;
        if(browsers == null) {
            browsers = new String[] { "FF3.6" };
        }
        for(String browser : browsers) {
            try {
                File runnerFile = writeSpecRunnerToOutputDirectory();
                result = new SpecRunnerExecutor().execute(runnerFile.toURI().toURL(),browser);
            } catch (Exception e) {
                throw new MojoExecutionException("There was a problem executing Jasmine specs",e);
            }
            logResults(result,browser);
            if(!testFailureIgnore && !result.didPass()) {
                throw new MojoFailureException("There were Jasmine spec failures.");
            }
        }
	}

	private void logResults(JasmineResult result, String browser) {
		JasmineResultLogger resultLogger = new JasmineResultLogger();
        resultLogger.setBrowser(browser);
		resultLogger.setLog(getLog());
		resultLogger.log(result);
	}

	private File writeSpecRunnerToOutputDirectory() throws IOException {
		SpecRunnerHtmlGenerator htmlGenerator = new SpecRunnerHtmlGenerator(preloadSources,new File(jasmineTargetDir,srcDirectoryName),new File(
            jasmineTargetDir,specDirectoryName),new File(jasmineTargetDir,libsDirectory));
		String html = htmlGenerator.generate(pluginArtifacts, ReporterType.JsApiReporter);
		
		getLog().debug("Writing out Spec Runner HTML " + html + " to directory " + jasmineTargetDir);
		File runnerFile = new File(jasmineTargetDir,specRunnerHtmlFileName);
		FileUtils.writeStringToFile(runnerFile, html);
		return runnerFile;
	}

}
