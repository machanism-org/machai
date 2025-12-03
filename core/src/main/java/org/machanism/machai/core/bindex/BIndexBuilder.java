package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BIndexBuilder {

	private File projectDir;
	private GenAIProvider provider;
	private ObjectMapper mapper = new ObjectMapper();
	private File bindexDir;

	public BIndexBuilder provider(GenAIProvider provider) {
		this.provider = provider;
		return this;
	}

	public BIndex build() throws IOException {
		URL systemResource = getClass().getResource("/schema/bindex-schema-v1.json");
		String schema = IOUtils.toString(systemResource, "UTF8");
		provider.prompt("The bindex schema https://machanism.org/machai/schema/bindex-schema-v1.json:\n" + schema);

		File file = new File(projectDir, "pom.xml");

		Model model = PomReader.getProjectModel(file);

		String sourceDirectory = model.getBuild().getSourceDirectory();
		removeNotImportantData(model);

		String pom = PomReader.printModel(model);
		provider.prompt("Project POM file:\n" + pom);

		Path startPath = Paths.get(
				StringUtils.defaultIfEmpty(sourceDirectory, new File(projectDir, "src/main/java").getAbsolutePath()));

		if (Files.exists(startPath)) {
			Files.walk(startPath).filter(Files::isRegularFile).forEach((f) -> {
				try {
					provider.promptFile("This is a source file: " + f.toFile().getName() + "\n\n", f.toFile());
				} catch (IOException e) {
					System.out.println("File: " + f + " adding failed.");
				}
			});
		}

		provider.prompt(
				"Based on bindex schema generate detail json object. No additional text required, output format should be only json object.");

		if (bindexDir != null) {
			provider.saveInput(new File(bindexDir, "inputs.txt"));
		}

		String output = provider.perform();
		provider.clear();

		BIndex value = null;
		if (output != null) {
			value = mapper.readValue(output, BIndex.class);
		}
		return value;
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

	public BIndexBuilder projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

	public BIndexBuilder bindexDir(File bindexDir) {
		this.bindexDir = bindexDir;
		return this;
	}

}
