package org.machanism.machai.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.BindexRegister;
import org.machanism.machai.core.ai.GenAIProvider;

import com.openai.models.ChatModel;

@Mojo(name = "register", requiresProject = false, requiresDependencyCollection = ResolutionScope.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class Register extends AbstractBindexMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		GenAIProvider provider = new GenAIProvider(ChatModel.of(model));
		try (BindexRegister register = new BindexRegister(provider)) {
			register.update(update);
			register.scanProjects(project.getBasedir());
		} catch (IOException | XmlPullParserException e) {
			throw new MojoExecutionException("Bindex register failed.", e);
		}
	}

}
