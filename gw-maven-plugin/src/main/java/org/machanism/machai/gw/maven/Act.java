package org.machanism.machai.gw.maven;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;

@Mojo(name = "act", aggregator = true)
public class Act extends AbstractGWGoal {

	/**
	 * Interactive prompt provider used to collect the assembly prompt when
	 * {@link #assemblyPromptFile} does not exist.
	 */
	@Component
	protected Prompter prompter;

	@Override
	public void execute() throws MojoExecutionException {
		try {
			GenAIProvider provider = GenAIProviderManager.getProvider(genai, getConfiguration());
			provider.setWorkingDir(basedir);
			FunctionToolsLoader.getInstance().applyTools(provider);

			try {
				String action = prompter.prompt("Action");

				String name = StringUtils.substringBefore(action, " ");
				String prompt = StringUtils.substringAfter(action, " ");

				ResourceBundle promptBundle = ResourceBundle.getBundle("act/" + name);

				String commitInstructions = promptBundle.getString("instructions");
				provider.instructions(commitInstructions);
				String inputs = MessageFormat.format(promptBundle.getString("inputs"), prompt);
				provider.prompt(inputs);
				String perform = provider.perform();
				getLog().info(perform);

			} catch (PrompterException e) {
				getLog().error("Error: " + e.getMessage());
			}

		} finally {
			GenAIProviderManager.logUsage();
		}
	}

}
