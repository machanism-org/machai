package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds {@link Bindex} documents for a project on disk by prompting a
 * {@link Genai} with:
 * <ul>
 * <li>the Bindex JSON schema,</li>
 * <li>project-specific context supplied by subclasses, and</li>
 * <li>optional prior (origin) {@code Bindex} content for incremental
 * updates.</li>
 * </ul>
 *
 * <p>
 * Subclasses override {@link #projectContext()} to contribute context.
 *
 * <p>
 * Example:
 *
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout, "openai", config).build();
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see ProjectLayout
 */
public class BindexBuilder {

	/** Relative path under the project directory where input logs are written. */
	public static final String BINDEX_TEMP_DIR = ".machai/bindex-inputs.txt";

	/** Classpath resource path to the Bindex JSON schema. */
	public static final String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";

	private static final ResourceBundle PROMPT_BUNDLE = ResourceBundle.getBundle("prompts");

	/**
	 * Optional origin {@link Bindex} from which the builder can request an update.
	 */
	private Bindex origin;

	/** Project layout used to locate files and contextual data for generation. */
	private final ProjectLayout projectLayout;

	/** Provider used to build the prompt and perform generation. */
	private final Genai provider;

	private boolean logInputs;

	/**
	 * Constructs a builder for the specified project layout.
	 *
	 * @param projectLayout describes the target project on disk
	 * @param genai         provider identifier used by
	 *                      {@link GenaiProviderManager#getProvider(String, Configurator)}
	 * @param config        configuration used to initialize the provider
	 */
	public BindexBuilder(ProjectLayout projectLayout, String genai, Configurator config) {
		this.projectLayout = projectLayout;
		this.provider = GenaiProviderManager.getProvider(genai, config);
		FunctionToolsLoader.getInstance().applyTools(provider);

		String systemPrompt = PROMPT_BUNDLE.getString("bindex_system_instructions");
		provider.instructions(systemPrompt);
	}

	/**
	 * Builds a new {@link Bindex} instance.
	 *
	 * <p>
	 * The default implementation:
	 * <ol>
	 * <li>optionally adds an update request if {@link #origin(Bindex)} was
	 * provided,</li>
	 * <li>adds project context via {@link #projectContext()},</li>
	 * <li>issues the generation prompt and calls
	 * {@link Genai#perform()},</li>
	 * <li>deserializes the provider output into a {@link Bindex}.</li>
	 * </ol>
	 *
	 * @return the generated {@link Bindex}, or {@code null} if the provider returns
	 *         {@code null}
	 * @throws IOException if inputs cannot be logged, or the output cannot be
	 *                     parsed
	 */
	public Bindex build() throws IOException {

		StringBuilder prompt = new StringBuilder();

		if (origin != null) {
			String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(origin);
			String updateBindexPrompt = MessageFormat.format(PROMPT_BUNDLE.getString("update_bindex_prompt"),
					bindexStr);
			prompt.append(updateBindexPrompt).append(Genai.LINE_SEPARATOR);
		}

		prompt.append(projectContext()).append(Genai.LINE_SEPARATOR).append(Genai.LINE_SEPARATOR);
		prompt.append(PROMPT_BUNDLE.getString("bindex_generation_prompt")).append(Genai.LINE_SEPARATOR)
				.append(Genai.LINE_SEPARATOR);

		provider.prompt(prompt.toString());

		if (logInputs) {
			File tmpBindexDir = new File(projectLayout.getProjectDir(), BINDEX_TEMP_DIR);
			provider.inputsLog(tmpBindexDir);
		}

		String output = provider.perform();

		if (output == null) {
			return null;
		}

		String normalizedOutput = output;
		if (Strings.CS.startsWith(normalizedOutput, "```json")) {
			normalizedOutput = StringUtils.substringBetween(normalizedOutput, "```json", "```");
		}

		// Sonar java:S1488 - avoid unnecessary temporary variable.
		return new ObjectMapper().readValue(normalizedOutput, Bindex.class);
	}

	/**
	 * Builds and returns project-specific prompt context.
	 *
	 * <p>
	 * Subclasses typically:
	 * <ul>
	 * <li>read project manifest files,</li>
	 * <li>walk selected source/resource trees, and</li>
	 * <li>return a concatenated prompt string.</li>
	 * </ul>
	 *
	 * @return project context prompt content (never {@code null})
	 * @throws IOException if project context cannot be established
	 */
	protected String projectContext() throws IOException {
		return "";
	}

	/**
	 * Sets an origin {@link Bindex} for incremental update operations.
	 *
	 * @param bindex origin instance used to request an update
	 * @return this builder for fluent chaining
	 */
	public BindexBuilder origin(Bindex bindex) {
		this.origin = bindex;
		return this;
	}

	/**
	 * Returns the origin {@link Bindex} instance.
	 *
	 * @return the currently configured origin, or {@code null}
	 */
	public Bindex getOrigin() {
		return origin;
	}

	/**
	 * Returns the project layout used for analysis.
	 *
	 * @return project layout
	 */
	public ProjectLayout getProjectLayout() {
		return projectLayout;
	}

	/**
	 * Builds a prompt string from the provided file.
	 *
	 * <p>
	 * The file content is read as UTF-8 text. If {@code bundleMessageName} is not
	 * {@code null}, the read content is formatted using the corresponding template
	 * from {@link ResourceBundle} {@code prompts}.
	 *
	 * @param file              the file containing prompt data
	 * @param bundleMessageName key for a message in the {@link ResourceBundle}, or
	 *                          {@code null} to use raw file content
	 * @return the prompt string that should be appended to the overall prompt
	 * @throws IOException if reading the file fails
	 */
	public String promptFile(File file, String bundleMessageName) throws IOException {
		String type = FilenameUtils.getExtension(file.getName());
		try (FileInputStream input = new FileInputStream(file)) {
			String fileData = IOUtils.toString(input, StandardCharsets.UTF_8);
			if (bundleMessageName == null) {
				return fileData;
			}

			return MessageFormat.format(PROMPT_BUNDLE.getString(bundleMessageName), file.getName(), type, fileData);
		}
	}

	/**
	 * Returns the configured provider.
	 *
	 * @return provider instance
	 */
	public Genai getGenAIProvider() {
		return provider;
	}

	public boolean isLogInputs() {
		return logInputs;
	}

	public BindexBuilder logInputs(boolean logInputs) {
		this.logInputs = logInputs;
		return this;
	}
}
