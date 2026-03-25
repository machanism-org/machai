package org.machanism.machai.bindex.maven;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.project.layout.MavenProjectLayout;

/**
 * Base class for the Bindex Maven plugin goals.
 *
 * <p>
 * Provides common parameters (project/base directory/model selection) and
 * shared helper methods used by concrete goals such as {@link Create},
 * {@link Update}, and {@link Register}.
 * </p>
 */
public abstract class AbstractBindexMojo extends AbstractMojo {

	/**
	 * The Maven project associated with the current build.
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	/**
	 * The AI provider/model identifier used by the plugin (for example
	 * {@code OpenAI:gpt-5}).
	 */
	@Parameter(property = BindexCreator.MODEL_PROP_NAME, required = true)
	protected String model;

	/**
	 * The base directory of the Maven project.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * The Maven settings used to resolve credentials from {@code settings.xml}.
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;

	/**
	 * {@code settings.xml} {@code <server>} id used to read GenAI credentials.
	 */
	@Parameter(property = GenAIProvider.SERVERID_PROP_NAME, required = false)
	private String serverId;

	/**
	 * Creates or updates the Bindex index and related resources for the current
	 * project.
	 *
	 * @param update whether to run in update mode (incremental refresh) instead of
	 *               create mode
	 * @throws MojoExecutionException if the Bindex operation fails
	 */
	protected void createBindex(boolean update) throws MojoExecutionException {
		PropertiesConfigurator config = getConfigurator();
		BindexCreator creator = new BindexCreator(model, config);
		creator.update(update);

		boolean inputsLog = config.getBoolean(GenAIProvider.LOG_INPUTS_PROP_NAME, false);
		creator.setLogInputs(inputsLog);

		MavenProjectLayout projectLayout = new MavenProjectLayout();
		projectLayout.projectDir(basedir);
		projectLayout.model(project.getModel());
		creator.processFolder(projectLayout);
	}

	/**
	 * Indicates whether the current Maven project should be processed by Bindex.
	 *
	 * <p>
	 * The plugin skips projects with {@code pom} packaging (typically
	 * parent/aggregator modules).
	 * </p>
	 *
	 * @return {@code true} if the project packaging is not {@code pom}; otherwise
	 *         {@code false}
	 */
	protected boolean isBindexed() {
		return !"pom".equals(project.getPackaging());
	}

	/**
	 * Builds a {@link PropertiesConfigurator} for workflow execution.
	 *
	 * <p>
	 * When {@code genai.serverId} is set, credentials are read from Maven
	 * settings.
	 * </p>
	 *
	 * @return a configurator populated with any available workflow properties
	 * @throws MojoExecutionException if Maven settings are not available or
	 *                                configured incorrectly
	 */
	protected PropertiesConfigurator getConfigurator() throws MojoExecutionException {
		if (settings == null) {
			throw new MojoExecutionException("Maven settings are not available.");
		}

		PropertiesConfigurator config = new PropertiesConfigurator();

		if (serverId != null) {
			Server server = settings.getServer(serverId);
			if (server == null) {
				throw new MojoExecutionException("No <server> with id '" + serverId + "' found in Maven settings.xml.");
			}

			String username = server.getUsername();
			if (StringUtils.isNotBlank(username)) {
				config.set("GENAI_USERNAME", username);
			}
			String password = server.getPassword();
			if (StringUtils.isNotBlank(password)) {
				config.set("GENAI_PASSWORD", password);
			}
		}

		return config;
	}

}
