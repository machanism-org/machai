package org.machanism.machai.assembly.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

/**
 * Maven {@link org.apache.maven.plugin.Mojo} implementing the {@code assembly} goal.
 *
 * <p>
 * This goal runs MachAI's AI-assisted workflow against the Maven execution {@link #basedir}. It:
 * </p>
 * <ol>
 *   <li>Acquires a natural-language prompt from {@link #assemblyPromptFile} (if present) or requests it interactively.</li>
 *   <li>Uses {@link #pickChatModel} to recommend candidate libraries (as {@link Bindex} entries) via {@link Picker}.</li>
 *   <li>Filters recommendations by {@link #score}.</li>
 *   <li>Runs {@link ApplicationAssembly} with {@link #chatModel} to apply changes in {@link #basedir}.</li>
 * </ol>
 *
 * <p>
 * The provider identifiers are resolved by {@link GenAIProviderManager} and augmented with standard function tools via
 * {@link FunctionToolsLoader}.
 * </p>
 *
 * <h2>Plugin parameters</h2>
 * <ul>
 *   <li>{@code assembly.genai} (default {@code OpenAI:gpt-5}) &ndash; Provider id for the assembly phase.</li>
 *   <li>{@code pick.genai} (default {@code OpenAI:gpt-5-mini}) &ndash; Provider id for the library recommendation (picker)
 *   phase.</li>
 *   <li>{@code assembly.prompt.file} (default {@code project.txt}) &ndash; File containing the prompt; if absent, the prompt
 *   is requested interactively.</li>
 *   <li>{@code assembly.score} (default {@code 0.9}) &ndash; Minimum score required for a recommended library to be
 *   listed/used.</li>
 *   <li>{@code bindex.register.url} (optional) &ndash; Registration/lookup endpoint used by the picker.</li>
 * </ul>
 *
 * <h2>Usage examples</h2>
 *
 * <p><b>Command line:</b></p>
 * <pre>
 * mvn org.machanism.machai:assembly-maven-plugin:assembly
 *   -Dassembly.genai=OpenAI:gpt-5
 *   -Dpick.genai=OpenAI:gpt-5-mini
 *   -Dassembly.prompt.file=project.txt
 *   -Dassembly.score=0.9
 * </pre>
 */
@Mojo(name = "assembly", requiresProject = false, requiresDependencyCollection = ResolutionScope.NONE)
public class Assembly extends AbstractMojo {

	/**
	 * Interactive prompt provider used to collect the assembly prompt when {@link #assemblyPromptFile} does not exist.
	 */
	@Component
	protected Prompter prompter;

	/**
	 * GenAI provider identifier used for the assembly workflow.
	 *
	 * <p>
	 * The value is resolved by {@link GenAIProviderManager} (for example, {@code OpenAI:gpt-5}).
	 * </p>
	 */
	@Parameter(property = "assembly.genai", defaultValue = "OpenAI:gpt-5")
	protected String chatModel;

	/**
	 * GenAI provider identifier used for library recommendation/picking.
	 *
	 * <p>
	 * The value is resolved by {@link GenAIProviderManager} (for example, {@code OpenAI:gpt-5-mini}).
	 * </p>
	 */
	@Parameter(property = "pick.genai", defaultValue = "OpenAI:gpt-5-mini")
	protected String pickChatModel;

	/**
	 * Prompt file for the assembly workflow.
	 *
	 * <p>
	 * If the file exists, it is read as text and used as the prompt; otherwise the prompt is requested interactively.
	 * </p>
	 */
	@Parameter(property = "assembly.prompt.file", defaultValue = "project.txt")
	protected File assemblyPromptFile;

	/**
	 * Minimum score threshold for recommended libraries.
	 */
	@Parameter(property = "assembly.score", defaultValue = "0.9")
	protected Double score;

	/**
	 * Optional registration URL used by the picker for metadata lookups/registration.
	 */
	@Parameter(property = "bindex.register.url")
	protected String registerUrl;

	/**
	 * Maven execution base directory where changes are applied.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * Executes the {@code assembly} goal.
	 *
	 * <p>
	 * Execution steps:
	 * </p>
	 * <ol>
	 *   <li>Read the prompt from {@link #assemblyPromptFile} if it exists; otherwise prompt the user.</li>
	 *   <li>Create a {@link Configurator} backed by {@code bindex.properties}.</li>
	 *   <li>Run {@link Picker} using {@link #pickChatModel} and log any recommended {@link Bindex} entries.</li>
	 *   <li>Run {@link ApplicationAssembly} using {@link #chatModel} to apply changes to {@link #basedir}.</li>
	 * </ol>
	 *
	 * @throws MojoExecutionException if prompt acquisition fails, provider interaction fails, or the assembly workflow fails
	 */
	@Override
	public void execute() throws MojoExecutionException {
		try {
			String query;
			if (assemblyPromptFile.exists()) {
				try (FileReader reader = new FileReader(assemblyPromptFile)) {
					query = IOUtils.toString(reader);
				}
			} else {
				query = prompter.prompt("Please enter the project assembly prompt or specify the file name");
			}

			Configurator config = new PropertiesConfigurator("bindex.properties");

			Picker picker = new Picker(pickChatModel, registerUrl, config);
			picker.setScore(score);
			List<Bindex> bindexList = picker.pick(query);

			if (bindexList.isEmpty()) {
				getLog().info("No libraries were recommended by the picker.");
				return;
			}

			int i = 1;
			getLog().info("Recommended libraries:");
			for (Bindex bindex : bindexList) {
				String scoreStr = picker.getScore(bindex.getId()) != null ? picker.getScore(bindex.getId()).toString() : "";
				getLog().info(String.format("%2$3s. %1s %3s", bindex.getId(), i++, scoreStr));
			}

			ApplicationAssembly assembly = new ApplicationAssembly(chatModel, config, basedir);

			getLog().info("The project directory: " + basedir);
			assembly.projectDir(basedir);
			assembly.assembly(query, bindexList);

		} catch (IOException | PrompterException e) {
			throw new MojoExecutionException("The project assembly process failed.", e);
		}
	}
}
