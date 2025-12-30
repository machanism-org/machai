package org.machanism.machai.bindex.bulder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBIndexBuilder extends BIndexBuilder {

	public MavenBIndexBuilder(boolean callLLM) {
		super(callLLM);
	}

	private static Logger logger = LoggerFactory.getLogger(MavenBIndexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("maven_project_prompts");

	private static final String PROJECT_MODEL_FILE_NAME = "pom.xml";

	private Model model;
	private boolean effectivePomRequired;

	public void projectContext() throws IOException {
		Build build = getModel().getBuild();
		String sourceDirectory = build.getSourceDirectory();
		addResources(sourceDirectory);

		List<Resource> resourcesDirectory = build.getResources();
		if (resourcesDirectory != null) {
			for (Resource resource : resourcesDirectory) {
				addResources(resource.getDirectory());
			}
		}

		List<Resource> testResourcesDirectory = build.getTestResources();
		if (testResourcesDirectory != null) {
			for (Resource resource : testResourcesDirectory) {
				addResources(resource.getDirectory());
			}
		}

		String testSourceDirectory = build.getTestSourceDirectory();
		addResources(testSourceDirectory);

		Model model = getModel();
		removeNotImportantData(model);

		String pom = PomReader.printModel(model);
		String prompt = MessageFormat.format(promptBundle.getString("pom_resource_section"), pom);
		getProvider().prompt(prompt);

		prompt = promptBundle.getString("additional_rules");
		getProvider().prompt(prompt);
	}

	private Model getModel() {
		if (model == null) {
			model = PomReader.getProjectModel(new File(getProjectDir(), PROJECT_MODEL_FILE_NAME));
		}

		return model;
	}

	private void addResources(String sourceDirectory) throws IOException {
		if (StringUtils.isNotBlank(sourceDirectory)) {
			Path startPath = Paths.get(sourceDirectory);

			if (Files.exists(startPath)) {
				Files.walk(startPath).filter(Files::isRegularFile).forEach((f) -> {
					try {
						getProvider().promptFile(f.toFile(), "source_resource_section");
					} catch (IOException e) {
						logger.warn("File: {} adding failed.", f);
					}
				});
			}
		}
	}

	@Override
	public BIndex build() throws IOException {
		BIndex bindex = super.build();
		if (bindex != null) {
			Model model = getModel();
			bindex.setId(model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion());
			bindex.setName(model.getName());
		}
		return bindex;
	}

	private void removeNotImportantData(Model model) {
		model.setDistributionManagement(null);

		model.setDistributionManagement(null);
		model.setBuild(null);
		model.setProperties(null);
		model.setDependencyManagement(null);
		model.setReporting(null);
		model.setScm(null);
		model.setPluginRepositories(null);
	}

	public static boolean isMavenProject(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	@Override
	public List<String> getModules() {
		List<String> modules = null;

		File pomFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		Model model = getModel();

		if ("pom".equals(model.getPackaging())) {
			try {
				model = PomReader.getProjectModel(pomFile, effectivePomRequired);
			} catch (Exception e) {
				logger.warn("Effective model building failed: {}",
						StringUtils.abbreviate(e.getLocalizedMessage(), 120));
			}
			modules = model.getModules();
		}

		return modules;
	}

	public MavenBIndexBuilder model(Model model) {
		this.model = model;
		return this;
	}

	public MavenBIndexBuilder effectivePomRequired(boolean effectivePomRequired) {
		this.effectivePomRequired = effectivePomRequired;
		return this;
	}

	@Override
	public List<String> getSources() {
		List<String> sources = new ArrayList<>();

		Model model = getModel();
		sources.add(ProjectProcessor.getRelatedPath(getProjectDir(), new File(model.getBuild().getSourceDirectory())));
		sources.addAll(
				model.getBuild()
						.getResources()
						.stream()
						.map(r -> r.getDirectory())
						.map(p -> ProjectProcessor.getRelatedPath(getProjectDir(), new File(p)))
						.collect(Collectors.toList()));
		return sources;
	}

	@Override
	public List<String> getDocuments() {
		List<String> docs = new ArrayList<>();
		String sourceDirectory = "src/site/markdown";
		docs.add(sourceDirectory);
		return docs;
	}

	@Override
	public List<String> getTests() {
		List<String> sources = new ArrayList<>();
		Model model = getModel();
		sources.add(
				ProjectProcessor.getRelatedPath(getProjectDir(), new File(model.getBuild().getTestSourceDirectory())));
		sources.addAll(
				model.getBuild()
						.getTestResources()
						.stream()
						.map(r -> r.getDirectory())
						.map(p -> ProjectProcessor.getRelatedPath(getProjectDir(), new File(p)))
						.collect(Collectors.toList()));
		return sources;
	}
}
