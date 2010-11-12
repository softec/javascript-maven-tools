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

package searls.jasmine.runner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import searls.jasmine.io.IOUtilsWrapper;
import searls.jasmine.model.JasmineResult;

public class SpecRunnerExecutor {

    public static final Map Browsers = new HashMap();

	public static final String BUILD_REPORT_JS = "/buildReport.js";
	public static final String BUILD_CONCLUSION_JS = "/buildConclusion.js";

	private static final long MAX_EXECUTION_MILLIS = 300000; //5 minutes - TODO make this configurable
	
	private IOUtilsWrapper ioUtilsWrapper = new IOUtilsWrapper();

    public SpecRunnerExecutor() {
        Browsers.put("FF3",   BrowserVersion.FIREFOX_3);
        Browsers.put("FF3.6", BrowserVersion.FIREFOX_3_6);
        Browsers.put("IE6", BrowserVersion.INTERNET_EXPLORER_6);
        Browsers.put("IE7", BrowserVersion.INTERNET_EXPLORER_7);
        Browsers.put("IE8", BrowserVersion.INTERNET_EXPLORER_8);
    }
	
    public JasmineResult execute(URL runnerUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {
        return execute(runnerUrl, "IE7");
    }

	public JasmineResult execute(URL runnerUrl, String browser) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {
		WebClient webClient = new WebClient((BrowserVersion) Browsers.get(browser));
		webClient.setJavaScriptEnabled(true);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		
		quietIncorrectnessListener(webClient);
		
	    HtmlPage page = webClient.getPage(runnerUrl);
	    waitForRunnerToFinish(page);
	    
	    JasmineResult jasmineResult = new JasmineResult();
	    jasmineResult.setDescription(buildRunnerDescription(page));
	    jasmineResult.setDetails(buildReport(page));

	    webClient.closeAllWindows();
	    
	    return jasmineResult;
	}


	private String buildReport(HtmlPage page) throws IOException {
		ScriptResult report = page.executeJavaScript(ioUtilsWrapper.toString(getClass().getResourceAsStream(BUILD_REPORT_JS)));
		return report.getJavaScriptResult().toString();
	}

	private String buildRunnerDescription(HtmlPage page) throws IOException {
		ScriptResult description = page.executeJavaScript(ioUtilsWrapper.toString(getClass().getResourceAsStream(BUILD_CONCLUSION_JS)));
		return description.getJavaScriptResult().toString();
	}


	private void waitForRunnerToFinish(HtmlPage page) throws InterruptedException {		
		page.getWebClient().waitForBackgroundJavaScript(5000);
		int waitInMillis = 500;
		for (int i = 0; i < MAX_EXECUTION_MILLIS/waitInMillis; i++) {
			if(executionFinished(page)) {
				return;
			} else {
        		synchronized (page) {
					page.wait(waitInMillis);
        		}
            }
        }
		if(!executionFinished(page)) {
			throw new IllegalStateException("Attempted to wait for the test to complete processing over the course of "+(MAX_EXECUTION_MILLIS/1000)+" seconds," +
					"but it still appears to be running. Aborting test execution.");
		}
	}

	private Boolean executionFinished(HtmlPage page) {
		ScriptResult result = page.executeJavaScript("reporter.finished");
		return (Boolean) result .getJavaScriptResult();
	}

	private void quietIncorrectnessListener(WebClient webClient) {
		//Disables stuff like this "com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl notify WARNING: Obsolete content type encountered: 'text/javascript'."
		webClient.setIncorrectnessListener(new IncorrectnessListener() {
			public void notify(String arg0, Object arg1) {}
		});
	}



}
