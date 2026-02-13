package org.machanism.machai.project.layout;

import java.io.File;
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

	public static String getRelativePath(File dir, File file) {
		return getRelativePath(dir, file, false);
	}

	/**
	 * Computes relative path, optionally prepending single dot.
	 * 
	 * @param dir          Project directory
	 * @param file         Target file
	 * @param addSingleDot If true, prefix with "./" if needed
	 * @return Relative path string
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

	public String getParentProjectId() {
		return null;
	}

}
