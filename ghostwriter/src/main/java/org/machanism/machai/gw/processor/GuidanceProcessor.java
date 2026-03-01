package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans a project tree, extracts per-file {@code @guidance} directives through
 * {@link Reviewer}s, and dispatches the resulting prompts to a configured
 * {@link GenAIProvider}.
 *
 * <p>
 * The processor supports single-module and multi-module project layouts. For
 * multi-module builds, modules are processed child-first (each module is
 * scanned before the parent project directory). Processing is traversal-based;
 * it does not attempt to build projects or resolve dependencies.
 * </p>
 */
public class GuidanceProcessor extends AIFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(GuidanceProcessor.class);

	/** Tag name for guidance comments. */
	public static final String GUIDANCE_TAG_NAME = "@" + "guidance:";

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/** Reviewer associations keyed by file extension. */
	private final Map<String, Reviewer> reviewerMap = new HashMap<>();

	/**
	 * Constructs a processor.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param genai        provider key/name to use
	 * @param configurator configuration source
	 */
	public GuidanceProcessor(File rootDir, String genai, Configurator configurator) {
		super(rootDir, configurator, genai);
		logger.info("File processing root directory: {}", rootDir);
		logger.info("GenAI: {}", genai);

		FunctionToolsLoader.getInstance().setConfiguration(configurator);

		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping
	 * supported file extensions to a reviewer.
	 */
	private void loadReviewers() {
		reviewerMap.clear();

		ServiceLoader<Reviewer> reviewerServiceLoader = ServiceLoader.load(Reviewer.class);
		for (Reviewer reviewer : reviewerServiceLoader) {
			String[] extensions = reviewer.getSupportedFileExtensions();
			for (String extension : extensions) {
				String key = normalizeExtensionKey(extension);
				if (key != null) {
					reviewerMap.putIfAbsent(key, reviewer);
				}
			}
		}
	}

	/**
	 * Normalizes a file extension (with or without a leading dot) into a lower-case
	 * lookup key.
	 *
	 * @param extension the extension to normalize (e.g., {@code "java"} or
	 *                  {@code ".java"})
	 * @return normalized key, or {@code null} if the input is blank
	 */
	private static String normalizeExtensionKey(String extension) {
		String value = StringUtils.trimToNull(extension);
		if (value == null) {
			return null;
		}
		if (value.startsWith(".")) {
			value = value.substring(1);
		}
		return value.toLowerCase();
	}

	/**
	 * Scans documents within the specified project root directory and the provided
	 * start subdirectory or pattern, preparing inputs for documentation generation.
	 *
	 * <p>
	 * The {@code scanDir} parameter can be either:
	 * </p>
	 * <ul>
	 * <li>A raw directory name (e.g., {@code src}),</li>
	 * <li>or a pattern string prefixed with {@code glob:} or {@code regex:}, as
	 * supported by {@link java.nio.file.FileSystem#getPathMatcher(String)}.</li>
	 * </ul>
	 *
	 * <p>
	 * If a directory path is provided, it should be relative to the current
	 * processing project. If an absolute path is provided, it must be located
	 * within the {@code projectDir}.
	 * </p>
	 *
	 * @param projectDir the root directory of the project to scan
	 * @param scanDir    the file glob/regex pattern or start directory to scan;
	 *                   must be a relative path with respect to the project, or an
	 *                   absolute path located within {@code projectDir}
	 * @throws IOException              if an error occurs while reading files
	 *                                  during the scan
	 * @throws IllegalArgumentException if the scan path is not located within the
	 *                                  root project directory
	 */
	public void scanDocuments(File projectDir, String scanDir) throws IOException {
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		if (StringUtils.isBlank(scanDir)) {
			throw new IllegalArgumentException("scanDir must not be blank");
		}

		if (!Strings.CS.equals(projectDir.getAbsolutePath(), scanDir)) {

			logger.info("Scan path: {}", scanDir);

			if (!isPathPattern(scanDir)) {
				super.setScanDir(new File(scanDir));
				String relativePath = ProjectLayout.getRelativePath(projectDir, new File(scanDir));
				if (relativePath == null) {
					throw new IllegalArgumentException(
							"Error: The specified scan path must be located within the root project directory: "
									+ projectDir.getAbsolutePath());
				}

				if (getDefaultPrompt() == null) {
					scanDir = "glob:" + relativePath + "{,/**}";
				} else {
					scanDir = "glob:" + relativePath;
				}
			}
			super.setPathMatcher(FileSystems.getDefault().getPathMatcher(scanDir));
		}

		scanFolder(projectDir);
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
		if (getScanDir() != null) {
			logger.info("Starting scan of directory: {}", getScanDir());
		}

		ProjectLayout projectLayout = getProjectLayout(projectDir);
		if (!isNonRecursive()) {
			List<String> modules = projectLayout.getModules();

			if (modules != null && !modules.isEmpty()) {
				if (isModuleMultiThread()) {
					processModulesMultiThreaded(projectDir, modules);
				} else {
					for (String module : modules) {
						processModule(projectDir, module);
					}
				}
			}
		}

		processParentFiles(projectLayout);
	}

	/**
	 * Applies matching logic and default-guidance behavior.
	 *
	 * @param file       candidate file/directory
	 * @param projectDir current project directory
	 * @return {@code true} when the candidate should be processed
	 */
	@Override
	protected boolean match(File file, File projectDir) {
		if (getPathMatcher() == null) {
			return getDefaultPrompt() == null || Objects.equals(file, projectDir);
		}

		return super.match(file, projectDir);
	}

	/**
	 * Processes all discovered modules concurrently.
	 *
	 * @param projectDir the parent project directory
	 * @param modules    module relative paths
	 */
	private void processModulesMultiThreaded(File projectDir, List<String> modules) {
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(modules.size(), getMaxModuleThreads()));
		try {
			List<Future<Void>> futures = new ArrayList<>();
			for (String module : modules) {
				futures.add(executor.submit(() -> {
					processModule(projectDir, module);
					return null;
				}));
			}

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("Thread interrupted while processing modules", e);
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof IOException) {
						throw new IllegalStateException("Module processing failed.", cause);
					}
					if (cause instanceof RuntimeException) {
						throw (RuntimeException) cause;
					}
					throw new IllegalStateException("Module processing failed.", cause);
				}
			}
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(getModuleThreadTimeoutMinutes(), TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Thread interrupted while awaiting module termination", e);
			}
		}
	}

	/**
	 * Processes a module directory.
	 *
	 * <p>
	 * When a scan directory or pattern is configured, modules are only processed
	 * when the module itself matches or contains the scan directory.
	 * </p>
	 *
	 * @param projectDir parent project directory
	 * @param module     module relative path
	 * @throws IOException if scanning the module fails
	 */
	@Override
	protected void processModule(File projectDir, String module) throws IOException {
		if (getScanDir() != null) {
			File moduleDir = new File(projectDir, module);
			String relativePath = ProjectLayout.getRelativePath(moduleDir, getScanDir());
			if (match(moduleDir, projectDir) || relativePath != null) {
				super.processModule(projectDir, module);
			}
		} else {
			super.processModule(projectDir, module);
		}
	}

	/**
	 * Processes files and folders under the parent project directory (excluding
	 * modules).
	 */
	@Override
	protected void processParentFiles(ProjectLayout projectLayout) throws FileNotFoundException, IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = findFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		for (File child : children) {
			processFile(projectLayout, child);
		}

		boolean match = match(projectDir, projectDir);

		if (match && getDefaultPrompt() != null) {
			String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"), projectDir,
					getDefaultPrompt());
			process(projectLayout, projectDir, defaultGuidanceText);
		}
	}

	/**
	 * Extracts guidance for a file and, when present, performs provider processing.
	 *
	 * @param projectLayout project layout
	 * @param file          file to process
	 * @throws IOException if reading the file or provider execution fails
	 */
	@Override
	protected void processFile(ProjectLayout projectLayout, File file) throws IOException {
		String perform = null;

		File projectDir = projectLayout.getProjectDir();
		if (match(file, projectDir)) {
			String guidance = parseFile(projectDir, file);

			if (guidance != null) {
				perform = process(projectLayout, file, guidance);

			} else if (getDefaultPrompt() != null) {
				String defaultGuidanceText = MessageFormat.format(promptBundle.getString("default_guidance"), file,
						getDefaultPrompt());
				perform = process(projectLayout, file, defaultGuidanceText);
			}
		}

		if (StringUtils.isNotBlank(perform)) {
			logger.debug(perform);
		}
	}

	/**
	 * Composes the final prompt and dispatches it to the configured provider.
	 *
	 * @param projectLayout project layout
	 * @param file          file currently being processed
	 * @param guidance      extracted guidance and/or default guidance
	 * @return provider output
	 * @throws IOException if provider execution fails
	 */
	protected String process(ProjectLayout projectLayout, File file, String guidance) throws IOException {
		String effectiveInstructions = MessageFormat.format(promptBundle.getString("sys_instructions"),
				getInstructions());
		String instructions = MessageFormat.format(promptBundle.getString("sys_instructions"), effectiveInstructions);

		StringBuilder guidanceBuilder = new StringBuilder();
		String docsProcessingInstructions = promptBundle.getString("docs_processing_instructions");
		String osName = System.getProperty("os.name");
		docsProcessingInstructions = MessageFormat.format(docsProcessingInstructions, osName);
		guidanceBuilder.append(docsProcessingInstructions).append("\r\n");

		return super.process(projectLayout, file, instructions, guidanceBuilder.toString());
	}

	/**
	 * Uses a {@link Reviewer} (based on file extension) to extract guidance.
	 *
	 * @param projectDir project root directory
	 * @param file       file being parsed
	 * @return guidance text, or {@code null} if the file type is not supported
	 * @throws IOException if the file cannot be read
	 */
	private String parseFile(File projectDir, File file) throws IOException {
		if (!file.isFile()) {
			return null;
		}

		String extension = FilenameUtils.getExtension(file.getName());
		Reviewer reviewer = getReviewerForExtension(extension);
		if (reviewer == null) {
			return null;
		}

		return reviewer.perform(projectDir, file);
	}

	/**
	 * Resolves a reviewer for a given file extension.
	 *
	 * @param extension file extension (with or without a dot)
	 * @return reviewer, or {@code null} if none is registered for that extension
	 */
	private Reviewer getReviewerForExtension(String extension) {
		String key = normalizeExtensionKey(extension);
		if (key == null) {
			return null;
		}
		return reviewerMap.get(key);
	}

	/**
	 * Deletes the input-log temporary directory.
	 *
	 * @param basedir project base directory
	 * @return {@code true} if the directory was deleted, otherwise {@code false}
	 */
	public static boolean deleteTempFiles(File basedir) {
		File file = new File(basedir,
				GuidanceProcessor.MACHAI_TEMP_DIR + File.separator + GuidanceProcessor.GW_TEMP_DIR);
		logger.info("Removing '{}' inputs log file.", file);
		return FileUtils.deleteQuietly(file);
	}

}
