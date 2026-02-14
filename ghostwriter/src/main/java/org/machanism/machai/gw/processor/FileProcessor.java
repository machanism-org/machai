package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans a project tree, extracts per-file {@code @guidance} directives through {@link Reviewer}s, and dispatches the
 * resulting prompts to a configured {@link GenAIProvider}.
 *
 * <p>
 * The processor supports single-module and multi-module project layouts. For multi-module builds, modules are processed
 * child-first (each module is scanned before the parent project directory). Processing is traversal-based; it does not
 * attempt to build projects or resolve dependencies.
 * </p>
 */
public class FileProcessor extends ProjectProcessor {

	/** String used in generated output when a value is absent in project metadata. */
	public static final String NOT_DEFINED = "not defined";

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

	/** Tag name for guidance comments. */
	public static final String GUIDANCE_TAG_NAME = "@" + "guidance:";

	/**
	 * Temporary directory name for documentation inputs under {@link #MACHAI_TEMP_DIR}.
	 */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Root scanning directory for the current documentation run. */
	private File rootDir;

	/**
	 * Specifies a special scanning path or path pattern. This should be a relative path with respect to the current
	 * processing project. If an absolute path is provided, it must be located within the {@code rootDir}.
	 */
	private File scanDir;

	/** Resource bundle supplying prompt templates for generators. */
	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Utility that installs tool functions (filesystem/command) into the provider when supported.
	 */
	private final SystemFunctionTools systemFunctionTools;

	/** Reviewer associations keyed by file extension. */
	private final Map<String, Reviewer> reviewerMap = new HashMap<>();

	/** Provider key/name (including model) used when creating GenAI providers. */
	private final String genai;

	/** Whether module processing is executed concurrently. */
	private boolean moduleMultiThread;

	/** Optional additional instructions appended to each prompt. */
	private String instructions = StringUtils.EMPTY;

	/** Whether to persist the composed inputs to a per-file log. */
	private boolean logInputs;

	/** Whether module discovery/recursion is disabled for the current run. */
	private boolean nonRecursive;

	/** Default guidance applied when a file does not contain embedded guidance. */
	private String defaultGuidance;

	/** Optional matcher used to limit module/file processing to a subset. */
	private PathMatcher pathMatcher;

	/** Optional list of path patterns or exact paths to exclude. */
	private String[] excludes;

	/** Configuration source used to initialize providers. */
	private final Configurator configurator;

	/** Maximum number of threads used for module processing. */
	private int maxModuleThreads = Math.max(1, Runtime.getRuntime().availableProcessors());

	/** Timeout for module processing worker pool shutdown. */
	private long moduleThreadTimeoutMinutes = 60;

	/**
	 * Constructs a processor.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param genai        provider key/name to use
	 * @param configurator configuration source
	 */
	public FileProcessor(File rootDir, String genai, Configurator configurator) {
		this.genai = genai;
		this.rootDir = rootDir;
		this.configurator = configurator;
		this.systemFunctionTools = new SystemFunctionTools();
		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping supported file extensions to a reviewer.
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
	 * Normalizes a file extension (with or without a leading dot) into a lower-case lookup key.
	 *
	 * @param extension the extension to normalize (e.g., {@code "java"} or {@code ".java"})
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
	 * Scans documents in the given root directory and prepares inputs for documentation generation. This overload
	 * defaults the scan start directory to {@code basedir}.
	 *
	 * @param basedir root directory to scan
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File basedir) throws IOException {
		rootDir = basedir;
		scanFolder(basedir);
	}

	/**
	 * Scans documents within the specified project root directory and the provided start subdirectory or pattern,
	 * preparing inputs for documentation generation.
	 *
	 * <p>
	 * The {@code scanDir} parameter can be either:
	 * </p>
	 * <ul>
	 * <li>A raw directory name (e.g., {@code src}),</li>
	 * <li>or a pattern string prefixed with {@code glob:} or {@code regex:}, as supported by
	 * {@link java.nio.file.FileSystems#getPathMatcher(String)}.</li>
	 * </ul>
	 *
	 * <p>
	 * If a directory path is provided, it should be relative to the current processing project. If an absolute path is
	 * provided, it must be located within the {@code projectDir}.
	 * </p>
	 *
	 * @param projectDir the root directory of the project to scan
	 * @param scanDir    the file glob/regex pattern or start directory to scan; must be a relative path with respect to
	 *                   the project, or an absolute path located within {@code projectDir}
	 * @throws IOException              if an error occurs while reading files during the scan
	 * @throws IllegalArgumentException if the scan path is not located within the root project directory
	 */
	public void scanDocuments(File projectDir, String scanDir) throws IOException {
		logger.info("Using project directory: {}", projectDir);
		logger.info("Initiating scan in path: {}", scanDir);
		
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		if (StringUtils.isBlank(scanDir)) {
			throw new IllegalArgumentException("scanDir must not be blank");
		}

		if (!Strings.CS.equals(projectDir.getAbsolutePath(), scanDir)) {
			if (!isPathPattern(scanDir)) {
				this.scanDir = new File(scanDir);
				String relativePath = ProjectLayout.getRelativePath(projectDir, new File(scanDir));
				if (relativePath == null) {
					throw new IllegalArgumentException(
							"Error: The specified scan path must be located within the root project directory: "
									+ projectDir.getAbsolutePath());
				}

				scanDir = "glob:" + relativePath + "{,/**}";
			}

			this.pathMatcher = FileSystems.getDefault().getPathMatcher(scanDir);
		}

		scanFolder(projectDir);
	}

	/**
	 * Recursively scans project folders, processing documentation inputs for all found modules and files.
	 *
	 * @param projectDir the directory containing the project/module to be scanned
	 * @throws IOException if an error occurs reading files
	 */
	@Override
	public void scanFolder(File projectDir) throws IOException {
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
	 * Determines whether a file should be considered for processing.
	 *
	 * @param file       file to test
	 * @param projectDir project root directory
	 * @return {@code true} if the file should be included, otherwise {@code false}
	 */
	protected boolean match(File file, File projectDir) {
		if (file == null) {
			return false;
		}

		if (Strings.CI.containsAny(file.getAbsolutePath(), ProjectLayout.EXCLUDE_DIRS)) {
			return false;
		}

		if (pathMatcher == null) {
			return true;
		}

		String path = ProjectLayout.getRelativePath(projectDir, file);
		if (path == null) {
			return false;
		}
		Path pathToMatch = Path.of(path);
		boolean result = pathMatcher.matches(pathToMatch);

		if (!result && scanDir != null) {
			String relativePath = ProjectLayout.getRelativePath(file, scanDir);
			if (relativePath != null) {
				Path scanFilePath = scanDir.toPath().resolve(relativePath);
				String relatedToRoot = ProjectLayout.getRelativePath(projectDir, scanFilePath.toFile());
				result = relatedToRoot != null && pathMatcher.matches(Path.of(relatedToRoot));
			}
		}

		return result;
	}

	/**
	 * Processes non-module files and directories directly under {@code projectDir}.
	 *
	 * @param projectLayout project layout
	 * @throws FileNotFoundException if the project layout cannot be created
	 * @throws IOException           if file reading fails
	 */
	protected void processParentFiles(ProjectLayout projectLayout) throws FileNotFoundException, IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = findFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		for (File child : children) {
			if (child.isDirectory()) {
				processProjectDir(projectLayout, child.getAbsolutePath());
			} else {
				logIfNotBlank(processFile(projectLayout, child));
			}
		}

		if (match(projectDir, projectDir) && children.isEmpty() && defaultGuidance != null) {
			String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"),
					defaultGuidance);
			process(projectLayout, projectLayout.getProjectDir(), defaultGuidanceText);
		}
	}

	/**
	 * Checks whether {@code dir} is one of the project module directories.
	 *
	 * @param projectLayout layout containing module definitions
	 * @param dir           directory candidate
	 * @return {@code true} if {@code dir} is a module directory, otherwise {@code false}
	 */
	private static boolean isModuleDir(ProjectLayout projectLayout, File dir) {
		List<String> modules = projectLayout.getModules();
		if (modules == null || modules.isEmpty() || dir == null) {
			return false;
		}

		String relativePath = ProjectLayout.getRelativePath(projectLayout.getProjectDir(), dir);

		return relativePath != null && Strings.CI.startsWithAny(relativePath, modules.toArray(new String[0]));
	}

	/**
	 * Logs a message at debug level when it is non-blank.
	 *
	 * @param message message to log
	 */
	private static void logIfNotBlank(String message) {
		if (StringUtils.isNotBlank(message)) {
			logger.debug(message);
		}
	}

	/**
	 * Processes a project layout for documentation gathering.
	 *
	 * @param projectLayout layout describing sources, tests, docs, and modules
	 */
	@Override
	public void processFolder(ProjectLayout projectLayout) {
		try {
			List<File> files = findFiles(projectLayout.getProjectDir());
			for (File file : files) {
				logIfNotBlank(processFile(projectLayout, file));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Processes files in a project directory matching a provided pattern or directory.
	 *
	 * @param layout      project layout
	 * @param filePattern directory path, {@code glob:}, or {@code regex:} pattern
	 */
	public void processProjectDir(ProjectLayout layout, String filePattern) {
		try {
			List<File> files = findFiles(layout.getProjectDir(), filePattern);
			for (File file : files) {
				logIfNotBlank(processFile(layout, file));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
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
	private String processFile(ProjectLayout projectLayout, File file) throws IOException {
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
	private String process(ProjectLayout projectLayout, File file, String guidance) throws IOException {
		logger.info("Processing file: '{}'", file);

		String perform;
		try (GenAIProvider provider = GenAIProviderManager.getProvider(genai, configurator)) {
			systemFunctionTools.applyTools(provider);
			File projectDir = projectLayout.getProjectDir();
			provider.setWorkingDir(projectDir);

			HashMap<String, String> props = getProperties(projectLayout);

			String effectiveInstructions = MessageFormat.format(promptBundle.getString("sys_instructions"),
					instructions);

			String instructionsLines = parseLines(effectiveInstructions, props);
			provider.instructions(instructionsLines);

			String docsProcessingInstructions = promptBundle.getString("docs_processing_instructions");
			String osName = System.getProperty("os.name");
			docsProcessingInstructions = MessageFormat.format(docsProcessingInstructions, osName);
			provider.prompt(docsProcessingInstructions);

			String projectInfo = getProjectStructureDescription(projectLayout);
			provider.prompt(projectInfo);

			String guidanceLines = parseLines(guidance, props);
			provider.prompt(guidanceLines);

			provider.prompt(promptBundle.getString("output_format"));

			if (isLogInputs()) {
				String inputsFileName = ProjectLayout.getRelativePath(rootDir, file);
				File docsTempDir = new File(rootDir, MACHAI_TEMP_DIR + File.separator + GW_TEMP_DIR);
				File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
				File parentDir = inputsFile.getParentFile();
				if (parentDir != null) {
					Files.createDirectories(parentDir.toPath());
				}
				provider.inputsLog(inputsFile);
			}

			perform = provider.perform();
		}

		logger.info("Finished processing file: {}", file.getAbsolutePath());
		return perform;
	}

	/**
	 * Builds a substitution map used to expand project metadata placeholders in prompt templates.
	 *
	 * @param projectLayout current project layout
	 * @return placeholder map
	 */
	private HashMap<String, String> getProperties(ProjectLayout projectLayout) {
		HashMap<String, String> valueMap = new HashMap<>();
		valueMap.put("projectId", projectLayout.getProjectId());
		valueMap.put("projectName", projectLayout.getProjectName());
		String parentId = projectLayout.getParentId();
		if (parentId != null) {
			valueMap.put("parentId", parentId);
		}
		File parentDir = projectLayout.getProjectDir().getParentFile();
		if (parentDir != null) {
			valueMap.put("parentDirName", parentDir.getName());
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

		String relativePath = ProjectLayout.getRelativePath(rootDir, projectDir);
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
	 * Finds all files/directories in the provided project folder that match a pattern.
	 *
	 * @param projectDir project root
	 * @param pattern    directory path, {@code glob:} matcher, or {@code regex:} matcher
	 * @return matching files/directories
	 * @throws IOException if directory traversal fails
	 */
	private List<File> findFiles(File projectDir, String pattern) throws IOException {
		List<File> result = new ArrayList<>();
		File dir = new File(pattern);
		PathMatcher matcher = null;

		if (isPathPattern(pattern)) {
			matcher = FileSystems.getDefault().getPathMatcher(pattern);
			dir = projectDir;
		} else {
			if (!dir.isAbsolute()) {
				dir = new File(projectDir, pattern);
			}
		}

		List<File> files = new ArrayList<>(
				FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY));

		files.sort(Comparator.comparingInt((File f) -> pathDepth(f.getPath())).reversed());

		for (File file : files) {
			String path = ProjectLayout.getRelativePath(projectDir, file);
			if (path == null) {
				continue;
			}

			if (Strings.CI.containsAny(path, ProjectLayout.EXCLUDE_DIRS) || shouldExcludePath(Path.of(path))) {
				continue;
			}

			if (matcher == null || matcher.matches(file.toPath())) {
				result.add(file);
			}
		}

		return result;
	}

	/**
	 * Tests whether a scan pattern string is a {@code glob:} or {@code regex:} matcher.
	 *
	 * @param pattern scan directory argument
	 * @return {@code true} when the pattern uses a path-matcher prefix
	 */
	private static boolean isPathPattern(String pattern) {
		return Strings.CI.startsWithAny(pattern, "glob:", "regex:");
	}

	/**
	 * Recursively lists all files under a directory, excluding known build/tooling directories.
	 *
	 * @param projectDir directory to traverse
	 * @return files found
	 * @throws IOException if directory listing fails
	 */
	private List<File> findFiles(File projectDir) throws IOException {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
		}

		File[] files = projectDir.listFiles();
		if (files == null) {
			throw new IOException("Unable to list files for directory: " + projectDir.getAbsolutePath());
		}

		List<File> result = new ArrayList<>();
		for (File file : files) {
			String name = file.getName();
			String relativePathString = ProjectLayout.getRelativePath(projectDir, file);
			if (relativePathString == null) {
				continue;
			}
			Path relativePath = Path.of(relativePathString);

			if (Strings.CI.equalsAny(name, ProjectLayout.EXCLUDE_DIRS) || shouldExcludePath(relativePath)) {
				continue;
			}
			if (file.isDirectory()) {
				result.addAll(findFiles(file));
			} else {
				result.add(file);
			}
		}

		result.sort(Comparator.comparingInt((File f) -> pathDepth(f.getPath())).reversed());
		return result;
	}

	/**
	 * Determines whether a relative path should be excluded according to {@link #excludes}.
	 *
	 * @param path project-relative path
	 * @return {@code true} when excluded
	 */
	private boolean shouldExcludePath(Path path) {
		if (path != null && excludes != null) {
			for (String exclude : excludes) {
				PathMatcher matcher = getPatternPath(exclude);
				if (matcher != null) {
					if (matcher.matches(path)) {
						return true;
					}
				} else {
					String relative = path.toString();
					if (Strings.CS.equals(relative, exclude)
							|| Strings.CS.equals(path.getFileName().toString(), exclude)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Computes the depth of a path for sorting.
	 *
	 * @param path input path
	 * @return number of path segments
	 */
	private static int pathDepth(String path) {
		if (path == null || path.isBlank()) {
			return 0;
		}
		String normalized = path.replace("\\", "/");
		return normalized.split("/").length;
	}

	/**
	 * Returns a {@link PathMatcher} when the provided string is a path pattern.
	 *
	 * @param path pattern candidate
	 * @return matcher or {@code null} when {@code path} is not a pattern
	 */
	private static PathMatcher getPatternPath(String path) {
		if (StringUtils.isNotBlank(path) && isPathPattern(path)) {
			return FileSystems.getDefault().getPathMatcher(path);
		}

		return null;
	}

	/**
	 * Returns the root directory used as a base for relative paths.
	 *
	 * @return root directory
	 */
	public File getRootDir() {
		return rootDir;
	}

	/**
	 * Deletes the input-log temporary directory.
	 *
	 * @param basedir project base directory
	 * @return {@code true} if the directory was deleted, otherwise {@code false}
	 */
	public static boolean deleteTempFiles(File basedir) {
		File file = new File(basedir, FileProcessor.MACHAI_TEMP_DIR + File.separator + FileProcessor.GW_TEMP_DIR);
		logger.info("Removing '{}' inputs log file.", file);
		return FileUtils.deleteQuietly(file);
	}

	/**
	 * Returns whether module processing is executed concurrently.
	 *
	 * @return {@code true} if multi-threaded module processing is enabled
	 */
	public boolean isModuleMultiThread() {
		return moduleMultiThread;
	}

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * <p>
	 * When enabling, this method verifies that the configured provider is thread-safe.
	 * </p>
	 *
	 * @param moduleMultiThread {@code true} to enable, {@code false} to disable
	 * @throws IllegalArgumentException if enabling is requested but the provider is not thread-safe
	 */
	public void setModuleMultiThread(boolean moduleMultiThread) {
		if (!moduleMultiThread) {
			this.moduleMultiThread = false;
			return;
		}

		try (GenAIProvider provider = GenAIProviderManager.getProvider(genai, configurator)) {
			if (!provider.isThreadSafe()) {
				throw new IllegalArgumentException("The provider '" + genai
						+ "' is not thread-safe and cannot be used in a multi-threaded context.");
			}
		}
		this.moduleMultiThread = true;
	}

	/**
	 * Sets the instructions string used by this processor.
	 *
	 * <p>
	 * The provided input is parsed line-by-line:
	 * </p>
	 * <ul>
	 * <li>Blank lines are preserved as line breaks.</li>
	 * <li>Lines starting with {@code http://} or {@code https://} are treated as URLs and the referenced content is
	 * included.</li>
	 * <li>Lines starting with {@code file:} are treated as file references and the referenced content is included.</li>
	 * <li>All other lines are included as-is.</li>
	 * </ul>
	 *
	 * @param instructions instructions input (plain text, URL, or {@code file:})
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
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
	 * Returns whether recursion into modules/subdirectories is disabled.
	 *
	 * @return {@code true} when non-recursive mode is enabled
	 */
	public boolean isNonRecursive() {
		return nonRecursive;
	}

	/**
	 * Sets whether scanning is restricted to the current directory only.
	 *
	 * @param nonRecursive {@code true} to disable module recursion
	 */
	public void setNonRecursive(boolean nonRecursive) {
		this.nonRecursive = nonRecursive;
	}

	/**
	 * Sets the default guidance applied when a file contains no embedded guidance.
	 *
	 * <p>
	 * The provided input is parsed line-by-line:
	 * </p>
	 * <ul>
	 * <li>Blank lines are preserved as line breaks.</li>
	 * <li>Lines starting with {@code http://} or {@code https://} are treated as URLs and the referenced content is
	 * included.</li>
	 * <li>Lines starting with {@code file:} are treated as file references and the referenced content is included.</li>
	 * <li>All other lines are included as-is.</li>
	 * </ul>
	 *
	 * @param defaultGuidance default guidance input (plain text, URL, or {@code file:})
	 */
	public void setDefaultGuidance(String defaultGuidance) {
		this.defaultGuidance = defaultGuidance;
	}

	/**
	 * Parses input line-by-line and expands any {@code http(s)://} or {@code file:} references.
	 *
	 * @param data     raw input
	 * @param valueMap value substitution map
	 * @return expanded content with preserved line breaks
	 */
	private static String parseLines(String data, HashMap<String, String> valueMap) {
		if (data == null) {
			return StringUtils.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		data.lines().forEach(line -> {
			String normalizedLine = StringUtils.stripToNull(line);
			if (normalizedLine == null) {
				sb.append(System.lineSeparator());
				return;
			}

			normalizedLine = StrSubstitutor.replaceSystemProperties(normalizedLine);
			normalizedLine = StrSubstitutor.replace(normalizedLine, valueMap);

			String content = tryToGetInstructionsFromFile(normalizedLine, valueMap);
			if (content != null) {
				sb.append(content);
			}
			sb.append(System.lineSeparator());
		});

		return sb.toString();
	}

	/**
	 * Returns excludes configured for this processor.
	 *
	 * @return exclude list
	 */
	public String[] getExcludes() {
		return excludes;
	}

	/**
	 * Sets exclude patterns/paths.
	 *
	 * <p>
	 * Each entry may be:
	 * </p>
	 * <ul>
	 * <li>a {@code glob:} or {@code regex:} matcher expression</li>
	 * <li>an exact relative path (compared using {@link Strings#CS})</li>
	 * </ul>
	 *
	 * @param excludes exclude list
	 */
	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	/**
	 * Returns the maximum number of worker threads used for module processing.
	 *
	 * @return maximum module thread count
	 */
	public int getMaxModuleThreads() {
		return maxModuleThreads;
	}

	/**
	 * Sets the maximum number of worker threads used for module processing.
	 *
	 * @param maxModuleThreads maximum thread count (values &lt;= 0 are not allowed)
	 */
	public void setMaxModuleThreads(int maxModuleThreads) {
		if (maxModuleThreads <= 0) {
			throw new IllegalArgumentException("maxModuleThreads must be > 0");
		}
		this.maxModuleThreads = maxModuleThreads;
	}

	/**
	 * Returns timeout (in minutes) to wait for module processing completion during shutdown.
	 *
	 * @return shutdown timeout in minutes
	 */
	public long getModuleThreadTimeoutMinutes() {
		return moduleThreadTimeoutMinutes;
	}

	/**
	 * Sets the module processing shutdown timeout.
	 *
	 * @param moduleThreadTimeoutMinutes timeout in minutes
	 */
	public void setModuleThreadTimeoutMinutes(long moduleThreadTimeoutMinutes) {
		if (moduleThreadTimeoutMinutes <= 0) {
			throw new IllegalArgumentException("moduleThreadTimeoutMinutes must be > 0");
		}
		this.moduleThreadTimeoutMinutes = moduleThreadTimeoutMinutes;
	}

	/**
	 * Attempts to retrieve instructions from the given data string.
	 *
	 * <ul>
	 * <li>If {@code data} starts with {@code http://} or {@code https://}, reads content from the specified URL.</li>
	 * <li>If {@code data} starts with {@code file:}, reads content from the specified file path.</li>
	 * <li>Otherwise, returns {@code data}.</li>
	 * </ul>
	 *
	 * @param data     input string (URL, {@code file:} reference, or plain text)
	 * @param valueMap value substitution map
	 * @return content read from the URL/file, or the original input
	 */
	private static String tryToGetInstructionsFromFile(String data, HashMap<String, String> valueMap) {
		if (data == null) {
			return null;
		}

		try {
			String trimmed = data.trim();
			if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
				return parseLines(readFromHttpUrl(trimmed), valueMap);
			}

			if (Strings.CS.startsWith(trimmed, "file:")) {
				String filePath = StringUtils.substringAfter(trimmed, "file:");
				return parseLines(readFromFilePath(filePath), valueMap);
			}

			return data;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL: " + data, e);
		}
	}

	/**
	 * Reads text content from a URL using UTF-8.
	 *
	 * <p>
	 * If an initial unauthenticated request fails and the URL includes user-info (e.g., {@code https://user:pass@host}),
	 * the request is retried with an HTTP Basic {@code Authorization} header.
	 * </p>
	 *
	 * @param urlString URL to read
	 * @return response body
	 * @throws MalformedURLException if {@code urlString} is not a valid URL
	 */
	private static String readFromHttpUrl(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		try (InputStream in = url.openStream()) {
			String result = IOUtils.toString(in, StandardCharsets.UTF_8);
			logger.info("Included: `{}`", urlString);
			return result;
		} catch (IOException e) {
			String userInfo = url.getUserInfo();
			if (userInfo == null) {
				throw new IllegalArgumentException("Invalid URL: " + urlString, e);
			}
			return readFromHttpUrlWithBasicAuth(urlString, url, userInfo, e);
		}
	}

	/**
	 * Retries reading a URL using an HTTP Basic {@code Authorization} header.
	 *
	 * @param urlString original URL string (may include user-info)
	 * @param url       parsed URL
	 * @param userInfo  raw user-info from {@link URL#getUserInfo()}
	 * @param cause     original exception
	 * @return response body
	 */
	private static String readFromHttpUrlWithBasicAuth(String urlString, URL url, String userInfo, IOException cause) {
		try {
			String cleanUrl = urlString.replaceFirst("//" + userInfo + "@", "//");
			URL urlNoAuth = new URL(cleanUrl);
			URLConnection connection = urlNoAuth.openConnection();

			String basicToken = Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
			connection.setRequestProperty("Authorization", "Basic " + basicToken);

			try (InputStream in = connection.getInputStream()) {
				String result = IOUtils.toString(in, StandardCharsets.UTF_8);
				logger.info("Included: `{}`", url);
				return result;
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException("Invalid basic auth token in URL: " + urlString, cause);
		}
	}

	/**
	 * Reads text content from a local file path using UTF-8.
	 *
	 * @param filePath local filesystem path (may be a raw path or a {@code file:} URI)
	 * @return file content
	 */
	private static String readFromFilePath(String filePath) {
		Path path = toLocalPath(filePath);
		if (!Files.exists(path)) {
			throw new IllegalArgumentException("File not found: " + path.toAbsolutePath());
		}

		try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			String result = IOUtils.toString(reader);
			logger.info("Included: `{}`", path);
			return result;
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read file: " + path.toAbsolutePath(), e);
		}
	}

	/**
	 * Converts a provided path string into a local {@link Path}.
	 *
	 * @param filePath raw path or {@code file:} URI
	 * @return local path
	 */
	private static Path toLocalPath(String filePath) {
		String trimmed = StringUtils.trimToNull(filePath);
		if (trimmed == null) {
			throw new IllegalArgumentException("File path must not be blank");
		}

		try {
			URI uri = new URI(trimmed);
			if (uri.getScheme() == null) {
				return Path.of(trimmed);
			}
			if (!Strings.CI.equals(uri.getScheme(), "file")) {
				throw new IllegalArgumentException("Unsupported URI scheme for file path: " + trimmed);
			}
			return Path.of(uri);
		} catch (URISyntaxException e) {
			return Path.of(trimmed);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid file path: " + trimmed, e);
		}
	}
}
