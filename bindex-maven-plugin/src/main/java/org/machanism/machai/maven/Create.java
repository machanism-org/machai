package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

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
	 * Runs the {@code create} goal.
	 *
	 * @throws MojoExecutionException if Bindex fails to create the index/resources
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (isBindexed()) {
			createBindex(false);
		}
	}
}
