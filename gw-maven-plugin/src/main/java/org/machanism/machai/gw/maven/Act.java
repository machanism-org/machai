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

/**
 * Maven goal that runs an interactive "act" workflow against the current project.
 *
 * <p>
 * The goal prompts the user for an action name followed by an optional free-form prompt. It then loads a
 * {@link ResourceBundle} from {@code act/<name>} that describes how to execute that action and delegates processing to
 * {@link AIFileProcessor}.
 * </p>
 *
 * <h2>Input format</h2>
 * <p>
 * The interactive input is a single line:
 * </p>
 * <ul>
 *   <li><b>{@code <name>}</b> - identifies the action bundle under {@code act/}.</li>
 *   <li><b>{@code <prompt>}</b> (optional) - appended after the first whitespace; when omitted the value of
 *   {@code -Dgw.guidance} (if provided) is used.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>
 * This goal inherits the common parameters from {@link AbstractGWGoal}:
 * {@code gw.genai}, {@code gw.instructions}, {@code gw.guidance}, {@code gw.excludes},
 * {@code gw.genai.serverId}, and {@code gw.logInputs}.
 * </p>
 *
 * <h2>Usage examples</h2>
 * <pre>
 * mvn gw:act
 * </pre>
 *
 * <pre>
 * mvn gw:act -Dgw.guidance="Improve readability"
 * </pre>
 */
@Mojo(name = "act", aggregator = true)
public class Act extends AbstractGWGoal {

	/**
	 * Interactive prompt provider used to collect the action input.
	 */
	@Component
	protected Prompter prompter;

	/**
	 * Executes the interactive action.
	 *
	 * @throws MojoExecutionException if an I/O failure occurs while processing files
	 */
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
