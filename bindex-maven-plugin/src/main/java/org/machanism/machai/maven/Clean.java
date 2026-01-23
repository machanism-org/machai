package org.machanism.machai.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven goal that removes Bindex/Machai plugin temporary artifacts.
 *
 * <p>
 * Currently deletes the Bindex inputs log file created under the {@value #MACHAI_TEMP_DIR} directory.
 * </p>
 *
 * <p>
 * Example:
 * {@code mvn org.machanism.machai:bindex-maven-plugin:clean}
 * </p>
 */
@Mojo(name = "clean", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.CLEAN)
public class Clean extends AbstractMojo {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Clean.class);

	/**
	 * Name of the temporary directory used by Machai/Bindex.
	 */
	public static final String MACHAI_TEMP_DIR = ".machai";

	/**
	 * Project base directory (injected by Maven).
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * Executes the goal by attempting to delete the inputs log file.
	 *
	 * @throws MojoExecutionException if an unexpected error occurs while executing the goal
	 */
	@Override
	public void execute() throws MojoExecutionException {
		File file = new File(basedir, MACHAI_TEMP_DIR + "/bindex-inputs.txt");
		logger.info("Removing '{}' inputs log file.", file);
		boolean deleted = file.delete();
		if (deleted) {
			logger.info("Cleanup process finished.");
		}
	}
}
