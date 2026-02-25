package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assembles prompt inputs for downstream LLM-assisted workflows using a set of
 * selected {@link Bindex} documents.
 *
 * <p>
 * This type is responsible for constructing a prompt conversation in a
 * deterministic order: system instructions, assembly instructions, the Bindex
 * JSON schema, a sequence of recommended library sections (one per Bindex), and
 * finally the user prompt.
 *
 * <h2>Example</h2>
 *
 * <pre>
 * GenAIProvider provider = ...;
 * List&lt;Bindex&gt; bindexes = ...;
 *
 * new ApplicationAssembly(provider)
 *     .projectDir(new File("C:\\work\\my-project"))
 *     .assembly("Create a minimal sample that uses the selected libraries", bindexes);
 * </pre>
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
	 * Relative path (from {@link #projectDir}) where the provider input log is
	 * written.
	 */
	private static final String ASSEMBLY_TEMP_DIR = ".machai/assembly-inputs.txt";

	private final GenAIProvider provider;
	private File projectDir = SystemUtils.getUserDir();

	/**
	 * Constructs an instance that uses the provided {@link GenAIProvider} to
	 * execute the assembled prompt.
	 *
	 * @param provider provider used to send instructions/prompts and execute the
	 *                 request
	 */
	public ApplicationAssembly(GenAIProvider provider) {
		this.provider = provider;
	}

	/**
	 * Builds and executes an assembly prompt using the supplied user prompt and a
	 * list of relevant {@link Bindex} documents.
	 *
	 * <p>
	 * The provider input log is written to {@code .machai/assembly-inputs.txt}
	 * under the current {@linkplain #projectDir(File) project directory}.
	 *
	 * @param prompt     user prompt describing the desired assembly/result
	 * @param bindexList list of Bindex documents to include as context
	 * @throws IllegalArgumentException if any serialization or I/O error occurs
	 */
	public void assembly(final String prompt, List<Bindex> bindexList) {
		String systemPrompt = PROMPT_BUNDLE.getString("assembly_system_instructions");
		provider.instructions(systemPrompt);

		String assemblyInstructions = PROMPT_BUNDLE.getString("assembly_instructions");
		provider.prompt(assemblyInstructions);

		try {
			provider.prompt(BindexBuilder.bindexSchemaPrompt());

			ObjectMapper objectMapper = new ObjectMapper();
			for (Bindex bindex : bindexList) {
				String bindexStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bindex);
				String bindexPrompt = MessageFormat.format(PROMPT_BUNDLE.getString("recommended_library_section"),
						bindex.getId(), bindexStr);
				provider.prompt(bindexPrompt);
			}

			String userPrompt = MessageFormat.format(PROMPT_BUNDLE.getString("user_prompt"), prompt);
			provider.prompt(userPrompt);

			File bindexTempDir = new File(projectDir, ASSEMBLY_TEMP_DIR);
			provider.inputsLog(bindexTempDir);
			String response = provider.perform();
			if (StringUtils.isNotBlank(response)) {
				LOGGER.info(response);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Sets the project directory used when writing local artifacts (such as
	 * provider input logs).
	 *
	 * @param projectDir project directory to use
	 * @return this instance for chaining
	 */
	public ApplicationAssembly projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

}
