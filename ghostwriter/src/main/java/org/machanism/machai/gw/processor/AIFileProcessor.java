package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for processors that build prompts and execute a configured
 * {@link GenAIProvider} against project files.
 *
 * <p>
 * This type extends {@link AbstractFileProcessor} with the mechanics required
 * to invoke a GenAI provider:
 * </p>
 * <ul>
 * <li>create a provider using {@link GenAIProviderManager},</li>
 * <li>apply registered function tools via {@link FunctionToolsLoader},</li>
 * <li>optionally log the composed provider inputs for auditing/debugging,
 * and</li>
 * <li>provide helper methods for prompt templates (e.g., project-structure
 * description).</li>
 * </ul>
 *
 * <p>
 * Subclasses typically decide what files to process and how guidance is
 * derived; this class focuses on orchestration and provider execution.
 * </p>
 */
public class AIFileProcessor extends AbstractFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(AIFileProcessor.class);

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * String used in generated output when a value is absent in project metadata.
	 */
	public static final String NOT_DEFINED = "not defined";

	/**
	 * Temporary directory name for documentation inputs under
	 * {@link #MACHAI_TEMP_DIR}.
	 */
	public static final String GW_TEMP_DIR = "docs-inputs";

	/** Provider key/name (including model) used when creating GenAI providers. */
	private final String genai;

	/** Whether to persist the composed inputs to a per-file log. */
	private boolean logInputs;

	/**
	 * Creates a new processor using the given provider key.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param configurator configuration source
	 * @param genai        provider key/name (including model)
	 */
	public AIFileProcessor(File rootDir, Configurator configurator, String genai) {
		super(rootDir, configurator);
		this.genai = genai;
	}

	/**
	 * Creates a provider and performs a full prompt run for the given file.
	 *
	 * @param projectLayout project layout
	 * @param file          file being processed (used for logging and templating)
	 * @param guidance      guidance content to include in the prompt
	 * @param instructions  system or execution instructions for the provider
	 * @return provider output
	 * @throws IOException if creating input logs fails or provider I/O fails
	 */
	public String process(ProjectLayout projectLayout, File file, String instructions, String guidance)
			throws IOException {
		logger.info("Processing file: '{}'", file);

		GenAIProvider provider = GenAIProviderManager.getProvider(genai, getConfigurator());

		FunctionToolsLoader.getInstance().applyTools(provider);

		File projectDir = projectLayout.getProjectDir();
		provider.setWorkingDir(projectDir);

		provider.instructions(instructions);
		provider.prompt(guidance);

		if (isLogInputs()) {
			String inputsFileName = ProjectLayout.getRelativePath(getRootDir(), file);
			File docsTempDir = new File(getRootDir(), MACHAI_TEMP_DIR + File.separator + GW_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			File parentDir = inputsFile.getParentFile();
			if (parentDir != null) {
				Files.createDirectories(parentDir.toPath());
			}
			provider.inputsLog(inputsFile);
		}

		String perform = provider.perform();

		logger.info("Finished processing file: {}", file.getAbsolutePath());
		return perform;
	}

	/**
	 * Builds a human-readable description of the project structure used in prompts.
	 *
	 * @param projectLayout current project layout
	 * @return formatted project information block
	 * @throws IOException if computing relative paths fails
	 */
	public String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
		List<String> content = new ArrayList<>();

		File projectDir = projectLayout.getProjectDir();

		List<String> sources = projectLayout.getSources();
		List<String> tests = projectLayout.getTests();
		List<String> documents = projectLayout.getDocuments();
		List<String> modules = projectLayout.getModules();

		content.add(projectLayout.getProjectName() != null ? "`" + projectLayout.getProjectName() + "`" : NOT_DEFINED);
		content.add(projectLayout.getProjectId());

		String relativePath = ProjectLayout.getRelativePath(getRootDir(), projectDir);
		content.add(relativePath);

		content.add(projectLayout.getProjectLayoutType());
		content.add(getDirInfoLine(sources, projectDir));
		content.add(getDirInfoLine(tests, projectDir));
		content.add(getDirInfoLine(documents, projectDir));
		content.add(getDirInfoLine(modules, projectDir));

		Object[] array = content.toArray(new String[0]);
		return MessageFormat.format(promptBundle.getString("project_information"), array);
	}

	/**
	 * Produces a formatted list of existing directories from the provided list.
	 *
	 * @param sources    directory list from the layout
	 * @param projectDir project root directory
	 * @return formatted directory list, or {@link #NOT_DEFINED} if none apply
	 */
	private String getDirInfoLine(List<String> sources, File projectDir) {
		String line = null;
		if (sources != null && !sources.isEmpty()) {
			List<String> dirs = sources.stream().filter(t -> t != null && new File(projectDir, t).exists())
					.map(e -> "`" + e + "`").collect(Collectors.toList());
			line = StringUtils.join(dirs, ", ");
		}

		if (StringUtils.isBlank(line)) {
			line = NOT_DEFINED;
		}
		return line;
	}

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * <p>
	 * When enabling, this method verifies that the configured provider is
	 * thread-safe.
	 * </p>
	 *
	 * @param moduleMultiThread {@code true} to enable, {@code false} to disable
	 * @throws IllegalArgumentException if enabling is requested but the provider is
	 *                                  not thread-safe
	 */
	@Override
	public void setModuleMultiThread(boolean moduleMultiThread) {
		if (moduleMultiThread) {
			GenAIProvider provider = GenAIProviderManager.getProvider(genai, getConfigurator());
			if (!provider.isThreadSafe()) {
				throw new IllegalArgumentException(
						"The provider '" + genai
								+ "' is not thread-safe and cannot be used in a multi-threaded context.");
			}
		}
		super.setModuleMultiThread(moduleMultiThread);
	}

	/**
	 * Returns whether composed prompt inputs are logged to files.
	 *
	 * @return {@code true} when input logging is enabled
	 */
	public boolean isLogInputs() {
		return logInputs;
	}

	/**
	 * Enables or disables logging of composed prompt inputs.
	 *
	 * @param logInputs {@code true} to log inputs, otherwise {@code false}
	 */
	public void setLogInputs(boolean logInputs) {
		this.logInputs = logInputs;
	}
}
