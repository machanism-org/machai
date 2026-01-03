package org.machanism.machai.project.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenProjectLayout extends ProjectLayout {

	private static Logger logger = LoggerFactory.getLogger(MavenProjectLayout.class);
	private static final String PROJECT_MODEL_FILE_NAME = "pom.xml";

	private Model model;
	private boolean effectivePomRequired;

	public static boolean isMavenProject(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	@Override
	public List<String> getModules() {
		List<String> modules = null;

		File pomFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		if (model == null) {
			try {
				model = PomReader.getProjectModel(pomFile, effectivePomRequired);
			} catch (Exception e) {
				logger.warn("Effective model building failed: {}",
						StringUtils.abbreviate(e.getLocalizedMessage(), 120));
			}
		}

		Model model = getModel();
		if ("pom".equals(model.getPackaging())) {
			modules = model.getModules();
		}

		return modules;
	}

	public Model getModel() {
		if (model == null) {
			model = PomReader.getProjectModel(new File(getProjectDir(), PROJECT_MODEL_FILE_NAME));
		}

		return model;
	}

	public MavenProjectLayout model(Model model) {
		this.model = model;
		return this;
	}

	public MavenProjectLayout effectivePomRequired(boolean effectivePomRequired) {
		this.effectivePomRequired = effectivePomRequired;
		return this;
	}

	@Override
	public List<String> getSources() {
		List<String> sources = new ArrayList<>();

		Model model = getModel();
		Build build = model.getBuild();
		String sourceDirectory = build.getSourceDirectory();
		if (sourceDirectory != null) {
			sources.add(ProjectLayout.getRelatedPath(getProjectDir(), new File(sourceDirectory)));
		}
		if (build.getResources() != null) {
			sources.addAll(build.getResources().stream().map(r -> r.getDirectory())
					.map(p -> ProjectLayout.getRelatedPath(getProjectDir(), new File(p))).collect(Collectors.toList()));
		}
		return sources;
	}

	@Override
	public List<String> getDocuments() {
		List<String> docs = new ArrayList<>();
		String sourceDirectory = "src/site";
		docs.add(sourceDirectory);
		return docs;
	}

	@Override
	public List<String> getTests() {
		List<String> sources = new ArrayList<>();
		Model model = getModel();
		Build build = model.getBuild();
		if (build.getTestSourceDirectory() != null) {
			sources.add(ProjectLayout.getRelatedPath(getProjectDir(), new File(build.getTestSourceDirectory())));
		}
		if (build.getTestResources() != null) {
			sources.addAll(build.getTestResources().stream().map(r -> r.getDirectory())
					.map(p -> ProjectLayout.getRelatedPath(getProjectDir(), new File(p))).collect(Collectors.toList()));
		}
		return sources;
	}

}
