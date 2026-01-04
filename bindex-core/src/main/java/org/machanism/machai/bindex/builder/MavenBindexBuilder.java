package org.machanism.machai.bindex.builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.PomReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBindexBuilder extends BindexBuilder {

	private static Logger logger = LoggerFactory.getLogger(MavenBindexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("maven_project_prompts");

	private MavenProjectLayout projectLayout;

	public MavenBindexBuilder(MavenProjectLayout projectLayout) {
		super(projectLayout);
		this.projectLayout = projectLayout;
	}

	public void projectContext() throws IOException {
		Build build = projectLayout.getModel().getBuild();
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

		Model model = projectLayout.getModel();
		removeNotImportantData(model);

		String pom = PomReader.printModel(model);
		String prompt = MessageFormat.format(promptBundle.getString("pom_resource_section"), pom);
		getGenAIProvider().prompt(prompt);

		prompt = promptBundle.getString("additional_rules");
		getGenAIProvider().prompt(prompt);
	}

	private void addResources(String sourceDirectory) throws IOException {
		if (StringUtils.isNotBlank(sourceDirectory)) {
			Path startPath = Paths.get(sourceDirectory);

			if (Files.exists(startPath)) {
				Files.walk(startPath).filter(Files::isRegularFile).forEach((f) -> {
					try {
						getGenAIProvider().promptFile(f.toFile(), "source_resource_section");
					} catch (IOException e) {
						logger.warn("File: {} adding failed.", f);
					}
				});
			}
		}
	}

	void removeNotImportantData(Model model) {
		model.setDistributionManagement(null);

		model.setDistributionManagement(null);
		model.setBuild(null);
		model.setProperties(null);
		model.setDependencyManagement(null);
		model.setReporting(null);
		model.setScm(null);
		model.setPluginRepositories(null);
	}

}
