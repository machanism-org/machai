package org.machanism.machai.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.machanism.machai.bindex.BindexRegister;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.ai.GenAIProviderManager;

@Mojo(name = "register", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Register extends AbstractBindexMojo {

	@Parameter(defaultValue = "true")
	protected boolean update;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isBindexed()) {
			GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);
			try (BindexRegister register = new BindexRegister(provider)) {
				register.update(update);
				register.scanProjects(project.getBasedir());
			} catch (IOException e) {
				throw new MojoExecutionException("Bindex register failed.", e);
			}
		}
	}

}
