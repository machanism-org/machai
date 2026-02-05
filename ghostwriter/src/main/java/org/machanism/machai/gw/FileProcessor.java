package org.machanism.machai.gw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.text.MessageFormat;
import java.util.ArrayList;
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
	private long moduleThreadTimeoutMinutes = 20;

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

		if (!isPathPattern(scanDir)) {
			this.scanDir = new File(scanDir);
			String relatedPath = ProjectLayout.getRelatedPath(rootDir, new File(scanDir));

			scanDir = "glob:" + relatedPath + "{,/**}";
		}

		this.pathMatcher = FileSystems.getDefault().getPathMatcher(scanDir);

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
		boolean result = true;
		String path = ProjectLayout.getRelatedPath(rootDir, file);
		if (Strings.CI.containsAny(file.getAbsolutePath(), ProjectLayout.EXCLUDE_DIRS)) {
			result = false;
		} else if (pathMatcher != null) {
			result = pathMatcher.matches(Path.of(path));

			if (!result && scanDir != null) {
				String relatedPath = ProjectLayout.getRelatedPath(file, scanDir);
				if (relatedPath != null) {
					String normalizedFileAbsolutePath = file.getAbsolutePath().replace("\\", "/");
					String scanPath = normalizedFileAbsolutePath + "/" + relatedPath;
					String relatedToRoot = ProjectLayout.getRelatedPath(rootDir, new File(scanPath));
					result = pathMatcher.matches(Path.of(relatedToRoot));
				}
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

		if (children.isEmpty() && defaultGuidance != null) {
			String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"), projectDir,
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
				logger.info("Processing file: {}", file.getAbsolutePath());
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
		String perform;

		logger.info("Processing file: '{}'", file);

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

		content.add(".");
		content.add(getDirInfoLine(projectLayout.getSources(), projectDir));
		content.add(getDirInfoLine(projectLayout.getTests(), projectDir));
		content.add(getDirInfoLine(projectLayout.getDocuments(), projectDir));
		content.add(getDirInfoLine(projectLayout.getModules(), projectDir));

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
		String result = null;

		if (file.isFile()) {
			String extension = FilenameUtils.getExtension(file.getName());
			Reviewer reviewer = getReviewerForExtension(extension);
			if (reviewer == null) {
				return null;
			}

			result = reviewer.perform(projectDir, file);
		}

		return result;
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
				(List<File>) FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY));

		files.sort(Comparator.comparingInt((File f) -> pathDepth(f.getPath())).reversed());

		for (File file : files) {
			String path = ProjectLayout.getRelatedPath(projectDir, file);

			if (Strings.CI.containsAny(path, ProjectLayout.EXCLUDE_DIRS) || shouldExcludePath(new File(path))) {
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
			File relatedPath = new File(ProjectLayout.getRelatedPath(rootDir, file));

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

	private boolean shouldExcludePath(File path) {
		if (excludes != null) {
			Path pathToMatch = path == null ? null : path.toPath();
			for (String exclude : excludes) {
				PathMatcher matcher = getPatternPath(exclude);
				if (matcher != null) {
					if (pathToMatch != null && matcher.matches(pathToMatch)) {
						return true;
					}
				} else {
					if (path != null && Strings.CS.equals(path.getPath(), exclude)) {
						return true;
					}
				}
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
		PathMatcher matcher = null;
		if (StringUtils.isBlank(path)) {
			return null;
		}
		if (isPathPattern(path)) {
			matcher = FileSystems.getDefault().getPathMatcher(path);
		}

		return matcher;
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
	 * Sets additional instructions appended to each GenAI request.
	 *
	 * @param instructions free-form instruction text
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
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
	 * Loads instruction text from multiple locations.
	 *
	 * <p>
	 * Each item may be:
	 * </p>
	 * <ul>
	 * <li>a URL ({@code http://} or {@code https://})</li>
	 * <li>a file path</li>
	 * <li>raw instruction text (fallback if location cannot be read)</li>
	 * </ul>
	 *
	 * @param instructions locations or raw strings
	 * @throws IOException if reading from a file location fails
	 */
	public void setInstructions(String[] instructions) throws IOException {
		StringBuilder instructionsText = new StringBuilder();
		if (instructions != null && instructions.length > 0) {
			for (String location : instructions) {
				if (!StringUtils.isBlank(location)) {
					String content;
					try {
						if (location.startsWith("http://") || location.startsWith("https://")) {
							try (InputStream in = new URL(location).openStream()) {
								content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
							}
						} else {
							content = Files.readString(new File(location).toPath(), StandardCharsets.UTF_8);
						}
					} catch (Exception e) {
						content = location;
					}

					instructionsText.append(content);
					instructionsText.append(System.lineSeparator());
					instructionsText.append(System.lineSeparator());
				}
			}
		}
		this.instructions = StringUtils.trimToNull(instructionsText.toString());
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
	 * Sets the default guidance that is used when a file contains no embedded
	 * guidance.
	 *
	 * @param defaultGuidance guidance text to apply by default
	 */
	public void setDefaultGuidance(String defaultGuidance) {
		this.defaultGuidance = defaultGuidance;
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
	 * @param maxModuleThreads maximum thread count (values <= 0 are not allowed)
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
}
