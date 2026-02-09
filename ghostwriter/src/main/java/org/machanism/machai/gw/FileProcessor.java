package org.machanism.machai.gw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
 * Scans a project workspace, extracts {@code @guidance:} blocks, and submits
 * per-file review requests to a configured GenAI provider.
 *
 * <p>
 * The processor discovers file-type specific {@link Reviewer} implementations
 * using {@link ServiceLoader}. Each reviewer knows how to parse its
 * corresponding file format and produce a prompt fragment that includes the
 * original file contents and any embedded guidance comments.
 * </p>
 *
 * <h2>High-level flow</h2>
 * <ol>
 * <li>Discover modules (if any) using {@link ProjectLayout}.</li>
 * <li>Traverse files (optionally filtered by pattern and excludes).</li>
 * <li>For each supported file, use a {@link Reviewer} to extract guidance.</li>
 * <li>Compose a prompt (project info + guidance + output format) and execute
 * {@link GenAIProvider#perform()}.</li>
 * </ol>
 */
public class FileProcessor extends ProjectProcessor {
	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

	/** Tag name for guidance comments. */
	public static final String GUIDANCE_TAG_NAME = "@" + "guidance:";

	/**
	 * Temporary directory name for documentation inputs under
	 * {@link #MACHAI_TEMP_DIR}.
	 */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Resource bundle supplying prompt templates for generators. */
	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Utility that installs tool functions (filesystem/command) into the provider
	 * when supported.
	 */
	private final SystemFunctionTools systemFunctionTools;

	/** Reviewer associations keyed by file extension. */
	private final Map<String, Reviewer> reviewMap = new HashMap<>();

	/** Root scanning directory for the current documentation run. */
	private File rootDir;

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

	private File scanDir;

	/**
	 * Constructs a processor.
	 *
	 * @param genai        provider key/name to use
	 * @param configurator configuration source
	 */
	public FileProcessor(String genai, Configurator configurator) {
		this.genai = genai;
		this.configurator = configurator;
		this.systemFunctionTools = new SystemFunctionTools();
		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping
	 * supported file extensions to a reviewer.
	 */
	private void loadReviewers() {
		reviewMap.clear();

		ServiceLoader<Reviewer> reviewerServiceLoader = ServiceLoader.load(Reviewer.class);
		for (Reviewer reviewer : reviewerServiceLoader) {
			String[] extensions = reviewer.getSupportedFileExtensions();
			for (String extension : extensions) {
				String key = normalizeExtensionKey(extension);
				if (key != null) {
					reviewMap.putIfAbsent(key, reviewer);
				}
			}
		}
	}

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
	 * Scans documents in the given root directory and prepares inputs for
	 * documentation generation. This overload defaults the scan start directory to
	 * {@code basedir}.
	 *
	 * @param basedir root directory to scan
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File basedir) throws IOException {
		rootDir = basedir;
		scanFolder(basedir);
	}

	/**
	 * Scans documents in the given root directory and start subdirectory, preparing
	 * inputs for documentation generation.
	 *
	 * <p>
	 * Note: callers may pass a raw directory (e.g. {@code src}) or a {@code glob:}
	 * / {@code regex:} pattern supported by
	 * {@link FileSystems#getPathMatcher(String)}.
	 * </p>
	 *
	 * @param rootDir the root directory of the project to scan
	 * @param scanDir file glob/regex pattern or start directory
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File rootDir, String scanDir) throws IOException {
		if (!Strings.CS.equals(rootDir.getAbsolutePath(), scanDir)) {
			if (!isPathPattern(scanDir)) {
				this.scanDir = new File(scanDir);
				String relatedPath = ProjectLayout.getRelatedPath(rootDir, new File(scanDir));

				scanDir = "glob:" + relatedPath + "{,/**}";
			}

			this.pathMatcher = FileSystems.getDefault().getPathMatcher(scanDir);
		}

		this.rootDir = rootDir;
		scanFolder(rootDir);
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
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		if (!isNonRecursive()) {
			List<String> modules = projectLayout.getModules();

			if (modules != null && !modules.isEmpty()) {
				if (isModuleMultiThread()) {
					processModulesMultiThreaded(projectDir, modules);
				} else {
					logger.info("Multi-threaded processing mode disabled.");
					for (String module : modules) {
						processModule(projectDir, module);
					}
				}
			}
		}

		processParentFiles(projectLayout);
	}

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

	@Override
	protected void processModule(File projectDir, String module) throws IOException {
		if (match(new File(projectDir, module))) {
			super.processModule(projectDir, module);
		}
	}

	private boolean match(File file) {
		if (file == null) {
			return false;
		}

		if (Strings.CI.containsAny(file.getAbsolutePath(), ProjectLayout.EXCLUDE_DIRS)) {
			return false;
		}

		String path = ProjectLayout.getRelatedPath(rootDir, file);
		if (pathMatcher == null) {
			return true;
		}

		Path pathToMatch = Path.of(path);
		boolean result = pathMatcher.matches(pathToMatch);

		if (!result && scanDir != null) {
			String relatedPath = ProjectLayout.getRelatedPath(file, scanDir);
			if (relatedPath != null) {
				String normalizedFileAbsolutePath = file.getAbsolutePath().replace("\\\\", "/");
				String scanPath = normalizedFileAbsolutePath + "/" + relatedPath;
				String relatedToRoot = ProjectLayout.getRelatedPath(rootDir, new File(scanPath));
				result = pathMatcher.matches(Path.of(relatedToRoot));
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

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child));

		for (File child : children) {
			if (child.isDirectory()) {
				processProjectDir(projectLayout, child.getAbsolutePath());
			} else {
				logIfNotBlank(processFile(projectLayout, child));
			}
		}

		if (match(projectDir) && children.isEmpty() && defaultGuidance != null) {
			String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"),
					defaultGuidance);
			process(projectLayout, projectLayout.getProjectDir(), defaultGuidanceText);
		}
	}

	private static boolean isModuleDir(ProjectLayout projectLayout, File dir) {
		List<String> modules = projectLayout.getModules();
		if (modules == null || modules.isEmpty() || dir == null) {
			return false;
		}

		String relatedPath = ProjectLayout.getRelatedPath(projectLayout.getProjectDir(), dir);

		return Strings.CI.startsWithAny(relatedPath, modules.toArray(new String[0]));
	}

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
	 * Processes files in a project directory matching a provided pattern or
	 * directory.
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

	private String processFile(ProjectLayout projectLayout, File file) throws IOException {
		String perform = null;

		if (match(file)) {
			File projectDir = projectLayout.getProjectDir();
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

	private String process(ProjectLayout projectLayout, File file, String guidance) throws IOException {
		logger.info("Processing file: '{}'", file);

		String perform;
		try (GenAIProvider provider = GenAIProviderManager.getProvider(genai, configurator)) {
			systemFunctionTools.applyTools(provider);
			File projectDir = projectLayout.getProjectDir();
			provider.setWorkingDir(projectDir);

			String effectiveInstructions = MessageFormat.format(promptBundle.getString("sys_instructions"),
					instructions);
			provider.instructions(effectiveInstructions);

			String docsProcessingInstructions = promptBundle.getString("docs_processing_instructions");
			String osName = System.getProperty("os.name");
			docsProcessingInstructions = MessageFormat.format(docsProcessingInstructions, osName);
			provider.prompt(docsProcessingInstructions);

			String projectInfo = getProjectStructureDescription(projectLayout);
			provider.prompt(projectInfo);

			provider.prompt(guidance);

			provider.prompt(promptBundle.getString("output_format"));

			if (isLogInputs()) {
				String inputsFileName = ProjectLayout.getRelatedPath(rootDir, file);
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

	private String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<>();

		File projectDir = projectLayout.getProjectDir();

		List<String> sources = projectLayout.getSources();
		List<String> tests = projectLayout.getTests();
		List<String> documents = projectLayout.getDocuments();
		List<String> modules = projectLayout.getModules();

		content.add(projectLayout.getProjectName());
		content.add(projectLayout.getProjectId());
		content.add(".");
		content.add(getDirInfoLine(sources, projectDir));
		content.add(getDirInfoLine(tests, projectDir));
		content.add(getDirInfoLine(documents, projectDir));
		content.add(getDirInfoLine(modules, projectDir));

		Object[] array = content.toArray(new String[0]);
		return MessageFormat.format(promptBundle.getString("project_information"), array);
	}

	private String getDirInfoLine(List<String> sources, File projectDir) {
		String line = null;
		if (sources != null && !sources.isEmpty()) {
			List<String> dirs = sources.stream().filter(t -> t != null && new File(projectDir, t).exists())
					.map(e -> "`" + e + "`").collect(Collectors.toList());
			line = StringUtils.join(dirs, ", ");
		}

		if (StringUtils.isBlank(line)) {
			line = "not defined";
		}
		return line;
	}

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

	private Reviewer getReviewerForExtension(String extension) {
		String key = normalizeExtensionKey(extension);
		if (key == null) {
			return null;
		}
		return reviewMap.get(key);
	}

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
			String path = ProjectLayout.getRelatedPath(projectDir, file);

			if (Strings.CI.containsAny(path, ProjectLayout.EXCLUDE_DIRS) || shouldExcludePath(Path.of(path))) {
				continue;
			}

			if (matcher == null || matcher.matches(file.toPath())) {
				result.add(file);
			}
		}

		return result;
	}

	private boolean isPathPattern(String pattern) {
		return Strings.CI.startsWithAny(pattern, "glob:", "regex:");
	}

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
			Path relatedPath = Path.of(ProjectLayout.getRelatedPath(rootDir, file));

			if (Strings.CI.equalsAny(name, ProjectLayout.EXCLUDE_DIRS) || shouldExcludePath(relatedPath)) {
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

	private boolean shouldExcludePath(Path path) {
		if (excludes == null || excludes.length == 0) {
			return false;
		}

		for (String exclude : excludes) {
			PathMatcher matcher = getPatternPath(exclude);
			if (matcher != null) {
				if (path != null && matcher.matches(path)) {
					return true;
				}
				continue;
			}

			if (path != null && Strings.CS.equals(path.toString(), exclude)) {
				return true;
			}
		}

		return false;
	}

	private static int pathDepth(String path) {
		if (path == null || path.isBlank()) {
			return 0;
		}
		String normalized = path.replace("\\", "/");
		return normalized.split("/").length;
	}

	private PathMatcher getPatternPath(String path) {
		if (StringUtils.isBlank(path)) {
			return null;
		}
		if (isPathPattern(path)) {
			return FileSystems.getDefault().getPathMatcher(path);
		}

		return null;
	}

	/**
	 * @return root directory used as a base for relative paths
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
	 * @return whether module processing is executed concurrently
	 */
	public boolean isModuleMultiThread() {
		return moduleMultiThread;
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
	 * <li>Lines starting with {@code http://} or {@code https://} are treated as
	 * URLs and the referenced content is included.</li>
	 * <li>Lines starting with {@code file:} are treated as file references and the
	 * referenced content is included.</li>
	 * <li>All other lines are included as-is.</li>
	 * </ul>
	 *
	 * @param instructions instructions input (plain text, URL, or {@code file:})
	 */
	public void setInstructions(String instructions) {
		this.instructions = parseLines(instructions);
	}

	/**
	 * @return whether composed prompt inputs are logged to files
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
	 * @return whether recursion into modules/subdirectories is disabled
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
	 * <li>Lines starting with {@code http://} or {@code https://} are treated as
	 * URLs and the referenced content is included.</li>
	 * <li>Lines starting with {@code file:} are treated as file references and the
	 * referenced content is included.</li>
	 * <li>All other lines are included as-is.</li>
	 * </ul>
	 *
	 * @param defaultGuidance default guidance input (plain text, URL, or
	 *                        {@code file:})
	 */
	public void setDefaultGuidance(String defaultGuidance) {
		this.defaultGuidance = parseLines(defaultGuidance);
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
		data.lines().forEach(line -> {
			String normalizedLine = StringUtils.stripToNull(line);
			if (normalizedLine == null) {
				sb.append(System.lineSeparator());
				return;
			}

			String content = tryToGetInstructionsFromFile(normalizedLine);
			if (content != null) {
				sb.append(content);
			}
			sb.append(System.lineSeparator());
		});

		return sb.toString();
	}

	/**
	 * @return excludes configured for this processor
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
	 * @return maximum number of worker threads used for module processing
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
	 * @return timeout (in minutes) to wait for module processing completion during
	 *         shutdown
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
	 * <li>If {@code data} starts with {@code http://} or {@code https://}, reads
	 * content from the specified URL.</li>
	 * <li>If {@code data} starts with {@code file:}, reads content from the
	 * specified file path.</li>
	 * <li>Otherwise, returns {@code data}.</li>
	 * </ul>
	 *
	 * @param data input string (URL, {@code file:} reference, or plain text)
	 * @return content read from the URL/file, or the original input
	 */
	private static String tryToGetInstructionsFromFile(String data) {
		if (data == null) {
			return null;
		}

		try {
			if (data.startsWith("http://") || data.startsWith("https://")) {
				return readFromHttpUrl(data);
			}

			String trimmed = data.trim();
			if (Strings.CS.startsWith(trimmed, "file:")) {
				String filePath = StringUtils.substringAfter(trimmed, "file:");
				return readFromFilePath(filePath);
			}

			return data;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL: " + data, e);
		}
	}

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

	private static String readFromFilePath(String filePath) {
		Path path = Path.of(filePath);
		if (!Files.exists(path)) {
			throw new IllegalArgumentException("File not found: " + path.toAbsolutePath());
		}

		try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			String result = IOUtils.toString(reader);
			logger.info("Included: `{}`", filePath);
			return result;
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read file: " + path.toAbsolutePath(), e);
		}
	}
}
