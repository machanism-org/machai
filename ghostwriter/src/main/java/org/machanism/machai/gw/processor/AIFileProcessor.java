package org.machanism.machai.gw.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.EndTaskException;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.gw.tools.ProjectContextFunctionTools;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * File processor implementation that prepares project context and prompts for a
 * configured AI provider and optionally supports interactive execution.
 */
public class AIFileProcessor extends AbstractFileProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AIFileProcessor.class);

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	public static final String FILE_INCLUDED_MARKER = ">>>";

	public static final String LOG_OUTPUT_PREFIX = FILE_INCLUDED_MARKER + " {}";

	public static final String EXIT_SPECIAL_PROMPT_COMMAND = ".";

	public static final String CONTINUE_SPECIAL_PROMPT_COMMAND = ">";

	public static final String NOT_DEFINED_VALUE = "<NOT_DEFINED_VALUE>";

	public static final String GW_TEMP_DIR = "docs-inputs";

	private String model;

	private boolean logInputs;

	private String instructions = "You are a highly skilled software engineer and developer, with expertise in all major programming languages, frameworks, and platforms.";

	private String defaultPrompt;

	private boolean interactive;

	private List<FunctionTools> toolFunctions = new ArrayList<>();

	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	/**
	 * Creates a processor for the given project directory and AI provider
	 * identifier.
	 * 
	 * @param rootDir   the project root directory
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
		return process(projectLayout, file, getInstructions(), prompt);
	}

	protected String process(ProjectLayout projectLayout, File file, String instructions, String... prompts) {
		logger.info("Processing path: `{}`", file);
		setProjectLayoutContext(projectLayout);

		String perform = null;
		if (StringUtils.isNoneBlank(prompts)) {
			try {
				Genai provider = GenaiProviderManager.getProvider(getModel(), getConfigurator());
				if (provider == null) {
					throw new IllegalArgumentException("`gw.model` is required.");
				}

				functionToolsLoader.applyTools(provider, getClass());
				toolFunctions.forEach(ft -> provider.addTools(ft));

				File projectDir = projectLayout.getProjectDir();
				provider.setProjectDir(projectDir);

				StringBuilder instructionsBuilder = new StringBuilder(promptBundle.getString("sys_instructions"));
				if (StringUtils.isNotBlank(instructions)) {
					instructionsBuilder.append(AbstractAIProvider.PARAGRAPH_SEPARATOR);
					instructionsBuilder.append(instructions);
				}

				provider.instructions(instructionsBuilder.toString());

				String processVars = getProcessInfo(projectLayout, file);
				provider.prompt(processVars);

				for (String prompt : prompts) {
					String promptLines = parseLines(prompt);
					provider.prompt(promptLines);
				}

				perform = perform(file, provider);

			} catch (EndTaskException e) {
				perform = e.getMessage();

			} finally {
				logger.info("Finished processing path: {}", file.getAbsolutePath());

			}
		} else {
			logger.info("Received an empty prompt. Skipping processing.");
		}
		return perform;
	}

	public String getProcessInfo(ProjectLayout projectLayout, File file) {
		Map<String, Map<String, String>> result = new HashMap<>();

		Map<String, String> map = new HashMap<>();
		File projectDir = projectLayout.getProjectDir();
		map.put("PROCESSED_FILE_REL_PATH", ProjectLayout.getRelativePath(projectDir, file));
		map.put("PROCESS_MODE", interactive ? "INTERACTIVE" : "NOT-INTERACTIVE");
		result.put("PROCESS_INFO", map);

		String jsonString;
		try {
			jsonString = new ObjectMapper().writeValueAsString(result);
		} catch (Exception e) {
			jsonString = result.toString();
		}
		return jsonString;
	}

	private String perform(File file, Genai provider) {
		if (isLogInputs()) {
			String inputsFileName = ProjectLayout.getRelativePath(getRootDir(), file);
			File docsTempDir = new File(getRootDir(), MACHAI_TEMP_DIR + File.separator + GW_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			File parentDir = inputsFile.getParentFile();
			if (parentDir != null) {
				try {
					Files.createDirectories(parentDir.toPath());
				} catch (Exception e) {
					throw new IllegalStateException("Failed to create inputs log directory: " + parentDir, e);
				}
			}
			provider.inputsLog(inputsFile);
		}

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
	 * Generates a JSON-formatted string describing the structure and configuration
	 * of a project layout.
	 * <p>
	 * This method collects various metadata and directory information from the
	 * provided {@link ProjectLayout} and the specified file, and organizes them
	 * into a JSON object. The resulting JSON includes the following properties:
	 * <ul>
	 * <li><b>OPERATING_SYSTEM</b>: The name of the operating system.</li>
	 * <li><b>PROJECT_NAME</b>: The name of the project.</li>
	 * <li><b>PROJECT_ID</b>: The unique identifier of the project.</li>
	 * <li><b>PROJECT_DIR_NAME</b>: The name of the project's root directory.</li>
	 * <li><b>PARENT_PROJECT_ID</b>: The unique identifier of the parent project, if
	 * any.</li>
	 * <li><b>PARENT_PROJECT_DIR_NAME</b>: The name of the parent project's
	 * directory, if any.</li>
	 * <li><b>CURRENT_PROJECT_DIR</b>: The current project directory (always set to
	 * ".").</li>
	 * <li><b>REL_PATH_FROM_ROOT</b>: The relative path from the root project
	 * directory to the current project directory.</li>
	 * <li><b>LAYOUT_TYPE</b>: The type of the project layout.</li>
	 * <li><b>SRC_AND_RESOURCE_DIRS</b>: An array of source and resource directory
	 * paths.</li>
	 * <li><b>TEST_SRC_AND_RESOURCE_DIRS</b>: An array of test source and resource
	 * directory paths.</li>
	 * <li><b>DOCS_DIRS</b>: An array of documentation directory paths.</li>
	 * <li><b>MODULES</b>: An array of module directory paths.</li>
	 * <li><b>PROCESSED_FILE_REL_PATH</b>: The relative path of the processed file
	 * with respect to the project directory.</li>
	 * <li><b>PROCESS_MODE</b>: The process mode, either "INTERACTIVE" or
	 * "NOT-INTERACTIVE".</li>
	 * </ul>
	 * Directory-related fields are represented as JSON arrays.
	 * <p>
	 * If pretty-printing the JSON fails, a compact JSON string is returned as a
	 * fallback.
	 *
	 * @param projectLayout the {@link ProjectLayout} instance containing project
	 *                      structure and metadata
	 * @param file          the {@link File} object representing the file being
	 *                      processed within the project
	 * @return a JSON-formatted string containing the project structure description
	 *         and metadata
	 * @throws JsonProcessingException
	 */
	public void setProjectLayoutContext(ProjectLayout projectLayout) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode layoutVars = mapper.createObjectNode();

			File projectDir = projectLayout.getProjectDir();
			String parentId = projectLayout.getParentId();
			File parentDir = projectDir.getParentFile();

			Collection<String> sources = projectLayout.getSources();
			Collection<String> tests = projectLayout.getTests();
			Collection<String> documents = projectLayout.getDocuments();
			Collection<String> modules = projectLayout.getModules();

			layoutVars.put("OPERATING_SYSTEM", SystemUtils.OS_NAME);
			layoutVars.put("PROJECT_NAME", projectLayout.getProjectName());
			layoutVars.put("PROJECT_ID", projectLayout.getProjectId());
			layoutVars.put("PROJECT_DIR_NAME", projectDir.getName());
			layoutVars.put("PARENT_PROJECT_ID", parentId);
			layoutVars.put("PARENT_PROJECT_DIR_NAME", parentDir != null ? parentDir.getName() : null);
			layoutVars.put("REL_PATH_FROM_ROOT", ProjectLayout.getRelativePath(getRootDir(), projectDir));
			layoutVars.put("LAYOUT_TYPE", projectLayout.getProjectLayoutType());

			layoutVars.set("SRC_AND_RESOURCE_DIRS", getDirInfoLine(sources, projectDir));
			layoutVars.set("TEST_SRC_AND_RESOURCE_DIRS", getDirInfoLine(tests, projectDir));
			layoutVars.set("DOCS_DIRS", getDirInfoLine(documents, projectDir));
			layoutVars.set("MODULES", getDirInfoLine(modules, projectDir));

			String jsonString;
			try {
				jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(layoutVars);
			} catch (Exception e) {
				jsonString = layoutVars.toString();
			}

			ProjectContextFunctionTools.put(projectDir, "PROJECT_LAYOUT_CONTEXT", jsonString);

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
	 * Indicates whether provider input logging is enabled.
	 * 
	 * @return {@code true} when input logging is enabled; otherwise {@code false}
	 */
	public boolean isLogInputs() {
		return logInputs;
	}

	/**
	 * Enables or disables logging of provider inputs.
	 * 
	 * @param logInputs {@code true} to enable input logging; otherwise
	 *                  {@code false}
	 */
	public void setLogInputs(boolean logInputs) {
		this.logInputs = logInputs;
	}

	/**
	 * Sets the base instructions used for processing after normalizing line content
	 * and resolving supported references.
	 * 
	 * @param instructions the raw instruction text
	 */
	public void setInstructions(String instructions) {
		this.instructions = parseLines(instructions);
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
	 * @param data the input text to parse
	 * @return the normalized text
	 */
	public String parseLines(String data) {
		if (data == null) {
			return StringUtils.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String content = tryToGetInstructionsFromReference(line);
				if (content != null) {
					sb.append(content);
				}
				sb.append(AbstractAIProvider.LINE_SEPARATOR);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		return sb.toString();
	}

	/**
	 * Resolves a single instruction line that may point to external content.
	 * 
	 * @param data the instruction line to inspect
	 * @return the resolved content, or the original line when no reference is
	 *         found, or {@code null} when the input is {@code null}
	 * @throws java.io.IOException if referenced remote content cannot be read
	 */
	String tryToGetInstructionsFromReference(String data) throws java.io.IOException {
		if (data == null) {
			return null;
		}

		String trimmed = data.trim();
		if (trimmed.startsWith(FILE_INCLUDED_MARKER)) {
			trimmed = StringUtils.substringAfter(trimmed, FILE_INCLUDED_MARKER);
			if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
				return parseLines(readFromHttpUrl(trimmed));
			}

			if (Strings.CS.startsWith(trimmed, "file://")) {
				String filePath = StringUtils.substringAfter(trimmed, "file://");
				filePath = StringSubstitutor.replaceSystemProperties(filePath);
				return parseLines(readFromFilePath(filePath));
			}
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
	 * @param filePath the absolute or project-relative file path
	 * @return the file content as text
	 */
	String readFromFilePath(String filePath) {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(getRootDir(), filePath);
		}

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			String result = IOUtils.toString(reader);
			logger.info("Included file: `{}`", file);
			return result;
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException("Failed to read file: " + file.getAbsolutePath() + ", Error: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Configures scanning based on the provided directory or path pattern and then
	 * starts scanning the project folder.
	 * 
	 * @param projectDir the project root directory
	 * @param path       the directory or path matcher expression to scan
	 * @throws java.io.IOException if scanning fails
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
