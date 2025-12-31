package org.machanism.machai.documents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

public class DocsProcessor extends ProjectProcessor {
	private static final String GUIDANCE_TAG_NAME = "@guidance";
	private static final String GUIDANCE_FILE_NAME = GUIDANCE_TAG_NAME + ".txt";

	private static final String DOCS_TEMP_DIR = ".machai/docs-inputs";

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");
	private String chatModel = "OpenAI:gpt-5-mini";

	private GenAIProvider provider;
	private SystemFunctionTools systemFunctionTools;

	private Map<String, String> dirGuidanceMap = new HashMap<>();

	public DocsProcessor() {
		provider = GenAIProviderManager.getProvider(chatModel);
		provider.promptBundle(promptBundle);

		systemFunctionTools = new SystemFunctionTools(null);
		systemFunctionTools.applyTools(provider);
	}

	@Override
	public void processProject(ProjectLayout projectLayout) {

		File projectDir = projectLayout.getProjectDir();
		File docsTempDir = new File(projectDir, DOCS_TEMP_DIR);

		systemFunctionTools.setWorkingDir(projectDir);

		try {
			List<File> guidances = findFiles(projectDir);
			if (!guidances.isEmpty()) {

				for (File guidancesFile : guidances) {
					String guidance = parseGuidanceFile(projectDir, guidancesFile);

					if (guidance != null) {
						fillProjectLayerInformation(projectLayout);

						String parentsPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile());
						String[] parents = StringUtils.split(parentsPath, "/");

						StringBuilder path = new StringBuilder();
						for (String parent : parents) {
							path.append("/" + parent);
							String dirGuidance = dirGuidanceMap.get(path.toString());
							if (StringUtils.isNotBlank(dirGuidance)) {
								provider.prompt(dirGuidance);
							}
						}

						provider.prompt(guidance);
						String inputsFileName = ProjectLayout.getRelatedPath(projectDir, guidancesFile);
						File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
						provider.saveInput(inputsFile);
						provider.perform(false);
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void fillProjectLayerInformation(ProjectLayout projectLayout) {
		List<String> sources = projectLayout.getSources();
		provider.prompt("Sources folders: " + StringUtils.join(sources, ", ") + ".");

		List<String> documents = projectLayout.getDocuments();
		provider.prompt("Documents folders: " + StringUtils.join(documents, ", ") + ".");

		List<String> tests = projectLayout.getTests();
		provider.prompt("Tests folders: " + StringUtils.join(tests, ", ") + ".");
	}

	private String parseGuidanceFile(File projectDir, File guidancesFile) throws IOException {
		String extension = FilenameUtils.getExtension(guidancesFile.getName()).toLowerCase();

		String result = null;
		switch (extension) {
		case "txt":
			result = parseTextFile(projectDir, guidancesFile);
			break;

		case "java":
			result = parseJavaFile(projectDir, guidancesFile);
			break;

		case "md":
			result = parseMarkdownFile(projectDir, guidancesFile);
			break;

		default:
			break;
		}

		return result;
	}

	private String parseTextFile(File projectDir, File guidancesFile) throws IOException {
		String guidance = null;
		if (StringUtils.equals(guidancesFile.getName(), GUIDANCE_FILE_NAME)) {
			guidance = Files.readString(guidancesFile.toPath());
			String parentsPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile());
			dirGuidanceMap.put("/" + parentsPath, guidance);
		}
		return guidance;
	}

	private String parseJavaFile(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());
		StringBuilder result = null;
		if (StringUtils.contains(content, GUIDANCE_TAG_NAME)) {
			result = new StringBuilder();
			if (StringUtils.equals(guidancesFile.getName(), "package-info.java")) {
				Pattern pattern = Pattern.compile("/\\*.*?" + GUIDANCE_TAG_NAME + ":\\s*(.*?)\\s*\\*/", Pattern.DOTALL);
				Matcher matcher = pattern.matcher(content);

				if (matcher.find()) {
					String guidanceText = matcher.group(1).replaceAll("\\s*\\*\\s?", " ").trim();
					result.append(
							"Folder: " + ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile())
									+ "\r\n");
					result.append("Guidance: " + guidanceText);
				}

			} else {
				result.append(
						"File: " + ProjectLayout.getRelatedPath(projectDir, guidancesFile) + "\r\n");
				result.append("```java\r\n");
				result.append(content);
				result.append("\r\n```\r\n");
			}

		}
		return result != null ? result.toString() : null;
	}

	private String parseMarkdownFile(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());

		Pattern pattern = Pattern.compile("\\[" + GUIDANCE_TAG_NAME + "\\]\\s*#\\s*\\((.*?)\\)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		String result = null;
		if (matcher.find()) {
			StringBuilder prompt = new StringBuilder();

			prompt.append(
					"Follow the rules described in markdown reference-style link comments marked as `@guidance` in the format `[@guidance] # ({RULES})`. Process the file below accordingly.\r\n");
			prompt.append("File: " + ProjectLayout.getRelatedPath(projectDir, guidancesFile) + "\r\n\r\n");
			prompt.append("```md\r\n");
			prompt.append(content);
			prompt.append("\r\n```\r\n");

			result = prompt.toString();
		}

		return result;
	}

	private List<File> findFiles(File projectDir) {
		List<File> result = new ArrayList<>();
		if (projectDir != null && projectDir.isDirectory()) {
			File[] files = projectDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()
							&& !StringUtils.equalsAnyIgnoreCase(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
						result.addAll(findFiles(file));
					} else {
						result.add(file);
					}
				}
			}
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		DocsProcessor documents = new DocsProcessor();
		documents.scanProjects(SystemUtils.getUserDir());
	}

}
