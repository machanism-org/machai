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

@Mojo(name = "create", requiresProject = false, requiresDependencyCollection = ResolutionScope.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class Create extends AbstractBindexMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		GenAIProvider provider = new GenAIProvider(ChatModel.of(model));
		provider.setDebugMode(debug);

		BindexCreator creator = new BindexCreator(provider);
		creator.update(false);
		
		try {
			creator.scanProjects(project.getBasedir());
		} catch (IOException | XmlPullParserException e) {
			throw new MojoExecutionException("Bindex generation failed.", e);
		}
	}

}
