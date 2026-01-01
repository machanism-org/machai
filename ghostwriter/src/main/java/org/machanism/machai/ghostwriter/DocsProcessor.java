package org.machanism.machai.ghostwriter;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.ghostwriter.reviewer.JavaReviewer;
import org.machanism.machai.ghostwriter.reviewer.MarkdownReviewer;
import org.machanism.machai.ghostwriter.reviewer.Reviewer;
import org.machanism.machai.ghostwriter.reviewer.TextReviewer;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

public class DocsProcessor extends ProjectProcessor {
	public static final String GUIDANCE_TAG_NAME = "@guidance";
	private static final String DOCS_TEMP_DIR = ".machai/docs-inputs";
	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	private String chatModel = "OpenAI:gpt-5-mini";

	private GenAIProvider provider;
	private SystemFunctionTools systemFunctionTools;

	private Map<String, String> dirGuidanceMap = new HashMap<>();
	private Map<String, Reviewer> reviewMap = new HashMap<>();

	private File rootDir;

	public DocsProcessor() {
		provider = GenAIProviderManager.getProvider(chatModel);
		provider.promptBundle(promptBundle);
		systemFunctionTools = new SystemFunctionTools(null);
		systemFunctionTools.applyTools(provider);

		reviewMap.put("txt", new TextReviewer(dirGuidanceMap));
		reviewMap.put("java", new JavaReviewer());
		reviewMap.put("md", new MarkdownReviewer());
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
					if (!StringUtils.equalsAnyIgnoreCase(file.getName(), modules.toArray(new String[] {}))
							&& !StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
						if (file.isDirectory()) {
							processProject(projectLayout, file);
						} else {
							processFile(projectLayout, file);
						}
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

			List<String> parentsGuidances = getParentsGuidances(parentsPath, parents);
			for (String dirGuidance : parentsGuidances) {
				provider.prompt(dirGuidance);
			}

			String projectInfo = getProjectStructureDescription(projectLayout);
			provider.prompt(projectInfo);
			provider.prompt(guidance);

			String inputsFileName = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
			File docsTempDir = new File(projectDir, DOCS_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			provider.saveInput(inputsFile);

			provider.perform(false);
		}
	}

	private List<String> getParentsGuidances(String parentsPath, String[] parents) {
		List<String> guidances = new ArrayList<>();
		StringBuilder path = new StringBuilder();
		for (String parent : parents) {
			path.append("/" + parent);
			if (!StringUtils.equals(path, "/" + parentsPath)) {
				String dirGuidance = dirGuidanceMap.get(path.toString());
				if (StringUtils.isNotBlank(dirGuidance)) {
					guidances.add(dirGuidance);
				}
			}
		}
		return guidances;
	}

	private String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<String>();

		File projectDir = projectLayout.getProjectDir();
		String path = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), projectDir);
		content.add(path);
		content.add(getDirInfoLine(projectLayout.getSources()));
		content.add(getDirInfoLine(projectLayout.getTests()));
		content.add(getDirInfoLine(projectLayout.getDocuments()));
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

	private String parseFile(File projectDir, File file) throws IOException {
		String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
		Reviewer reviewer = reviewMap.get(extension);

		String result = null;
		if (reviewer != null) {
			result = reviewer.perform(getRootDir(projectDir), file);
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
