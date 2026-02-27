package org.machanism.machai.gw.maven;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

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

			AIFileProcessor fileProcessor = new AIFileProcessor(basedir, getConfiguration(), genai);

			try {
				String action = prompter.prompt("Act");

				String name = StringUtils.substringBefore(action, " ");
				String prompt = StringUtils.defaultIfBlank(StringUtils.substringAfter(action, " "),
						StringUtils.defaultString(guidance));

				ResourceBundle promptBundle = ResourceBundle.getBundle("act/" + name);

				String commitInstructions;
				try {
					commitInstructions = promptBundle.getString("instructions");
				} catch (MissingResourceException e) {
					commitInstructions = instructions;
				}

				String inputs = MessageFormat.format(promptBundle.getString("inputs"), prompt);

				ProjectLayout projectLayout = fileProcessor.getProjectLayout(basedir);
				String perform = fileProcessor.process(projectLayout, basedir, commitInstructions, inputs);
				getLog().info(perform);

			} catch (PrompterException e) {
				getLog().error("Error: " + e.getMessage());
			}

		} catch (IOException e) {
			getLog().error("I/O error occurred during file processing: " + e.getMessage());
			throw new MojoExecutionException("I/O error occurred during file processing", e);

		} finally {
			GenAIProviderManager.logUsage();
		}
	}

}
