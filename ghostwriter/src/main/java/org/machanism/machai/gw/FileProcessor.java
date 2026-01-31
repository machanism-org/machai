package org.machanism.machai.gw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans a project directory, extracts guidance instructions from supported files,
 * and prepares prompt inputs for AI-assisted documentation processing.
 *
 * <p>
 * This processor delegates file-specific guidance extraction to
 * {@link Reviewer} implementations discovered via {@link ServiceLoader}. For
 * every supported file it finds, it builds a prompt using templates from the
 * {@code document-prompts} resource bundle and invokes a {@link GenAIProvider}.
 * </p>
 */
public class FileProcessor extends ProjectProcessor {
	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

	/** Tag name for guidance comments. */
	public static final String GUIDANCE_TAG_NAME = "@guidance:";

	/**
	 * Temporary directory name for documentation inputs under
	 * {@link #MACHAI_TEMP_DIR}.
	 */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Resource bundle supplying prompt templates for generators. */
	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Utility that installs tool functions (filesystem/command) into the provider
	 * when supported.
	 */
	private final SystemFunctionTools systemFunctionTools;

	/** Reviewer associations keyed by normalized (lowercase) file extension. */
	private final Map<String, Reviewer> reviewMap = new HashMap<>();

	/** Root scanning directory for the current documentation run. */
	private File rootDir;

	private final String genai;

	private boolean moduleMultiThread;

	private String instructions;

	private boolean logInputs = true;

	private boolean nonRecursive;

	private String defaultGuidance;

	private File defaultProcessingDir;

	/**
	 * Constructs a processor.
	 *
	 * @param genai provider key/name to use
	 */
	public FileProcessor(String genai) {
		this.genai = genai;
		this.systemFunctionTools = new SystemFunctionTools();
		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping
	 * supported file extensions to a reviewer.
	 */
	private void loadReviewers() {
		reviewMap.clear();

		ServiceLoader<Reviewer> reviewerServiceLoader = ServiceLoader.load(Reviewer.class);
		for (Reviewer reviewer : reviewerServiceLoader) {
			if (reviewer == null) {
				continue;
			}

			String[] extensions = reviewer.getSupportedFileExtensions();
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
	 * Scans documents in the given root directory and prepares inputs for
	 * documentation generation. This overload defaults the scan start directory to
	 * {@code basedir}.
	 *
	 * @param basedir root directory to scan
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File basedir) throws IOException {
		scanDocuments(basedir, basedir);
	}

	/**
	 * Scans documents in the given root directory and start subdirectory, preparing
	 * inputs for documentation generation.
	 *
	 * @param rootDir the root directory of the project to scan
	 * @param dir     the directory to begin scanning
	 * @throws IOException if an error occurs reading files
	 */
	public void scanDocuments(File rootDir, File dir) throws IOException {
		this.rootDir = rootDir;

		File scanDir = dir;
		ProjectLayout projectLayout = getProjectLayout(dir);
		if (defaultGuidance != null) {
			if (projectLayout instanceof DefaultProjectLayout) {
				defaultProcessingDir = dir;
				scanDir = rootDir;
			}

			scanFolder(scanDir);
			process(projectLayout, defaultProcessingDir, scanDir, defaultGuidance);
		} else {
			scanFolder(scanDir);
		}
	}

	/**
	 * Recursively scans project folders, processing documentation inputs for all
	 * found modules and files.
	 *
	 * @param projectDir the directory containing the project/module to be scanned
	 * @throws IOException if an error occurs reading files
	 */
	@Override
	public void scanFolder(File projectDir) throws IOException {

		if (!isNonRecursive()) {
			ProjectLayout projectLayout = getProjectLayout(projectDir);
			List<String> modules = projectLayout.getModules();

			if (modules != null) {
				if (isModuleMultiThread()) {
					logger.info("Multi-threaded processing mode enabled.");

					ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, modules.size()));
					try {
						for (String module : modules) {
							executor.submit(() -> {
								try {
									processModule(projectDir, module);
								} catch (IOException e) {
									logger.error("Module processing failed.", e);
								}
							});
						}
					} finally {
						executor.shutdown();
					}

					try {
						executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						logger.error("Thread interrupted while processing modules", e);
					}
				} else {
					for (String module : modules) {
						processModule(projectDir, module);
					}
				}
			}
		}

		processParentFiles(projectDir);
	}

	/**
	 * Processes non-module files and directories directly under {@code projectDir}.
	 *
	 * @param projectDir directory to scan
	 * @throws FileNotFoundException if the project layout cannot be created
	 * @throws IOException           if file reading fails
	 */
	protected void processParentFiles(File projectDir) throws FileNotFoundException, IOException {
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();

		List<File> children = listFiles(projectDir);

		if (children.isEmpty()) {
			return;
		}

		for (File child : children) {
			if (isModuleDir(modules, child) || isExcludedByLayout(child)) {
				continue;
			}

			if (child.isDirectory()) {
				processProjectDir(child);
			} else {
				logIfNotBlank(processFile(projectLayout, child));
			}
		}
	}

	private static boolean isModuleDir(List<String> modules, File dir) {
		if (modules == null || modules.isEmpty() || dir == null) {
			return false;
		}
		return Strings.CI.equalsAny(dir.getName(), modules.toArray(new String[0]));
	}

	private static boolean isExcludedByLayout(File file) {
		if (file == null) {
			return false;
		}
		return Strings.CI.equalsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS);
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
		try {
			List<File> files = findFiles(projectLayout.getProjectDir());
			for (File file : files) {
				logIfNotBlank(processFile(projectLayout, file));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void processProjectDir(File scanDir) {
		try {
			ProjectLayout layout = getProjectLayout(scanDir);
			List<File> files = findFiles(scanDir);
			for (File file : files) {
				logIfNotBlank(processFile(layout, file));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private String processFile(ProjectLayout projectLayout, File file) throws IOException {
		String perform = null;

		if (defaultProcessingDir == null || Strings.CS.startsWith(file.getPath(), defaultProcessingDir.getPath())) {

			File projectDir = projectLayout.getProjectDir();
			String guidance = parseFile(projectDir, file);

			if (guidance != null) {
				logger.info("Processing file: {}", file.getAbsolutePath());
				perform = process(projectLayout, file, projectDir, guidance);
			}
		}
		return perform;
	}

	private String process(ProjectLayout projectLayout, File file, File projectDir, String guidance) throws IOException {
		String perform;

		try (GenAIProvider provider = GenAIProviderManager.getProvider(genai)) {
			systemFunctionTools.applyTools(provider);
			provider.setWorkingDir(getRootDir(projectDir));

			String effectiveInstructions = MessageFormat.format(promptBundle.getString("sys_instructions"), "");
			provider.instructions(effectiveInstructions);

			String docsProcessingInstructions = promptBundle.getString("docs_processing_instructions");
			String osName = System.getProperty("os.name");
			docsProcessingInstructions = MessageFormat.format(docsProcessingInstructions, osName);
			provider.prompt(docsProcessingInstructions);

			String projectInfo = getProjectStructureDescription(projectLayout);
			provider.prompt(projectInfo);

			String currentFile = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
			String guidancePrompt = MessageFormat.format(promptBundle.getString("guidance_file"), currentFile, guidance);
			provider.prompt(guidancePrompt);

			provider.prompt(promptBundle.getString("output_format"));

			String additionalInstructions = StringUtils.trimToNull(this.instructions);
			if (additionalInstructions != null) {
				provider.prompt(additionalInstructions);
			}

			if (isLogInputs()) {
				String inputsFileName = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
				File docsTempDir = new File(projectDir, MACHAI_TEMP_DIR + "/" + GW_TEMP_DIR);
				File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
				File parentDir = inputsFile.getParentFile();
				if (parentDir != null) {
					Files.createDirectories(parentDir.toPath());
				}
				provider.inputsLog(inputsFile);
			}

			perform = provider.perform();
		}

		logger.info("Finished processing file: {}", file.getAbsolutePath());
		return perform;
	}

	private String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<>();

		String path = ProjectLayout.getRelatedPath(rootDir, projectLayout.getProjectDir());

		content.add(path);
		File projectDir = projectLayout.getProjectDir();
		content.add(getDirInfoLine(projectLayout.getSources(), projectDir));
		content.add(getDirInfoLine(projectLayout.getTests(), projectDir));
		content.add(getDirInfoLine(projectLayout.getDocuments(), projectDir));
		content.add(getDirInfoLine(projectLayout.getModules(), projectDir));

		Object[] array = content.toArray(new String[0]);
		return MessageFormat.format(promptBundle.getString("project_information"), array);
	}

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

	private String parseFile(File projectDir, File file) throws IOException {
		if (file == null || !file.isFile()) {
			return null;
		}

		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(file.getName()));
		Reviewer reviewer = reviewMap.get(extension);

		if (reviewer == null) {
			return null;
		}

		return reviewer.perform(getRootDir(projectDir), file);
	}

	private static List<File> listFiles(File dir) throws IOException {
		if (dir == null || !dir.isDirectory()) {
			return Collections.emptyList();
		}
		File[] files = dir.listFiles();
		if (files == null) {
			throw new IOException("Unable to list files for directory: " + dir.getAbsolutePath());
		}
		List<File> result = new ArrayList<>();
		Collections.addAll(result, files);
		return result;
	}

	private List<File> findFiles(File projectDir) throws IOException {
		if (projectDir == null || !projectDir.isDirectory()) {
			return Collections.emptyList();
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

		result.sort(Comparator.comparingInt((File f) -> pathDepth(f.getPath())).reversed());
		return result;
	}

	private static int pathDepth(String path) {
		if (path == null || path.isBlank()) {
			return 0;
		}
		String normalized = path.replace("\\", "/");
		return normalized.split("/").length;
	}

	public File getRootDir(File projectDir) {
		return rootDir != null ? rootDir : projectDir;
	}

	public static boolean deleteTempFiles(File basedir) {
		File file = new File(basedir, FileProcessor.MACHAI_TEMP_DIR + "/" + FileProcessor.GW_TEMP_DIR);
		logger.info("Removing '{}' inputs log file.", file);
		return FileUtils.deleteQuietly(file);
	}

	public boolean isModuleMultiThread() {
		return moduleMultiThread;
	}

	public void setModuleMultiThread(boolean moduleMultiThread) {
		if (!moduleMultiThread) {
			this.moduleMultiThread = false;
			return;
		}

		try (GenAIProvider provider = GenAIProviderManager.getProvider(genai)) {
			if (!provider.isThreadSafe()) {
				throw new IllegalArgumentException(
						"The provider '" + genai + "' is not thread-safe and cannot be used in a multi-threaded context.");
			}
		}
		this.moduleMultiThread = true;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public boolean isLogInputs() {
		return logInputs;
	}

	public void setLogInputs(boolean logInputs) {
		this.logInputs = logInputs;
	}

	public void setInstructionLocations(String[] instructions) throws IOException {
		StringBuilder instructionsText = new StringBuilder();
		if (instructions == null || instructions.length == 0) {
			setInstructions(null);
			return;
		}
		for (String instruction : instructions) {
			if (StringUtils.isBlank(instruction)) {
				continue;
			}
			String content;
			String location = StringUtils.trim(instruction);
			if (location.startsWith("http://") || location.startsWith("https://")) {
				try (InputStream in = new URL(location).openStream()) {
					content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				}
			} else {
				content = Files.readString(new File(location).toPath(), StandardCharsets.UTF_8);
			}
			instructionsText.append(content);
			instructionsText.append(System.lineSeparator());
			instructionsText.append(System.lineSeparator());
		}
		String text = StringUtils.trimToNull(instructionsText.toString());
		setInstructions(text);
	}

	public boolean isNonRecursive() {
		return nonRecursive;
	}

	public void setNonRecursive(boolean nonRecursive) {
		this.nonRecursive = nonRecursive;
	}

	public void setDefaultGuidance(String defaultGuidance) {
		this.defaultGuidance = defaultGuidance;
	}
}
