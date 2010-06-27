package searls.jasmine;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal testResources
 * @phase process-test-resources
 */
public class ProcessTestResourcesMojo extends AbstractJasmineMojo {
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			getLog().info("Processing JavaScript Specs");
			if(FileUtils.fileExists(testSrcDir)) {
				FileUtils.copyDirectory(new File(testSrcDir), new File(jasmineTargetDir+File.separatorChar+specDirectoryName));
			} else {
				getLog().warn("JavaScript test source folder was expected but was not found. Set configuration property `testSrcDir` to the directory containing your specs. Skipping jasmine:testResources processing.");
			}
		} catch (IOException e) {
			throw new MojoFailureException("Failed to copy JavaScript test sources.");
		}
	}

}
