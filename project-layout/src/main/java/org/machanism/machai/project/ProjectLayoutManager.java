package org.machanism.machai.project;

import java.io.File;
import java.io.FileNotFoundException;

import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

/**
 * Manages detection and instantiation of specific {@link ProjectLayout} implementations
 * based on the structure of a given project directory.
 * <p>
 * This class determines the appropriate project layout for various languages and build tools
 * such as Maven, Node (package.json), and Python, defaulting to a generic layout if necessary.
 *
 * <p>Usage Example:
 * <pre>{@code
 *   File dir = new File("/path/to/project");
 *   ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);
 * }</pre>
 *
 * @author machanism
 * @since 0.0.2
 */
public class ProjectLayoutManager {

    /**
     * Detects the project layout based on the contents of the specified directory.
     * Supports Maven, Node.js (package.json), Python, or a default layout.
     *
     * @param projectDir The directory of the project to analyze.
     * @return The detected {@link ProjectLayout} implementation matching the project type.
     * @throws FileNotFoundException If project directory does not exist.
     */
    public static ProjectLayout detectProjectLayout(File projectDir) throws FileNotFoundException {
        ProjectLayout projectLayout;
        if (MavenProjectLayout.isMavenProject(projectDir)) {
            projectLayout = new MavenProjectLayout();
        } else if (JScriptProjectLayout.isPackageJsonPresent(projectDir)) {
            projectLayout = new JScriptProjectLayout();
        } else if (PythonProjectLayout.isPythonProject(projectDir)) {
            projectLayout = new PythonProjectLayout();
        } else if (projectDir.exists()) {
            projectLayout = new DefaultProjectLayout();
        } else {
            throw new FileNotFoundException(projectDir.getAbsolutePath());
        }

        projectLayout.projectDir(projectDir);
        return projectLayout;
    }

}
