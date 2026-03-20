package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Base implementation for processors that traverse a project directory and
 * perform work on files and folders.
 *
 * <p>
 * {@code AbstractFileProcessor} provides common functionality used by the
 * Ghostwriter CLI such as:
 * </p>
 * <ul>
 * <li>collecting files under a project directory while excluding common build
 * and tooling folders,</li>
 * <li>supporting optional include matching via {@link PathMatcher} and optional
 * exclusion patterns, and</li>
 * <li>delegating per-file work to subclasses via
 * {@link #processFile(ProjectLayout, File)}.</li>
 * </ul>
 *
 * <p>
 * This class does not perform dependency resolution or builds; it operates on
 * the filesystem only.
 * </p>
 */
public abstract class AbstractFileProcessor extends ProjectProcessor {

	/** Prefix for project-layout properties exposed for template substitution. */
	protected static final String GW_PROJECT_LAYOUT_PROP_PREFIX = "project.";

	/** Root scanning directory for the current documentation run. */
	private File rootDir;

	/**
	 * Specifies a special scanning path or path pattern. This should be a relative
	 * path with respect to the current processing project. If an absolute path is
	 * provided, it must be located within the {@code rootDir}.
	 */
	private File scanDir;

	/** Whether module processing is executed concurrently. */
	private int degreeOfConcurrency;

	/** Whether module discovery/recursion is disabled for the current run. */
	private boolean nonRecursive;

	/** Optional matcher used to limit module/file processing to a subset. */
	private PathMatcher pathMatcher;

	/** Optional list of path patterns or exact paths to exclude. */
	private String[] excludes;

	/** Configuration source used to initialize providers. */
	private final Configurator configurator;

	/** Timeout for module processing worker pool shutdown. */
	private long moduleThreadTimeoutMinutes = 60;

	/**
	 * Creates a new file processor.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param configurator configuration source used by implementations
	 */
	protected AbstractFileProcessor(File rootDir, Configurator configurator) {
		super();
		this.rootDir = rootDir;
		this.configurator = configurator;
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
				if (degreeOfConcurrency > 1) {
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
	void processModulesMultiThreaded(File projectDir, List<String> modules) {
		ExecutorService executor = Executors.newFixedThreadPool(degreeOfConcurrency);
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
					if (cause instanceof IOException) {
						throw new IllegalStateException("Module processing failed.", cause);
					}
					if (cause instanceof RuntimeException) {
						throw (RuntimeException) cause;
					}
					throw new IllegalStateException("Module processing failed.", cause);
				}
			}
		} finally {
			shutdownExecutor(executor);
		}
	}

	/**
	 * Shuts down an ExecutorService safely without throwing from a finally block.
	 * 
	 * <p>
	 * Sonar java:S1143/java:S1163 - do not throw from finally blocks; preserve the
	 * thread interruption status.
	 * </p>
	 *
	 * @param executor executor to stop (may be {@code null})
	 */
	void shutdownExecutor(ExecutorService executor) {
		if (executor == null) {
			return;
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(getModuleThreadTimeoutMinutes(), TimeUnit.MINUTES)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Checks whether {@code dir} is one of the project module directories.
	 *
	 * @param projectLayout layout containing module definitions
	 * @param dir           directory candidate
	 * @return {@code true} if {@code dir} is a module directory, otherwise
	 *         {@code false}
	 */
	public static boolean isModuleDir(ProjectLayout projectLayout, File dir) {
		List<String> modules = projectLayout.getModules();
		if (modules == null || modules.isEmpty() || dir == null) {
			return false;
		}

		String relativePath = ProjectLayout.getRelativePath(projectLayout.getProjectDir(), dir);

		return relativePath != null && Strings.CI.startsWithAny(relativePath, modules.toArray(new String[0]));
	}

	/**
	 * Determines whether the specified file should be included for processing based
	 * on exclusion rules, path matching patterns, and project structure.
	 *
	 * <p>
	 * The matching logic proceeds as follows:
	 * </p>
	 * <ol>
	 * <li>If the {@code file} is {@code null}, returns {@code false}.</li>
	 * <li>If the file's absolute path contains any of the excluded directory names
	 * defined in {@code ProjectLayout.EXCLUDE_DIRS}, returns {@code false}.</li>
	 * <li>Computes the relative path from {@code projectDir} to {@code file}. If
	 * this is {@code null}, returns {@code false}.</li>
	 * <li>Uses {@code pathMatcher} to check if the relative path matches the
	 * configured pattern.</li>
	 * <li>If it does not match and {@code scanDir} is not {@code null}, performs a
	 * secondary match that attempts to resolve the scan directory against the file
	 * and re-check from the project root.</li>
	 * </ol>
	 *
	 * @param file       the file to check for inclusion
	 * @param projectDir the root directory of the project
	 * @return {@code true} if the file matches all criteria for processing;
	 *         {@code false} otherwise
	 */
	protected boolean match(File file, File projectDir) {
		if (file == null) {
			return false;
		}

		if (Strings.CI.containsAny(file.getAbsolutePath(), ProjectLayout.getExcludeDirs())) {
			return false;
		}

		String relativeProjectDir = ProjectLayout.getRelativePath(getRootDir(), projectDir);
		String relativeScanDir = ProjectLayout.getRelativePath(projectDir, file);

		if (relativeProjectDir == null || relativeScanDir == null) {
			return false;
		}

		return matchPath(projectDir, file, relativeProjectDir, relativeScanDir);
	}

	boolean matchPath(File projectDir, File file, String relativeProjectDir, String relativeScanDir) {
		String relativeScanPart = ".".equals(relativeScanDir) ? "" : File.separator + relativeScanDir;
		String path = relativeProjectDir.isEmpty() ? relativeScanDir : relativeProjectDir + relativeScanPart;

		if (pathMatcher == null) {
			return scanDir != null && Strings.CS.equals(scanDir.getAbsolutePath(), file.getAbsolutePath());
		}

		Path pathToMatch = new File(path).toPath();
		boolean result = pathMatcher.matches(pathToMatch) || pathMatcher.matches(new File(relativeScanDir).toPath());
		if (result || scanDir == null) {
			return result;
		}

		String relativePath = ProjectLayout.getRelativePath(scanDir, file);
		if (relativePath == null) {
			return false;
		}

		Path scanFilePath = scanDir.toPath().resolve(relativePath);
		String relatedToRoot = ProjectLayout.getRelativePath(projectDir, scanFilePath.toFile());
		return relatedToRoot != null && pathMatcher.matches(new File(relatedToRoot).toPath());
	}

	/**
	 * Processes non-module files and directories directly under {@code projectDir}.
	 *
	 * @param projectLayout project layout
	 * @throws FileNotFoundException if the project layout cannot be created
	 * @throws IOException           if file reading fails
	 */
	protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
	}

	/**
	 * Extracts guidance for a file and, when present, performs provider processing.
	 *
	 * @param projectLayout project layout
	 * @param file          file to process
	 * @throws IOException if reading the file or provider execution fails
	 */
	protected void processFile(ProjectLayout projectLayout, File file) throws IOException {
	}

	/**
	 * Recursively lists all files under a directory, excluding known build/tooling
	 * directories.
	 *
	 * @param projectDir directory to traverse
	 * @return files found
	 * @throws IOException if directory listing fails
	 */
	List<File> findFiles(File projectDir) throws IOException {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
		}

		File[] files = projectDir.listFiles();
		if (files == null) {
			throw new IOException("Unable to list files for directory: " + projectDir.getAbsolutePath());
		}

		List<File> result = new ArrayList<>();
		for (File file : files) {
			if (!shouldIncludeInFindFiles(projectDir, file)) {
				continue;
			}

			result.add(file);
			if (file.isDirectory()) {
				result.addAll(findFiles(file));
			}
		}

		result.sort(Comparator.comparingInt((File f) -> pathDepth(f.getPath())).reversed());
		return result;
	}

	/**
	 * Determines whether an entry found in {@link #findFiles(File)} should be
	 * included.
	 *
	 * <p>
	 * Sonar java:S135 - reduce break/continue statements by isolating filtering.
	 * </p>
	 *
	 * @param projectDir project directory being traversed
	 * @param file       candidate entry
	 * @return {@code true} when the entry should be included
	 */
	boolean shouldIncludeInFindFiles(File projectDir, File file) {
		String name = file.getName();
		String relativePathString = ProjectLayout.getRelativePath(projectDir, file);
		if (relativePathString == null) {
			return false;
		}
		Path relativePath = new File(relativePathString).toPath();

		return !Strings.CI.equalsAny(name, ProjectLayout.getExcludeDirs()) && !shouldExcludePath(relativePath);
	}

	/**
	 * Determines whether a relative path should be excluded according to
	 * {@link #excludes}.
	 *
	 * @param path project-relative path
	 * @return {@code true} when excluded
	 */
	boolean shouldExcludePath(Path path) {
		if (path == null || excludes == null) {
			return false;
		}
		for (String exclude : excludes) {
			if (exclude == null) {
				continue;
			}
			PathMatcher matcher = getPatternPath(exclude);
			if (matcher != null) {
				if (matcher.matches(path)) {
					return true;
				}
			} else {
				String relative = path.toString();
				if (Strings.CS.equals(relative, exclude) || Strings.CS.equals(path.getFileName().toString(), exclude)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Tests whether a scan pattern string is a {@code glob:} or {@code regex:}
	 * matcher.
	 *
	 * @param pattern scan directory argument
	 * @return {@code true} when the pattern uses a path-matcher prefix
	 */
	static boolean isPathPattern(String pattern) {
		return Strings.CI.startsWithAny(pattern, "glob:", "regex:");
	}

	/**
	 * Returns a {@link PathMatcher} when the provided string is a path pattern.
	 *
	 * @param path pattern candidate
	 * @return matcher or {@code null} when {@code path} is not a pattern
	 */
	static PathMatcher getPatternPath(String path) {
		if (StringUtils.isNotBlank(path) && isPathPattern(path)) {
			return FileSystems.getDefault().getPathMatcher(path);
		}

		return null;
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
				processFile(projectLayout, file);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Finds all files/directories in the provided project folder that match a
	 * pattern.
	 *
	 * @param projectDir project root
	 * @param pattern    directory path, {@code glob:} matcher, or {@code regex:}
	 *                   matcher
	 * @return matching files/directories
	 * @throws IOException if directory traversal fails
	 */
	List<File> findFiles(File projectDir, String pattern) throws IOException {
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

			if (Strings.CI.containsAny(path, ProjectLayout.getExcludeDirs()) || shouldExcludePath(new File(path).toPath())) {
				continue;
			}

			if (matcher == null || matcher.matches(file.toPath())) {
				result.add(file);
			}
		}

		return result;
	}

	/**
	 * Computes the depth of a path for sorting.
	 *
	 * @param path input path
	 * @return number of path segments
	 */
	static int pathDepth(String path) {
		if (StringUtils.isBlank(path)) {
			return 0;
		}
		String normalized = path.replace("\\", "/");
		return normalized.split("/").length;
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
				processFile(layout, file);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * @param data {@code true} to enable, {@code false} to disable
	 */
	public void setDegreeOfConcurrency(int data) {
		this.degreeOfConcurrency = data;
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
	 * Returns the root directory used as a base for relative paths.
	 *
	 * @return root directory
	 */
	public File getRootDir() {
		return rootDir;
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
	 * Returns timeout (in minutes) to wait for module processing completion during
	 * shutdown.
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
	 * Sets the scan directory that originated the current match operation.
	 *
	 * @param scanDir scan directory
	 */
	public void setScanDir(File scanDir) {
		this.scanDir = scanDir;
	}

	/**
	 * Sets the path matcher used to include only matching files.
	 *
	 * @param pathMatcher matcher (may be {@code null} to disable matching)
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Returns the scan directory used to derive match semantics.
	 *
	 * @return scan directory or {@code null}
	 */
	public File getScanDir() {
		return scanDir;
	}

	/**
	 * Returns the matcher used to decide whether a file is included.
	 *
	 * @return matcher or {@code null} when matching is disabled
	 */
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	/**
	 * Returns the configuration source for this processor.
	 *
	 * @return configurator
	 */
	public Configurator getConfigurator() {
		return configurator;
	}

}
