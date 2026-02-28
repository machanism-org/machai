package org.machanism.machai.gw.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for processors that build prompts and execute a configured
 * {@link GenAIProvider} against project files.
 *
 * <p>
 * This type extends {@link AbstractFileProcessor} with the mechanics required
 * to invoke a GenAI provider:
 * </p>
 * <ul>
 * <li>create a provider using {@link GenAIProviderManager},</li>
 * <li>apply registered function tools via {@link FunctionToolsLoader},</li>
 * <li>optionally log the composed provider inputs for auditing/debugging,
 * and</li>
 * <li>provide helper methods for prompt templates (e.g., project-structure
 * description).</li>
 * </ul>
 *
 * <p>
 * Subclasses typically decide what files to process and how guidance is
 * derived; this class focuses on orchestration and provider execution.
 * </p>
 */
public class AIFileProcessor extends AbstractFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(AIFileProcessor.class);

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * String used in generated output when a value is absent in project metadata.
	 */
	public static final String NOT_DEFINED = "not defined";

	/**
	 * Temporary directory name for documentation inputs under
	 * {@link #MACHAI_TEMP_DIR}.
	 */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Provider key/name (including model) used when creating GenAI providers. */
	private final String genai;

	/** Whether to persist the composed inputs to a per-file log. */
	private boolean logInputs;

	/**
	 * Optional additional instructions appended to each prompt sent to the GenAI
	 * provider.
	 */
	private String instructions = "You are a highly skilled software engineer and developer, with expertise in all major programming languages, frameworks, and platforms.";

	/**
	 * Default guidance applied when a file does not contain embedded
	 * {@code @guidance} directives.
	 */
	private String defaultGuidance;

	/**
	 * Creates a new processor using the given provider key.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param configurator configuration source
	 * @param genai        provider key/name (including model)
	 */
	public AIFileProcessor(File rootDir, Configurator configurator, String genai) {
		super(rootDir, configurator);
		this.genai = genai;
	}

	/**
	 * Creates a provider and performs a full prompt run for the given file.
	 *
	 * @param projectLayout project layout
	 * @param file          file being processed (used for logging and templating)
	 * @param instructions  system or execution instructions for the provider
	 * @param guidance      guidance content to include in the prompt
	 * @return provider output
	 * @throws IOException if creating input logs fails or provider I/O fails
	 */
	public String process(ProjectLayout projectLayout, File file, String instructions, String guidance)
			throws IOException {
		logger.info("Processing file: '{}'", file);

		GenAIProvider provider = GenAIProviderManager.getProvider(genai, getConfigurator());

		FunctionToolsLoader.getInstance().applyTools(provider);

		File projectDir = projectLayout.getProjectDir();
		provider.setWorkingDir(projectDir);

		provider.instructions(instructions);
		provider.prompt(guidance);

		if (isLogInputs()) {
			String inputsFileName = ProjectLayout.getRelativePath(getRootDir(), file);
			File docsTempDir = new File(getRootDir(), MACHAI_TEMP_DIR + File.separator + GW_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			File parentDir = inputsFile.getParentFile();
			if (parentDir != null) {
				Files.createDirectories(parentDir.toPath());
			}
			provider.inputsLog(inputsFile);
		}

		try {
			String perform = provider.perform();

			logger.info("Finished processing file: {}", file.getAbsolutePath());
			return perform;

		} catch (ProcessTerminationException e) {
			throw e;

		} catch (RuntimeException e) {
			logger.error("File processing failed: " + file, e);
			return null;
		}
	}

	/**
	 * Builds a human-readable description of the project structure used in prompts.
	 *
	 * @param projectLayout current project layout
	 * @return formatted project information block
	 * @throws IOException if computing relative paths fails
	 */
	public String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<>();

		File projectDir = projectLayout.getProjectDir();

		List<String> sources = projectLayout.getSources();
		List<String> tests = projectLayout.getTests();
		List<String> documents = projectLayout.getDocuments();
		List<String> modules = projectLayout.getModules();

		content.add(projectLayout.getProjectName() != null ? "`" + projectLayout.getProjectName() + "`" : NOT_DEFINED);
		content.add(projectLayout.getProjectId());

		String relativePath = ProjectLayout.getRelativePath(getRootDir(), projectDir);
		content.add(relativePath);

		content.add(projectLayout.getProjectLayoutType());
		content.add(getDirInfoLine(sources, projectDir));
		content.add(getDirInfoLine(tests, projectDir));
		content.add(getDirInfoLine(documents, projectDir));
		content.add(getDirInfoLine(modules, projectDir));

		Object[] array = content.toArray(new String[0]);
		return MessageFormat.format(promptBundle.getString("project_information"), array);
	}

	/**
	 * Produces a formatted list of existing directories from the provided list.
	 *
	 * @param sources    directory list from the layout
	 * @param projectDir project root directory
	 * @return formatted directory list, or {@link #NOT_DEFINED} if none apply
	 */
	private String getDirInfoLine(List<String> sources, File projectDir) {
		String line = null;
		if (sources != null && !sources.isEmpty()) {
			List<String> dirs = sources.stream().filter(t -> t != null && new File(projectDir, t).exists())
					.map(e -> "`" + e + "`").collect(Collectors.toList());
			line = StringUtils.join(dirs, ", ");
		}

		if (StringUtils.isBlank(line)) {
			line = NOT_DEFINED;
		}
		return line;
	}

	/**
	 * Returns whether composed prompt inputs are logged to files.
	 *
	 * @return {@code true} when input logging is enabled
	 */
	public boolean isLogInputs() {
		return logInputs;
	}

	/**
	 * Enables or disables logging of composed prompt inputs.
	 *
	 * @param logInputs {@code true} to log inputs, otherwise {@code false}
	 */
	public void setLogInputs(boolean logInputs) {
		this.logInputs = logInputs;
	}

	/**
	 * Sets the additional instructions to be appended to each prompt sent to the
	 * GenAI provider.
	 *
	 * <p>
	 * The input is parsed line-by-line:
	 * </p>
	 * <ul>
	 * <li>Blank lines are preserved.</li>
	 * <li>Lines starting with {@code http://} or {@code https://} are fetched and
	 * included.</li>
	 * <li>Lines starting with {@code file:} are read from the referenced file and
	 * included.</li>
	 * <li>All other lines are included as-is.</li>
	 * </ul>
	 *
	 * @param instructions instructions input (plain text, URL, or {@code file:})
	 */
	public void setInstructions(String instructions) {
		this.instructions = parseLines(instructions);
	}

	/**
	 * Returns the parsed and expanded instructions.
	 *
	 * @return instructions text (never {@code null})
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * Parses input line-by-line and expands any {@code http(s)://} or {@code file:}
	 * references.
	 *
	 * @param data raw input
	 * @return expanded content with preserved line breaks
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
					sb.append(System.lineSeparator());
					continue;
				}

				try {
					String content = tryToGetInstructionsFromReference(normalizedLine);
					if (content != null) {
						sb.append(content);
					}
					sb.append(System.lineSeparator());
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return sb.toString();
	}

	/**
	 * Attempts to retrieve expanded content from the given data string.
	 *
	 * <ul>
	 * <li>If {@code data} starts with {@code http://} or {@code https://}, reads
	 * content from the specified URL.</li>
	 * <li>If {@code data} starts with {@code file:}, reads content from the
	 * specified file path.</li>
	 * <li>Otherwise, returns {@code data}.</li>
	 * </ul>
	 *
	 * @param data input string (URL, {@code file:} reference, or plain text)
	 * @return content read from the URL/file, or the original input
	 * @throws IOException if reading referenced content fails
	 */
	private String tryToGetInstructionsFromReference(String data) throws IOException {
		if (data == null) {
			return null;
		}

		String trimmed = data.trim();
		if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
			return parseLines(readFromHttpUrl(trimmed));
		}

		if (Strings.CS.startsWith(trimmed, "file:")) {
			String filePath = StringUtils.substringAfter(trimmed, "file:");
			filePath = StringSubstitutor.replaceSystemProperties(filePath);
			return parseLines(readFromFilePath(filePath));
		}

		return data;
	}

	/**
	 * Reads text content from a URL using UTF-8.
	 *
	 * @param urlString URL to read
	 * @return response body
	 * @throws IOException if an I/O error occurs
	 */
	private static String readFromHttpUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		try (InputStream in = url.openStream()) {
			String result = IOUtils.toString(in, StandardCharsets.UTF_8);
			logger.info("Included: `{}`", urlString);
			return result;
		}
	}

	/**
	 * Reads text content from a local file path using UTF-8.
	 *
	 * @param filePath local filesystem path (may be a raw path or a {@code file:}
	 *                 URI)
	 * @return file content
	 */
	private String readFromFilePath(String filePath) {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(getRootDir(), filePath);
		}

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			String result = IOUtils.toString(reader);
			logger.info("Included file: `{}`", file);
			return result;
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Failed to read file: " + file.getAbsolutePath() + ", Error: " + e.getMessage(), e);
		}
	}

	/**
	 * Scans the project directory and applies the configured default guidance.
	 *
	 * @param projectDir project root
	 * @param scanDir    scan start directory or pattern (currently unused by this
	 *                   implementation)
	 * @throws IOException if provider execution fails
	 */
	public void scanDocuments(File projectDir, String scanDir) throws IOException {
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		String perform = process(projectLayout, getRootDir(), getInstructions(), defaultGuidance);
		logger.info(perform);
	}

	/**
	 * Returns the default guidance, if configured.
	 *
	 * @return default guidance, or {@code null}
	 */
	public String getDefaultGuidance() {
		return defaultGuidance;
	}

	/**
	 * Sets the default guidance applied when a file does not contain embedded
	 * {@code @guidance} directives.
	 *
	 * @param defaultGuidance default guidance input (plain text, URL, or
	 *                        {@code file:})
	 */
	public void setDefaultGuidance(String defaultGuidance) {
		this.defaultGuidance = defaultGuidance;
	}

}
