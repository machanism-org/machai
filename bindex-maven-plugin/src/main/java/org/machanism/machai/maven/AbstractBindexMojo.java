package org.machanism.machai.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.project.layout.MavenProjectLayout;

/**
 * Abstract base Mojo for Bindex Maven plugin operations.
 * <p>
 * This class provides common functionality for the core plugin goals
 * (create, update, register) and manages interaction with the Bindex AI
 * provider, resource processing, and Maven project context.
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * AbstractBindexMojo mojo = new Create();
 * mojo.execute();
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public abstract class AbstractBindexMojo extends AbstractMojo {

	/**
	 * The Maven project associated with the current build.
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	/**
	 * If true, only input resources are processed; output generation is skipped.
	 */
	@Parameter(property = "bindex.inputs.only", defaultValue = "false")
	protected boolean inputsOnly;

	/**
	 * The chat model identifier used by Bindex AI provider.
	 */
	@Parameter(property = "bindex.chatModel", defaultValue = "OpenAI:gpt-5")
	protected String chatModel;

	/**
	 * The base directory of the Maven project.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * Default constructor.
	 */
	public AbstractBindexMojo() {
		super();
	}

	/**
	 * Creates or updates Bindex index and resources for the project folder.
	 *
	 * @param update true if the update mode should be enabled
	 */
	void createBindex(boolean update) {
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);

		BindexCreator creator = new BindexCreator(provider, !inputsOnly);
		creator.update(update);

		MavenProjectLayout projectLayout = new MavenProjectLayout();
		projectLayout.projectDir(basedir);
		projectLayout.effectivePomRequired(true);
		projectLayout.model(project.getModel());
		creator.processFolder(projectLayout);
	}

	/**
	 * Returns true if the project packaging type is suitable for Bindex operations.
	 *
	 * @return true if packaging is not "pom"
	 */
	boolean isBindexed() {
		return !"pom".equals(project.getPackaging());
	}
}
