package org.machanism.machai.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.bindex.bulder.MavenBIndexBuilder;
import org.machanism.machai.core.ai.GenAIProvider;

import com.openai.models.ChatModel;

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
		GenAIProvider provider = new GenAIProvider(ChatModel.of(chatModel));

		BindexCreator creator = new BindexCreator(provider);
		creator.update(update);

		MavenBIndexBuilder bindexBuilder = new MavenBIndexBuilder(!inputsOnly);
		bindexBuilder.effectivePomRequired(true);
		bindexBuilder.model(project.getModel());
		creator.processProject(bindexBuilder);
	}

	boolean isBindexed() {
		return !"pom".equals(project.getPackaging());
	}
}