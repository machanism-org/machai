package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.project.ProjectProcessor;

/**
 * Abstract base class for project layout structures.
 * <p>
 * Defines a common interface and directory exclusion strategy for child layouts
 * like Maven, JS/TS, Python, etc. Provides relative path helpers for consistent
 * path management.
 *
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new File("/workspace"));
 * List<String> sources = layout.getSources();
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public abstract class ProjectLayout {

	/**
	 * Standard set of excluded directories for project structure analysis.
	 */
	public static final String[] EXCLUDE_DIRS = { "node_modules", ".git", ".nx", ".svn",
			ProjectProcessor.MACHAI_TEMP_DIR, "target", "build", ".venv", "__", ".pytest_cache", ".idea", ".egg-info",
			".classpath", ".settings", ".settings", ".project", ".m2" };

	private File projectDir;

	/**
	 * Sets the current project directory (used by derived layout implementations).
	 * 
	 * @param projectDir the file root for the project
	 * @return this layout object (for chaining)
	 */
	public ProjectLayout projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

	/**
	 * Gets the current project directory.
	 * 
	 * @return project's root directory as File
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Gets a list of project modules.
	 * 
	 * @return list of module names or paths
	 */
	public List<String> getModules() {
		return null;
	};

	/**
	 * Computes relative path from current directory to target file.
	 * 
	 * @param basePath Absolute path string
	 * @param file     Target file
	 * @return Relative path (string)
	 */
	public String getRelativePath(String basePath, File file) {
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(basePath.replace("\\", "/"), "");
		if (Strings.CS.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}

	/**
	 * @return List of source directory paths (implementation required)
	 */
	public abstract List<String> getSources();

	/**
	 * @return List of document directory paths (implementation required)
	 */
	public abstract List<String> getDocuments();

	/**
	 * @return List of test directory paths (implementation required)
	 */
	public abstract List<String> getTests();

	/**
	 * Computes the relative path from the specified project directory to the target
	 * file. The result is not prefixed with "./".
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
	 * file. Optionally, the result can be prefixed with "./" if
	 * {@code addSingleDot} is {@code true}.
	 *
	 * <p>
	 * If the target file is the same as the project directory, returns ".". If an
	 * absolute path is provided, it must be located within the project directory.
	 * </p>
	 *
	 * @param dir          the base project directory
	 * @param file         the target file for which to compute the relative path
	 * @param addSingleDot if {@code true}, prefixes the result with "./" when
	 *                     appropriate
	 * @return the relative path string, or {@code null} if the target file is not
	 *         within the project directory
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
	 * Recursively lists all files under a directory, excluding known build/tooling
	 * directories.
	 *
	 * @param projectDir directory to traverse
	 * @return files found
	 * @throws IOException if directory listing fails
	 */
	public static List<File> findFiles(File projectDir) {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
		}

		File[] files = projectDir.listFiles();

		List<File> result = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				result.add(file);
				if (file.isDirectory()) {
					result.addAll(findFiles(file));
				}
			}
		}

		return result;
	}

	public String getProjectName() {
		return null;
	}

	public String getProjectId() {
		return null;
	}

	public String getProjectLayoutType() {
		String replace = getClass().getSimpleName().replace(ProjectLayout.class.getSimpleName(), "");
		return replace;
	}

	public String getParentId() {
		return null;
	}

}
