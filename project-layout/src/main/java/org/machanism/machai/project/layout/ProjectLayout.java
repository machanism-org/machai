package org.machanism.machai.project.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.project.ProjectProcessor;

/**
 * Base abstraction for describing a project's conventional on-disk layout.
 *
 * <p>
 * A {@code ProjectLayout} implementation is responsible for translating build tool conventions and/or build metadata
 * into a set of root-relative paths, such as source roots, test roots, documentation roots, and (optionally) module
 * directories.
 * </p>
 *
 * <p>
 * Implementations are expected to be configured with a project root via {@link #projectDir(File)} prior to calling any
 * accessors.
 * </p>
 *
 * <h2>Root-relative paths</h2>
 * <p>
 * Paths returned from this API are typically expressed as root-relative strings using {@code /} as a separator.
 * Callers should resolve them against {@link #getProjectDir()} before accessing the filesystem.
 * </p>
 *
 * <h2>Example</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("C:\\repo");
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);
 *
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * </code></pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public abstract class ProjectLayout {

	/**
	 * Directory names that should be ignored when scanning projects.
	 */
	public static final String[] EXCLUDE_DIRS = { "node_modules", ".git", ".nx", ".svn",
			ProjectProcessor.MACHAI_TEMP_DIR, "target", "build", ".venv", "__", ".pytest_cache", ".idea", ".egg-info",
			".classpath", ".settings", ".settings", ".project", ".m2" };

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
	 * Returns a list of module directories (or names) within this project.
	 *
	 * <p>
	 * Implementations may return {@code null} or an empty list when the project does not define modules.
	 * </p>
	 *
	 * @return module directories (root-relative) or {@code null}
	 */
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
	public abstract List<String> getSources();

	/**
	 * Returns the root-relative documentation directories.
	 *
	 * @return list of root-relative documentation directories
	 */
	public abstract List<String> getDocuments();

	/**
	 * Returns the root-relative source directories for test code.
	 *
	 * @return list of root-relative test source directories
	 */
	public abstract List<String> getTests();

	/**
	 * Computes the relative path from the specified project directory to the target file.
	 * The result is not prefixed with {@code ./}.
	 *
	 * @param dir  the base project directory
	 * @param file the target file for which to compute the relative path
	 * @return the relative path string, or {@code null} if the target file is not within the project directory
	 * @see #getRelativePath(File, File, boolean)
	 */
	public static String getRelativePath(File dir, File file) {
		return getRelativePath(dir, file, false);
	}

	/**
	 * Computes the relative path from the specified project directory to the target file.
	 * Optionally, the result can be prefixed with {@code ./} if {@code addSingleDot} is {@code true}.
	 *
	 * <p>
	 * If the target file is the same as the project directory, returns {@code .}. If an absolute path is provided, it
	 * must be located within the project directory.
	 * </p>
	 *
	 * @param dir          the base project directory
	 * @param file         the target file for which to compute the relative path
	 * @param addSingleDot if {@code true}, prefixes the result with {@code ./} when appropriate
	 * @return the relative path string, or {@code null} if the target file is not within the project directory
	 */
	public static String getRelativePath(File dir, File file, boolean addSingleDot) {
		String currentPath = dir.getAbsolutePath().replace("\\", "/");
		String fileStr = file.getAbsolutePath().replace("\\", "/");
		String relativePath = fileStr.replace(currentPath, "");
		if (Strings.CS.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		String result = StringUtils.defaultIfBlank(relativePath, ".");
		if (StringUtils.isBlank(result)) {
			result = ".";
		} else if (!Strings.CS.startsWith(result, ".") && addSingleDot) {
			result = "./" + result;
		}

		if (Strings.CS.equals(fileStr, result)) {
			result = null;
		}
		return result;
	}

	/**
	 * Recursively lists all files under a directory, excluding known build/tooling directories.
	 *
	 * @param projectDir directory to traverse
	 * @return files found; never {@code null}
	 */
	public static List<File> findFiles(File projectDir) {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
		}

		File[] files = projectDir.listFiles();

		List<File> result = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				if (!Strings.CS.startsWithAny(file.getName(), EXCLUDE_DIRS)) {
					result.add(file);
					if (file.isDirectory()) {
						result.addAll(findFiles(file));
					}
				}
			}
		}

		return result;
	}

	/**
	 * Recursively lists all directories, excluding known build/tooling directories.
	 *
	 * @param projectDir directory to traverse
	 * @return directories found; never {@code null}
	 */
	public static List<File> findDirectories(File projectDir) {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
		}

		File[] files = projectDir.listFiles();

		List<File> result = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory() && !Strings.CS.startsWithAny(file.getName(), EXCLUDE_DIRS)) {
					result.add(file);
					result.addAll(findDirectories(file));
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
		String replace = getClass().getSimpleName().replace(ProjectLayout.class.getSimpleName(), "");
		return replace;
	}

	/**
	 * Returns the parent project identifier, when available.
	 *
	 * @return parent project identifier or {@code null} if unknown
	 */
	public String getParentId() {
		return null;
	}

}
