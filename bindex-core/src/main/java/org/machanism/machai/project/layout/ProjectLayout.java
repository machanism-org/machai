package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Abstract base class for project layout structures.
 * <p>
 * Defines a common interface and directory exclusion strategy for child layouts like Maven, JS/TS, Python, etc. Provides relative path helpers for consistent path management.
 *
 *
 * <p>Example usage:
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new File("/workspace"));
 * List&lt;String&gt; sources = layout.getSources();
 * </pre>
 *
 * @author machai
 * @since 1.0
 */
public abstract class ProjectLayout {

    /**
     * Standard set of excluded directories for project structure analysis.
     */
    public static final String[] EXCLUDE_DIRS = { "node_modules", ".git", ".nx", ".svn", ".machai", "target", "build",
            ".venv", "__", ".pytest_cache", ".idea", ".egg-info", ".classpath", ".settings", "logs", ".settings",
            ".project", ".m2" };

    private File projectDir;

    /**
     * Sets the current project directory (used by derived layout implementations).
     * @param projectDir the file root for the project
     * @return this layout object (for chaining)
     */
    public ProjectLayout projectDir(File projectDir) {
        this.projectDir = projectDir;
        return this;
    }

    /**
     * Gets the current project directory.
     * @return project's root directory as File
     */
    public File getProjectDir() {
        return projectDir;
    }

    /**
     * Gets a list of project modules.
     * @return list of module names or paths
     * @throws IOException when directory scan fails (for implementations that require IO)
     */
    public List<String> getModules() throws IOException {
        return null;
    };

    /**
     * Computes relative path from current directory to target file.
     * @param currentPath Absolute path string
     * @param file Target file
     * @return Relative path (string)
     */
    public String getRelatedPath(String currentPath, File file) {
        String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
        if (StringUtils.startsWith(relativePath, "/")) {
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
     * Computes relative path from project directory to target file.
     * @param dir Project directory
     * @param file Target file
     * @return Relative path string
     */
    public static String getRelatedPath(File dir, File file) {
        return getRelatedPath(dir, file, false);
    }

    /**
     * Computes relative path, optionally prepending single dot.
     * @param dir Project directory
     * @param file Target file
     * @param addSingleDot If true, prefix with "./" if needed
     * @return Relative path string
     */
    public static String getRelatedPath(File dir, File file, boolean addSingleDot) {
        String currentPath = dir.getAbsolutePath().replace("\\", "/");
        String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
        if (StringUtils.startsWith(relativePath, "/")) {
            relativePath = StringUtils.substring(relativePath, 1);
        }
        String result = StringUtils.defaultIfBlank(relativePath, ".");
        if (StringUtils.isBlank(result)) {
            result = ".";
        } else if (!StringUtils.startsWith(result, ".") && addSingleDot) {
            result = "./" + result;
        }

        return result;
    }

}
