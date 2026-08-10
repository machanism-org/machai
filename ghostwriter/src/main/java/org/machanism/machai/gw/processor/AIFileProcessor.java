package org.machanism.machai.gw.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.LayeredConfigurator;
import org.machanism.macha.core.commons.configurator.Substitutor;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.gw.tools.ProjectContextFunctionTools;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/*@guidance:
 * IMPORTANT: Create or update javadoc of AIFileProcessor class.
 * Class javadoc description should describe supported functionality and provide examples to use it.
 * If the method used as Javadoc documentation is not public or protected, the method name should not be specified.
 * Functionality:
 *  - describe supported special markers, see javadoc for following constants:
 *  	- FILE_INCLUDED_MARKER
 *  	- EXIT_SPECIAL_PROMPT_COMMAND
 *  	- CONTINUE_SPECIAL_PROMPT_COMMAND
 *  	- ENABLED_TOOLS_PARAM_NAME
 *  	- PUBLIC_PROP_GROUP_NAME
 *  - describe supported input params, see extractInputParams() method javadoc. 
 *  - Describe the functionality provided by the getProcessInfo() method.
 */
/**
 * File processor that drives a configured {@link Genai} provider with
 * project-aware context, prompt metadata, optional external prompt inclusions,
 * public configuration substitution, and function-tool registration.
 * <p>
 * The processor can handle a single file, a project folder, or a path/pattern
 * scan. Before a prompt is sent to the provider it is normalized, include
 * markers are resolved recursively, public configuration placeholders are
 * substituted, project layout details are stored for project-context tools, and
 * processing metadata is supplied to the provider as JSON. The processing
 * metadata includes the relative path of the file being processed and whether
 * execution is running in interactive or non-interactive mode.
 * </p>
 * <h2>Supported special markers and parameters</h2>
 * <ul>
 * <li>{@link #FILE_INCLUDED_MARKER}: a line prefix used to include UTF-8
 * content from {@code http://}, {@code https://}, or {@code file://}
 * references. Included content is parsed again, so includes may be nested.</li>
 * <li>{@link #EXIT_SPECIAL_PROMPT_COMMAND}: in interactive mode, entering this
 * command exits processing successfully.</li>
 * <li>{@link #CONTINUE_SPECIAL_PROMPT_COMMAND}: in interactive mode, entering
 * this command accepts the current provider response and continues without
 * another provider prompt.</li>
 * <li>{@link #ENABLED_TOOLS_PARAM_NAME}: YAML front-matter property used to
 * limit the provider tools enabled for the current prompt.</li>
 * <li>{@link #PUBLIC_PROP_GROUP_NAME}: configuration-property prefix whose
 * values are exposed for substitution in prompts, for example
 * {@code ${public.projectName}}.</li>
 * </ul>
 * <h2>Supported prompt input parameters</h2>
 * <p>
 * Prompts may start with YAML front matter delimited by {@code ---}. Supported
 * properties include {@code gw.model}, which overrides the configured provider
 * or model for the prompt, and {@code enabledTools}, which may be either a
 * scalar or a YAML list naming tools to enable. String values in the metadata
 * are resolved with the active configurator before use.
 * </p>
 * <h2>Examples</h2>
 * 
 * <pre>{@code
 * AIFileProcessor processor = new AIFileProcessor(rootDir, configurator, "openai:gpt-4.1");
 * processor.setInstructions("Follow the project coding standards.");
 * processor.setDefaultPrompt(">>> file://docs/review-prompt.md\nReview the project.");
 * processor.processFolder(projectLayout);
 * }</pre>
 *
 * <pre>{@code
 * ---
 * gw.model: ${public.reviewModel}
 * enabledTools:
 *   - get_project_context_variable
 *   - read_file
 * ---
 * Analyze ${public.projectName} and use
 * ``` 
 * >>> file://docs/checklist.md
 * ``` 
 * as checklist input.
 * }</pre>
 */
public class AIFileProcessor extends AbstractFileProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AIFileProcessor.class);

	/**
	 * Parameter name used to configure the list of tool definitions exposed to the
	 * LLM agent during an episode.
	 * <p>
	 * This parameter is typically declared as YAML metadata inside an episode's
	 * head block. It accepts a comma-separated list of tool names to selectively
	 * restrict the agent's toolset for that specific stage. If omitted or left
	 * empty, all registered tools remain available to the agent.
	 * </p>
	 *
	 * <h3>Example Usage</h3>
	 * <ol>
	 * <li><b>Restrict tools inside an Episode YAML head block:</b>
	 * 
	 * <pre>{@code
	 *     ---
	 *     enabledTools: 
	 *     	- get_bindex
	 *     	- pick_libraries
	 *     ---
	 *     # Episode Instructions
	 *     Locate and validate our integration boundaries...
	 *     }</pre>
	 * 
	 * </li>
	 * </ol>
	 */
	public static final String ENABLED_TOOLS_PARAM_NAME = "enabledTools";

	/**
	 * Prefix for property groups whose values are exposed and injectable directly
	 * into prompt templates.
	 * <p>
	 * Any configuration property starting with this prefix (e.g.,
	 * {@code public.schemaUrl}) will be automatically collected and made available
	 * as a fully-prefixed variable placeholder (e.g., {@code ${public.schemaUrl}})
	 * inside LLM prompt files or system instructions.
	 * </p>
	 *
	 * <h3>Example Usage</h3>
	 * <ol>
	 * <li><b>Define the property in a configuration file (e.g.,
	 * {@code mcp.properties}):</b>
	 * 
	 * <pre>{@code
	 *     public.schemaUrl=https://raw.githubusercontent.com/machanism-org/bindex/schema-v2.json
	 *     public.projectName=Bindex Core
	 *     }</pre>
	 * 
	 * </li>
	 * <li><b>Reference the property in an Act prompt template:</b>
	 * 
	 * <pre>{@code
	 *     # Load Bindex Schema
	 *     Validate your JSON against this schema: ${public.schemaUrl}
	 *     Processing context for: ${public.projectName}
	 *     }</pre>
	 * 
	 * </li>
	 * </ol>
	 */
	public static final String[] PUBLIC_PROP_GROUP_NAME = { "public.", "default.public." };

	/**
	 * Prefix marker for prompt lines that include external content. A line
	 * beginning with this marker is treated as a reference. Supported references
	 * are {@code http://...}, {@code https://...}, and {@code file://...}.
	 * <p>
	 * For {@code file://} references, the path is resolved relative to the active
	 * project directory.
	 * </p>
	 * Referenced content is read as UTF-8 and recursively parsed, so included files
	 * may contain additional include markers.
	 * <p>
	 * Example: {@code >>> file://docs/instructions.md} (resolves to
	 * {@code <projectDir>/docs/instructions.md})
	 * </p>
	 */
	public static final String FILE_INCLUDED_MARKER = ">>>";

	/**
	 * The format pattern used to prepend logger output blocks when reporting
	 * responses generated by the AI provider.
	 */
	public static final String LOG_OUTPUT_PREFIX = ">>> {}";

	/**
	 * Interactive-mode command that terminates processing immediately by throwing a
	 * {@link ProcessTerminationException} with exit code {@code 0}.
	 * <p>
	 * Example interactive input: {@code .}
	 * </p>
	 */
	public static final String EXIT_SPECIAL_PROMPT_COMMAND = ".";

	/**
	 * Interactive-mode command that accepts the current response and continues
	 * processing without sending another prompt to the provider.
	 * <p>
	 * Example interactive input: {@code >}
	 * </p>
	 */
	public static final String CONTINUE_SPECIAL_PROMPT_COMMAND = ">";

	/**
	 * The specific AI model identifier or provider label being utilized for
	 * operations.
	 */
	private String model;

	/**
	 * Base instructions that set the persona, tone, and scope for the AI provider
	 * during execution.
	 */
	private String instructions;

	/**
	 * Default fallback prompt used to process directories or projects when no
	 * specific file prompt is given.
	 */
	private String defaultPrompt;

	/**
	 * Flag indicating whether interactive mode is active, allowing continuous
	 * prompt looping and execution.
	 */
	private boolean interactive;

	/**
	 * Collection of user-configured function tools that are registered with and
	 * made available to the AI provider.
	 */
	private List<FunctionTools> toolFunctions = new ArrayList<>();

	/**
	 * Loader utility responsible for discovering, instantiating, and applying
	 * functional tools to the AI provider.
	 */
	private static FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	/**
	 * Creates a processor for the given project directory and AI provider
	 * identifier.
	 * 
	 * @param rootDir      the project root directory
	 * @param configurator the application configuration
	 * @param genai        the AI provider or model identifier
	 */
	public AIFileProcessor(File rootDir, Configurator configurator, String genai) {
		super(rootDir, configurator);
		this.model = genai;
	}

	/**
	 * Processes the given file using the currently configured instructions.
	 * 
	 * @param projectLayout the current project layout metadata
	 * @param file          the file to process
	 * @param prompt        the prompt to send to the AI provider
	 * @return the provider response, or {@code null} when no response is produced
	 */
	public String process(ProjectLayout projectLayout, File file, String prompt) {
		return process(projectLayout, file, getInstructions(), getProcessInfo(projectLayout, file), prompt);
	}

	/*
	 * @guidance: show in the javadoc for this method the list of supported input
	 * properties.
	 */
	/**
	 * Processes a specified file within a project layout by configuring and
	 * invoking a Generative AI (GenAI) provider using a sequence of instructions
	 * and prompts.
	 * 
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 * <li>Establishes the thread context for the provided
	 * {@link ProjectLayout}.</li>
	 * <li>Extracts input parameters from the provided array of prompts.</li>
	 * <li>Resolves the GenAI model configuration (falling back to the default
	 * configured model if not explicitly overridden in prompt metadata via
	 * {@code gw.model}).</li>
	 * <li>Instantiates the target {@code Genai} provider and registers enabled
	 * toolkits and custom function tools.</li>
	 * <li>Constructs system instructions by combining default bundle instructions
	 * with any custom parameters passed to {@code instructions}.</li>
	 * <li>Feeds file-specific contextual metadata and substituted prompts to the AI
	 * provider.</li>
	 * <li>Executes the AI operation and returns the generated content.</li>
	 * </ul>
	 * 
	 * <p>
	 * <b>Supported Input Properties (extracted dynamically from the prompts'
	 * metadata):</b>
	 * </p>
	 * <ul>
	 * <li>{@code gw.model} (String) - Overrides the default model identifier used
	 * to initialize the GenAI provider. If not present, the method falls back to
	 * the default instance model.</li>
	 * <li>{@code enabledTools} (String or List&lt;?&gt;) - Configures which
	 * toolkits or tools should be enabled for the AI provider. Defined via the
	 * constant {@link #ENABLED_TOOLS_PARAM_NAME}.</li>
	 * </ul>
	 * 
	 * @param projectLayout the directory structure and metadata context of the
	 *                      active project
	 * @param file          the target file currently being processed
	 * @param instructions  additional custom system instructions to append to the
	 *                      default system instructions; can be {@code null} or
	 *                      blank
	 * @param prompts       a variable-length list or array of user prompt sequences
	 *                      to be evaluated and sent to the GenAI model
	 * @return the output string containing the model's response if processing was
	 *         successful; {@code null} if prompts were empty or blank
	 * @throws IllegalArgumentException if the resolved GenAI model identifier is
	 *                                  missing or no matching provider can be
	 *                                  initialized
	 */
	protected String process(ProjectLayout projectLayout, File file, String instructions, String... prompts) {
		setProjectLayoutContext(projectLayout);

		String perform = null;
		if (StringUtils.isNoneBlank(prompts)) {
			try {
				Map<String, Object> inputProps = new HashMap<>();
				for (int i = 0; i < prompts.length; i++) {
					prompts[i] = extractInputParams(prompts[i], inputProps);
				}

				String model = (String) inputProps.get("gw.model");
				if (model == null) {
					model = this.model;
				}

				LayeredConfigurator conf = new LayeredConfigurator(getConfigurator());
				inputProps.entrySet().stream().forEach(e -> {
					if (e.getValue() instanceof String)
						conf.set(e.getKey(), (String) e.getValue());
				});

				conf.set(GWConstants.MODEL_PROP_NAME, this.model);

				logger.info("Processing path: `{}`, Model: `{}`", file, model);
				Genai provider = GenaiProviderManager.getProvider(model, conf);

				if (provider == null) {
					throw new IllegalArgumentException("`" + GWConstants.MODEL_PROP_NAME + "` is required.");
				}

				String[] tools = null;
				Object toolsVal = inputProps.get(ENABLED_TOOLS_PARAM_NAME);
				if (toolsVal instanceof String) {
					tools = new String[] { (String) toolsVal };
				} else if (toolsVal instanceof List) {
					tools = ((List<?>) toolsVal).stream()
							.map(item -> item != null ? item.toString() : null)
							.toArray(String[]::new);
				}

				provider.setEnabledTools(tools);

				functionToolsLoader.applyTools(provider, getClass());
				toolFunctions.forEach(ft -> provider.addTools(ft));

				File projectDir = projectLayout.getProjectDir();
				provider.setProjectDir(projectDir);

				instructions = parseLines(instructions, projectDir, conf);
				provider.instructions(instructions);
				if (logger.isDebugEnabled()) {
					logger.debug("Instructions: {}", instructions);
				}

				for (String prompt : prompts) {
					String promptLines = Substitutor.replace(prompt, conf, PUBLIC_PROP_GROUP_NAME);
					promptLines = parseLines(promptLines, projectDir, conf);
					provider.prompt(promptLines);
					if (logger.isDebugEnabled()) {
						logger.debug("Input: {}", promptLines);
					}
				}

				perform = perform(file, provider);

			} finally {
				logger.info("Finished processing path: {}", file.getAbsolutePath());

			}
		} else {
			logger.info("Empty prompt. Skipping processing.");
		}
		return perform;
	}

	/**
	 * Extracts supported YAML front-matter input parameters from the beginning of a
	 * prompt.
	 * <p>
	 * A prompt may start with a YAML block delimited by {@code ---}. The block is
	 * removed from the returned prompt content and each YAML entry is merged into
	 * {@code inputProps}. String values are resolved with the processor
	 * configurator; non-string values, such as YAML lists, are preserved.
	 * </p>
	 * <p>
	 * Supported special input parameters include:
	 * </p>
	 * <ul>
	 * <li>{@code gw.model}: model or provider identifier to use for this
	 * prompt.</li>
	 * <li>{@code enabledTools}: a string or YAML list naming provider tools that
	 * should be enabled.</li>
	 * </ul>
	 * <p>
	 * Example:
	 * </p>
	 *
	 * <pre>{@code
	 * ---
	 * gw.model: ${public.ai.model}
	 * enabledTools:
	 *   - project-context
	 *   - file-system
	 * ---
	 * Analyze this file.
	 * }</pre>
	 *
	 * @param prompt     the input prompt string which may contain a leading YAML
	 *                   configuration block
	 * @param inputProps the map to populate with extracted and resolved parameters
	 * @return the stripped prompt content when a configuration block is present;
	 *         otherwise, the original prompt string
	 */
	private String extractInputParams(String prompt, Map<String, Object> inputProps) {
		if (Strings.CS.startsWith(prompt, "---")) {
			String marker = "---";
			String inputParams = StringUtils.substringBetween(prompt, marker, marker);

			if (inputParams != null) {
				Map<String, Object> parseResult = new Yaml().load(inputParams);
				if (parseResult != null) {
					Set<Entry<String, Object>> entrySet = parseResult.entrySet();
					entrySet.forEach((e) -> inputProps.put(e.getKey(), resolveInputParamValue(e.getValue())));
				}
			}

			prompt = StringUtils.substringAfter(prompt.substring(inputParams.length()), marker);
		}
		return prompt;
	}

	private Object resolveInputParamValue(Object value) {
		if (value instanceof String) {
			return Substitutor.replace((String) value, getConfigurator());
		}
		if (value instanceof List) {
			return ((List<?>) value).stream()
					.map(this::resolveInputParamValue)
					.collect(Collectors.toList());
		}
		return value;
	}

	/**
	 * Generates a structured JSON string containing execution metadata about the
	 * file being processed and the current processing environment context.
	 * 
	 * <p>
	 * This method builds a map structure containing processing details and attempts
	 * to serialize it to a JSON format. The generated map holds the following
	 * properties:
	 * </p>
	 * <ul>
	 * <li>{@code "PROCESSED_FILE_REL_PATH"} - The relative path of the processed
	 * file with respect to the project directory.</li>
	 * <li>{@code "PROCESS_MODE"} - The current interaction mode, returning
	 * {@code "INTERACTIVE"} if the execution is interactive, otherwise
	 * {@code "NOT-INTERACTIVE"}.</li>
	 * </ul>
	 * 
	 * <p>
	 * <strong>Serialization Fallback:</strong> If Jackson's {@link ObjectMapper}
	 * fails to serialize the map to a standard JSON string, the method falls back
	 * to the default string representation of the map (via
	 * {@link Object#toString()}).
	 * </p>
	 *
	 * @param projectLayout the layout configuration of the project, used to resolve
	 *                      the base project directory for path relative-ization;
	 *                      must not be {@code null}
	 * @param file          the file currently undergoing processing, used to
	 *                      determine its relative path; must not be {@code null}
	 * @return a string representation of the processing information map; ideally a
	 *         valid JSON-formatted string, or a stringified map representation if
	 *         serialization fails
	 * @see ProjectLayout#getRelativePath(File, File)
	 * @see com.fasterxml.jackson.databind.ObjectMapper#writeValueAsString(Object)
	 */
	public String getProcessInfo(ProjectLayout projectLayout, File file) {
		Map<String, String> result = new HashMap<>();

		File projectDir = projectLayout.getProjectDir();
		result.put("PROCESSED_FILE_REL_PATH", ProjectLayout.getRelativePath(projectDir, file));
		result.put("PROCESS_MODE", interactive ? "INTERACTIVE" : "NOT-INTERACTIVE");
		result.put("OS_NAME", SystemUtils.OS_NAME);

		String jsonString;
		try {
			jsonString = new ObjectMapper().writeValueAsString(result);
		} catch (Exception e) {
			jsonString = result.toString();
		}
		return jsonString;
	}

	private String perform(File file, Genai provider) {
		String perform = provider.perform();
		if (interactive) {
			if (StringUtils.isNoneBlank(perform)) {
				logger.info(LOG_OUTPUT_PREFIX, perform);
			}
			String input = input();
			if (input != null) {
				switch (input.toLowerCase().trim()) {
				case CONTINUE_SPECIAL_PROMPT_COMMAND:
					perform = null;
					break;

				case EXIT_SPECIAL_PROMPT_COMMAND:
					throw new ProcessTerminationException(0);

				default:
					provider.prompt(input);
					perform = perform(file, provider);
					break;
				}
			}
		}
		return perform;
	}

	protected String input() {
		logger.warn(
				"Interactive mode is not supported in this environment. Please use a supported mode or refer to the documentation for available options.");
		return null;
	}

	/**
	 * Extracts context metadata from the provided {@link ProjectLayout} and
	 * registers it in the project context registry.
	 * <p>
	 * This method evaluates essential environment information (such as the
	 * operating system), project structure configurations (like name, IDs, and
	 * relative paths), and maps directory definitions (such as source files, tests,
	 * documentation, and sub-modules) into a centralized storage registry managed
	 * by {@link ProjectContextFunctionTools}.
	 * </p>
	 * <p>
	 * Directory collections are consolidated into formatted string information
	 * lines relative to the project directory before registration.
	 * </p>
	 *
	 * @param projectLayout the {@link ProjectLayout} containing the current project
	 *                      structure, directories, and parent configurations; must
	 *                      not be {@code null}
	 * @throws IllegalArgumentException if an error occurs during JSON serialization
	 *                                  or parsing of the layout information (wraps
	 *                                  {@link JsonProcessingException})
	 * @see ProjectContextKey
	 * @see ProjectContextFunctionTools
	 */
	private void setProjectLayoutContext(ProjectLayout projectLayout) {

		try {
			File projectDir = projectLayout.getProjectDir();
			String parentId = projectLayout.getParentId();
			File parentDir = projectDir.getParentFile();

			Collection<String> sources = projectLayout.getSources();
			Collection<String> tests = projectLayout.getTests();
			Collection<String> documents = projectLayout.getDocuments();
			Collection<String> modules = projectLayout.getModules();

			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.OPERATING_SYSTEM.getKey(),
					SystemUtils.OS_NAME);
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.PROJECT_NAME.getKey(),
					projectLayout.getProjectName());
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.PROJECT_ID.getKey(),
					projectLayout.getProjectId());
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.PROJECT_DIR_NAME.getKey(),
					projectDir.getName());
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.PARENT_PROJECT_ID.getKey(), parentId);
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.PARENT_PROJECT_DIR_NAME.getKey(),
					parentDir != null ? parentDir.getName() : null);
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.REL_PATH_FROM_ROOT.getKey(),
					ProjectLayout.getRelativePath(getRootDir(), projectDir));
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.LAYOUT_TYPE.getKey(),
					projectLayout.getProjectLayoutType());

			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.SRC_AND_RESOURCE_DIRS.getKey(),
					getDirInfoLine(sources, projectDir));
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.TEST_SRC_AND_RESOURCE_DIRS.getKey(),
					getDirInfoLine(tests, projectDir));
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.DOCS_DIRS.getKey(),
					getDirInfoLine(documents, projectDir));
			ProjectContextFunctionTools.put(projectDir, ProjectContextKey.MODULES.getKey(),
					getDirInfoLine(modules, projectDir));

		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns a JsonNode (ArrayNode) containing the names of directories from the
	 * given collection that exist within the specified project directory. Each
	 * directory name is wrapped in backticks.
	 *
	 * @param sources    a collection of directory names (relative to projectDir) to
	 *                   check for existence
	 * @param projectDir the base directory in which to check for the existence of
	 *                   each source directory
	 * @return a JsonNode (ArrayNode) of existing directory names, each wrapped in
	 *         backticks (e.g., ["`src`", "`resources`"])
	 */
	ArrayNode getDirInfoLine(Collection<String> sources, File projectDir) {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode dirs = mapper.createArrayNode();
		if (sources != null) {
			sources.stream()
					.filter(t -> t != null && new File(projectDir, t).exists())
					.forEach(e -> dirs.add(e));
		}

		return dirs.size() == 0 ? null : dirs;
	}

	/**
	 * Sets the base instructions used for processing after normalizing line content
	 * and resolving supported references.
	 * 
	 * @param instructions the raw instruction text
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Returns the current base instructions used for processing.
	 * 
	 * @return the configured instruction text
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * Normalizes multi-line input and resolves supported line references such as
	 * HTTP URLs and {@code file:} references.
	 * 
	 * @param data       the input text to parse
	 * @param projectDir
	 * @param conf
	 * @return the normalized text
	 */
	public String parseLines(String data, File projectDir, Configurator conf) {
		if (data == null) {
			return StringUtils.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (sb.length() > 0) {
					sb.append(AbstractAIProvider.LINE_SEPARATOR);
				}
				String content = tryToGetFromReference(line, projectDir, conf);

				if (content != null) {
					sb.append(content);
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		return sb.toString();
	}

	/**
	 * Resolves a single instruction line that may point to external content.
	 * <p>
	 * If the instruction contains an external reference marker, it will be fetched
	 * and resolved. URLs starting with {@code http://} or {@code https://} are
	 * fetched remotely. URIs starting with {@code file://} are resolved and loaded
	 * relative to the provided project directory.
	 * </p>
	 * 
	 * @param data       the instruction line to inspect
	 * @param projectDir the root directory of the project, used as the base context
	 *                   to resolve relative {@code file://} references
	 * @param conf
	 * @return the resolved content, or the original line when no reference is
	 *         found, or {@code null} when the input is {@code null}
	 * @throws java.io.IOException if the referenced remote content or local file
	 *                             cannot be read
	 */
	String tryToGetFromReference(String data, File projectDir, Configurator conf) throws java.io.IOException {
		if (data != null) {
			String trimmed = data.trim();
			if (trimmed.startsWith(FILE_INCLUDED_MARKER)) {
				trimmed = StringUtils.substringAfter(trimmed, FILE_INCLUDED_MARKER).trim();
				if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
					data = parseLines(readFromHttpUrl(trimmed), projectDir, conf);
				}

				if (Strings.CS.startsWith(trimmed, "file://")) {
					String filePath = StringUtils.substringAfter(trimmed, "file://");
					data = parseLines(readFromFilePath(filePath, projectDir), projectDir, conf);
				}
			}

			data = Substitutor.replace(data, conf, PUBLIC_PROP_GROUP_NAME);
		}

		return data;
	}

	/**
	 * Reads UTF-8 text content from the given HTTP or HTTPS URL.
	 * 
	 * @param urlString the URL to read
	 * @return the response content as text
	 * @throws java.io.IOException if the URL cannot be read
	 */
	static String readFromHttpUrl(String urlString) throws java.io.IOException {
		URL url = URI.create(urlString).toURL();
		try (InputStream in = url.openStream()) {
			String result = IOUtils.toString(in, StandardCharsets.UTF_8);
			logger.info("Included: `{}`", urlString);
			return result;
		}
	}

	/**
	 * Reads UTF-8 text content from the given file path.
	 * 
	 * @param filePath   the absolute or project-relative file path
	 * @param projectDir
	 * @return the file content as text
	 * @throws IOException
	 */
	String readFromFilePath(String filePath, File projectDir) throws IOException {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(projectDir, filePath);
		}

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			String result = IOUtils.toString(reader);
			logger.info("Included file: `{}`", file);
			return result;
		}
	}

	/**
	 * Configures scanning based on the provided directory or path pattern and then
	 * starts scanning the project folder.
	 * <p>
	 * The {@code path} argument may be specified as:
	 * <ul>
	 * <li><b>An absolute path</b> — used as-is to scan a specific location.</li>
	 * <li><b>A relative path</b> — resolved against {@code projectDir}.</li>
	 * <li><b>A glob pattern</b> — e.g., {@code "glob:**&#47;*.java"}, matched
	 * against files under {@code projectDir}.</li>
	 * <li><b>A regex pattern</b> — e.g., {@code "regex:.*\\.java"}, matched against
	 * files under {@code projectDir}.</li>
	 * </ul>
	 * If {@code path} equals the absolute path of {@code projectDir}, the entire
	 * project directory is scanned without applying any pattern matching.
	 *
	 * @param projectDir the project root directory; must not be {@code null}
	 * @param path       the directory, relative path, glob pattern, or regex
	 *                   pattern used to match files to scan; must not be blank
	 * @throws IllegalArgumentException if {@code projectDir} is {@code null} or
	 *                                  {@code path} is blank
	 * @throws java.io.IOException      if scanning fails
	 */
	public void scanDocuments(File projectDir, String path) throws java.io.IOException {
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("path must not be blank");
		}

		if (!Strings.CS.equals(projectDir.getAbsolutePath(), path)) {
			if (!isPathPattern(path)) {
				path = parsePath(projectDir, path);
			}
			PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(path);
			super.setPathMatcher(pathMatcher);
		} else {
			setPath(projectDir);
		}

		scanFolder(projectDir);
	}

	/**
	 * Resolves the effective scan directory and converts it into a glob expression
	 * when required.
	 * 
	 * @param projectDir the base project directory
	 * @param path       the configured scan directory
	 * @return the resolved path matcher expression
	 */
	String parsePath(File projectDir, String path) {
		File pathFile = new File(path);
		if (!pathFile.isAbsolute()) {
			if (".".equals(path)) {
				pathFile = getRootDir();
			} else {
				pathFile = new File(getRootDir(), path);
			}
		}
		String relativePath = ProjectLayout.getRelativePath(projectDir, pathFile);
		if (relativePath == null) {
			relativePath = ".";
			pathFile = getRootDir();
		}
		super.setPath(pathFile);

		if (getDefaultPrompt() == null) {
			path = "glob:" + relativePath + "{,/**}";
		} else {
			path = "glob:" + relativePath;
		}
		return path;
	}

	/**
	 * Returns the default prompt used for folder processing.
	 * 
	 * @return the default prompt
	 */
	public String getDefaultPrompt() {
		return defaultPrompt;
	}

	/**
	 * Sets the default prompt used for folder processing.
	 * 
	 * @param defaultPrompt the default prompt text
	 */
	public void setDefaultPrompt(String defaultPrompt) {
		this.defaultPrompt = defaultPrompt;
	}

	/**
	 * Processes the project root folder using the configured default prompt.
	 * 
	 * @param projectLayout the current project layout metadata
	 */
	@Override
	public void processFolder(ProjectLayout projectLayout) {
		try {
			String perform = process(projectLayout, projectLayout.getProjectDir(), getDefaultPrompt());
			if (StringUtils.isNoneBlank(perform)) {
				logger.info(LOG_OUTPUT_PREFIX, perform);
			}

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns the configured AI model or provider identifier.
	 * 
	 * @return the model or provider identifier
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Sets the AI model or provider identifier.
	 * 
	 * @param genai the model or provider identifier
	 */
	public void setModel(String genai) {
		this.model = genai;
	}

	/**
	 * Enables or disables interactive processing mode.
	 * 
	 * @param interactive {@code true} to enable interactive mode; otherwise
	 *                    {@code false}
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * Indicates whether interactive processing mode is enabled.
	 * 
	 * @return {@code true} when interactive mode is enabled; otherwise
	 *         {@code false}
	 */
	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * Adds a tool definition that will be exposed to the AI provider.
	 * 
	 * @param toolFunction the tool definition to add
	 */
	public void addTool(FunctionTools toolFunction) {
		logger.debug("FunctionTools: {}", toolFunction.getClass().getName());
		toolFunctions.add(toolFunction);
	}
}
