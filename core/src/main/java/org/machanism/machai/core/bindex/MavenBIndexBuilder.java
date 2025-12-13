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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBIndexBuilder extends BIndexBuilder {

	private static Logger logger = LoggerFactory.getLogger(MavenBIndexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("maven_project_prompts");

	private static final String PROJECT_MODEL_FILE_NAME = "pom.xml";

	protected void projectContext() throws IOException {
		Model model = PomReader.getProjectModel(new File(getProjectDir(), PROJECT_MODEL_FILE_NAME));

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
					logger.warn("File: " + f + " adding failed.");
				}
			});
		}
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
				model = PomReader.getProjectModel(pomFile, true);
			} catch (Exception e) {
				// ignore.
			}
			modules = model.getModules();
		}

		return modules;
	}

}
