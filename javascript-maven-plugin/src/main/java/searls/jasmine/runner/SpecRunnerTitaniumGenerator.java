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

package searls.jasmine.runner;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import searls.jasmine.io.FileUtilsWrapper;
import searls.jasmine.io.IOUtilsWrapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Class generating the titanium app.js file.
 */
public class SpecRunnerTitaniumGenerator {
    private static final String JASMINE_JS = "/vendor/js/jasmine.js";
    private static final String JASMINE_TITANIUM_JS = "/vendor/js/jasmine-titanium.js";

    /**
     * The name of the javascript dependencies placeholder in the template document.
     */
    private static final String JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME = "javascriptDependencies";

    /**
     * The name of the sources placeholder in the template document.
     */
    private static final String SOURCES_TEMPLATE_ATTR_NAME = "sources";

    /**
     * The name of the specs placeholder in the template document.
     */
    private static final String SPECS_TEMPLATE_ATTR_NAME = "specs";

    /**
     * The name of the reporter placeholder in the template document.
     */
    private static final String REPORTER_ATTR_NAME = "reporter";

    /**
     * The template of the app.js file.
     */
    private static final String RUNNER_JS_TEMPLATE = "(function(){\n" +
            "  $" + JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME + "$\n" +
            "\n" +
            "  $"+ SOURCES_TEMPLATE_ATTR_NAME +"$\n" +
            "  \n" +
            "  // Include all the test files\n" +
            "  $" + SPECS_TEMPLATE_ATTR_NAME + "$\n" +
            "  \n" +
            "  var reporter = new jasmine.$" + REPORTER_ATTR_NAME + "$();\n" +
            "  jasmine.getEnv().addReporter(reporter);\n" +
            "})();";

    private List<String> sourcesToLoadFirst;
    private List<String> sourceFiles;
    private File specDir;
    private String specDirName;
    private File libDir;
    private String libDirName;
    private File baseDir;

    private FileUtilsWrapper fileUtilsWrapper = new FileUtilsWrapper();
    private IOUtilsWrapper ioUtilsWrapper = new IOUtilsWrapper();

    /**
     * Jasmine reporter type.
     */
    public enum ReporterType {
        /**
         * Titanium specific reporter.
         */
        TITANIUM
    };

    public SpecRunnerTitaniumGenerator(List<String> sourcesToLoadFirst, List<String> sourceFiles,
                                       File baseDir,
                                       String libDirName, String specDirName) {
        this.sourcesToLoadFirst = sourcesToLoadFirst;
        this.sourceFiles = sourceFiles;
        this.baseDir = baseDir;
        this.specDirName = specDirName;
        this.specDir = new File(baseDir, specDirName);
        this.libDir = new File(baseDir, libDirName);
        this.libDirName = libDirName;
    }

    /**
     * Generate the jasmine tests runner.
     *
     * @param reporterType The reporter to use.
     */
    public String generate(ReporterType reporterType) {
        try {
            StringTemplate template = new StringTemplate(RUNNER_JS_TEMPLATE, DefaultTemplateLexer.class);
            includeDependencies(Arrays.asList(JASMINE_JS, JASMINE_TITANIUM_JS), template);
            includeSources(template);
            includeSpecs(template);
            switch (reporterType) {
                case TITANIUM:
                    template.setAttribute(REPORTER_ATTR_NAME,"TitaniumReporter");
            }

            return template.toString();
        } catch (IOException t) {
            throw new RuntimeException("Failed to load file names for dependencies or scripts", t);
        }
    }

    /**
     * Include the dependencies in the templates.
     * @param dependencies The dependencies to add.
     * @param template The template where the dependencies should be added.
     * @throws IOException
     */
    private void includeDependencies(List<String> dependencies, StringTemplate template)
            throws IOException {
        StringBuilder jsDependencies = new StringBuilder();

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File(baseDir, "dependencies.js"));
            if (dependencies != null) {
                for (String dep : dependencies) {
                    writer.append(ioUtilsWrapper.toString(dep));
                    writer.append("\n");
                }
            }
        } finally {
            IOUtil.close(writer);
        }
        jsDependencies.append("Ti.include(\"dependencies.js\");\n");
        template.setAttribute(JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME, jsDependencies.toString());
    }

    private void includeSources(StringTemplate template) {
        StringBuilder sourcesDependencies = new StringBuilder();

        if (sourcesToLoadFirst != null) {
            for (String source : sourcesToLoadFirst) {
                File sourceFile = new File(baseDir, source);
                if (sourceFile.exists()) {
                    sourcesDependencies.append("Ti.include(\"");
                    sourcesDependencies.append(source);
                    sourcesDependencies.append("\");\n");
                } else {
                    sourceFile = new File(libDir, source);
                    if (sourceFile.exists()) {
                        sourcesDependencies.append("Ti.include(\"");
                        sourcesDependencies.append(libDirName);
                        sourcesDependencies.append("/");
                        sourcesDependencies.append(source);
                        sourcesDependencies.append("\");\n");

                    }
                }
            }
        }
        sourcesDependencies.append("\n");

        if (sourceFiles != null) {
            for (String source : sourceFiles) {
                if (sourcesToLoadFirst == null || !sourcesToLoadFirst.contains(source)) {
                    sourcesDependencies.append("Ti.include(\"");
                    sourcesDependencies.append(source);
                    sourcesDependencies.append("\");\n");
                }
            }
        }
        template.setAttribute(SOURCES_TEMPLATE_ATTR_NAME, sourcesDependencies.toString());
    }

    private void includeSpecs(StringTemplate template) {
        StringBuilder specBuilder = new StringBuilder();

        String[] specFiles = null;
        if (specDir != null && specDir.exists()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(specDir);
            scanner.setIncludes(new String[] {"**/*.js"});
            scanner.addDefaultExcludes();
            scanner.scan();
            specFiles = scanner.getIncludedFiles();
        }

        if (specFiles != null) {
            for (String spec : specFiles) {
                specBuilder.append("Ti.include(\"");
                specBuilder.append(specDirName);
                specBuilder.append("/");
                specBuilder.append(spec);
                specBuilder.append("\");\n");
            }
        }

        template.setAttribute(SPECS_TEMPLATE_ATTR_NAME, specBuilder.toString());
    }
}
