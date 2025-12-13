package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JScriptBIndexBuilder extends BIndexBuilder {
	private static Logger logger = LoggerFactory.getLogger(JScriptBIndexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

	private static final String PROJECT_MODEL_FILE_NAME = "package.json";

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

		File projectDir = getProjectDir();
		File packageFile = new File(projectDir, PROJECT_MODEL_FILE_NAME);
		JsonNode packageJson = new ObjectMapper().readTree(packageFile);

		String currentPath = projectDir.getAbsolutePath();

		JsonNode workspacesNode = packageJson.get("workspaces");
		if (workspacesNode != null) {
			List<String> modules = new ArrayList<String>();

			if (workspacesNode.isArray()) {
				Iterator<JsonNode> iterator = workspacesNode.iterator();
				while (iterator.hasNext()) {
					String module = iterator.next().asText();

					try (Stream<Path> stream = Files.walk(projectDir.toPath())) {
						stream.filter(p -> {
							File file = p.toFile();

							String relativePath = file.getAbsolutePath().replace(currentPath, "");
							String path = "\\" + module.replace("/", "\\").replace("**", "");

							if (StringUtils.startsWith(relativePath, path)
									&& !StringUtils.contains(relativePath, "node_modules")) {
								return StringUtils.equals(file.getName(), "package.json");
							}

							return false;
						}).forEach(p -> {
							String dir = p.toFile().getParent();
							String relativePath = dir.replace(currentPath, "");
							modules.add(relativePath);
						});
					}
				}
				result = modules;
			}
		}

		return result;
	}

}
