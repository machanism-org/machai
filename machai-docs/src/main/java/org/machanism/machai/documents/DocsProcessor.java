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
import org.machanism.machai.project.ProjectLayoutManager;
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
	public void scanProjects(File projectDir) throws IOException {
		ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();
		if (modules != null) {
			File[] files = projectDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()
							&& !StringUtils.equalsAnyIgnoreCase(file.getName(), modules.toArray(new String[] {}))
							&& !StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {

						systemFunctionTools.setWorkingDir(projectDir);
						if (modules != null) {
							provider.prompt("Child projects: " + StringUtils.join(modules, ", ") + ".");
						}

						processProject(projectLayout, file);
					}
				}
			}
		}

		super.scanProjects(projectDir);
	}

	@Override
	public void processProject(ProjectLayout projectLayout) {
		File projectDir = projectLayout.getProjectDir();
		processProject(projectLayout, projectDir);
	}

	private void processProject(ProjectLayout projectLayout, File scanDir) {
		try {
			List<File> files = findFiles(scanDir);
			if (!files.isEmpty()) {
				for (File file : files) {
					systemFunctionTools.setWorkingDir(scanDir);
					processFile(projectLayout, file);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void processFile(ProjectLayout projectLayout, File file)
			throws IOException {
		File projectDir = projectLayout.getProjectDir();
		String guidance = parseGuidanceFile(projectDir, file);

		if (guidance != null) {
			fillProjectLayerInformation(projectLayout);

			String parentsPath = ProjectLayout.getRelatedPath(projectDir, file.getParentFile());
			String[] parents = StringUtils.split(parentsPath, "/");

			provider.instructions(
					"You are smart software engineer and developer. You are expert in all popular programming languages, frameworks, platforms.\r\n\r\n"
							+ "# Constraints\r\n\r\n"
							+ "1. You must implement comprehensive, correct code.\r\n"
							+ "Important:\r\n"
							+ "1. You have ability to work with local file system and command line.");

			StringBuilder path = new StringBuilder();
			for (String parent : parents) {
				path.append("/" + parent);
				String dirGuidance = dirGuidanceMap.get(path.toString());
				if (StringUtils.isNotBlank(dirGuidance)) {
					provider.prompt(dirGuidance);
				}
			}

			provider.prompt(guidance);

			String inputsFileName = ProjectLayout.getRelatedPath(projectDir, file);
			File docsTempDir = new File(projectDir, DOCS_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			provider.saveInput(inputsFile);

			provider.perform(false);
		}
	}

	private void fillProjectLayerInformation(ProjectLayout projectLayout) throws IOException {
		List<String> sources = projectLayout.getSources();
		if (sources != null) {
			provider.prompt("Sources folders: " + StringUtils.join(sources, ", ") + ".");
		}
		List<String> documents = projectLayout.getDocuments();
		if (documents != null) {
			provider.prompt("Documents folders: " + StringUtils.join(documents, ", ") + ".");
		}
		List<String> tests = projectLayout.getTests();
		if (tests != null) {
			provider.prompt("Tests folders: " + StringUtils.join(tests, ", ") + ".");
		}
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
			if (StringUtils.isNotBlank(guidance)) {
				String parentsPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile());

				StringBuilder prompt = new StringBuilder();
				prompt.append("# Directory Guidance\r\n\r\n");
				prompt.append("Important: Do not remove @guidance directives.\r\n");
				prompt.append("Path: " + parentsPath + "\r\n\r\n");
				prompt.append(guidance + "\r\n");

				guidance = prompt.toString();
				dirGuidanceMap.put("/" + parentsPath, guidance);
			}
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
					result.append("# Java Package Gudance File\r\n\r\n");
					result.append("Important: Do not remove @guidance directives.\r\n");
					result.append(
							"Folder: " + ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile())
									+ "\r\n\r\n");
					result.append("Guidance: " + guidanceText);
				}

			} else {
				result.append("# Java Source File: `" + guidancesFile.getName() + "`\r\n\r\n");
				result.append("Important: Do not remove @guidance directives.\r\n");
				result.append(
						"Path: " + ProjectLayout.getRelatedPath(projectDir, guidancesFile) + "\r\n\r\n");
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

			prompt.append("# Markdown File: `" + guidancesFile.getName() + "`\r\n\r\n");
			prompt.append("Important: Do not remove @guidance directives.\r\n");
			prompt.append(
					"Follow the rules described in markdown reference-style link comments marked as `@guidance` in the format `[@guidance] # ({RULES})`. Process the file below accordingly.\r\n");
			prompt.append("Path: " + ProjectLayout.getRelatedPath(projectDir, guidancesFile) + "\r\n\r\n");
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
					if (!StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
						if (file.isDirectory()) {
							result.addAll(findFiles(file));
						} else {
							result.add(file);
						}
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
