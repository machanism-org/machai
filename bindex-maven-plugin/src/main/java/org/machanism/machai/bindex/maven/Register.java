package org.machanism.machai.bindex.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.machanism.machai.bindex.BindexRegister;

/**
 * Maven goal that registers the project's Bindex metadata in an external
 * registry.
 *
 * <p>
 * Uses the configured AI provider/model to scan the project folder and publish
 * metadata to the configured registry. The goal is skipped for projects with
 * {@code pom} packaging.
 * </p>
 *
 * <p>
 * Example:
 * {@code mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.register.url=http://localhost:8080}
 * </p>
 */
@Mojo(name = "register", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Register extends AbstractBindexMojo {

	/**
	 * Whether to perform an update while registering.
	 */
	@Parameter(defaultValue = "true")
	protected boolean update;

	/**
	 * URL of the registry database endpoint where project metadata should be
	 * stored.
	 */
	@Parameter(property = "bindex.register.url")
	protected String registerUrl;

	/**
	 * Runs the {@code register} goal.
	 *
	 * @throws MojoExecutionException if an unexpected error occurs during
	 *                                registration
	 * @throws MojoFailureException   if the goal fails due to a known,
	 *                                user-correctable problem
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isBindexed()) {
			try {
				BindexRegister register = new BindexRegister(genai, registerUrl, getConfigurator());
				register.update(update);
				register.scanFolder(project.getBasedir());
			} catch (IOException e) {
				throw new MojoExecutionException("Bindex register failed.", e);
			}
		}
	}
}
