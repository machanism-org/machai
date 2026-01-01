package org.machanism.machai.documents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
	private File rootDir;

	public DocsProcessor() {
		provider = GenAIProviderManager.getProvider(chatModel);
		provider.promptBundle(promptBundle);
		systemFunctionTools = new SystemFunctionTools(null);
		systemFunctionTools.applyTools(provider);
	}

	public void scanDocuments(File rootDir) throws IOException {
		this.rootDir = rootDir;
		scanProjects(rootDir);
	}

	@Override
	public void scanProjects(File projectDir) throws IOException {
		systemFunctionTools.setWorkingDir(getRootDir(projectDir));

		ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();
		if (modules != null) {
			File[] files = projectDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()
							&& !StringUtils.equalsAnyIgnoreCase(file.getName(), modules.toArray(new String[] {}))
							&& !StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {

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
					processFile(projectLayout, file);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void processFile(ProjectLayout projectLayout, File file) throws IOException {
		File projectDir = projectLayout.getProjectDir();
		String guidance = parseFile(projectDir, file);

		if (guidance != null) {

			String parentsPath = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()),
					file.getParentFile());
			String[] parents = StringUtils.split(parentsPath, "/");

			provider.instructions(promptBundle.getString("docs_instractions"));

			StringBuilder path = new StringBuilder();
			for (String parent : parents) {
				path.append("/" + parent);
				if (!StringUtils.equals(path, "/" + parentsPath)) {
					String dirGuidance = dirGuidanceMap.get(path.toString());
					if (StringUtils.isNotBlank(dirGuidance)) {
						provider.prompt(dirGuidance);
					}
				}
			}
			String projectInfo = getProjectStructureDescriprion(projectLayout);
			provider.prompt(projectInfo);
			provider.prompt(guidance);

			String inputsFileName = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
			File docsTempDir = new File(projectDir, DOCS_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			provider.saveInput(inputsFile);

			provider.perform(false);
		}
	}

	private String getProjectStructureDescriprion(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<String>();

		File projectDir = projectLayout.getProjectDir();
		String path = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), projectDir);
		content.add(path);
		content.add(getDirInfoLine(projectLayout.getSources()));
		content.add(getDirInfoLine(projectLayout.getDocuments()));
		content.add(getDirInfoLine(projectLayout.getTests()));
		content.add(getDirInfoLine(projectLayout.getModules()));

		return MessageFormat.format(promptBundle.getString("project_information"), content.toArray());
	}

	private String getDirInfoLine(List<String> sources) {
		String line;
		if (sources != null && !sources.isEmpty()) {
			line = StringUtils.join(sources.stream().map(e -> "`" + e + "`").collect(Collectors.toList()), ", ");
		} else {
			line = "not defined";
		}
		return line;
	}

	private String parseFile(File projectDir, File guidancesFile) throws IOException {
		String extension = FilenameUtils.getExtension(guidancesFile.getName()).toLowerCase();

		String result = null;
		switch (extension) {
		case "txt":
			if (StringUtils.equals(guidancesFile.getName(), GUIDANCE_FILE_NAME)) {
				result = getGuidanceFile(projectDir, guidancesFile);
			}
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

	private String getGuidanceFile(File projectDir, File guidancesFile) throws IOException {
		String guidance = Files.readString(guidancesFile.toPath());
		if (StringUtils.isNotBlank(guidance)) {
			String parentsPath = ProjectLayout.getRelatedPath(getRootDir(projectDir), guidancesFile.getParentFile());
			guidance = MessageFormat.format(promptBundle.getString("guidance_file"), parentsPath, guidance);
			dirGuidanceMap.put("/" + parentsPath, guidance);
		}

		return guidance;
	}

	private String parseJavaFile(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());
		String result = null;
		if (StringUtils.contains(content, GUIDANCE_TAG_NAME)) {
			Pattern pattern = Pattern.compile("(?:/\\*.*?" + GUIDANCE_TAG_NAME + ":\\s*(.*?)\\s*\\*/)|(?://\\s*"
					+ GUIDANCE_TAG_NAME + ":\\s*(.*))", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				if (StringUtils.equals(guidancesFile.getName(), "package-info.java")) {
					String guidanceText = matcher.group(1).replaceAll("\\s*\\*\\s?", " ").trim();
					result = MessageFormat.format(promptBundle.getString("java_package_info_file"),
							ProjectLayout.getRelatedPath(getRootDir(projectDir), guidancesFile.getParentFile()),
							guidanceText);
				} else {
					String relatedPath = ProjectLayout.getRelatedPath(getRootDir(projectDir), guidancesFile);
					String name = guidancesFile.getName();
					result = MessageFormat.format(promptBundle.getString("java_file"), name, relatedPath, content);
				}
			}
		}
		return result;
	}

	private String parseMarkdownFile(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());

		Pattern pattern = Pattern.compile("\\[" + GUIDANCE_TAG_NAME + "\\]:\\s*#\\s*\\((.*?)\\)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		String result = null;
		if (matcher.find()) {
			result = MessageFormat.format(promptBundle.getString("markdown_file"), guidancesFile.getName(),
					ProjectLayout.getRelatedPath(getRootDir(projectDir), guidancesFile), content);
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

	public File getRootDir(File projectDir) {
		return rootDir != null ? rootDir : projectDir;
	}

}
