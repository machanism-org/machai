package org.machanism.machai.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.ai.provider.none.NoneProvider;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

/**
 * Maven {@link org.apache.maven.plugin.Mojo} implementing the {@code assembly} goal.
 *
 * <p>
 * The goal orchestrates an AI-assisted project "assembly" workflow:
 * </p>
 * <ol>
 *   <li>Acquire a prompt from {@link #assemblyPromptFile} (if present) or request it interactively.</li>
 *   <li>Use {@link #pickChatModel} to recommend candidate libraries (as {@link Bindex} entries).</li>
 *   <li>Filter recommendations by {@link #score}.</li>
 *   <li>Run {@link ApplicationAssembly} with {@link #chatModel} to apply changes in {@link #basedir}.</li>
 * </ol>
 *
 * <h2>Plugin parameters</h2>
 * <ul>
 *   <li>{@code assembly.genai} (default {@code OpenAI:gpt-5}) &ndash; Provider id for the assembly phase.</li>
 *   <li>{@code pick.genai} (default {@code OpenAI:gpt-5-mini}) &ndash; Provider id for the library recommendation
 *   (picker) phase.</li>
 *   <li>{@code assembly.prompt.file} (default {@code project.txt}) &ndash; File containing the prompt; if absent,
 *   the prompt is requested interactively.</li>
 *   <li>{@code assembly.score} (default {@code 0.9}) &ndash; Minimum score required for a recommended library to be
 *   listed/used.</li>
 *   <li>{@code bindex.register.url} (optional) &ndash; Registration/lookup endpoint used by the picker.</li>
 * </ul>
 *
 * <h2>Usage examples</h2>
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
	 * Interactive prompter used to collect the prompt when {@link #assemblyPromptFile} does not exist.
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
	 * If the file exists, it is read as text and used as the prompt; otherwise the prompt is requested
	 * interactively.
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
	 * The execution performs the following steps:
	 * </p>
	 * <ol>
	 *   <li>Read the prompt from {@link #assemblyPromptFile} when it exists; otherwise prompt the user.</li>
	 *   <li>Initialize the picker GenAI provider and apply standard system function tools.</li>
	 *   <li>Use {@link Picker} to recommend libraries and log recommendations to the build output.</li>
	 *   <li>Run the {@link ApplicationAssembly} workflow in {@link #basedir} using the assembly GenAI provider.</li>
	 *   <li>Optionally enter an interactive prompt loop (skipped for {@link NoneProvider}).</li>
	 * </ol>
	 *
	 * @throws MojoExecutionException if prompt acquisition fails, provider interaction fails, or the assembly
	 *                                workflow fails
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

			GenAIProvider provider = GenAIProviderManager.getProvider(pickChatModel, config);
			provider.setWorkingDir(basedir);
			new SystemFunctionTools().applyTools(provider);

			try (Picker picker = new Picker(provider, registerUrl)) {
				picker.setScore(score);
				List<Bindex> bindexList = picker.pick(query);

				if (bindexList.isEmpty()) {
					getLog().info("No libraries were recommended by the picker.");
					return;
				}

				int i = 1;
				getLog().info("Recommended libraries:");
				for (Bindex bindex : bindexList) {
					String scoreStr = picker.getScore(bindex.getId()) != null ? picker.getScore(bindex.getId()).toString()
							: "";
					getLog().info(String.format("%2$3s. %1s %3s", bindex.getId(), i++, scoreStr));
				}

				GenAIProvider assemblyProvider = GenAIProviderManager.getProvider(chatModel, config);
				assemblyProvider.setWorkingDir(basedir);
				new SystemFunctionTools().applyTools(assemblyProvider);

				ApplicationAssembly assembly = new ApplicationAssembly(assemblyProvider);

				getLog().info("The project directory: " + basedir);
				assembly.projectDir(basedir);
				assembly.assembly(query, bindexList);

				if (!(assemblyProvider instanceof NoneProvider)) {
					String prompt;
					while (!StringUtils.equalsIgnoreCase(prompt = prompter.prompt("Prompt"), "exit")) {
						assemblyProvider.prompt(prompt);
					}
				}
			}
		} catch (IOException | PrompterException e) {
			throw new MojoExecutionException("The project assembly process failed.", e);
		}
	}

}
