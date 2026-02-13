package org.machanism.machai.project.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Maven-specific implementation for project layout.
 * <p>
 * Supports detection of modules, sources, documents, and tests within Maven
 * projects by parsing <code>pom.xml</code> and its effective model.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see PomReader
 */
public class MavenProjectLayout extends ProjectLayout {

	private static Logger logger = LoggerFactory.getLogger(MavenProjectLayout.class);
	private static final String PROJECT_MODEL_FILE_NAME = "pom.xml";

	private Model model;
	private boolean effectivePomRequired = true;

	/**
	 * Checks if the given directory is a Maven project by looking for
	 * <code>pom.xml</code>.
	 *
	 * @param projectDir The directory to check
	 * @return true if <code>pom.xml</code> exists; false otherwise
	 */
	public static boolean isMavenProject(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	/**
	 * Returns a list of modules for Maven projects if packaging is
	 * <code>pom</code>.
	 *
	 * @return List of module names, or {@code null} if not a multi-module Maven
	 *         project
	 * @see PomReader#getProjectModel(File, boolean)
	 */
	@Override
	public List<String> getModules() {
		List<String> modules = null;

		File projectDir = getProjectDir();
		File pomFile = new File(projectDir, PROJECT_MODEL_FILE_NAME);
		if (model == null) {
			try {
				model = new PomReader().getProjectModel(pomFile, effectivePomRequired);
			} catch (Exception e) {
				logger.warn("Effective model building failed: {}",
						StringUtils.abbreviate(e.getLocalizedMessage(), 120));
				logger.debug("Effective model building failed.", e);
			}
		}

		Model model = getModel();
		if ("pom".equals(model.getPackaging())) {
			modules = model.getModules();
		}

		return modules;
	}

	/**
	 * Returns the current Maven Model (parsed <code>pom.xml</code>).
	 *
	 * @return Parsed Maven Model for this project
	 * @see PomReader#getProjectModel(File)
	 */
	public Model getModel() {
		if (model == null) {
			File projectDir = getProjectDir();
			File file = new File(projectDir, PROJECT_MODEL_FILE_NAME);
			try {
				model = new PomReader().getProjectModel(file, effectivePomRequired);
			} catch (Exception e) {
				if (effectivePomRequired) {
					model = new PomReader().getProjectModel(file, false);
				} else {
					throw e;
				}
			}
		}

		return model;
	}

	/**
	 * Sets the Maven Model for chaining configuration.`
	 * 
	 * @param model The model to set
	 * @return this object (for method chaining)
	 */
	public MavenProjectLayout model(Model model) {
		this.model = model;
		return this;
	}

	/**
	 * Enables/disables effective POM calculation for module resolution.
	 * 
	 * @param effectivePomRequired true to enable, false otherwise
	 * @return this object (for method chaining)
	 */
	public MavenProjectLayout effectivePomRequired(boolean effectivePomRequired) {
		this.effectivePomRequired = effectivePomRequired;
		return this;
	}

	/**
	 * Returns a list of source directories by inspecting the Maven build section.
	 *
	 * @return List of source directories defined in Maven <code>pom.xml</code>
	 * 
	 *         <pre>
	 * Example usage:
	 * MavenProjectLayout layout = new MavenProjectLayout();
	 * List<String> sources = layout.projectDir(new File("/repo")).getSources();
	 *         </pre>
	 */
	@Override
	public List<String> getSources() {
		List<String> sources = new ArrayList<>();

		Model model = getModel();
		Build build = model.getBuild();
		if (build == null) {
			build = new Build();
			model.setBuild(build);
		}

		if (build.getSourceDirectory() == null) {
			build.setSourceDirectory(new File(getProjectDir(), "src/main/java").getAbsolutePath());
		}
		if (build.getTestSourceDirectory() == null) {
			build.setTestSourceDirectory(new File(getProjectDir(), "src/test/java").getAbsolutePath());
		}

		String sourceDirectory = build.getSourceDirectory();
		if (sourceDirectory != null) {
			sources.add(ProjectLayout.getRelativePath(getProjectDir(), new File(sourceDirectory)));
		}
		if (build.getResources() != null) {
			sources.addAll(build.getResources().stream().map(r -> r.getDirectory())
					.map(p -> ProjectLayout.getRelativePath(getProjectDir(), new File(p)))
					.collect(Collectors.toList()));
		}
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
		Model model = getModel();
		Build build = model.getBuild();
		if (build != null) {
			if (build.getTestSourceDirectory() != null) {
				sources.add(ProjectLayout.getRelativePath(getProjectDir(), new File(build.getTestSourceDirectory())));
			}
			if (build.getTestResources() != null) {
				sources.addAll(build.getTestResources().stream().map(r -> r.getDirectory())
						.map(p -> ProjectLayout.getRelativePath(getProjectDir(), new File(p)))
						.collect(Collectors.toList()));
			}
		}
		return sources;
	}

	@Override
	public MavenProjectLayout projectDir(File projectDir) {
		return (MavenProjectLayout) super.projectDir(projectDir);
	}

	@Override
	public String getProjectId() {
		Model model = getModel();
		return model.getArtifactId();
	}

	@Override
	public String getProjectName() {
		return getModel().getName();
	}

	public String getParentId() {
		String artifactId = getModel().getParent().getArtifactId();
		return artifactId;
	}
}
