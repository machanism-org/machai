package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBIndexBuilder extends BIndexBuilder {

	private static Logger logger = LoggerFactory.getLogger(MavenBIndexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("maven_project_prompts");

	private static final String PROJECT_MODEL_FILE_NAME = "pom.xml";

	private Model model;
	private boolean effectivePomRequired;

	protected void projectContext() throws IOException {
		if (model == null) {
			model = PomReader.getProjectModel(new File(getProjectDir(), PROJECT_MODEL_FILE_NAME));
		}

		String sourceDirectory = model.getBuild().getSourceDirectory();
		removeNotImportantData(model);

		String pom = PomReader.printModel(model);
		String prompt = MessageFormat.format(promptBundle.getString("pom_resource_section"), pom);
		getProvider().prompt(prompt);

		Path startPath = Paths.get(
				StringUtils.defaultIfEmpty(sourceDirectory,
						new File(getProjectDir(), "src/main/java").getAbsolutePath()));

		if (Files.exists(startPath)) {
			Files.walk(startPath).filter(Files::isRegularFile).forEach((f) -> {
				try {
					getProvider().promptFile("source_resource_section", f.toFile());
				} catch (IOException e) {
					logger.warn("File: {} adding failed.", f);
				}
			});
		}

		prompt = promptBundle.getString("additional_rules");
		getProvider().prompt(prompt);
	}

	@Override
	public BIndex build() throws IOException {
		BIndex bindex = super.build();
		if (bindex != null) {
			bindex.setId(model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion());
			bindex.setName(model.getName());
		}
		return bindex;
	}

	private void removeNotImportantData(Model model) {
		model.setDistributionManagement(null);

		model.setDistributionManagement(null);
		model.setProperties(null);
		model.setDependencyManagement(null);
		model.setBuild(null);
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
		Model model = PomReader.getProjectModel(pomFile, false);

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
}
