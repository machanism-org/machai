package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line entry point for Ghostwriter.
 *
 * <p>
 * This CLI bootstraps configuration, parses command-line options, and invokes a
 * processor to scan a directory tree for supported files. For each file,
 * Ghostwriter extracts embedded {@code @guidance} directives and submits the
 * resulting prompt to the configured GenAI provider.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * java -jar gw.jar <scanDir> [options]
 * }</pre>
 *
 * <p>
 * The {@code <scanDir>} argument may be a directory path (relative to the
 * configured project root), or a {@code glob:} / {@code regex:} expression as
 * supported by {@link java.nio.file.FileSystem#getPathMatcher(String)}.
 * </p>
 */
public final class Ghostwriter {

	/**
	 * Continuation marker used by {@link #readText(String)} to allow users to enter
	 * multi-line values via standard input.
	 */
	public static final String MULTIPLE_LINES_BREAKER = "\\";

	private static final String GUIDANCE_OPTION = "guidance";

	/** Logger for the Ghostwriter application. */
	private static Logger logger;

	/** Default Ghostwriter properties file name. */
	public static final String GW_PROPERTIES_FILE_NAME = "gw.properties";

	/** System property key to override the Ghostwriter configuration file path. */
	public static final String GW_CONFIG_PROP_NAME = "gw.config";

	/** System property key indicating the Ghostwriter home directory. */
	public static final String GW_HOME_PROP_NAME = "gw.home";

	/** Configuration key specifying the GenAI provider/model to use. */
	public static final String GW_MODEL_PROP_NAME = "gw.model";

	/** Configuration key containing optional system instructions. */
	public static final String INSTRUCTIONS_PROP_NAME = "instructions";

	/** Configuration key containing comma-separated scan exclusions. */
	public static final String GW_EXCLUDES_PROP_NAME = "gw.excludes";

	/** Configuration key containing comma-separated scan exclusions. */
	public static final String GW_ACTS_PROP_NAME = "gw.acts";

	/** Configuration key containing comma-separated scan exclusions. */
	public static final String GW_ACT_PROP_NAME = "gw.act";

	/** Configuration key containing default guidance. */
	public static final String GW_GUIDANCE_PROP_NAME = "gw.guidance";

	/** Configuration key enabling multi-threaded processing. */
	public static final String GW_THREADS_PROP_NAME = "gw.threads";

	/** Configuration key specifying a default scan directory/pattern. */
	public static final String GW_SCAN_DIR_PROP_NAME = "gw.scanDir";

	public static final String GW_NONRECURSIVE_PROP_NAME = "gw.nonRecursive";

	public static final String INPUTS_PROPERTY_NAME = "inputs";
	
	public static final String INTERACTIVE_MODE_PROP_NAME = "gw.interactive";

	/** Processor implementation used by this CLI instance. */
	private final AIFileProcessor processor;

	/**
	 * Creates a new Ghostwriter CLI instance.
	 *
	 * @param genai     provider key/name (including model), e.g.
	 *                  {@code OpenAI:gpt-5.1}
	 * @param processor processor implementation
	 */
	public Ghostwriter(String genai, AIFileProcessor processor) {
		if (StringUtils.isBlank(genai)) {
			throw new IllegalArgumentException("No GenAI provider/model configured. Set '" + GW_MODEL_PROP_NAME
					+ "' in " + GW_PROPERTIES_FILE_NAME + " or pass -m/--model (e.g., OpenAI:gpt-5.1).");
		}

		this.processor = processor;
	}

	/**
	 * Performs a scan of the provided scan directories/patterns.
	 *
	 * @param scanDirs scan directory arguments
	 * @return exit code (0 for success)
	 * @throws IOException if scanning fails while reading files
	 */
	public int perform(String[] scanDirs) throws IOException {
		int exitCode = 0;
		try {
			for (String scanDir : scanDirs) {
				logger.info("Starting scan of directory: {}", scanDir);
				File projectDir = processor.getProjectDir();

				processor.scanDocuments(projectDir, scanDir);
				logger.info("Finished scanning directory: {}", scanDir);
			}

		} catch (ProcessTerminationException e) {
			logger.error("Process terminated: {}, Exit code: {}", e.getMessage(), e.getExitCode());
			exitCode = e.getExitCode();
		} catch (IllegalArgumentException e) {
			logger.error("Error: {}", e.getMessage(), e);
			exitCode = 1;
		} catch (Exception e) {
			logger.error("Unexpected error: {}", e.getMessage(), e);
			exitCode = 1;
		} finally {
			GenaiProviderManager.logUsage();
			logger.info("File processing completed.");
		}

		return exitCode;
	}

	/**
	 * Initializes Ghostwriter configuration.
	 *
	 * <p>
	 * The configuration file defaults to {@link #GW_PROPERTIES_FILE_NAME} in the
	 * resolved home directory, but can be overridden via the
	 * {@link #GW_CONFIG_PROP_NAME} system property.
	 * </p>
	 *
	 * @param projectDir optional root directory used to resolve the home directory
	 * @return initialized configuration
	 */
	static PropertiesConfigurator initializeConfiguration(File projectDir) {
		PropertiesConfigurator config = new PropertiesConfigurator();

		File gwHomeDir = config.getFile(GW_HOME_PROP_NAME, null);
		if (gwHomeDir == null) {
			gwHomeDir = projectDir;
			if (gwHomeDir == null) {
				gwHomeDir = SystemUtils.getUserDir();
			}
		}

		System.setProperty(GW_HOME_PROP_NAME, gwHomeDir.getAbsolutePath());
		logger = LoggerFactory.getLogger(Ghostwriter.class);

		String version = Ghostwriter.class.getPackage().getImplementationVersion();
		if (version != null) {
			logger.info("Ghostwriter {} (Machai project)", version);
		}
		logger.info("Home directory: {}", gwHomeDir);

		try {
			File configFile = new File(gwHomeDir, System.getProperty(GW_CONFIG_PROP_NAME, GW_PROPERTIES_FILE_NAME));
			config.setConfiguration(configFile.getAbsolutePath());
		} catch (IOException e) {
			// The property file is not defined, ignore.
		} catch (RuntimeException e) {
			logger.warn("Failed to initialize configuration.", e);
		}

		return config;
	}

	/**
	 * Prints the Ghostwriter CLI help text, including usage instructions, option
	 * descriptions, and example commands.
	 *
	 * @param options the configured CLI options to display in the help message
	 */
	static void help(Options options) {
		String header = "\nGhostwriter CLI - Scan and process directories or files using GenAI guidance.\n\n"
				+ "Usage:\n  java -jar gw.jar <scanDir> [options]\n\n"
				+ "  <scanDir> specifies the scanning path or pattern.\n"
				+ "    - Use a relative path with respect to the current project directory.\n"
				+ "    - If an absolute path is provided, it must be located within the root project directory.\n"
				+ "    - Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex patterns (e.g., \"regex:^.*/[^/]+\\.java$\").\n\n"
				+ "Options:";
		String footer = "\nExamples:\n" + "  java -jar gw.jar C:\\\\projects\\\\project\n"
				+ "  java -jar gw.jar src\\project\n" + "  java -jar gw.jar \"glob:**/*.java\"\n"
				+ "  java -jar gw.jar \"regex:^.*/[^/]+\\.java$\"\n";

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar gw.jar <scanDir> [options]", header, options, footer, true);
	}

	/**
	 * Reads multi-line text from standard input.
	 *
	 * <p>
	 * Input is read until a line does not end with {@link #MULTIPLE_LINES_BREAKER}
	 * or until EOF.
	 * </p>
	 *
	 * @param prompt prompt to display before reading
	 * @return the entered text (never {@code null})
	 */
	@SuppressWarnings("java:S106")
	public static String readText(String prompt) {
		System.out.print(prompt);

		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				String nextLine = scanner.nextLine();
				if (Strings.CS.endsWith(nextLine, MULTIPLE_LINES_BREAKER)) {
					sb.append(StringUtils.substringBeforeLast(nextLine, MULTIPLE_LINES_BREAKER)).append(Genai.LINE_SEPARATOR);
					logger.info("\t");
				} else {
					sb.append(nextLine);
					break;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Sets file/directory excludes.
	 *
	 * @param excludes exclude list
	 */
	public void setExcludes(String[] excludes) {
		if (excludes != null) {
			logger.info("Excludes: {}", Arrays.toString(excludes));
			processor.setExcludes(excludes);
		}
	}

	/**
	 * Sets the system instructions used for provider execution.
	 *
	 * @param instructions instruction text, URL, or {@code file:} reference
	 */
	public void setInstructions(String instructions) {
		if (instructions != null) {
			if (logger.isInfoEnabled()) {
				// Sonar java:S2629 - avoid eager string construction when INFO is disabled.
				logger.info("Instructions: {}", StringUtils.abbreviate(instructions, 60));
			}
			processor.setInstructions(instructions);
		}
	}

	/**
	 * Sets default guidance applied when embedded guidance tag directives are not
	 * present.
	 *
	 * @param defaultGuidance default guidance text, URL, or {@code file:} reference
	 */
	public void setDefaultPrompt(String defaultGuidance) {
		if (defaultGuidance != null) {
			processor.setDefaultPrompt(defaultGuidance);
		}
	}

	/**
	 * Enables or disables request input logging.
	 *
	 * @param logInputs {@code true} to log inputs
	 */
	public void setLogInputs(boolean logInputs) {
		processor.setLogInputs(logInputs);
	}

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * @param multiThreadCount the configured worker count
	 */
	public void setDegreeOfConcurrency(String multiThreadCount) {
		if (multiThreadCount != null) {
			int value = Integer.parseInt(multiThreadCount);
			processor.setDegreeOfConcurrency(value);
		}
	}

	static AIFileProcessor createProcessor(CommandLine cmd, File projectDir, PropertiesConfigurator config,
			String genai) {
		AIFileProcessor processor;
		String defaultPrompt;
		if (cmd.hasOption("act")) {
			processor = createActProcessor(cmd, projectDir, config, genai);
			defaultPrompt = resolveActPrompt(cmd, config);
			logDefaultPrompt("Act", defaultPrompt);
		} else {
			processor = new GuidanceProcessor(projectDir, genai, config);
			defaultPrompt = resolveGuidancePrompt(cmd, config);
			logDefaultPrompt("Default Prompt", defaultPrompt);
		}
		processor.setDefaultPrompt(defaultPrompt);
		return processor;
	}

	static AIFileProcessor createActProcessor(CommandLine cmd, File projectDir, PropertiesConfigurator config,
			String genai) {
		ActProcessor processor = new ActProcessor(projectDir, config, genai);

		// Sonar java:S1854 - avoid useless assignment; only read when needed.
		if (cmd.hasOption("acts")) {
			String acts = cmd.getOptionValue("acts");
			logger.info("Custom acts location specified: {}", acts);
			processor.setActsLocation(acts);
		} else {
			String acts = config.get(GW_ACTS_PROP_NAME, null);
			if (acts != null) {
				processor.setActsLocation(acts);
			}
		}
		return processor;
	}

	static String resolveActPrompt(CommandLine cmd, PropertiesConfigurator config) {
		String defaultPrompt = config.get(GW_ACT_PROP_NAME, null);
		if (cmd.hasOption("act")) {
			defaultPrompt = cmd.getOptionValue("act");
			if (defaultPrompt == null) {
				defaultPrompt = readText("Act");
			}
		}
		return defaultPrompt;
	}

	static String resolveGuidancePrompt(CommandLine cmd, PropertiesConfigurator config) {
		String defaultPrompt = config.get(GW_GUIDANCE_PROP_NAME, null);
		if (cmd.hasOption(GUIDANCE_OPTION)) {
			defaultPrompt = cmd.getOptionValue(GUIDANCE_OPTION);
			if (defaultPrompt == null) {
				defaultPrompt = readText("Guidance");
			}
		}
		return defaultPrompt;
	}

	static void logDefaultPrompt(String label, String prompt) {
		if (prompt != null && logger.isInfoEnabled()) {
			logger.info("{}: {}", label, StringUtils.abbreviate(prompt, 60));
		}
	}

	static String[] resolveScanDirs(CommandLine cmd, PropertiesConfigurator config) {
		String[] scanDirs = cmd.getArgs();
		if (scanDirs == null || scanDirs.length == 0) {
			String gwScanDir = config.get(GW_SCAN_DIR_PROP_NAME, null);
			if (gwScanDir != null) {
				scanDirs = new String[] { gwScanDir };
			}
		}
		if (scanDirs == null || scanDirs.length == 0) {
			scanDirs = new String[] { SystemUtils.getUserDir().getAbsolutePath() };
		}
		return scanDirs;
	}

	/**
	 * Main entry point.
	 *
	 * @param args command line arguments
	 * @throws IOException    if scanning fails while reading files
	 * @throws ParseException if command-line arguments cannot be parsed
	 */
	public static void main(String[] args) throws IOException, ParseException {
		Options options = new Options();
		Option helpOption = new Option("h", "help", false, "Show this help message and exit.");
		Option logInputsOption = new Option("l", Genai.LOG_INPUTS_PROP_NAME, false,
				"Log LLM request inputs to dedicated log files.");

		Option multiThreadOption = Option.builder("t").longOpt("threads")
				.desc("The degree of concurrency for the processing to improve performance.")
				.hasArg(true).build();

		Option projectDirOpt = new Option("d", "projectDir", true,
				"Specify the path to the root directory for file processing.");

		Option genaiOpt = new Option("m", "model", true, "Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1').");

		Option instructionsOpt = Option.builder("i").longOpt(INSTRUCTIONS_PROP_NAME)
				.desc("Specify system instructions as plain text, by URL, or by file path. "
						+ "Each line of input is processed: blank lines are preserved, lines starting with 'http://' or 'https://' are loaded from the specified URL, "
						+ "lines starting with 'file:' are loaded from the specified file path, and other lines are used as-is. "
						+ "If the option is used without a value, you will be prompted to enter instruction text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build();

		Option excludesOpt = new Option("e", "excludes", true,
				"Specify a comma-separated list of directories to exclude from processing.");

		Option guidanceOpt = Option.builder("g").longOpt(GUIDANCE_OPTION)
				.desc("Specify the default guidance as plain text, by URL, or by file path to apply as a final step for the current directory. "
						+ "Each line of input is processed: blank lines are preserved, lines starting with 'http://' or 'https://' are loaded from the specified URL, "
						+ "lines starting with 'file:' are loaded from the specified file path, and other lines are used as-is. "
						+ "To provide the guidance directly, use the option without a value and you will be prompted to enter the guidance text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build();

		Option actsDirOpt = new Option("as", "acts", true,
				"Specify the path to the directory containing predefined act prompt files for processing.");

		Option actOpt = Option.builder("a").longOpt("act")
				.desc("Run Ghostwriter in Act mode: an interactive mode for executing predefined prompts.")
				.hasArg(true).optionalArg(true).build();

		options.addOption(helpOption);
		options.addOption(projectDirOpt);
		options.addOption(multiThreadOption);
		options.addOption(genaiOpt);
		options.addOption(instructionsOpt);
		options.addOption(guidanceOpt);
		options.addOption(excludesOpt);
		options.addOption(logInputsOption);
		options.addOption(actsDirOpt);
		options.addOption(actOpt);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption(helpOption)) {
			help(options);
			return;
		}

		File projectDir = null;
		if (cmd.hasOption(projectDirOpt)) {
			projectDir = new File(cmd.getOptionValue(projectDirOpt));
		}

		PropertiesConfigurator config = initializeConfiguration(projectDir);

		String genai = config.get(GW_MODEL_PROP_NAME, null);
		if (cmd.hasOption(genaiOpt)) {
			String opt = StringUtils.trimToNull(cmd.getOptionValue(genaiOpt));
			if (opt != null) {
				genai = opt;
			}
		}

		String instructions = config.get(INSTRUCTIONS_PROP_NAME, null);
		if (cmd.hasOption(instructionsOpt)) {
			instructions = cmd.getOptionValue(instructionsOpt);

			if (instructions == null) {
				instructions = readText("No instructions were provided as an option value.\n"
						+ "Please enter the instructions text below.");
			}
		}

		String[] excludes = StringUtils.split(config.get(GW_EXCLUDES_PROP_NAME, null), ',');
		if (cmd.hasOption(excludesOpt)) {
			excludes = StringUtils.split(cmd.getOptionValue(excludesOpt), ',');
		}

		String multiThread = config.get(GW_THREADS_PROP_NAME, null);
		if (cmd.hasOption(multiThreadOption)) {
			multiThread = cmd.getOptionValue(multiThreadOption);
		}

		boolean logInputs = config.getBoolean(Genai.LOG_INPUTS_PROP_NAME, false);
		if (cmd.hasOption(logInputsOption)) {
			logInputs = true;
		}

		if (projectDir == null) {
			projectDir = config.getFile(ProjectLayout.PROJECT_DIR_PROP_NAME, null);
			if (projectDir == null) {
				projectDir = SystemUtils.getUserDir();
			}
		}

		logger.info("Root directory: {}", projectDir);

		try {
			AIFileProcessor processor = createProcessor(cmd, projectDir, config, genai);

			Ghostwriter ghostwriter = new Ghostwriter(genai, processor);

			ghostwriter.setInstructions(instructions);
			ghostwriter.setExcludes(excludes);
			ghostwriter.setDegreeOfConcurrency(multiThread);
			ghostwriter.setLogInputs(logInputs);

			String[] scanDirs = resolveScanDirs(cmd, config);

			int exitCode = ghostwriter.perform(scanDirs);
			if (exitCode != 0) {
				System.exit(exitCode);
			}

		} catch (IOException e) {
			logger.error("I/O error occurred during file processing: {}", e.getMessage(), e);
		}
	}
}
