package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds {@link Bindex} documents for a project on disk by prompting a
 * {@link GenAIProvider} with:
 * <ul>
 * <li>the Bindex JSON schema,</li>
 * <li>project-specific context supplied by subclasses, and</li>
 * <li>optional prior (origin) {@code Bindex} content for incremental
 * updates.</li>
 * </ul>
 *
 * <p>
 * Subclasses override {@link #projectContext()} to contribute context by
 * calling {@link GenAIProvider#prompt(String)} and/or
 * {@link GenAIProvider#promptFile(File, String)}.
 *
 * <p>
 * Example:
 * 
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout).genAIProvider(provider).build();
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
	public static String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";

	private static final ResourceBundle PROMPT_BUNDLE = ResourceBundle.getBundle("prompts");

	/**
	 * Optional origin {@link Bindex} from which the builder can request an update.
	 */
	private Bindex origin;

	/** Optional {@link ResourceBundle} used to format prompt templates. */
	private ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	/** Project layout used to locate files and contextual data for generation. */
	private final ProjectLayout projectLayout;

	private GenAIProvider provider;

	/**
	 * Constructs a builder for the specified project layout.
	 *
	 * @param projectLayout describes the target project on disk
	 */
	public BindexBuilder(ProjectLayout projectLayout, String genai, Configurator config) {
		this.projectLayout = projectLayout;

		provider = GenAIProviderManager.getProvider(genai, config);

		String systemPrompt = PROMPT_BUNDLE.getString("bindex_system_instructions");
		provider.instructions(systemPrompt);
	}

	/**
	 * Builds a new {@link Bindex} instance.
	 *
	 * <p>
	 * The default implementation:
	 * <ol>
	 * <li>prompts the schema via {@link #bindexSchemaPrompt(GenAIProvider)},</li>
	 * <li>optionally prompts an update request if {@link #origin(Bindex)} was
	 * provided,</li>
	 * <li>prompts project context via {@link #projectContext()},</li>
	 * <li>issues the generation prompt and calls
	 * {@link GenAIProvider#perform()},</li>
	 * <li>deserializes the provider output into a {@link Bindex}.</li>
	 * </ol>
	 *
	 * @return the generated {@link Bindex}, or {@code null} if the provider returns
	 *         {@code null}
	 * @throws IOException if prompts cannot be issued, inputs cannot be logged, or
	 *                     the output cannot be parsed
	 */
	public Bindex build() throws IOException {

		StringBuilder prompt = new StringBuilder();
		String bindexSchemaPrompt = bindexSchemaPrompt();
		prompt.append(bindexSchemaPrompt + System.lineSeparator());

		if (origin != null) {
			String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(origin);
			String update_bindex_prompt = MessageFormat.format(PROMPT_BUNDLE.getString("update_bindex_prompt"),
					bindexStr);
			prompt.append(update_bindex_prompt + System.lineSeparator());
		}

		prompt.append(projectContext() + System.lineSeparator());
		prompt.append(PROMPT_BUNDLE.getString("bindex_generation_prompt") + System.lineSeparator());

		provider.prompt(prompt.toString());

		File tmpBindexDir = new File(projectLayout.getProjectDir(), BINDEX_TEMP_DIR);
		provider.inputsLog(tmpBindexDir);
		String output = provider.perform();

		if (output == null) {
			return null;
		}

		if (StringUtils.startsWith(output, "```json")) {
			output = StringUtils.substringBetween(output, "```json", "```");
		}
		Bindex value = new ObjectMapper().readValue(output, Bindex.class);
		return value;
	}

	/**
	 * Adds project-specific context to the provider.
	 *
	 * <p>
	 * The base implementation is a no-op.
	 *
	 * @throws IOException if project context cannot be established
	 */
	protected String projectContext() throws IOException {
		return "";
	}

	/**
	 * Prompts the provider with the Bindex JSON schema.
	 *
	 * @param provider provider to receive schema instructions
	 * @return
	 * @throws IOException if the schema cannot be read or prompting fails
	 */
	public static String bindexSchemaPrompt() throws IOException {
		URL systemResource = Bindex.class.getResource(BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, "UTF8");
		String prompt = MessageFormat.format(PROMPT_BUNDLE.getString("bindex_schema_section"), schema);
		return prompt;
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
	 * Adds a prompt from a file.
	 *
	 * <p>
	 * The file content is read as UTF-8 text. If {@code bundleMessageName} is not
	 * {@code null}, the read content is formatted using either the configured
	 * {@link #promptBundle(ResourceBundle)} (when available) or
	 * {@link String#format(String, Object...)}.
	 * </p>
	 *
	 * @param file              the file containing prompt data
	 * @param bundleMessageName key for a message in the {@link ResourceBundle}, or
	 *                          {@code null} to use raw file content
	 * @param provider
	 * @throws IOException if reading the file fails
	 */
	public String promptFile(File file, String bundleMessageName) throws IOException {
		String type = FilenameUtils.getExtension(file.getName());
		try (FileInputStream input = new FileInputStream(file)) {
			String fileData = IOUtils.toString(input, "UTF8");
			String prompt;
			if (bundleMessageName != null) {
				if (promptBundle != null) {
					prompt = MessageFormat.format(promptBundle.getString(bundleMessageName), file.getName(), type,
							fileData);
				} else {
					prompt = String.format(bundleMessageName, file.getName(), type);
				}
			} else {
				prompt = fileData;
			}
			return prompt;
		}
	}

	public GenAIProvider getGenAIProvider() {
		return provider;
	}
}
