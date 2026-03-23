package org.machanism.machai.bindex.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven goal that updates (refreshes) an existing Bindex index for the current
 * project.
 *
 * <p>
 * The goal is skipped for projects with {@code pom} packaging.
 * </p>
 *
 * <p>
 * Example:
 * {@code mvn org.machanism.machai:bindex-maven-plugin:update}
 * </p>
 */
@Mojo(name = "update", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Update extends AbstractBindexMojo {

	/**
	 * Logger for this Mojo.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Update.class);

	/**
	 * Runs the {@code update} goal.
	 *
	 * @throws MojoExecutionException if Bindex fails to update the index/resources
	 */
	@Override
	public void execute() throws MojoExecutionException {
		try {
			if (isBindexed()) {
				logger.debug("Updating Bindex index for project '{}' in '{}'.", project.getArtifactId(), basedir);
				createBindex(true);
			} else {
				logger.debug("Skipping Bindex update for pom-packaged project '{}'.", project.getArtifactId());
			}
		} finally {
			GenAIProviderManager.logUsage();
		}
	}
}
