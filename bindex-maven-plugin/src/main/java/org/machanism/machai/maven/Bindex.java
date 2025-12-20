package org.machanism.machai.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.BindexCreator;
import org.machanism.machai.core.ai.GenAIProvider;

import com.openai.models.ChatModel;

@Mojo(name = "bindex", requiresProject = false, requiresDependencyCollection = ResolutionScope.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class Bindex extends AbstractBindexMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		GenAIProvider provider = new GenAIProvider(ChatModel.of(model));
		provider.setDebugMode(debug);

		BindexCreator register = new BindexCreator(provider);
		register.update(update);
		try {
			register.scanProjects(project.getBasedir());
		} catch (IOException | XmlPullParserException e) {
			throw new MojoExecutionException("Bindex generation failed.", e);
		}
	}

}
