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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.maven.artifact.Artifact;

import searls.jasmine.io.FileUtilsWrapper;

public class SpecRunnerHtmlGenerator {
	
	private static final String CSS_TYPE = "css";
	private static final String CSS_DEPENDENCIES_TEMPLATE_ATTR_NAME = "cssDependencies";	
	private static final String JAVASCRIPT_TYPE = "js";
	private static final String JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME = "javascriptDependencies";	
	private static final String SOURCES_TEMPLATE_ATTR_NAME = "sources";
	private static final String REPORTER_ATTR_NAME = "reporter";
	private static final String RUNNER_HTML_TEMPLATE = 
		"<html>" +
		"<head><title>Jasmine Test Runner</title>" +
		"$"+CSS_DEPENDENCIES_TEMPLATE_ATTR_NAME+"$ " +
		"$"+JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME+"$ " +
		"$"+SOURCES_TEMPLATE_ATTR_NAME+"$ " +
		"</head>" +
		"<body><script type=\"text/javascript\">var reporter = new jasmine.$"+REPORTER_ATTR_NAME+"$(); jasmine.getEnv().addReporter(reporter); jasmine.getEnv().execute();</script></body>" +
		"</html>";
	
	public enum ReporterType { TrivialReporter, JsApiReporter };

	private FileUtilsWrapper fileUtilsWrapper = new FileUtilsWrapper();
	
	private final File sourceDir;
	private final File specDir;
    private final File libDir;
    private final File baseDir;
	private List<String> sourcesToLoadFirst;
	private List<File> fileNamesAlreadyWrittenAsScriptTags = new ArrayList<File>();

	public SpecRunnerHtmlGenerator(List<String> sourcesToLoadFirst, File sourceDir, File specDir, File libDir, File baseDir) {
		this.sourcesToLoadFirst = sourcesToLoadFirst;
		this.sourceDir = sourceDir;
		this.specDir = specDir;
        this.libDir = libDir;
        this.baseDir = baseDir;
	}

	public String generate(List<Artifact> dependencies, ReporterType reporterType) {
		try {
			StringTemplate template = new StringTemplate(RUNNER_HTML_TEMPLATE,DefaultTemplateLexer.class);
			
			includeJavaScriptAndCssDependencies(dependencies, template);
			setJavaScriptSourcesAttribute(template);			
			template.setAttribute(REPORTER_ATTR_NAME, reporterType.name());
			
			return template.toString();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load file names for dependencies or scripts",e);
		}
	}

	private void includeJavaScriptAndCssDependencies(
			List<Artifact> dependencies, StringTemplate template)
			throws IOException {
		StringBuilder javaScriptDependencies = new StringBuilder();
		StringBuilder cssDependencies = new StringBuilder();
		for(Artifact dep : dependencies) {
			if(JAVASCRIPT_TYPE.equals(dep.getType())) {
				javaScriptDependencies.append("<script type=\"text/javascript\">").append(fileUtilsWrapper.readFileToString(dep.getFile())).append("</script>");
			} else if(CSS_TYPE.equals(dep.getType())) {
				cssDependencies.append("<style type=\"text/css\">").append(fileUtilsWrapper.readFileToString(dep.getFile())).append("</style>");
			}
		}
		template.setAttribute(JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME, javaScriptDependencies.toString());
		template.setAttribute(CSS_DEPENDENCIES_TEMPLATE_ATTR_NAME, cssDependencies.toString());
	}
	
	private void setJavaScriptSourcesAttribute(StringTemplate template)
			throws IOException {
		StringBuilder scriptTags = new StringBuilder();
		appendScriptTagsForFiles(scriptTags, expandSourcesToLoadFirst());
        if( sourceDir.exists() ) {
    		appendScriptTagsForFiles(scriptTags, filesForScriptsInDirectory(sourceDir));
        }
		appendScriptTagsForFiles(scriptTags, filesForScriptsInDirectory(specDir));
		template.setAttribute(SOURCES_TEMPLATE_ATTR_NAME,scriptTags.toString());
	}

	private List<File> expandSourcesToLoadFirst() {
		List<File> files = new ArrayList<File>();
		if(sourcesToLoadFirst != null) {
			for(String sourceToLoadFirst : sourcesToLoadFirst) {
                File file = new File(sourceDir,sourceToLoadFirst);
                if( !file.exists() ) {
                    file = new File(libDir,sourceToLoadFirst);
                }
                files.add(file);
			}
		}
		return files;
	}

	private List<File> filesForScriptsInDirectory(File directory) throws IOException {
		List<File> files = new ArrayList<File>();
		if(directory != null) {
			fileUtilsWrapper.forceMkdir(directory);
			files = new ArrayList<File>(fileUtilsWrapper.listFiles(directory, new String[] {"js"}, true));
			Collections.sort(files); 
		} 
		return files;
	}

	private void appendScriptTagsForFiles(StringBuilder sb, List<File> sourceFiles) throws MalformedURLException {
		for (File sourceFile : sourceFiles) {
			if(!fileNamesAlreadyWrittenAsScriptTags.contains(sourceFile)) {
				sb.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"").append(findRelativePath(baseDir,sourceFile)).append("\"></script>");
				fileNamesAlreadyWrittenAsScriptTags.add(sourceFile);
			}
		}
	}

    /**
     * Build a relative file to the given base path.
     *
     * @param base - the file used as the base
     * @param file - the file to compute relative to the base path
     * @return A relative path from base to file
     */
    public static String findRelativePath(File base, File file) throws MalformedURLException
    {
        String a = ( base == null ) ? ":" : base.toURI().getPath();
        String b = file.getParentFile().toURI().getPath();
        String[] basePaths = a.split("/");
        String[] otherPaths = b.split("/");
        int n = 0;
        for (; n < basePaths.length && n < otherPaths.length; n++) {
            if (basePaths[n].equals(otherPaths[n]) == false) {
                break;
            }
        }
        if( n == 0 ) {
            return file.toURI().toURL().toString();
        }

        StringBuffer tmp = new StringBuffer();
        for (int m = n; m < basePaths.length; m++) {
            tmp.append("../");
        }
        for (int m = n; m < otherPaths.length; m++) {
            tmp.append(otherPaths[m]).append("/");
        }
        tmp.append(file.getName());

        return tmp.toString();
    }
}
