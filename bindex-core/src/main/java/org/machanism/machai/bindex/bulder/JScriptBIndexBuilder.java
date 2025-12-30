package org.machanism.machai.bindex.bulder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JScriptBIndexBuilder extends BIndexBuilder {
	private static Logger logger = LoggerFactory.getLogger(JScriptBIndexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

	private static final String PROJECT_MODEL_FILE_NAME = "package.json";

	public JScriptBIndexBuilder(boolean callLLM) {
		super(callLLM);
	}

	@Override
	public BIndex build() throws IOException {
		JsonNode packageJson = getPackageJson();
		JsonNode workspacesNode = packageJson.get("private");
		if (workspacesNode == null || !workspacesNode.asBoolean()) {
			return super.build();
		}

		return null;
	}

	@Override
	public void projectContext() throws IOException {

		File packageFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		try (FileReader reader = new FileReader(packageFile)) {
			String prompt = MessageFormat.format(promptBundle.getString("js_resource_section"),
					IOUtils.toString(reader));
			getProvider().prompt(prompt);
		}

		Path startPath = Paths.get(new File(getProjectDir(), "src").getAbsolutePath());

		if (Files.exists(startPath)) {
			Files.walk(startPath).filter(f -> FilenameUtils.isExtension(f.toFile().getName(), "ts", "vue", "js"))
					.forEach((f) -> {
						try {
							getProvider().promptFile(f.toFile(), "source_resource_section");
						} catch (IOException e) {
							logger.warn("File: {P} adding failed.", f);
						}
					});
		}

		String prompt = promptBundle.getString("additional_rules");
		getProvider().prompt(prompt);
	}

	public static boolean isPackageJsonPresent(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	@Override
	public List<String> getModules() throws IOException {
		List<String> result = null;

		JsonNode packageJson = getPackageJson();
		JsonNode workspacesNode = packageJson.get("workspaces");
		if (workspacesNode != null) {
			List<String> modules = new ArrayList<String>();

			if (workspacesNode.isArray()) {
				Iterator<JsonNode> iterator = workspacesNode.iterator();
				while (iterator.hasNext()) {
					String module = iterator.next().asText();

					String requiredStartWith = StringUtils.substringBefore(module, "**");
					File dirToScan = new File(getProjectDir(), requiredStartWith);

					try (Stream<Path> stream = Files.walk(dirToScan.toPath())) {
						stream.filter(p -> {
							File file = p.toFile();
							boolean containsAny = StringUtils.containsAny(file.getAbsolutePath(),
									EXCLUDE_DIRS);
							boolean isProjectBuildFile = StringUtils.equals(file.getName(), PROJECT_MODEL_FILE_NAME);
							if (isProjectBuildFile) {
								if (!containsAny) {
									return true;
								}
							}
							return false;
						}).forEach(p -> {
							File dir = p.toFile().getParentFile();
							String relativePath = ProjectProcessor.getRelatedPath(getProjectDir(), dir);
							modules.add(relativePath);
						});
					}
				}
				result = modules;
			}
		}

		return result;
	}

	private JsonNode getPackageJson() throws IOException {
		File packageFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		JsonNode packageJson = new ObjectMapper().readTree(packageFile);
		return packageJson;
	}

	@Override
	public List<String> getSources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getDocuments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTests() {
		// TODO Auto-generated method stub
		return null;
	}

}
