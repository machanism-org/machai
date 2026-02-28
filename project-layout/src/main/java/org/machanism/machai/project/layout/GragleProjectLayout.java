package org.machanism.machai.project.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Gradle-specific {@link ProjectLayout} implementation.
 *
 * <p>
 * This layout uses the Gradle Tooling API to load a {@link GradleProject} model and expose a minimal set of layout
 * information:
 * </p>
 * <ul>
 *   <li>module names (based on the Gradle project children)</li>
 *   <li>conventional source roots (defaults to {@code src/main})</li>
 *   <li>conventional test roots (defaults to {@code src/test})</li>
 *   <li>documentation inputs (defaults to {@code src/site})</li>
 * </ul>
 *
 * <p>
 * Note: This implementation does not currently parse custom source sets; it returns conventional directories only.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class GragleProjectLayout extends ProjectLayout {

	private static final Logger logger = LoggerFactory.getLogger(GragleProjectLayout.class);
	private static final String PROJECT_MODEL_FILE_NAME = "build.gradle";

	private GradleProject project;

	/**
	 * Checks whether the given directory appears to be a Gradle project.
	 *
	 * @param projectDir directory to check
	 * @return {@code true} if {@code build.gradle} exists in the directory; {@code false} otherwise
	 */
	public static boolean isGradleProject(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	/**
	 * Returns a list of child module names for multi-project Gradle builds.
	 *
	 * @return list of child project names (root-relative module identifiers), or {@code null} if the build has no children
	 */
	@Override
	public List<String> getModules() {
		List<String> modules = null;

		DomainObjectSet<? extends GradleProject> children = getProject().getChildren();
		if (!children.isEmpty()) {
			modules = children.getAll().stream().map(GradleProject::getName).collect(Collectors.toList());
		}

		return modules;
	}

	/**
	 * Loads (and caches) the Gradle project model for the configured project directory.
	 *
	 * @return Gradle project model
	 */
	private GradleProject getProject() {
		File projectDir = getProjectDir();
		File buildFile = new File(projectDir, PROJECT_MODEL_FILE_NAME);
		if (project == null) {
			try (ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(buildFile.getParentFile())
					.connect()) {
				project = connection.getModel(GradleProject.class);
			} catch (Exception e) {
				logger.warn("Effective model building failed: {}", StringUtils.abbreviate(e.getLocalizedMessage(), 120));
			}
		}
		return project;
	}

	/**
	 * Returns conventional production source roots for Gradle projects.
	 *
	 * @return list containing {@code src/main}
	 */
	@Override
	public List<String> getSources() {
		List<String> sources = new ArrayList<>();
		sources.add("src/main");
		return sources;
	}

	/**
	 * Returns conventional documentation roots for Gradle projects.
	 *
	 * @return list containing {@code src/site}
	 */
	@Override
	public List<String> getDocuments() {
		List<String> docs = new ArrayList<>();
		docs.add("src/site");
		return docs;
	}

	/**
	 * Returns conventional test source roots for Gradle projects.
	 *
	 * @return list containing {@code src/test}
	 */
	@Override
	public List<String> getTests() {
		List<String> sources = new ArrayList<>();
		sources.add("src/test");
		return sources;
	}

	/**
	 * Sets the project directory and narrows the return type for fluent usage.
	 *
	 * @param projectDir project root directory
	 * @return this layout instance
	 */
	@Override
	public GragleProjectLayout projectDir(File projectDir) {
		return (GragleProjectLayout) super.projectDir(projectDir);
	}

	/**
	 * Returns the Gradle project name as a stable identifier.
	 *
	 * @return project name
	 */
	@Override
	public String getProjectId() {
		return getProject().getName();
	}

	/**
	 * Returns the Gradle project name.
	 *
	 * @return project name
	 */
	@Override
	public String getProjectName() {
		return getProject().getName();
	}
}
