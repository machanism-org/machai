package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that scans files/directories and runs the Ghostwriter
 * guidance pipeline.
 *
 * <p>
 * The command resolves defaults (root directory, GenAI model, guidance and
 * instructions) from the persisted configuration managed by
 * {@link ConfigCommand}, and then delegates processing to
 * {@link GuidanceProcessor}.
 *
 * <h2>Examples</h2>
 * 
 * <pre>
 * gw --scanDirs .\\my-project --excludes target,.git
 * gw --model OpenAI:gpt-5.1 --guidance "Refactor for clarity"
 * gw --instructions "You are a strict code reviewer" --logInputs true
 * </pre>
 */
@ShellComponent
public class GWCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(GWCommand.class);

	private static final int LOG_PREVIEW_LEN = 60;

	/**
	 * Spring lifecycle hook.
	 */
	@PostConstruct
	public void init() {
		// Kept for future initialization.
	}

	// Sonar java:S107 - group parameters into a request object to avoid long parameter
	// lists.
	private static final class GwOptions {
		private int threads;
		private String model;
		private String instructions;
		private String guidance;
		private String excludes;
		private Boolean logInputs;
		private File rootDir;
		private String[] scanDirs;

		private GwOptions() {
			// default
		}

		private GwOptions threads(int threads) {
			this.threads = threads;
			return this;
		}

		private GwOptions model(String model) {
			this.model = model;
			return this;
		}

		private GwOptions instructions(String instructions) {
			this.instructions = instructions;
			return this;
		}

		private GwOptions guidance(String guidance) {
			this.guidance = guidance;
			return this;
		}

		private GwOptions excludes(String excludes) {
			this.excludes = excludes;
			return this;
		}

		private GwOptions logInputs(Boolean logInputs) {
			this.logInputs = logInputs;
			return this;
		}

		private GwOptions rootDir(File rootDir) {
			this.rootDir = rootDir;
			return this;
		}

		private GwOptions scanDirs(String[] scanDirs) {
			this.scanDirs = scanDirs;
			return this;
		}
	}

	/**
	 * Scans and processes directories or files using the configured GenAI model and
	 * guidance.
	 */
	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	public void gw(
			@ShellOption(value = { "-t",
					"--threads" }, help = "Sets the number of threads for concurrent processing.", defaultValue = "1") int threads,
			@ShellOption(value = { "-m",
					"--model" }, help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String model,
			@ShellOption(value = { "-i",
					"--instructions" }, help = "System instructions as text, URL, or file path", defaultValue = ShellOption.NULL) String instructions,
			@ShellOption(value = { "-g",
					"--guidance" }, help = "Default guidance as text, URL, or file path", defaultValue = ShellOption.NULL) String guidance,
			@ShellOption(value = { "-e",
					"--excludes" }, help = "Comma-separated list of directories to exclude", defaultValue = ShellOption.NULL) String excludes,
			@ShellOption(value = { "-l",
					"--logInputs" }, help = "Log LLM request inputs to dedicated log files", defaultValue = ShellOption.NULL) Boolean logInputs,
			@ShellOption(value = { "-r",
					"--rootDir" }, help = "Specify the path to the root directory for file processing.", defaultValue = ShellOption.NULL) File rootDir,
			@ShellOption(value = { "-s",
					"--scanDir" }, help = "Directories to scan.", defaultValue = ShellOption.NULL) String[] scanDirs) {

		GwOptions options = new GwOptions().threads(threads).model(model).instructions(instructions).guidance(guidance)
				.excludes(excludes).logInputs(logInputs).rootDir(rootDir).scanDirs(scanDirs);
		try {
			runGw(options);
		} catch (ProcessTerminationException e) {
			LOGGER.error("Process terminated: {}, Exit code: {}", e.getMessage(), e.getExitCode());
		} catch (Exception e) {
			// Sonar java:S2629 - avoid eager string concatenation in logs.
			LOGGER.error("Unexpected error: {}", e.getMessage(), e);
		} finally {
			GenAIProviderManager.logUsage();
			LOGGER.info("File processing completed.");
		}
	}

	// Sonar java:S3776/java:S1141 - extract complex/nested try logic into focused
	// helpers.
	private void runGw(GwOptions options) throws IOException {
		File rootDir = resolveRootDir(options.rootDir);
		String genaiValue = resolveModel(options.model);
		Boolean logInputs = resolveLogInputs(options.logInputs);
		PromptContext prompts = resolvePrompts(options.instructions, options.guidance);
		String[] dirs = resolveScanDirs(options.scanDirs, rootDir);
		String[] excludesArr = splitExcludes(options.excludes);
		PropertiesConfigurator config = loadMachaiPropertiesConfig();

		for (String scanDir : dirs) {
			// Sonar java:S107 - avoid long parameter list by using a context object.
			ProcessingContext ctx = new ProcessingContext(rootDir, scanDir, genaiValue, config, excludesArr, prompts,
					new ExecutionContext(options.threads, logInputs));
			processSingleScanDir(ctx);
		}
	}

	private PromptContext resolvePrompts(String instructions, String guidance) {
		return new PromptContext(resolveInstructions(instructions), resolveGuidance(guidance));
	}

	// Sonar java:S107 - group processing parameters into a context object.
	private static final class ProcessingContext {
		private final File rootDir;
		private final String scanDir;
		private final String genaiValue;
		private final PropertiesConfigurator config;
		private final String[] excludesArr;
		private final PromptContext prompts;
		private final ExecutionContext execution;

		private ProcessingContext(File rootDir, String scanDir, String genaiValue, PropertiesConfigurator config,
				String[] excludesArr, PromptContext prompts, ExecutionContext execution) {
			this.rootDir = rootDir;
			this.scanDir = scanDir;
			this.genaiValue = genaiValue;
			this.config = config;
			this.excludesArr = excludesArr;
			this.prompts = prompts;
			this.execution = execution;
		}
	}

	// Sonar java:S107 - avoid constructors with too many parameters by grouping.
	private static final class PromptContext {
		private final String instructionsValue;
		private final String defaultGuidance;

		private PromptContext(String instructionsValue, String defaultGuidance) {
			this.instructionsValue = instructionsValue;
			this.defaultGuidance = defaultGuidance;
		}
	}

	// Sonar java:S107 - avoid constructors with too many parameters by grouping.
	private static final class ExecutionContext {
		private final int threads;
		private final Boolean logInputs;

		private ExecutionContext(int threads, Boolean logInputs) {
			this.threads = threads;
			this.logInputs = logInputs;
		}
	}

	private File resolveRootDir(File rootDir) {
		File effectiveRootDir = rootDir;
		if (effectiveRootDir == null) {
			effectiveRootDir = SystemUtils.getUserDir();
		}
		return ConfigCommand.config.getFile(Ghostwriter.GW_ROOTDIR_PROP_NAME, effectiveRootDir);
	}

	private String resolveModel(String model) {
		String genaiValue = ConfigCommand.config.get(Ghostwriter.GW_GENAI_PROP_NAME, null);
		return model != null ? model : genaiValue;
	}

	private Boolean resolveLogInputs(Boolean logInputs) {
		return ConfigCommand.config.getBoolean(Ghostwriter.GW_LOG_INPUTS_PROP_NAME, logInputs);
	}

	private String resolveInstructions(String instructions) {
		String instructionsValue = ConfigCommand.config.get(Ghostwriter.GW_INSTRUCTIONS_PROP_NAME, null);
		if (instructions == null) {
			return instructionsValue;
		}

		instructionsValue = instructions;
		if (instructionsValue.isEmpty()) {
			instructionsValue = readText("No instructions were provided as an option value.\n"
					+ "Please enter the instructions text below. When you are done, press Ctrl+D (or Ctrl+Z on Windows) to finish:");
		}
		return instructionsValue;
	}

	private String resolveGuidance(String guidance) {
		String defaultGuidance = ConfigCommand.config.get(Ghostwriter.GW_GUIDANCE_PROP_NAME, null);
		if (guidance == null) {
			return defaultGuidance;
		}

		defaultGuidance = guidance;
		if (defaultGuidance.isEmpty()) {
			defaultGuidance = readText(
					"Please enter the guidance text below. When finished, press Ctrl+D (or Ctrl+Z on Windows) to signal end of input:");
		}
		return defaultGuidance;
	}

	private String[] resolveScanDirs(String[] scanDirs, File rootDir) {
		if (scanDirs == null || scanDirs.length == 0) {
			return new String[] { rootDir.getAbsolutePath() };
		}
		return scanDirs;
	}

	private String[] splitExcludes(String excludes) {
		return excludes != null ? excludes.split(",") : null;
	}

	private PropertiesConfigurator loadMachaiPropertiesConfig() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		try {
			config.setConfiguration(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
		} catch (IOException e) {
			// The property file is not defined, ignore.
		}
		return config;
	}

	private void processSingleScanDir(ProcessingContext ctx) throws IOException {
		GuidanceProcessor processor = new GuidanceProcessor(ctx.rootDir, ctx.genaiValue, ctx.config);

		if (ctx.excludesArr != null) {
			LOGGER.info("Excludes: {}", Arrays.toString(ctx.excludesArr));
			processor.setExcludes(ctx.excludesArr);
		}

		if (ctx.prompts.instructionsValue != null) {
			// Sonar java:S2629 - abbreviate only when INFO is enabled.
			if (LOGGER.isInfoEnabled()) {
				String abbreviated = org.apache.commons.lang.StringUtils.abbreviate(ctx.prompts.instructionsValue,
						LOG_PREVIEW_LEN);
				LOGGER.info("Instructions: {}", abbreviated);
			}
			processor.setInstructions(ctx.prompts.instructionsValue);
		}

		processor.setDegreeOfConcurrency(ctx.execution.threads);

		if (ctx.prompts.defaultGuidance != null) {
			// Sonar java:S2629 - abbreviate only when INFO is enabled.
			if (LOGGER.isInfoEnabled()) {
				String abbreviated = org.apache.commons.lang.StringUtils.abbreviate(ctx.prompts.defaultGuidance,
						LOG_PREVIEW_LEN);
				LOGGER.info("Default Guidance: {}", abbreviated);
			}
			processor.setDefaultPrompt(ctx.prompts.defaultGuidance);
		}

		processor.setLogInputs(ObjectUtils.getIfNull(ctx.execution.logInputs, false));
		processor.scanDocuments(ctx.rootDir, ctx.scanDir);
		LOGGER.info("Finished scanning directory: {}", ctx.scanDir);
	}

	/**
	 * Reads multi-line text from stdin until EOF is reached.
	 *
	 * @param prompt message to show before reading input
	 * @return the entered text, or {@code null} if no content was provided
	 */
	private String readText(String prompt) {
		// Sonar java:S106 - use logger instead of System.out.
		LOGGER.info(prompt);
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append("\n");
			}
		}
		LOGGER.info("Input complete. Processing your text...");
		// Sonar java:S7158 - use isEmpty() for StringBuilder emptiness.
		return !sb.isEmpty() ? sb.deleteCharAt(sb.length() - 1).toString() : null;
	}
}
