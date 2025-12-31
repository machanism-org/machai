package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.project.ProjectProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JScriptProjectLayout extends ProjectLayout {
	private static Logger logger = LoggerFactory.getLogger(JScriptProjectLayout.class);
	public static final String PROJECT_MODEL_FILE_NAME = "package.json";

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
							String relativePath = ProjectLayout.getRelatedPath(getProjectDir(), dir);
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
