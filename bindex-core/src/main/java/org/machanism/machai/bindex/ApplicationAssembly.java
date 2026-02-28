package org.machanism.machai.bindex;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembles prompt inputs for downstream LLM-assisted workflows using a set of selected
 * {@link Bindex} documents.
 *
 * <p>The assembled conversation is sent to a {@link GenAIProvider} in a deterministic order:
 *
 * <ol>
 *   <li>System instructions</li>
 *   <li>Assembly instructions</li>
 *   <li>User prompt</li>
 *   <li>Recommended library section derived from the selected Bindexes</li>
 * </ol>
 *
 * <p>The provider inputs can optionally be logged to {@code .machai/assembly-inputs.txt} under
 * the configured project directory.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Configurator config = ...;
 * File projectDir = new File("C:\\work\\my-project");
 * List<Bindex> bindexes = ...;
 *
 * new ApplicationAssembly("openai", config, projectDir)
 *     .assembly("Create a minimal sample that uses the selected libraries", bindexes);
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class ApplicationAssembly {

	/** Logger instance for the class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAssembly.class);
	/** ResourceBundle for prompt configuration. */
	private static final ResourceBundle PROMPT_BUNDLE = ResourceBundle.getBundle("prompts");

	/**
	 * Relative path (from {@link #projectDir}) where the provider input log is written.
	 */
	private static final String ASSEMBLY_TEMP_DIR = ".machai/assembly-inputs.txt";

	private final GenAIProvider provider;
	private File projectDir = SystemUtils.getUserDir();

	/**
	 * Creates an instance that uses the configured GenAI provider.
	 *
	 * <p>The provider is configured with available function tools and a working directory.
	 *
	 * @param genai  provider identifier understood by {@link GenAIProviderManager}
	 * @param config configurator used to initialize the provider and tools
	 * @param dir    working directory for the provider
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public ApplicationAssembly(String genai, Configurator config, File dir) {
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
		if (dir == null) {
			throw new IllegalArgumentException("dir must not be null");
		}

		this.provider = GenAIProviderManager.getProvider(genai, config);
		FunctionToolsLoader.getInstance().applyTools(provider);
		provider.setWorkingDir(dir);
		FunctionToolsLoader.getInstance().setConfiguration(config);
	}

	/**
	 * Builds and executes an assembly prompt using the supplied user prompt and a list of relevant
	 * {@link Bindex} documents.
	 *
	 * <p>The provider input log is written to {@code .machai/assembly-inputs.txt} under the current
	 * {@linkplain #projectDir(File) project directory}.
	 *
	 * @param prompt     user prompt describing the desired assembly/result
	 * @param bindexList list of Bindex documents to include as context
	 * @throws IllegalArgumentException if {@code prompt} or {@code bindexList} is {@code null}
	 */
	public void assembly(final String prompt, List<Bindex> bindexList) {
		if (prompt == null) {
			throw new IllegalArgumentException("prompt must not be null");
		}
		if (bindexList == null) {
			throw new IllegalArgumentException("bindexList must not be null");
		}

		String systemPrompt = PROMPT_BUNDLE.getString("assembly_system_instructions");
		provider.instructions(systemPrompt);

		StringBuilder bindexPrompt = new StringBuilder();

		String assemblyInstructions = PROMPT_BUNDLE.getString("assembly_instructions");
		bindexPrompt.append(assemblyInstructions);

		String userPrompt = MessageFormat.format(PROMPT_BUNDLE.getString("user_prompt"), prompt);
		bindexPrompt.append(userPrompt).append("\r\n");

		StringBuilder picked = new StringBuilder();
		for (Bindex bindex : bindexList) {
			if (bindex == null) {
				continue;
			}
			picked.append("- `").append(bindex.getId()).append("`: `").append(bindex.getDescription())
					.append("`\r\n");
		}
		bindexPrompt.append("\r\n");

		String promptStr = MessageFormat.format(PROMPT_BUNDLE.getString("recommended_library_section"),
				picked.toString());
		bindexPrompt.append(promptStr).append("\r\n");

		provider.prompt(bindexPrompt.toString());

		File bindexTempDir = new File(projectDir, ASSEMBLY_TEMP_DIR);
		provider.inputsLog(bindexTempDir);
		provider.perform();
	}

	/**
	 * Sets the project directory used when writing local artifacts (such as provider input logs).
	 *
	 * @param projectDir project directory to use
	 * @return this instance for chaining
	 * @throws IllegalArgumentException if {@code projectDir} is {@code null}
	 */
	public ApplicationAssembly projectDir(File projectDir) {
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		this.projectDir = projectDir;
		return this;
	}

}
