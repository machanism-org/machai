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
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.ghostwriter.reviewer.HtmlReviewer;
import org.machanism.machai.ghostwriter.reviewer.JavaReviewer;
import org.machanism.machai.ghostwriter.reviewer.MarkdownReviewer;
import org.machanism.machai.ghostwriter.reviewer.PythonReviewer;
import org.machanism.machai.ghostwriter.reviewer.Reviewer;
import org.machanism.machai.ghostwriter.reviewer.TextReviewer;
import org.machanism.machai.ghostwriter.reviewer.TypeScriptReviewer;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Processor for project documentation generation.
 * <p>
 * Scans project sources, applies file reviewers for extracting documentation
 * guidance, and orchestrates the input preparation for large language model
 * document generation.
 */
public class DocsProcessor extends ProjectProcessor {
	public static final String GUIDANCE_TAG_NAME = "@guidance";
	private static final String DOCS_TEMP_DIR = ".machai/docs-inputs";
	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	private GenAIProvider provider;
	private SystemFunctionTools systemFunctionTools;

	private Map<String, String> dirGuidanceMap = new HashMap<>();
	private Map<String, Reviewer> reviewMap = new HashMap<>();

	private File rootDir;
	private boolean inheritance;
	private boolean useParentsGuidances;

	/**
	 * Constructs a DocsProcessor for documentation input preparation.
	 * @param p 
	 */
	public DocsProcessor(GenAIProvider provider) {
		this.provider = provider;
		
		systemFunctionTools = new SystemFunctionTools(null);
		systemFunctionTools.applyTools(provider);

		addReviewer(new TextReviewer(dirGuidanceMap));
		addReviewer(new JavaReviewer());
		addReviewer(new TypeScriptReviewer());
		addReviewer(new MarkdownReviewer());
		addReviewer(new PythonReviewer());
		addReviewer(new HtmlReviewer());
		addReviewer(new HtmlReviewer());
	}

	private void addReviewer(Reviewer reviwer) {
		String[] extentions = reviwer.getSupportedFileExtentions();
		for (String extention : extentions) {
			reviewMap.put(extention, reviwer);
		}
	}

	/**
	 * Scans documents in the given root directory and prepares inputs for
	 * documentation generation.
	 *
	 * @param rootDir the root directory of the project to scan
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File rootDir) throws IOException {
		this.rootDir = rootDir;
		scanFolder(rootDir);
	}

	/**
	 * Recursively scans projects, processing documentation inputs for all found
	 * project modules and files.
	 *
	 * @param projectDir the directory containing the project to be scanned
	 * @throws IOException if an error occurs reading files
	 */
	@Override
	public void scanFolder(File projectDir) throws IOException {
		systemFunctionTools.setWorkingDir(getRootDir(projectDir));

		ProjectLayout projectLayout = getProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();
		if (modules != null) {
			File[] files = projectDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (!StringUtils.equalsAnyIgnoreCase(file.getName(), modules.toArray(new String[] {}))
							&& !StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
						if (file.isDirectory()) {
							processProjectDir(projectLayout, file);
						} else {
							processFile(projectLayout, file);
						}
					}
				}
			}
		}

		super.scanFolder(projectDir);
	}

	/**
	 * Processes the given project layout for documentation purposes.
	 *
	 * @param projectLayout the detected project layout describing sources, tests,
	 *                      docs, and modules
	 */
	@Override
	public void processFolder(ProjectLayout projectLayout) {
		File projectDir = projectLayout.getProjectDir();
		processProjectDir(projectLayout, projectDir);
	}

	private void processProjectDir(ProjectLayout projectLayout, File scanDir) {
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

			provider.instructions(promptBundle.getString("sys_instractions"));
			provider.prompt(promptBundle.getString("docs_processing_instractions"));

			if (useParentsGuidances) {
				List<String> parentsGuidances = getParentsGuidances(projectLayout, file);
				for (String dirGuidance : parentsGuidances) {
					provider.prompt(dirGuidance);
				}
			}

			String projectInfo = getProjectStructureDescription(projectLayout);
			provider.prompt(projectInfo);
			provider.prompt(guidance);
			provider.prompt(promptBundle.getString("output_format"));

			String inputsFileName = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
			File docsTempDir = new File(projectDir, DOCS_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");

			provider.inputsLog(inputsFile).perform();
		}
	}

	private List<String> getParentsGuidances(ProjectLayout projectLayout, File file) {
		String projectPath = ProjectLayout.getRelatedPath(rootDir, projectLayout.getProjectDir(), true);
		int skipNumber = StringUtils.split(projectPath, "/").length;

		String parentsPath = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()),
				file.getParentFile(), true);

		StringBuilder path = new StringBuilder();
		String[] parents = StringUtils.split(parentsPath, "/");
		List<String> guidances = new ArrayList<String>();

		for (String parent : parents) {
			if (!".".equals(parent)) {
				path.append("/");
			}
			path.append(parent);
			if (skipNumber-- <= 0 || inheritance) {
				if (!StringUtils.equals(path, parentsPath)) {
					String dirGuidance = dirGuidanceMap.get(path.toString());
					if (StringUtils.isNotBlank(dirGuidance)) {
						guidances.add(dirGuidance);
					}
				}
			}
		}
		return guidances;
	}

	private String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<String>();

		String path = ProjectLayout.getRelatedPath(rootDir, projectLayout.getProjectDir());

		content.add(path);
		File projectDir = projectLayout.getProjectDir();
		content.add(getDirInfoLine(projectLayout.getSources(), projectDir));
		content.add(getDirInfoLine(projectLayout.getTests(), projectDir));
		content.add(getDirInfoLine(projectLayout.getDocuments(), projectDir));
		content.add(getDirInfoLine(projectLayout.getModules(), projectDir));

		return MessageFormat.format(promptBundle.getString("project_information"), content.toArray());
	}

	private String getDirInfoLine(List<String> sources, File projectDir) {
		String line = null;
		if (sources != null && !sources.isEmpty()) {
			List<String> dirs = sources.stream().filter(t -> {
				File file = new File(projectDir, t);
				boolean exists = file.exists();
				return exists;
			}).map(e -> {
				String path = ProjectLayout.getRelatedPath(rootDir, new File(projectDir, e));
				return "`" + path + "`";
			}).collect(Collectors.toList());
			line = StringUtils.join(dirs, ", ");
		}

		if (StringUtils.isBlank(line)) {
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

	/**
	 * Returns the root directory for the documentation scan. Defaults to input
	 * directory if root is not set.
	 *
	 * @param projectDir the directory detected as project root
	 * @return the effective root directory
	 */
	public File getRootDir(File projectDir) {
		return rootDir != null ? rootDir : projectDir;
	}

	public boolean isInheritance() {
		return inheritance;
	}

	public void setInheritance(boolean inheritance) {
		this.inheritance = inheritance;
	}

	public boolean isUseParentsGuidances() {
		return useParentsGuidances;
	}

	public void setUseParentsGuidances(boolean useParentsGuidances) {
		this.useParentsGuidances = useParentsGuidances;
	}

}
