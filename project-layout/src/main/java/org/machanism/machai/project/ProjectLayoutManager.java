package org.machanism.machai.project;

import java.io.File;
import java.io.FileNotFoundException;

import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.GradleProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

/**
 * Detects and configures the {@link ProjectLayout} appropriate for a project
 * directory.
 *
 * <p>The detector checks project descriptors in a deterministic order: Maven,
 * Gradle, JavaScript or TypeScript, and Python. A directory without a supported
 * descriptor receives a {@link DefaultProjectLayout}. The returned layout is
 * configured with the supplied directory before it is returned.</p>
 *
 * <p>
 * <h2>Usage</h2>
 * <pre><code>
 * File directory = new File("path/to/project");
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(directory);
 * </code></pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class ProjectLayoutManager {

	/**
	 * Prevents instantiation of this utility class.
	 */
	private ProjectLayoutManager() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Detects the project layout from descriptors in the specified directory and
	 * assigns that directory to the resulting layout.
	 *
	 * @param projectDir the project directory to analyze
	 * @return a configured layout matching Maven, Gradle, JavaScript/TypeScript,
	 *         Python, or the generic filesystem fallback
	 * @throws FileNotFoundException if {@code projectDir} does not exist
	 */
	public static ProjectLayout detectProjectLayout(File projectDir) throws FileNotFoundException {
		ProjectLayout projectLayout;
		if (MavenProjectLayout.isMavenProject(projectDir)) {
			projectLayout = new MavenProjectLayout();
		} else if (GradleProjectLayout.isGradleProject(projectDir)) {
			projectLayout = new GradleProjectLayout();
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
