package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.gw.tools.CommandFunctionTools;
import org.machanism.machai.gw.tools.FileFunctionTools;
import org.machanism.machai.gw.tools.WebFunctionTools;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance:
 * Class javadoc description should describe supported functionality and provide examples to use it.
 * If the method used as Javadoc documentation is not public or protected, the method name should not be specified.
 * Functionality:
 *  - describe supported special markers, see javadoc for following constants:
 *  	- GUIDANCE_TAG_NAME
 */
/**
 * Processes project files that contain inline guidance comments and dispatches
 * the extracted instructions to the configured AI provider.
 * <p>
 * The processor scans project files and modules selected by the configured path
 * matcher. For supported file types, it uses {@link Reviewer} implementations
 * discovered through {@link ServiceLoader} to extract mandatory guidance
 * instructions from source comments. If a default prompt is configured,
 * matching files without explicit guidance can still be processed by applying
 * that default prompt.
 * </p>
 * <p>
 * Guidance comments are identified by the special marker
 * {@link #GUIDANCE_TAG_NAME}. Reviewers are responsible for preserving marker
 * comments in their original source locations while allowing the provider to
 * update surrounding content. Processing results are collected in
 * {@link #getReport()} as relative file paths and provider messages.
 * </p>
 * <h2>Examples</h2>
 * 
 * <pre>{@code
 * Configurator configurator = ...;
 * GuidanceProcessor processor = new GuidanceProcessor(new File("."), "my-model", configurator);
 * processor.process(projectLayout, new File("src/main/java/App.java"), "Ensure documentation is current.");
 * List<Map<String, Object>> report = processor.getReport();
 * }</pre>
 * <p>
 * A supported source file may include a guidance block such as:
 * </p>
 * 
 * <pre>{@code
 * /*
 *  * &#64;guidance: Keep this class documented and ensure examples compile.
 * *&#47;
 * public class App {
 * }
 * }</pre>
 */
public class GuidanceProcessor extends AIFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(GuidanceProcessor.class);

	/**
	 * Special comment marker used to identify guidance blocks inside supported
	 * files.
	 * <p>
	 * A guidance block begins with this marker and contains mandatory processing
	 * instructions for the AI provider. For example, Java reviewers can extract
	 * comments that start with {@code /*@guidance:} and pass their contents to this
	 * processor. The marker itself must remain unchanged in processed files so
	 * future runs can discover the same guidance.
	 * </p>
	 */
	public static final String GUIDANCE_TAG_NAME = "@" + "guidance:";

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/** Reviewer associations keyed by file extension. */
	private final Map<String, Reviewer> reviewerMap = new HashMap<>();

	private final List<Map<String, Object>> report = new ArrayList<>();

	/**
	 * Constructs a new {@code GuidanceProcessor} for processing files with guidance
	 * tags.
	 * <p>
	 * Initializes the processor with the specified root directory, GenAI model
	 * identifier, and configuration. Logs the root directory and GenAI model (if
	 * provided), and loads reviewer information for guidance processing.
	 * </p>
	 *
	 * @param rootDir      the root directory to scan for files
	 * @param genai        the GenAI model identifier to use for processing (may be
	 *                     {@code null})
	 * @param configurator the configuration object for property resolution and
	 *                     runtime settings
	 */
	public GuidanceProcessor(File rootDir, String genai, Configurator configurator) {
		super(rootDir, configurator, genai);
		logger.info("File processing root directory: {}, Model: {}", rootDir, genai);
		loadReviewers();
	}

	/**
	 * Loads file reviewers via the {@link ServiceLoader} registry, mapping
	 * supported file extensions to a reviewer.
	 */
	void loadReviewers() {
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
	static String normalizeExtensionKey(String extension) {
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
		if (getPath() != null) {
			File moduleDir = new File(projectDir, module);
			String relativePath = ProjectLayout.getRelativePath(moduleDir, getPath());
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
	protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = listFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		for (File child : children) {
			processFile(projectLayout, child);
		}

		boolean match = match(projectDir, projectDir);

		if (match && getDefaultPrompt() != null) {
			String perform = process(projectLayout, projectDir, getDefaultPrompt());
			if (StringUtils.isNoneBlank(perform)) {
				logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, perform);
			}
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

			String guidance_rules = promptBundle.getString("guidance_rules");
			String processInfo = getProcessInfo(projectLayout, file);
			if (guidance != null) {
				perform = process(projectLayout, file, getInstructions(), processInfo, guidance_rules, guidance);

			} else if (getDefaultPrompt() != null) {
				perform = process(projectLayout, file, getInstructions(), processInfo, getDefaultPrompt());
			}
		}

		if (StringUtils.isNoneBlank(perform)) {
			logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, perform);
		}
	}

	/**
	 * Composes the final prompt and dispatches it to the configured provider.
	 *
	 * @param projectLayout project layout
	 * @param file          file currently being processed
	 * @param guidance      extracted guidance and/or default guidance
	 * @return provider output
	 */
	@Override
	public String process(ProjectLayout projectLayout, File file, String guidance) {

		String instructions = getInstructions();
		String guidance_rules = promptBundle.getString("guidance_rules");

		String result = super.process(projectLayout, file, instructions, guidance_rules, guidance);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("file", ProjectLayout.getRelativePath(getRootDir(), file));
		resultMap.put("message", Objects.toString(result, "Guidanced file processing finished."));
		getReport().add(resultMap);

		return result;
	}

	/**
	 * Returns the current base instructions used for processing.
	 * 
	 * @return the configured instruction text
	 */
	public String getInstructions() {
		String instructions = super.getInstructions();

		if (instructions == null) {
			instructions = promptBundle.getString("guidance_sys_instructions");
		}

		return instructions;
	}

	/**
	 * Uses a {@link Reviewer} (based on file extension) to extract guidance.
	 *
	 * @param projectDir project root directory
	 * @param file       file being parsed
	 * @return guidance text, or {@code null} if the file type is not supported
	 * @throws IOException if the file cannot be read
	 */
	String parseFile(File projectDir, File file) throws IOException {
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
	Reviewer getReviewerForExtension(String extension) {
		String key = normalizeExtensionKey(extension);
		if (key == null) {
			return null;
		}
		return reviewerMap.get(key);
	}

	protected void applyTools(String instructions, String[] prompts, Genai provider, String[] tools) {
		if (tools != null && tools.length != 0 && tools[0].equals("auto")) {
			tools = new String[] { CommandFunctionTools.class.getName(), FileFunctionTools.class.getName(),
					WebFunctionTools.class.getName() };
		}
		super.applyTools(instructions, prompts, provider, tools);
	}

	/**
	 * @return the report
	 */
	public List<Map<String, Object>> getReport() {
		return report;
	}

}
