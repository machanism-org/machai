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
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

/**
 * Maven Mojo that runs the MachAI project "assembly" workflow.
 *
 * <p>
 * The {@code assembly} goal is intentionally configured with
 * {@code requiresProject=false} so it can be executed outside of a standard
 * Maven project, operating on the configured {@link #basedir}.
 * </p>
 *
 * <p>
 * The workflow is:
 * </p>
 * <ol>
 * <li>Obtain an assembly prompt from {@link #assemblyPromptFile} if present, or
 * by interactively prompting the user.</li>
 * <li>Use {@link Picker} to recommend libraries (as {@link Bindex} entries).
 * Recommendations are logged to the Maven output.</li>
 * <li>Run {@link ApplicationAssembly} to apply changes to the project
 * directory.</li>
 * </ol>
 */
@Mojo(name = "assembly", requiresProject = false, requiresDependencyCollection = ResolutionScope.NONE)
public class Assembly extends AbstractMojo {

	/**
	 * Interactive prompt provider used to collect the assembly prompt when
	 * {@link #assemblyPromptFile} does not exist.
	 */
	@Component
	protected Prompter prompter;

	/**
	 * GenAI provider identifier used for the assembly workflow.
	 *
	 * <p>
	 * The value is resolved by the MachAI provider manager (for example,
	 * {@code OpenAI:gpt-5}).
	 * </p>
	 */
	@Parameter(property = ApplicationAssembly.MODEL_PROP_NAME, defaultValue = ApplicationAssembly.DEFAULT_MODEL, required = true)
	protected String assemblyModel;

	/**
	 * GenAI provider identifier used for the library recommendation (picker)
	 * workflow.
	 *
	 * <p>
	 * This provider can be different from {@link #assemblyModel} to reduce cost or
	 * latency during recommendation.
	 * </p>
	 */
	@Parameter(property = Picker.MODEL_PROP_NAME, defaultValue = Picker.DEFAULT_MODEL, required = true)
	protected String pickModel;

	/**
	 * Prompt file for the assembly workflow.
	 *
	 * <p>
	 * If the file exists, it is read as text and used as the prompt; otherwise the
	 * prompt is requested interactively.
	 * </p>
	 */
	@Parameter(property = "assembly.prompt.file", defaultValue = "project.txt")
	protected File assemblyPromptFile;

	/**
	 * Minimum score threshold for recommended libraries.
	 */
	@Parameter(property = "assembly.score")
	protected Double score = ApplicationAssembly.DEFAULT_SCORE_VALUE;

	/**
	 * Optional registration URL used by the picker for metadata
	 * lookups/registration.
	 */
	@Parameter(property = "bindex.register.url")
	protected String registerUrl;

	/**
	 * Maven execution base directory where changes are applied.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * Factory method for creating a {@link Picker}.
	 *
	 * <p>
	 * This method exists to make it easier to override picker creation in tests.
	 * </p>
	 *
	 * @param config configuration source passed to the picker
	 * @return a new picker instance
	 */
	protected Picker createPicker(Configurator config) {
		return new Picker(pickModel, registerUrl, config);
	}

	/**
	 * Factory method for creating an {@link ApplicationAssembly}.
	 *
	 * <p>
	 * This method exists to make it easier to override assembly creation in tests.
	 * </p>
	 *
	 * @param config configuration source passed to the assembly workflow
	 * @return a new assembly instance
	 */
	protected ApplicationAssembly createAssembly(Configurator config) {
		return new ApplicationAssembly(assemblyModel, config, basedir);
	}

	/**
	 * Executes the {@code assembly} goal.
	 *
	 * <p>
	 * Execution steps:
	 * </p>
	 * <ol>
	 * <li>Read the prompt from {@link #assemblyPromptFile} if it exists; otherwise
	 * prompt the user.</li>
	 * <li>Create a {@link Configurator} backed by {@code bindex.properties}.</li>
	 * <li>Run {@link Picker} using {@link #pickModel} and log any recommended
	 * {@link Bindex} entries.</li>
	 * <li>Run {@link ApplicationAssembly} using {@link #assemblyModel} to apply
	 * changes to {@link #basedir}.</li>
	 * </ol>
	 *
	 * @throws MojoExecutionException if prompt acquisition fails, provider
	 *                                interaction fails, or the assembly workflow
	 *                                fails
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
				query = prompter.prompt("Project assembly prompt or specify the file name");
			}

			Configurator config = new PropertiesConfigurator("bindex.properties");

			Picker picker = createPicker(config);
			picker.setScore(score);
			List<Bindex> bindexList = picker.pick(query);

			if (bindexList.isEmpty()) {
				getLog().info("No libraries were recommended by the picker.");
				return;
			}

			int i = 1;
			getLog().info("Recommended libraries:");
			for (Bindex bindex : bindexList) {
				String bindexId = bindex.getId();
				Double bindexScore = picker.getScore(bindexId);
				String scoreStr = bindexScore != null ? bindexScore.toString() : "";
				getLog().info(String.format("%2$3s. %1$s %3$s", bindexId, i++, scoreStr));
			}

			ApplicationAssembly assembly = createAssembly(config);

			getLog().info("The project directory: " + basedir);
			assembly.projectDir(basedir);
			boolean inputsLog = config.getBoolean(GenAIProvider.LOG_INPUTS_PROP_NAME, false);
			assembly.setLogInputs(inputsLog);
			assembly.assembly(query, bindexList);

		} catch (IOException | PrompterException e) {
			throw new MojoExecutionException("The project assembly process failed.", e);
		}
	}
}
