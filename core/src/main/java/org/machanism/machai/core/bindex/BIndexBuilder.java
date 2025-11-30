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

	public BIndexBuilder provider(GenAIProvider provider) {
		this.provider = provider;
		return this;
	}

	public BIndex build() throws IOException {
		URL systemResource = getClass().getResource("/schema/bindex-schema-v1.json");
		String schema = IOUtils.toString(systemResource, "UTF8");
		provider.prompt("The bindex schema:\n" + schema);
		provider.prompt(
				"Next posts will be project file. They should be analyzed for generation brick-index json response.");

		Model model = EffectivePomReader.getEffectivePom(new File(projectDir, "pom.xml"));

		String sourceDirectory = model.getBuild().getSourceDirectory();
		model.setDistributionManagement(null);

		model.setDistributionManagement(null);
		model.setProperties(null);
		model.setDependencyManagement(null);
		model.setBuild(null);
		model.setReporting(null);
		model.setScm(null);
		model.setPluginRepositories(null);

		String effectivePom = EffectivePomReader.printModel(model);
		provider.prompt(effectivePom);

		Path startPath = Paths.get(
				StringUtils.defaultIfEmpty(sourceDirectory, new File(projectDir, "src/main/java").getAbsolutePath()));

		if (Files.exists(startPath)) {
			Files.walk(startPath).filter(Files::isRegularFile).forEach((f) -> {
				try {
					provider.promptFile(f.toFile());
				} catch (IOException e) {
					System.out.println("File: " + f + " adding failed.");
				}
			});
		} 

		provider.prompt(
				"Generate detail json object according to bindex schema. No additional text required, output format should be only json object.");

		provider.saveInput(new File("input.txt"));
		String output = provider.perform();

		BIndex value = null;
		if (output != null) {
			mapper.readValue(output, BIndex.class);
		}
		return value;
	}

	public BIndexBuilder projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

}
