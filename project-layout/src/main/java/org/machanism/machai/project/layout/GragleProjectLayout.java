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

public class GragleProjectLayout extends ProjectLayout {

	private static Logger logger = LoggerFactory.getLogger(GragleProjectLayout.class);
	private static final String PROJECT_MODEL_FILE_NAME = "build.gradle";

	private GradleProject project;

	public static boolean isGradleProject(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	@Override
	public List<String> getModules() {
		List<String> modules = null;

		DomainObjectSet<? extends GradleProject> children = getProject().getChildren();
		if (!children.isEmpty()) {
			modules = children.getAll().stream().map(c -> c.getName()).collect(Collectors.toList());
		}

		return modules;
	}

	private GradleProject getProject() {
		File projectDir = getProjectDir();
		File buildFile = new File(projectDir, PROJECT_MODEL_FILE_NAME);
		if (project == null) {
			try (ProjectConnection connection = GradleConnector.newConnector()
					.forProjectDirectory(buildFile.getParentFile())
					.connect()) {
				project = connection.getModel(GradleProject.class);

			} catch (Exception e) {
				logger.warn("Effective model building failed: {}",
						StringUtils.abbreviate(e.getLocalizedMessage(), 120));
			}
		}
		return project;
	}

	@Override
	public List<String> getSources() {
		List<String> sources = new ArrayList<>();
		sources.add("src/main");
		return sources;
	}

	/**
	 * Returns a list of documentation sources for Maven projects.
	 *
	 * @return List of documentation sources (default: <code>src/site</code>)
	 */
	@Override
	public List<String> getDocuments() {
		List<String> docs = new ArrayList<>();
		String sourceDirectory = "src/site";
		docs.add(sourceDirectory);
		return docs;
	}

	/**
	 * Returns a list of test directories by inspecting the Maven build section.
	 *
	 * @return List of test source directories defined in Maven <code>pom.xml</code>
	 */
	@Override
	public List<String> getTests() {
		List<String> sources = new ArrayList<>();
		sources.add("src/test");
		return sources;
	}

	@Override
	public GragleProjectLayout projectDir(File projectDir) {
		return (GragleProjectLayout) super.projectDir(projectDir);
	}

	@Override
	public String getProjectId() {
		String name = getProject().getName();
		return name;
	}

	@Override
	public String getProjectName() {
		String name = getProject().getName();
		return name;
	}
}
