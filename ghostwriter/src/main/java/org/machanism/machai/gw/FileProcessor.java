package org.machanism.machai.gw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans a project directory, extracts {@code @guidance:} instructions from supported files, and prepares prompt
 * inputs for AI-assisted documentation processing.
 *
 * <p>
 * This processor delegates file-specific guidance extraction to {@link Reviewer} implementations discovered via
 * {@link ServiceLoader}. For every supported file it finds, it builds a prompt using templates from the
 * {@code document-prompts} resource bundle and invokes a {@link GenAIProvider}.
 * </p>
 */
public class FileProcessor extends ProjectProcessor {
	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

	/** Tag name for guidance comments. */
	public static final String GUIDANCE_TAG_NAME = "@guidance:";

	/** Temporary directory name for documentation inputs under {@link #MACHAI_TEMP_DIR}. */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Resource bundle supplying prompt templates for generators. */
	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/** Utility that installs tool functions (filesystem/command) into the provider when supported. */
	private final SystemFunctionTools systemFunctionTools;

	/** Directory-level guidance mappings. Currently unused but reserved for future directory-scoped rules. */
	@SuppressWarnings("unused")
	private final Map<String, String> dirGuidanceMap = new HashMap<>();
	/** Reviewer associations keyed by normalized (lowercase) file extension. */
	private final Map<String, Reviewer> reviewMap = new HashMap<>();

	/** Root scanning directory for the current documentation run. */
	private File rootDir;

	private String genai;

	/**
	 * Constructs a processor.
	 *
	 * @param provider the provider to use; must not be {@code null}
	 */
	public FileProcessor(String genai) {
		this.genai = genai;
		systemFunctionTools = new SystemFunctionTools();

		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping supported file extensions to a reviewer.
	 */
	private void loadReviewers() {
		reviewMap.clear();

		ServiceLoader<Reviewer> reviewerServiceLoader = ServiceLoader.load(Reviewer.class);
		for (Reviewer reviewer : reviewerServiceLoader) {
			if (reviewer == null) {
				continue;
			}

			String[] extensions = reviewer.getSupportedFileExtentions();
			if (extensions == null || extensions.length == 0) {
				continue;
			}

			for (String extension : extensions) {
				if (StringUtils.isBlank(extension)) {
					continue;
				}

				String normalizedExtension = StringUtils.lowerCase(StringUtils.trim(extension));
				reviewMap.putIfAbsent(normalizedExtension, reviewer);
			}
		}
	}

	/**
	 * Scans documents in the given root directory and prepares inputs for documentation generation. This overload
	 * defaults the scan start directory to {@code basedir}.
	 *
	 * @param basedir root directory to scan
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File basedir) throws IOException {
		scanDocuments(basedir, basedir);
	}

	/**
	 * Scans documents in the given root directory and start subdirectory, preparing inputs for documentation
	 * generation.
	 *
	 * @param rootDir the root directory of the project to scan
	 * @param dir     the directory to begin scanning
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File rootDir, File dir) throws IOException {
		this.rootDir = rootDir;
		scanFolder(dir);
	}

	/**
	 * Recursively scans project folders, processing documentation inputs for all found modules and files.
	 *
	 * @param projectDir the directory containing the project/module to be scanned
	 * @throws IOException if an error occurs reading files
	 */
	@Override
	public void scanFolder(File projectDir) throws IOException {
		processParentFiles(projectDir);
		super.scanFolder(projectDir);
	}

	/**
	 * Processes non-module files and directories directly under {@code projectDir}.
	 *
	 * @param projectDir directory to scan
	 * @throws FileNotFoundException if the project layout cannot be created
	 * @throws IOException           if file reading fails
	 */
	protected void processParentFiles(File projectDir) throws FileNotFoundException, IOException {
		GenAIProvider provider = GenAIProviderManager.getProvider(genai);
		systemFunctionTools.applyTools(provider );
		provider.setWorkingDir(getRootDir(projectDir));
		
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();

		File[] children = projectDir.listFiles();
		if (children != null) {
			for (File child : children) {
				if (isModuleDir(modules, child) || isExcludedByLayout(child)) {
					continue;
				}

				if (child.isDirectory()) {
					processProjectDir(projectLayout, child);
				} else {
					logIfNotBlank(processFile(projectLayout, child));
				}
			}
		}
	}

	private static boolean isModuleDir(List<String> modules, File dir) {
		if (modules == null || modules.isEmpty() || dir == null) {
			return false;
		}
		return StringUtils.equalsAnyIgnoreCase(dir.getName(), modules.toArray(new String[0]));
	}

	private static boolean isExcludedByLayout(File file) {
		if (file == null) {
			return false;
		}
		return StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS);
	}

	private static void logIfNotBlank(String message) {
		if (StringUtils.isNotBlank(message)) {
			logger.debug(message);
		}
	}

	/**
	 * Processes a project layout for documentation gathering.
	 *
	 * @param projectLayout layout describing sources, tests, docs, and modules
	 */
	@Override
	public void processFolder(ProjectLayout projectLayout) {
		File projectDir = projectLayout.getProjectDir();
		try {
			List<File> files = findFiles(projectDir);
			if (!files.isEmpty()) {
				for (File file : files) {
					String processFile = processFile(projectLayout, file);
					logIfNotBlank(processFile);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Processes the selected project directory for documentation guidance extraction.
	 *
	 * @param projectLayout layout against which files are processed
	 * @param scanDir       directory to scan for files
	 */
	private void processProjectDir(ProjectLayout projectLayout, File scanDir) {
		try {
			List<File> files = findFiles(scanDir);
			if (!files.isEmpty()) {
				for (File file : files) {
					logIfNotBlank(processFile(projectLayout, file));
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Processes a single file: extracts guidance and, if applicable, builds and executes an AI prompt.
	 *
	 * @param projectLayout the project layout instance
	 * @param file          the file to process
	 * @return the provider response (or {@code null} if the file is not supported or contains no guidance)
	 * @throws IOException if file reading fails
	 */
	private String processFile(ProjectLayout projectLayout, File file) throws IOException {
		File projectDir = projectLayout.getProjectDir();
		String guidance = parseFile(projectDir, file);

		if (guidance == null) {
			return null;
		}

		GenAIProvider provider = GenAIProviderManager.getProvider(genai);
		systemFunctionTools.applyTools(provider );
		provider.setWorkingDir(getRootDir(projectDir));
		
		provider.instructions(promptBundle.getString("sys_instractions"));
		provider.prompt(promptBundle.getString("docs_processing_instractions"));

		String projectInfo = getProjectStructureDescription(projectLayout);
		provider.prompt(projectInfo);
		provider.prompt(guidance);
		provider.prompt(promptBundle.getString("output_format"));

		String inputsFileName = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
		File docsTempDir = new File(projectDir, MACHAI_TEMP_DIR + "/" + GW_TEMP_DIR);
		File inputsFile = new File(docsTempDir, inputsFileName + ".txt");

		provider.inputsLog(inputsFile);
		return provider.perform();
	}

	/**
	 * Returns a textual description of the current project structure using prompt templates.
	 *
	 * @param projectLayout the layout to describe
	 * @return formatted structure description for prompts
	 * @throws IOException if template resources are unavailable
	 */
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

	/**
	 * Returns a comma-separated string of directories for sources, tests, documents, or modules.
	 *
	 * <p>
	 * Only directories that exist in the file system are listed.
	 * </p>
	 *
	 * @param sources    list of directory names
	 * @param projectDir base directory
	 * @return formatted line for prompt or {@code "not defined"} if no entries found
	 */
	private String getDirInfoLine(List<String> sources, File projectDir) {
		String line = null;
		if (sources != null && !sources.isEmpty()) {
			List<String> dirs = sources.stream().filter(t -> t != null && new File(projectDir, t).exists()).map(e -> {
				String relatedPath = ProjectLayout.getRelatedPath(rootDir, new File(projectDir, e));
				return "`" + relatedPath + "`";
			}).collect(Collectors.toList());
			line = StringUtils.join(dirs, ", ");
		}

		if (StringUtils.isBlank(line)) {
			line = "not defined";
		}
		return line;
	}

	/**
	 * Extracts file guidance using a matching {@link Reviewer}.
	 *
	 * @param projectDir root directory
	 * @param file       file to be processed
	 * @return guidance string, or {@code null} if the file is not supported or contains no guidance
	 * @throws IOException if the reviewer encounters a file error
	 */
	private String parseFile(File projectDir, File file) throws IOException {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(file.getName()));
		Reviewer reviewer = reviewMap.get(extension);

		if (reviewer == null) {
			return null;
		}

		return reviewer.perform(getRootDir(projectDir), file);
	}

	/**
	 * Recursively finds all files (excluding {@link ProjectLayout#EXCLUDE_DIRS}) in a directory structure.
	 *
	 * @param projectDir directory to search
	 * @return list of files found
	 * @throws IOException if a directory cannot be listed
	 */
	private List<File> findFiles(File projectDir) throws IOException {
		if (projectDir == null || !projectDir.isDirectory()) {
			return List.of();
		}

		File[] files = projectDir.listFiles();
		if (files == null) {
			throw new IOException("Unable to list files for directory: " + projectDir.getAbsolutePath());
		}

		List<File> result = new ArrayList<>();
		for (File file : files) {
			if (StringUtils.equalsAnyIgnoreCase(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
				continue;
			}
			if (file.isDirectory()) {
				result.addAll(findFiles(file));
			} else {
				result.add(file);
			}
		}
		return result;
	}

	/**
	 * Returns the root directory for documentation scanning, falling back to the provided directory if unset.
	 *
	 * @param projectDir the directory detected as project root
	 * @return the effective root directory
	 */
	public File getRootDir(File projectDir) {
		return rootDir != null ? rootDir : projectDir;
	}

	/**
	 * Deletes generated temporary inputs under {@code ${basedir}/.machai/docs-inputs}.
	 *
	 * @param basedir base directory
	 * @return {@code true} if deletion was successful or the target did not exist
	 */
	public static boolean deleteTempFiles(File basedir) {
		File file = new File(basedir, FileProcessor.MACHAI_TEMP_DIR + "/" + FileProcessor.GW_TEMP_DIR);
		logger.info("Removing '{}' inputs log file.", file);
		return FileUtils.deleteQuietly(file);
	}
}
