package org.machanism.machai.bindex.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
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
	@Parameter(property = "bindex.genai", defaultValue = "OpenAI:gpt-5")
	protected String genai;

	/**
	 * The base directory of the Maven project.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * Provides configuration properties to other components (for example API
	 * keys/provider settings).
	 */
	private final Configurator configurator;

	/**
	 * Creates a new instance.
	 */
	public AbstractBindexMojo() {
		super();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		try {
			configurator.setConfiguration("bindex.properties");
		} catch (IOException e) {
			getLog().warn("Configuration file `bindex.properties` not found.");
		}
		this.configurator = configurator;
	}

	/**
	 * Creates or updates the Bindex index and related resources for the current
	 * project.
	 *
	 * @param update whether to run in update mode (incremental refresh) instead of
	 *               create mode
	 */
	protected void createBindex(boolean update) {
		BindexCreator creator = new BindexCreator(genai, configurator);
		creator.update(update);

		MavenProjectLayout projectLayout = new MavenProjectLayout();
		projectLayout.projectDir(basedir);
		projectLayout.effectivePomRequired(true);
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
	 * Returns the configurator used to resolve plugin configuration properties.
	 *
	 * @return the configurator instance
	 */
	public Configurator getConfigurator() {
		return configurator;
	}
}
