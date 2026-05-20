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
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.gw.tools.EndTaskException;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File processor implementation that prepares project context and prompts for a
 * configured AI provider and optionally supports interactive execution.
 */
public class AIFileProcessor extends AbstractFileProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AIFileProcessor.class);

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	public static final String LOG_OUTPUT_PREFIX = ">>> {}";
	
	public static final String FILE_INCLUDED_MARKER = ">>>";
	
	public static final String EXIT_SPECIAL_PROMPT_COMMAND = ".";

	public static final String CONTINUE_SPECIAL_PROMPT_COMMAND = ">";

	public static final String NOT_DEFINED_VALUE = "<NOT_DEFINED_VALUE>";

	private static final String EMPTY_VALUE = "<EMPTY>";

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
	 * @param projectDir   the project root directory
	 * @param configurator the application configuration
	 * @param genai        the AI provider or model identifier
	 */
	public AIFileProcessor(File projectDir, Configurator configurator, String genai) {
		super(projectDir, configurator);
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
		String perform = null;
		if (StringUtils.isNoneBlank(prompts)) {
			try {
				Genai provider = GenaiProviderManager.getProvider(getModel(), getConfigurator());
				if(provider == null) {
					throw new IllegalArgumentException("`gw.model` is required.");
				}
				
				functionToolsLoader.applyTools(provider, getConfigurator());
				toolFunctions.forEach(ft -> ft.applyTools(provider));

				File projectDir = projectLayout.getProjectDir();
				provider.setWorkingDir(projectDir);

				String sysInstructions = promptBundle.getString("sys_instructions");
				String finalInstructions = String.format(sysInstructions, instructions,
						StringUtils.join(ProjectLayout.getExcludeDirs(), ", "));

				provider.instructions(finalInstructions);

				String projectInfo = getProjectStructureDescription(projectLayout, file);
				provider.prompt(projectInfo);

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

	private String perform(File file, Genai provider) {
		if (isLogInputs()) {
			String inputsFileName = ProjectLayout.getRelativePath(getProjectDir(), file);
			File docsTempDir = new File(getProjectDir(), MACHAI_TEMP_DIR + File.separator + GW_TEMP_DIR);
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
	 * Builds the formatted project structure description that is supplied to the AI
	 * provider as contextual information.
	 * 
	 * @param projectLayout the current project layout metadata
	 * @param file          the file currently being processed
	 * @return the formatted project structure description
	 */
	public String getProjectStructureDescription(ProjectLayout projectLayout, File file) {
		List<String> content = new ArrayList<>();

		File projectDir = projectLayout.getProjectDir();
		String parentId = projectLayout.getParentId();
		File parentDir = projectLayout.getProjectDir().getParentFile();

		Collection<String> sources = projectLayout.getSources();
		Collection<String> tests = projectLayout.getTests();
		Collection<String> documents = projectLayout.getDocuments();
		Collection<String> modules = projectLayout.getModules();

		content.add(SystemUtils.OS_NAME);
		content.add(projectLayout.getProjectName() != null ? projectLayout.getProjectName() : NOT_DEFINED_VALUE);
		content.add(projectLayout.getProjectId());
		content.add(projectDir.getName());
		content.add(Objects.toString(parentId, NOT_DEFINED_VALUE));
		content.add(parentDir != null ? parentDir.getName() : NOT_DEFINED_VALUE);

		String relativePath = ProjectLayout.getRelativePath(getProjectDir(), projectDir);
		content.add(relativePath);

		content.add(projectLayout.getProjectLayoutType());
		content.add(getDirInfoLine(sources, projectDir));
		content.add(getDirInfoLine(tests, projectDir));
		content.add(getDirInfoLine(documents, projectDir));
		content.add(getDirInfoLine(modules, projectDir));

		String relativeFile = ProjectLayout.getRelativePath(projectDir, file);
		content.add(relativeFile);

		if (!interactive) {
			content.add(
					"- This is an automated process.\n- Do not include explanations or any additional output.\n");
		} else {
			content.add("- This is an interactive process.\n"
					+ "- If the task is completed successfully, call the `terminate_process` function with exit code = 0.");
		}

		Object[] array = content.toArray(new String[0]);
		String projectInformation = promptBundle.getString("project_information");
		projectInformation = String.format(projectInformation, array);
		return projectInformation + Genai.LINE_SEPARATOR;
	}

	/**
	 * Returns a formatted line describing existing directories from the supplied
	 * collection.
	 * 
	 * @param sources    the directory paths to inspect
	 * @param projectDir the project root used to resolve relative paths
	 * @return a formatted description line for the directories
	 */
	String getDirInfoLine(Collection<String> sources, File projectDir) {
		String line = null;
		if (sources != null) {
			if (!sources.isEmpty()) {
				List<String> dirs = sources.stream().filter(t -> t != null && new File(projectDir, t).exists())
						.map(e -> "`" + e + "`").collect(Collectors.toList());
				line = StringUtils.join(dirs, ", ");
			} else {
				line = EMPTY_VALUE;
			}
		}

		if (StringUtils.isBlank(line)) {
			line = NOT_DEFINED_VALUE;
		}
		return line;
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
				String normalizedLine = StringUtils.stripToNull(line);
				if (normalizedLine == null) {
					sb.append(Genai.LINE_SEPARATOR);
					continue;
				}

				String content = tryToGetInstructionsFromReference(normalizedLine);
				if (content != null) {
					sb.append(content);
				}
				sb.append(Genai.LINE_SEPARATOR);
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

			if (Strings.CS.startsWith(trimmed, "file:")) {
				String filePath = StringUtils.substringAfter(trimmed, "file:");
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
			file = new File(getProjectDir(), filePath);
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
	 * @param scanDir    the directory or path matcher expression to scan
	 * @throws java.io.IOException if scanning fails
	 */
	public void scanDocuments(File projectDir, String scanDir) throws java.io.IOException {
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		if (StringUtils.isBlank(scanDir)) {
			throw new IllegalArgumentException("scanDir must not be blank");
		}

		if (!Strings.CS.equals(projectDir.getAbsolutePath(), scanDir)) {
			if (!isPathPattern(scanDir)) {
				scanDir = parseScanDir(projectDir, scanDir);
			}
			PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(scanDir);
			super.setPathMatcher(pathMatcher);
		} else {
			setScanDir(projectDir);
		}

		scanFolder(projectDir);
	}

	/**
	 * Resolves the effective scan directory and converts it into a glob expression
	 * when required.
	 * 
	 * @param projectDir the base project directory
	 * @param scanDir    the configured scan directory
	 * @return the resolved path matcher expression
	 */
	String parseScanDir(File projectDir, String scanDir) {
		File scanDirFile = new File(scanDir);
		if (!scanDirFile.isAbsolute()) {
			if (".".equals(scanDir)) {
				scanDirFile = getProjectDir();
			} else {
				scanDirFile = new File(getProjectDir(), scanDir);
			}
		}
		String relativePath = ProjectLayout.getRelativePath(projectDir, scanDirFile);
		if (relativePath == null) {
			relativePath = ".";
			scanDirFile = getProjectDir();
		}
		super.setScanDir(scanDirFile);

		if (getDefaultPrompt() == null) {
			scanDir = "glob:" + relativePath + "{,/**}";
		} else {
			scanDir = "glob:" + relativePath;
		}
		return scanDir;
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
			if (perform != null) {
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
