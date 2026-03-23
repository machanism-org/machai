package org.machanism.machai.bindex.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven goal that creates a new Bindex index for the current project.
 *
 * <p>
 * The goal is skipped for projects with {@code pom} packaging.
 * </p>
 *
 * <p>
 * Example:
 * {@code mvn org.machanism.machai:bindex-maven-plugin:create}
 * </p>
 */
@Mojo(name = "create", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Create extends AbstractBindexMojo {

	/**
	 * Logger for this Mojo.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Create.class);

	/**
	 * Runs the {@code create} goal.
	 *
	 * @throws MojoExecutionException if Bindex fails to create the index/resources
	 */
	@Override
	public void execute() throws MojoExecutionException {
		try {
			if (isBindexed()) {
				logger.info("Creating Bindex index for project '{}' in '{}'.", project.getArtifactId(), basedir);
				createBindex(false);
			} else {
				logger.info("Skipping Bindex create for pom-packaged project '{}'.", project.getArtifactId());
			}
		} finally {
			GenAIProviderManager.logUsage();
		}
	}
}
