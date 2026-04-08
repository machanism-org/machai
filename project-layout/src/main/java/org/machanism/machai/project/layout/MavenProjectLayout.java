package org.machanism.machai.project.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.gradle.internal.impldep.javax.annotation.Nullable;

/**
 * A Maven-specific {@link ProjectLayout} implementation.
 * <p>
 * This layout reads Maven build metadata from <code>pom.xml</code> to
 * determine:
 * </p>
 * <ul>
 * <li>modules for multi-module projects (when {@code packaging=pom})</li>
 * <li>source and resource directories</li>
 * <li>test source and resource directories</li>
 * <li>documentation inputs (defaults to <code>src/site</code>)</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see PomReader
 */
public class MavenProjectLayout extends ProjectLayout {

	private static final String PROJECT_MODEL_FILE_NAME = "pom.xml";

	private Model model;

	/**
	 * Checks whether the given directory appears to be a Maven project.
	 *
	 * @param projectDir directory to check
	 * @return {@code true} if a <code>pom.xml</code> file exists in the directory;
	 *         {@code false} otherwise
	 */
	public static boolean isMavenProject(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	/**
	 * Returns a list of modules for multi-module Maven projects.
	 * <p>
	 * A project is treated as multi-module when its {@code packaging} is
	 * {@code pom}.
	 * </p>
	 *
	 * @return list of module directories (as declared in <code>pom.xml</code>), or
	 *         null if the project is not a parent.
	 */
	@Override
	@Nullable
	@SuppressWarnings("java:S1168")
	public List<String> getModules() {
		Model mavenModel = getModel();
		if (mavenModel != null && "pom".equals(mavenModel.getPackaging())) {
			return mavenModel.getModules();
		}
		return null;
	}

	/**
	 * Returns the parsed Maven model for the configured project directory.
	 *
	 * @return Maven model
	 */
	public Model getModel() {
		if (model == null) {
			File projectDir = getProjectDir();
			File pomFile = new File(projectDir, PROJECT_MODEL_FILE_NAME);

			model = new PomReader().getProjectModel(pomFile);
		}

		return model;
	}

	/**
	 * Sets the Maven model directly.
	 *
	 * @param model Maven model
	 * @return this instance for chaining
	 */
	public MavenProjectLayout model(Model model) {
		this.model = model;
		return this;
	}

	/**
	 * Returns a list of source directories for the Maven project.
	 * <p>
	 * The method inspects the Maven {@code build} section. When source/test source
	 * directories are not defined, it applies Maven defaults.
	 * </p>
	 *
	 * @return list of source and resource directories, expressed as paths relative
	 *         to the configured project root
	 */
	@Override
	public Set<String> getSources() {
		Set<String> sources = new HashSet<>();

		Model mavenModel = getModel();
		Build build = mavenModel.getBuild();
		if (build == null) {
			build = new Build();
			mavenModel.setBuild(build);
		}

		if (build.getSourceDirectory() == null) {
			build.setSourceDirectory(new File(getProjectDir(), "src/main/java").getAbsolutePath());
		}
		if (build.getTestSourceDirectory() == null) {
			build.setTestSourceDirectory(new File(getProjectDir(), "src/test/java").getAbsolutePath());
		}

		String sourceDirectory = build.getSourceDirectory();
		if (sourceDirectory != null) {
			String relativePath = ProjectLayout.getRelativePath(getProjectDir(), new File(sourceDirectory));
			sources.add(relativePath);
		}
		if (build.getResources() != null) {
			sources.addAll(build.getResources().stream().map(org.apache.maven.model.FileSet::getDirectory)
					.map(name -> {
						File file = new File(name);
						return file.isAbsolute() ? ProjectLayout.getRelativePath(getProjectDir(), file) : name;
					})
					.collect(Collectors.toList()));
		}
		return sources;
	}

	/**
	 * Returns documentation source directories for Maven projects.
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
	 * Returns a list of test directories for the Maven project.
	 *
	 * @return list of test source and test resource directories, expressed as paths
	 *         relative to the configured project root
	 */
	@Override
	public List<String> getTests() {
		List<String> testPaths = new ArrayList<>();
		Model mavenModel = getModel();
		Build build = mavenModel.getBuild();
		if (build != null) {
			if (build.getTestSourceDirectory() != null) {
				testPaths.add(ProjectLayout.getRelativePath(getProjectDir(), new File(build.getTestSourceDirectory())));
			}
			if (build.getTestResources() != null) {
				testPaths.addAll(build.getTestResources().stream().map(org.apache.maven.model.FileSet::getDirectory)
						.map(p -> ProjectLayout.getRelativePath(getProjectDir(), new File(p)))
						.collect(Collectors.toList()));
			}
		}
		return testPaths;
	}

	/**
	 * Sets the project directory and narrows the return type for fluent usage.
	 *
	 * @param projectDir project root directory
	 * @return this layout instance
	 */
	@Override
	public MavenProjectLayout projectDir(File projectDir) {
		return (MavenProjectLayout) super.projectDir(projectDir);
	}

	/**
	 * Returns the Maven artifactId as the project identifier.
	 *
	 * @return artifactId
	 */
	@Override
	public String getProjectId() {
		Model mavenModel = getModel();
		return mavenModel.getArtifactId();
	}

	/**
	 * Returns the Maven project name.
	 *
	 * @return name or {@code null} if not defined
	 */
	@Override
	public String getProjectName() {
		return getModel().getName();
	}

	/**
	 * Returns the parent artifactId, if a parent is configured.
	 *
	 * @return parent artifactId or {@code null}
	 */
	@Override
	public String getParentId() {
		Parent parent = getModel().getParent();
		return parent != null ? parent.getArtifactId() : null;
	}
}
