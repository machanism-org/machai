package org.machanism.machai.core.bindex;

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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JScriptBIndexBuilder extends BIndexBuilder {
	private static Logger logger = LoggerFactory.getLogger(JScriptBIndexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

	private static final String PROJECT_MODEL_FILE_NAME = "package.json";
	private static final String[] EXCLUDE_DIRS = { "node_modules", ".venv", ".git", ".svn", ".nx" };

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
	protected void projectContext() throws IOException {

		File packageFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		try (FileReader reader = new FileReader(packageFile)) {
			String prompt = MessageFormat.format(promptBundle.getString("js_resource_section"),
					IOUtils.toString(reader));
			getProvider().prompt(prompt);
		}

		Path startPath = Paths.get(new File(getProjectDir(), "src").getAbsolutePath());

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
				String currentPath = getProjectDir().getAbsolutePath().replace("\\", "/");
				Iterator<JsonNode> iterator = workspacesNode.iterator();
				while (iterator.hasNext()) {
					String module = iterator.next().asText();

					try (Stream<Path> stream = Files.walk(getProjectDir().toPath())) {
						stream.filter(p -> {
							File file = p.toFile();

							String relativePath = getRelatedPath(currentPath, file);
							String requiredStartWith = StringUtils.substringBefore(module, "**");

							if (StringUtils.startsWith(relativePath, requiredStartWith)
									&& !StringUtils.containsAny(relativePath, EXCLUDE_DIRS)) {
								return StringUtils.equals(file.getName(), PROJECT_MODEL_FILE_NAME);
							}

							return false;
						}).forEach(p -> {
							File dir = p.toFile().getParentFile();
							String relativePath = getRelatedPath(currentPath, dir);
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

	private String getRelatedPath(String currentPath, File file) {
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
		if (StringUtils.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}

}
