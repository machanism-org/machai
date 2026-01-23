package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Maven goal that updates (refreshes) an existing Bindex index for the current project.
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
	 * Runs the {@code update} goal.
	 *
	 * @throws MojoExecutionException if Bindex fails to update the index/resources
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (isBindexed()) {
			createBindex(true);
		}
	}
}
