package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jline.reader.LineReader;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
 * gw --scanDir .\\my-project --excludes target,.git
 * gw --model OpenAI:gpt-5.1 --guidance "Refactor for clarity"
 * gw --instructions "You are a strict code reviewer" --logInputs true
 * </pre>
 */
@ShellComponent
public class GWCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(GWCommand.class);

	private static final int LOG_PREVIEW_LEN = 60;

	private final LineReader lineReader;

	/**
	 * Creates a new command instance.
	 *
	 * @param lineReader JLine reader used to prompt the user in interactive mode
	 */
	public GWCommand(@Lazy LineReader lineReader) {
		super();
		this.lineReader = lineReader;
	}

	/**
	 * Spring lifecycle hook.
	 */
	@PostConstruct
	public void init() {
		// Kept for future initialization.
	}

	/**
	 * Internal option container used to avoid a long parameter list between
	 * methods.
	 */
	private static final class GwOptions {
		private int threads;
		private String model;
		private String instructions;
		private String guidance;
		private String excludes;
		private Boolean logInputs;
		private File projectDir;
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

		private GwOptions projectDir(File projectDir) {
			this.projectDir = projectDir;
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
	 *
	 * @param threads      number of threads for concurrent processing
	 * @param model        GenAI provider and model identifier (for example,
	 *                     {@code OpenAI:gpt-5.1}); if {@code null}, uses the
	 *                     configured default
	 * @param instructions system instructions as text, URL, or file path; if
	 *                     {@code null}, uses the configured default
	 * @param guidance     default guidance as text, URL, or file path; if
	 *                     {@code null}, uses the configured default
	 * @param excludes     comma-separated list of directories to exclude; may be
	 *                     {@code null}
	 * @param logInputs    whether to log LLM request inputs to dedicated log files;
	 *                     if {@code null}, uses the configured default
	 * @param projectDir   root directory for file processing; if {@code null}, uses
	 *                     the configured default or the current working directory
	 * @param scanDirs     directories to scan; if {@code null} or empty, scans the
	 *                     resolved {@code projectDir}
	 */
	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	/**
	 * FalsePositive
	 * Method signature is dictated by Spring Shell option binding; grouping would
	 * reduce CLI UX.
	 */
	@SuppressWarnings("java:S107")
	public void gw(
			@ShellOption(value = { "-t",
					"--threads" }, help = "Sets the number of threads for concurrent processing.", defaultValue = "1") int threads,
			@ShellOption(value = { "-m",
					Ghostwriter.MODEL_PROP_NAME }, help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String model,
			@ShellOption(value = { "-i",
					"--instructions" }, help = "System instructions as text, URL, or file path", defaultValue = ShellOption.NULL) String instructions,
			@ShellOption(value = { "-g",
					"--guidance" }, help = "Default guidance as text, URL, or file path", defaultValue = ShellOption.NULL) String guidance,
			@ShellOption(value = { "-e",
					"--excludes" }, help = "Comma-separated list of directories to exclude", defaultValue = ShellOption.NULL) String excludes,
			@ShellOption(value = { "-l",
					"--" + Genai.LOG_INPUTS_PROP_NAME }, help = "Log LLM request inputs to dedicated log files", defaultValue = ShellOption.NULL) Boolean logInputs,
			@ShellOption(value = { "-d",
					ProjectLayout.PROJECT_DIR_PROP_NAME }, help = "Specify the path to the root directory for file processing.", defaultValue = ShellOption.NULL) File projectDir,
			@ShellOption(value = { "-s",
					"--scanDir" }, help = "Directories to scan.", defaultValue = ShellOption.NULL) String[] scanDirs) {

		GwOptions options = new GwOptions().threads(threads).model(model).instructions(instructions).guidance(guidance)
				.excludes(excludes).logInputs(logInputs).projectDir(projectDir).scanDirs(scanDirs);
		try {
			runGw(options);
		} catch (ProcessTerminationException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Process terminated: {}, Exit code: {}", e.getMessage(), e.getExitCode());
			}
		} catch (Exception e) {
			LOGGER.error("Unexpected error: {}", e.getMessage(), e);
		} finally {
			GenaiProviderManager.logUsage();
			LOGGER.info("File processing completed.");
		}
	}

	private void runGw(GwOptions options) throws IOException {
		File projectDir = resolveProjectDir(options.projectDir);
		String genaiValue = resolveModel(options.model);
		Boolean logInputs = resolveLogInputs(options.logInputs);
		PromptContext prompts = resolvePrompts(options.instructions, options.guidance);
		String[] dirs = resolveScanDirs(options.scanDirs, projectDir);
		String[] excludesArr = splitExcludes(options.excludes);
		PropertiesConfigurator config = loadMachaiPropertiesConfig();

		for (String scanDir : dirs) {
			ProcessingContext ctx = new ProcessingContext(projectDir, scanDir, genaiValue, config, excludesArr, prompts,
					new ExecutionContext(options.threads, logInputs));
			processSingleScanDir(ctx);
		}
	}

	private PromptContext resolvePrompts(String instructions, String guidance) {
		return new PromptContext(resolveInstructions(instructions), resolveGuidance(guidance));
	}

	/**
	 * Per-scan directory processing context.
	 */
	private static final class ProcessingContext {
		private final File projectDir;
		private final String scanDir;
		private final String genaiValue;
		private final PropertiesConfigurator config;
		private final String[] excludesArr;
		private final PromptContext prompts;
		private final ExecutionContext execution;

		private ProcessingContext(File projectDir, String scanDir, String genaiValue, PropertiesConfigurator config,
				String[] excludesArr, PromptContext prompts, ExecutionContext execution) {
			this.projectDir = projectDir;
			this.scanDir = scanDir;
			this.genaiValue = genaiValue;
			this.config = config;
			this.excludesArr = excludesArr;
			this.prompts = prompts;
			this.execution = execution;
		}
	}

	/**
	 * Pair of prompts used by Ghostwriter.
	 */
	private static final class PromptContext {
		private final String instructionsValue;
		private final String defaultGuidance;

		private PromptContext(String instructionsValue, String defaultGuidance) {
			this.instructionsValue = instructionsValue;
			this.defaultGuidance = defaultGuidance;
		}
	}

	/**
	 * Execution settings for Ghostwriter processing.
	 */
	private static final class ExecutionContext {
		private final int threads;
		private final Boolean logInputs;

		private ExecutionContext(int threads, Boolean logInputs) {
			this.threads = threads;
			this.logInputs = logInputs;
		}
	}

	private File resolveProjectDir(File projectDir) {
		File effectiveProjectDir = projectDir;
		if (effectiveProjectDir == null) {
			effectiveProjectDir = SystemUtils.getUserDir();
		}
		return ConfigCommand.config.getFile(ProjectLayout.PROJECT_DIR_PROP_NAME, effectiveProjectDir);
	}

	private String resolveModel(String model) {
		String genaiValue = ConfigCommand.config.get(Ghostwriter.MODEL_PROP_NAME, null);
		return model != null ? model : genaiValue;
	}

	private Boolean resolveLogInputs(Boolean logInputs) {
		return ConfigCommand.config.getBoolean(Genai.LOG_INPUTS_PROP_NAME, logInputs);
	}

	private String resolveInstructions(String instructions) {
		String instructionsValue = ConfigCommand.config.get(Ghostwriter.INSTRUCTIONS_PROP_NAME, null);
		if (instructions == null) {
			return instructionsValue;
		}

		instructionsValue = instructions;
		if (instructionsValue.isEmpty()) {
			instructionsValue = lineReader.readLine("Instructions: ");
		}
		return instructionsValue;
	}

	private String resolveGuidance(String guidance) {
		String defaultGuidance = ConfigCommand.config.get(Ghostwriter.GUIDANCE_PROP_NAME, null);
		if (guidance == null) {
			return defaultGuidance;
		}

		defaultGuidance = guidance;
		if (defaultGuidance.isEmpty()) {
			defaultGuidance = lineReader.readLine("Guidance: ");
		}
		return defaultGuidance;
	}

	private String[] resolveScanDirs(String[] scanDirs, File projectDir) {
		if (scanDirs == null || scanDirs.length == 0) {
			return new String[] { projectDir.getAbsolutePath() };
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
		GuidanceProcessor processor = new GuidanceProcessor(ctx.projectDir, ctx.genaiValue, ctx.config);

		if (ctx.excludesArr != null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Excludes: {}", Arrays.toString(ctx.excludesArr));
			}
			processor.setExcludes(ctx.excludesArr);
		}

		if (ctx.prompts.instructionsValue != null) {
			if (LOGGER.isInfoEnabled()) {
				String abbreviated = org.apache.commons.lang.StringUtils.abbreviate(ctx.prompts.instructionsValue,
						LOG_PREVIEW_LEN);
				LOGGER.info("Instructions: {}", abbreviated);
			}
			processor.setInstructions(ctx.prompts.instructionsValue);
		}

		processor.setDegreeOfConcurrency(ctx.execution.threads);

		if (ctx.prompts.defaultGuidance != null) {
			if (LOGGER.isInfoEnabled()) {
				String abbreviated = org.apache.commons.lang.StringUtils.abbreviate(ctx.prompts.defaultGuidance,
						LOG_PREVIEW_LEN);
				LOGGER.info("Default Guidance: {}", abbreviated);
			}
			processor.setDefaultPrompt(ctx.prompts.defaultGuidance);
		}

		processor.setLogInputs(ObjectUtils.getIfNull(ctx.execution.logInputs, false));
		processor.scanDocuments(ctx.projectDir, ctx.scanDir);
		LOGGER.info("Finished scanning directory: {}", ctx.scanDir);
	}

}
