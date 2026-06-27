package org.machanism.machai.project.layout;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.gradle.internal.impldep.javax.annotation.Nullable;
import org.machanism.machai.project.ProjectProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstraction for describing a project's conventional on-disk layout.
 *
 * <p>
 * A {@code ProjectLayout} implementation is responsible for translating build
 * tool conventions and/or build metadata into a set of root-relative path, such
 * as source roots, test roots, documentation roots, and (optionally) module
 * directories.
 * </p>
 *
 * <p>
 * Implementations are expected to be configured with a project root via
 * {@link #projectDir(File)} prior to calling any accessors.
 * </p>
 *
 * <h2>Root-relative path</h2>
 * <p>
 * Path returned from this API are typically expressed as root-relative strings
 * using {@code /} as a separator. Callers should resolve them against
 * {@link #getProjectDir()} before accessing the filesystem.
 * </p>
 *
 * <h2>Example</h2>
 * 
 * <pre>
 * <code>
 * java.io.File projectDir = new java.io.File("C:\\repo");
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);
 *
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * </code>
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public abstract class ProjectLayout {

	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(ProjectLayout.class);

	/**
	 * Directory names that should be ignored when scanning projects.
	 */
	private static final String[] EXCLUDE_DIRS = { "node_modules", ".git", ".nx", ".svn",
			ProjectProcessor.MACHAI_TEMP_DIR, "target", "build", ".venv", "__", ".pytest_cache", ".idea", ".egg-info",
			".classpath", ".settings", ".settings", ".project", ".m2", ".machai", "bin" };

	private static String tempDir;

	private File projectDir;

	/**
	 * Sets the project root directory used by this layout.
	 *
	 * @param projectDir the project root directory
	 * @return this instance for chaining
	 */
	public ProjectLayout projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

	/**
	 * Returns the configured project root directory.
	 *
	 * @return the project root directory
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Returns a list of module directories (or names) within this project or null
	 * for non-parent project.
	 */
	@Nullable
	@SuppressWarnings("java:S1168")
	public List<String> getModules() {
		return null;
	}

	/**
	 * Computes a root-relative path for a file, based on the provided base path.
	 *
	 * @param basePath absolute path of the base directory
	 * @param file     target file
	 * @return the path of {@code file} relative to {@code basePath}
	 */
	public String getRelativePath(String basePath, File file) {
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(basePath.replace("\\", "/"), "");
		if (Strings.CS.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}

	/**
	 * Returns the root-relative source directories for production code.
	 *
	 * @return list of root-relative source directories
	 */
	public abstract Collection<String> getSources();

	/**
	 * Returns the root-relative documentation directories.
	 *
	 * @return list of root-relative documentation directories
	 */
	public abstract Collection<String> getDocuments();

	/**
	 * Returns the root-relative source directories for test code.
	 *
	 * @return list of root-relative test source directories
	 */
	public abstract Collection<String> getTests();

	/**
	 * Computes the relative path from the specified project directory to the target
	 * file. The result is not prefixed with {@code ./}.
	 *
	 * @param dir  the base project directory
	 * @param file the target file for which to compute the relative path
	 * @return the relative path string, or {@code null} if the target file is not
	 *         within the project directory
	 * @see #getRelativePath(File, File, boolean)
	 */
	public static String getRelativePath(File dir, File file) {
		return getRelativePath(dir, file, false);
	}

	/**
	 * Computes the relative path from the specified project directory to the target
	 * file. Optionally, the result can be prefixed with {@code ./} if
	 * {@code addSingleDot} is {@code true}.
	 *
	 * <p>
	 * If the target file is the same as the project directory, returns {@code .}.
	 * If an absolute path is provided, it must be located within the project
	 * directory.
	 * </p>
	 *
	 * @param dir          the base project directory
	 * @param file         the target file for which to compute the relative path
	 * @param addSingleDot if {@code true}, prefixes the result with {@code ./} when
	 *                     appropriate
	 * @return the relative path string, or {@code null} if the target file is not
	 *         within the project directory
	 */
	public static String getRelativePath(File dir, File file, boolean addSingleDot) {
		String result = null;
		String relativePath = dir.toURI().relativize(file.toURI()).getPath();
		if (!new File(relativePath).isAbsolute()) {
			result = StringUtils.defaultIfBlank(relativePath, ".");
			if (StringUtils.isBlank(result)) {
				result = ".";
			} else if (!Strings.CS.startsWith(result, ".") && addSingleDot) {
				result = "./" + result;
			}

			if (Strings.CS.endsWith(result, "/")) {
				result = result.substring(0, result.length() - 1);
			}
		}
		return result;
	}

	/**
	 * Recursively lists all files under a directory, excluding known build/tooling
	 * directories.
	 *
	 * @param projectDir directory to traverse
	 * @return files found; never {@code null}
	 */
	public static List<File> listFiles(File dir) {
		List<File> fileList = new ArrayList<>();
		if (dir != null && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						fileList.addAll(listFiles(file));
					} else {
						fileList.add(file);
					}
				}
			}
		}
		return fileList;
	}

	/**
	 * Recursively lists all directories, excluding known build/tooling directories.
	 *
	 * @param projectDir directory to traverse
	 * @return directories found; never {@code null}
	 */
	public static List<File> listDirectories(File projectDir) {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
		}

		File[] files = projectDir.listFiles();

		List<File> result = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory() && !Strings.CS.startsWithAny(file.getName(), EXCLUDE_DIRS)) {
					result.add(file);
					result.addAll(listDirectories(file));
				}
			}
		}

		return result;
	}

	/**
	 * Returns a human-friendly project name, when available.
	 *
	 * @return the project name or {@code null} if unknown
	 */
	public String getProjectName() {
		return null;
	}

	/**
	 * Returns a stable project identifier, when available.
	 *
	 * @return the project identifier or {@code null} if unknown
	 */
	public String getProjectId() {
		return null;
	}

	/**
	 * Returns the layout type name (derived from the implementing class name).
	 *
	 * @return a short layout type name
	 */
	public String getProjectLayoutType() {
		return getClass().getSimpleName().replace(ProjectLayout.class.getSimpleName(), "");
	}

	/**
	 * Returns the parent project identifier, when available.
	 *
	 * @return parent project identifier or {@code null} if unknown
	 */
	public String getParentId() {
		return null;
	}

	/**
	 * Returns a copy of the exclude directories array to prevent external
	 * modification.
	 *
	 * @return a copy of the exclude directories array
	 */
	public static String[] getExcludeDirs() {
		return EXCLUDE_DIRS.clone();
	}

	public static boolean isExcludedPath(String path) {
		for (String exclude : getExcludeDirs()) {
			if (path.equals(exclude)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the system temporary directory path, initializing it if necessary.
	 * <p>
	 * If the temporary directory has not been set, this method retrieves the value
	 * of the {@code java.io.tmpdir} system property, logs the initialization, and
	 * caches the result for future calls.
	 * </p>
	 *
	 * @return the absolute path to the system temporary directory
	 */
	public static String getTempDir() {
		if (tempDir == null) {
			tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "/.machai").toString();
			logger.info("Temporary directory initialized: '{}'", tempDir);
		}
		return tempDir;
	}
}
