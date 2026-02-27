package org.machanism.machai.gw.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans a project tree, extracts per-file {@code @guidance} directives through
 * {@link Reviewer}s, and dispatches the resulting prompts to a configured
 * {@link GenAIProvider}.
 *
 * <p>
 * The processor supports single-module and multi-module project layouts. For
 * multi-module builds, modules are processed child-first (each module is scanned
 * before the parent project directory). Processing is traversal-based; it does
 * not attempt to build projects or resolve dependencies.
 * </p>
 */
public class GuidanceProcessor extends FileProcessor {

	/**
	 * String used in generated output when a value is absent in project metadata.
	 */
	public static final String NOT_DEFINED = "not defined";

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(GuidanceProcessor.class);

	/** Tag name for guidance comments. */
	public static final String GUIDANCE_TAG_NAME = "@" + "guidance:";

	/**
	 * Temporary directory name for documentation inputs under
	 * {@link #MACHAI_TEMP_DIR}.
	 */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/** Reviewer associations keyed by file extension. */
	private final Map<String, Reviewer> reviewerMap = new HashMap<>();

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
	String defaultGuidance;

	/**
	 * Constructs a processor.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param genai        provider key/name to use
	 * @param configurator configuration source
	 */
	public GuidanceProcessor(File rootDir, String genai, Configurator configurator) {
		super(rootDir, configurator);
		logger.info("File processing root directory: {}", rootDir);
		logger.info("GenAI: {}", genai);

		FunctionToolsLoader.getInstance().setConfiguration(configurator);

		this.genai = genai;
		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping
	 * supported file extensions to a reviewer.
	 */
	private void loadReviewers() {
		reviewerMap.clear();

		ServiceLoader<Reviewer> reviewerServiceLoader = ServiceLoader.load(Reviewer.class);
		for (Reviewer reviewer : reviewerServiceLoader) {
			String[] extensions = reviewer.getSupportedFileExtensions();
			for (String extension : extensions) {
				String key = normalizeExtensionKey(extension);
				if (key != null) {
					reviewerMap.putIfAbsent(key, reviewer);
				}
			}
		}
	}

	/**
	 * Normalizes a file extension (with or without a leading dot) into a lower-case
	 * lookup key.
	 *
	 * @param extension the extension to normalize (e.g., {@code "java"} or
	 *                  {@code ".java"})
	 * @return normalized key, or {@code null} if the input is blank
	 */
	private static String normalizeExtensionKey(String extension) {
		String value = StringUtils.trimToNull(extension);
		if (value == null) {
			return null;
		}
		if (value.startsWith(".")) {
			value = value.substring(1);
		}
		return value.toLowerCase();
	}

	/**
	 * Scans documents within the specified project root directory and the provided
	 * start subdirectory or pattern, preparing inputs for documentation generation.
	 *
	 * <p>
	 * The {@code scanDir} parameter can be either:
	 * </p>
	 * <ul>
	 * <li>A raw directory name (e.g., {@code src}),</li>
	 * <li>or a pattern string prefixed with {@code glob:} or {@code regex:}, as
	 * supported by {@link FileSystems#getPathMatcher(String)}.</li>
	 * </ul>
	 *
	 * <p>
	 * If a directory path is provided, it should be relative to the current
	 * processing project. If an absolute path is provided, it must be located
	 * within the {@code projectDir}.
	 * </p>
	 *
	 * @param projectDir the root directory of the project to scan
	 * @param scanDir    the file glob/regex pattern or start directory to scan;
	 *                   must be a relative path with respect to the project, or an
	 *                   absolute path located within {@code projectDir}
	 * @throws IOException              if an error occurs while reading files
	 *                                  during the scan
	 * @throws IllegalArgumentException if the scan path is not located within the
	 *                                  root project directory
	 */
	public void scanDocuments(File projectDir, String scanDir) throws IOException {
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		if (StringUtils.isBlank(scanDir)) {
			throw new IllegalArgumentException("scanDir must not be blank");
		}

		if (!Strings.CS.equals(projectDir.getAbsolutePath(), scanDir)) {

			logger.info("Scan path: {}", scanDir);

			if (!isPathPattern(scanDir)) {
				super.setScanDir(new File(scanDir));
				String relativePath = ProjectLayout.getRelativePath(projectDir, new File(scanDir));
				if (relativePath == null) {
					throw new IllegalArgumentException(
							"Error: The specified scan path must be located within the root project directory: "
									+ projectDir.getAbsolutePath());
				}

				if (defaultGuidance == null) {
					scanDir = "glob:" + relativePath + "{,/**}";
				} else {
					scanDir = "glob:" + relativePath;
				}
			}
			super.setPathMatcher(FileSystems.getDefault().getPathMatcher(scanDir));
		}

		scanFolder(projectDir);
	}

	/**
	 * Recursively scans project folders, processing documentation inputs for all
	 * found modules and files.
	 *
	 * @param projectDir the directory containing the project/module to be scanned
	 * @throws IOException if an error occurs reading files
	 */
	@Override
	public void scanFolder(File projectDir) throws IOException {
		if (getScanDir() != null) {
			logger.info("Starting scan of directory: {}", getScanDir());
		}

		ProjectLayout projectLayout = getProjectLayout(projectDir);
		if (!isNonRecursive()) {
			List<String> modules = projectLayout.getModules();

			if (modules != null && !modules.isEmpty()) {
				if (isModuleMultiThread()) {
					processModulesMultiThreaded(projectDir, modules);
				} else {
					for (String module : modules) {
						processModule(projectDir, module);
					}
				}
			}
		}

		processParentFiles(projectLayout);
	}

	/**
	 * Applies matching logic and default-guidance behavior.
	 *
	 * @param file       candidate file/directory
	 * @param projectDir current project directory
	 * @return {@code true} when the candidate should be processed
	 */
	@Override
	protected boolean match(File file, File projectDir) {
		if (getPathMatcher() == null) {
			return defaultGuidance == null || Objects.equals(file, projectDir);
		}

		return super.match(file, projectDir);
	}

	/**
	 * Processes all discovered modules concurrently.
	 *
	 * @param projectDir the parent project directory
	 * @param modules    module relative paths
	 */
	private void processModulesMultiThreaded(File projectDir, List<String> modules) {
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(modules.size(), getMaxModuleThreads()));
		try {
			List<Future<Void>> futures = new ArrayList<>();
			for (String module : modules) {
				futures.add(executor.submit(() -> {
					processModule(projectDir, module);
					return null;
				}));
			}

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("Thread interrupted while processing modules", e);
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof RuntimeException) {
						throw (RuntimeException) cause;
					}
					throw new IllegalStateException("Module processing failed.", cause);
				}
			}
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(getModuleThreadTimeoutMinutes(), TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Thread interrupted while awaiting module termination", e);
			}
		}
	}

	/**
	 * Processes a module directory.
	 *
	 * <p>
	 * When a scan directory or pattern is configured, modules are only processed
	 * when the module itself matches or contains the scan directory.
	 * </p>
	 */
	@Override
	protected void processModule(File projectDir, String module) throws IOException {
		if (getScanDir() != null) {
			String relativePath = ProjectLayout.getRelativePath(new File(projectDir, module), getScanDir());
			if (match(new File(projectDir, module), projectDir) || relativePath != null) {
				super.processModule(projectDir, module);
			}
		} else {
			super.processModule(projectDir, module);
		}
	}

	/**
	 * Processes files and folders under the parent project directory (excluding
	 * modules).
	 */
	@Override
	protected void processParentFiles(ProjectLayout projectLayout) throws FileNotFoundException, IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = findFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		for (File child : children) {
			processFile(projectLayout, child);
		}

		boolean match = match(projectDir, projectDir);

		if (match && defaultGuidance != null) {
			String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"), projectDir,
					defaultGuidance);
			process(projectLayout, projectDir, defaultGuidanceText);
		}
	}

	/**
	 * Extracts guidance for a file and, when present, performs provider processing.
	 *
	 * @param projectLayout project layout
	 * @param file          file to process
	 * @return provider output, or {@code null} if the file is skipped
	 * @throws IOException if reading the file or provider execution fails
	 */
	@Override
	String processFile(ProjectLayout projectLayout, File file) throws IOException {
		String perform = null;

		File projectDir = projectLayout.getProjectDir();
		if (match(file, projectDir)) {
			String guidance = parseFile(projectDir, file);

			if (guidance != null) {
				perform = process(projectLayout, file, guidance);

			} else if (defaultGuidance != null) {
				String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"), file,
						defaultGuidance);
				perform = process(projectLayout, file, defaultGuidanceText);
			}
		}

		if (StringUtils.isNotBlank(perform)) {
			logger.debug(perform);
		}

		return perform;
	}

	/**
	 * Creates a provider and performs a full prompt run for the given file.
	 *
	 * @param projectLayout project layout
	 * @param file          file being processed (used for logging and templating)
	 * @param guidance      guidance content to include in the prompt
	 * @return provider output
	 * @throws IOException if creating inputs logs fails or provider I/O fails
	 */
	String process(ProjectLayout projectLayout, File file, String guidance) throws IOException {
		logger.info("Processing file: '{}'", file);

		GenAIProvider provider = GenAIProviderManager.getProvider(genai, getConfigurator());

		FunctionToolsLoader.getInstance().applyTools(provider);

		File projectDir = projectLayout.getProjectDir();
		provider.setWorkingDir(projectDir);

		String effectiveInstructions = MessageFormat.format(promptBundle.getString("sys_instructions"), instructions);
		provider.instructions(effectiveInstructions);

		String docsProcessingInstructions = promptBundle.getString("docs_processing_instructions");
		String osName = System.getProperty("os.name");
		docsProcessingInstructions = MessageFormat.format(docsProcessingInstructions, osName);
		provider.prompt(docsProcessingInstructions);

		String projectInfo = getProjectStructureDescription(projectLayout);
		provider.prompt(projectInfo);

		String guidanceLines = parseLines(guidance);

		HashMap<String, String> props = getProperties(projectLayout);
		guidanceLines = StrSubstitutor.replace(guidanceLines, props);
		provider.prompt(guidanceLines);

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

		String perform = provider.perform();

		logger.info("Finished processing file: {}", file.getAbsolutePath());
		return perform;
	}

	/**
	 * Collects key project properties from the provided {@link ProjectLayout} and
	 * returns them as a map.
	 *
	 * @param projectLayout the {@link ProjectLayout} instance from which to extract
	 *                      properties
	 * @return a {@link HashMap} containing project property keys and their
	 *         corresponding values
	 */
	protected HashMap<String, String> getProperties(ProjectLayout projectLayout) {
		HashMap<String, String> valueMap = new HashMap<>();
		valueMap.put(GW_PROJECT_LAYOUT_PROP_PREFIX + "id", projectLayout.getProjectId());
		valueMap.put(GW_PROJECT_LAYOUT_PROP_PREFIX + "name", projectLayout.getProjectName());
		String parentId = projectLayout.getParentId();
		if (parentId != null) {
			valueMap.put(GW_PROJECT_LAYOUT_PROP_PREFIX + "parentId", parentId);
		}
		File parentDir = projectLayout.getProjectDir().getParentFile();
		if (parentDir != null) {
			valueMap.put(GW_PROJECT_LAYOUT_PROP_PREFIX + "parentDir", parentDir.getName());
		}
		return valueMap;
	}

	/**
	 * Builds a human-readable description of the project structure used in prompts.
	 *
	 * @param projectLayout current project layout
	 * @return formatted project information block
	 * @throws IOException if computing relative paths fails
	 */
	private String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
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
	 * Uses a {@link Reviewer} (based on file extension) to extract guidance.
	 *
	 * @param projectDir project root directory
	 * @param file       file being parsed
	 * @return guidance text, or {@code null} if the file type is not supported
	 * @throws IOException if the file cannot be read
	 */
	private String parseFile(File projectDir, File file) throws IOException {
		if (!file.isFile()) {
			return null;
		}

		String extension = FilenameUtils.getExtension(file.getName());
		Reviewer reviewer = getReviewerForExtension(extension);
		if (reviewer == null) {
			return null;
		}

		return reviewer.perform(projectDir, file);
	}

	/**
	 * Resolves a reviewer for a given file extension.
	 *
	 * @param extension file extension (with or without a dot)
	 * @return reviewer, or {@code null} if none is registered for that extension
	 */
	private Reviewer getReviewerForExtension(String extension) {
		String key = normalizeExtensionKey(extension);
		if (key == null) {
			return null;
		}
		return reviewerMap.get(key);
	}

	/**
	 * Deletes the input-log temporary directory.
	 *
	 * @param basedir project base directory
	 * @return {@code true} if the directory was deleted, otherwise {@code false}
	 */
	public static boolean deleteTempFiles(File basedir) {
		File file = new File(basedir, GuidanceProcessor.MACHAI_TEMP_DIR + File.separator + GuidanceProcessor.GW_TEMP_DIR);
		logger.info("Removing '{}' inputs log file.", file);
		return FileUtils.deleteQuietly(file);
	}

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * <p>
	 * When enabling, this method verifies that the configured provider is
	 * thread-safe.
	 * </p>
	 *
	 * @param moduleMultiThread {@code true} to enable, {@code false} to disable
	 * @throws IllegalArgumentException if enabling is requested but the provider is
	 *                                  not thread-safe
	 */
	@Override
	public void setModuleMultiThread(boolean moduleMultiThread) {
		if (moduleMultiThread) {
			GenAIProvider provider = GenAIProviderManager.getProvider(genai, getConfigurator());
			if (!provider.isThreadSafe()) {
				throw new IllegalArgumentException(
						"The provider '" + genai
								+ "' is not thread-safe and cannot be used in a multi-threaded context.");
			}
		}
		super.setModuleMultiThread(moduleMultiThread);
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
	 * Sets the default guidance applied when a file does not contain embedded
	 * {@code @guidance} directives.
	 *
	 * @param defaultGuidance default guidance input (plain text, URL, or
	 *                        {@code file:})
	 */
	public void setDefaultGuidance(String defaultGuidance) {
		this.defaultGuidance = defaultGuidance;
	}

	/**
	 * Parses input line-by-line and expands any {@code http(s)://} or {@code file:}
	 * references.
	 *
	 * @param data raw input
	 * @return expanded content with preserved line breaks
	 */
	private String parseLines(String data) {
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

}