package org.machanism.machai.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.ai.GenAIProviderManager;
import org.machanism.machai.project.layout.MavenProjectLayout;

public abstract class AbstractBindexMojo extends AbstractMojo {

	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	@Parameter(property = "bindex.inputs.only", defaultValue = "false")
	protected boolean inputsOnly;

	@Parameter(property = "bindex.chatModel", defaultValue = "gpt-5")
	protected String chatModel;

	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	public AbstractBindexMojo() {
		super();
	}

	void createBindex(boolean update) {
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);

		BindexCreator creator = new BindexCreator(provider, !inputsOnly);
		creator.update(update);

		MavenProjectLayout projectLayout = new MavenProjectLayout();
		projectLayout.projectDir(basedir);
		projectLayout.effectivePomRequired(true);
		projectLayout.model(project.getModel());
		creator.processProject(projectLayout);
	}

	boolean isBindexed() {
		return !"pom".equals(project.getPackaging());
	}
}