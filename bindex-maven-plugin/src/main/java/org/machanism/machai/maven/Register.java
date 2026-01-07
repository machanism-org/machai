package org.machanism.machai.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.BindexRegister;

/**
 * Maven Mojo to execute the "register" goal for Bindex.
 * <p>
 * This goal registers the existing Bindex index and resources for the project folder using the configured AI provider.
 * <p>
 * Usage Example:
 * <pre>
 * {@code
 * mvn org.machanism.machai:bindex-maven-plugin:register
 * }
 * </pre>
 */
@Mojo(name = "register", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Register extends AbstractBindexMojo {

	/**
	 * If true, performs update during register operation.
	 */
	@Parameter(defaultValue = "true")
	protected boolean update;
	
	/**
	 * "URL of the registration database for storing project metadata.
	 */
	@Parameter(property = "bindex.register.url")
	protected String registerUrl;

	/**
	 * Executes the register goal, updating and scanning Bindex resources.
	 *
	 * @throws MojoExecutionException if any errors occur during registration
	 * @throws MojoFailureException if the goal fails for a known project issue
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isBindexed()) {
			GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);
			try (BindexRegister register = new BindexRegister(provider, registerUrl)) {
				register.update(update);
				register.scanFolder(project.getBasedir());
			} catch (IOException e) {
				throw new MojoExecutionException("Bindex register failed.", e);
			}
		}
	}

}
