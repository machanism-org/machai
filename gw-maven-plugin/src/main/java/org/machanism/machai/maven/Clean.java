package org.machanism.machai.maven;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mojo(name = "clean", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.CLEAN)
public class Clean extends AbstractMojo {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(Clean.class);

	public static final String MACHAI_TEMP_DIR = ".machai";

	/**
	 * Project base directory. Set by Maven.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	@Override
	public void execute() throws MojoExecutionException {
		File file = new File(basedir, MACHAI_TEMP_DIR + "/docs-inputs");
		logger.info("Removing '{}' inputs log file.", file);
		boolean delete = FileUtils.deleteQuietly(file);
		if (delete) {
			logger.info("Cleanup process finished.");
		}
	}
}
