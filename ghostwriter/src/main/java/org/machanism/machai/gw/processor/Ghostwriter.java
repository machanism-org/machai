package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line entry point for Ghostwriter.
 *
 * <p>
 * This CLI bootstraps configuration, parses command-line options, and invokes
 * {@link GuidanceProcessor} to scan a directory tree for supported files. For
 * each file, Ghostwriter extracts embedded `guidance` tag directives and
 * submits the resulting prompt to the configured GenAI provider.
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
 * supported by {@link FileSystems#getDefault()
 * FileSystems.getDefault()}{@code .}{@link java.nio.file.FileSystem#getPathMatcher(String)
 * getPathMatcher(String)}.
 * </p>
 */
public final class Ghostwriter {

	/** Logger for the Ghostwriter application. */
	private static Logger logger;

	/** Default Ghostwriter properties file name. */
	public static final String GW_PROPERTIES_FILE_NAME = "gw.properties";

	/** System property key to override the Ghostwriter configuration file path. */
	public static final String GW_CONFIG_PROP_NAME = "gw.config";

	/** System property key indicating the Ghostwriter home directory. */
	public static final String GW_HOME_PROP_NAME = "gw.home";

	/** Configuration key for the root directory to scan. */
	public static final String GW_ROOTDIR_PROP_NAME = "gw.rootDir";

	/** Configuration key specifying the GenAI provider/model to use. */
	public static final String GW_GENAI_PROP_NAME = "gw.genai";

	/** Configuration key containing optional system instructions. */
	public static final String GW_INSTRUCTIONS_PROP_NAME = "gw.instructions";

	/** Configuration key containing comma-separated scan exclusions. */
	public static final String GW_EXCLUDES_PROP_NAME = "gw.excludes";

	/** Configuration key containing default guidance. */
	public static final String GW_GUIDANCE_PROP_NAME = "gw.guidance";

	/** Configuration key enabling multi-threaded processing. */
	public static final String GW_THREADS_PROP_NAME = "gw.threads";

	/** Configuration key enabling request input logging. */
	public static final String GW_LOG_INPUTS_PROP_NAME = "gw.logInputs";

	/** Directory used as the execution base for relative configuration files. */
	private static File gwHomeDir;

	/** Processor implementation used by this CLI instance. */
	private final AIFileProcessor processor;

	/**
	 * Creates a new Ghostwriter CLI instance.
	 *
	 * @param rootDir   root directory of the project to scan; if {@code null}, the
	 *                  value is derived from configuration or defaults to the
	 *                  current working directory
	 * @param genai     provider key/name (including model), e.g.
	 *                  {@code OpenAI:gpt-5.1}
	 * @param config    configuration source
	 * @param processor processor implementation
	 */
	public Ghostwriter(File rootDir, String genai, PropertiesConfigurator config, AIFileProcessor processor) {
		if (StringUtils.isBlank(genai)) {
			throw new IllegalArgumentException("No GenAI provider/model configured. Set '" + GW_GENAI_PROP_NAME
					+ "' in " + GW_PROPERTIES_FILE_NAME + " or pass -a/--genai (e.g., OpenAI:gpt-5.1).");
		}

		this.processor = processor;
	}

	/**
	 * Performs a scan of the provided scan directories/patterns.
	 *
	 * @param scanDirs scan directory arguments
	 * @return exit code (0 for success)
	 * @throws IOException    if scanning fails while reading files
	 * @throws ParseException if parsing-related failures occur
	 */
	public int perform(String[] scanDirs) throws IOException, ParseException {
		int exitCode = 0;
		try {
			for (String scanDir : scanDirs) {
				File projectDir = processor.getRootDir();
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
			GenAIProviderManager.logUsage();
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
	 * @param rootDir optional root directory used to resolve the home directory
	 * @return initialized configuration
	 */
	private static PropertiesConfigurator initializeConfiguration(File rootDir) {
		PropertiesConfigurator config = new PropertiesConfigurator();

		gwHomeDir = config.getFile(GW_HOME_PROP_NAME, null);
		if (gwHomeDir == null) {
			gwHomeDir = rootDir;
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
	private static void help(Options options) {
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
	 * Reads multi-line text from standard input until EOF.
	 *
	 * @param prompt prompt to display before reading
	 * @return the entered text, or {@code null} if no content was entered
	 */
	private static String readText(String prompt) {
		System.out.print(prompt + " (press ENTER and EOF: "
				+ (SystemUtils.IS_OS_WINDOWS ? "Ctrl + Z" : "Ctrl + D")
				+ " to complete):");
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append("\n");
			}
		}

		System.out.println("Input complete. Processing your text...");
		return sb.length() > 0 ? sb.deleteCharAt(sb.length() - 1).toString() : null;
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
			logger.info("Instructions: {}", StringUtils.abbreviate(instructions, 60));
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
	 * @param multiThread {@code true} to enable
	 */
	public void setMultiThread(boolean multiThread) {
		processor.setModuleMultiThread(multiThread);
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
		Option logInputsOption = new Option("l", "logInputs", false, "Log LLM request inputs to dedicated log files.");

		Option multiThreadOption = Option.builder("t").longOpt("threads")
				.desc("Enable multi-threaded processing to improve performance (default: false).")
				.hasArg(true).optionalArg(true).build();

		Option rootDirOpt = new Option("r", "root", true,
				"Specify the path to the root directory for file processing.");

		Option actsDirOpt = new Option("acts", true,
				"Specify the path to the directory containing predefined act prompt files for processing.");

		Option genaiOpt = new Option("a", "genai", true, "Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1').");

		Option instructionsOpt = Option.builder("i").longOpt("instructions")
				.desc("Specify system instructions as plain text, by URL, or by file path. "
						+ "Each line of input is processed: blank lines are preserved, lines starting with 'http://' or 'https://' are loaded from the specified URL, "
						+ "lines starting with 'file:' are loaded from the specified file path, and other lines are used as-is. "
						+ "If the option is used without a value, you will be prompted to enter instruction text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build();

		Option excludesOpt = new Option("e", "excludes", true,
				"Specify a comma-separated list of directories to exclude from processing.");

		Option guidanceOpt = Option.builder("g").longOpt("guidance")
				.desc("Specify the default guidance as plain text, by URL, or by file path to apply as a final step for the current directory. "
						+ "Each line of input is processed: blank lines are preserved, lines starting with 'http://' or 'https://' are loaded from the specified URL, "
						+ "lines starting with 'file:' are loaded from the specified file path, and other lines are used as-is. "
						+ "To provide the guidance directly, use the option without a value and you will be prompted to enter the guidance text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build();

		Option actOpt = Option.builder().longOpt("act")
				.desc("Run Ghostwriter in Act mode: an interactive mode for executing predefined prompts.")
				.hasArg(true).optionalArg(true).build();

		options.addOption(helpOption);
		options.addOption(rootDirOpt);
		options.addOption(actsDirOpt);
		options.addOption(multiThreadOption);
		options.addOption(genaiOpt);
		options.addOption(instructionsOpt);
		options.addOption(guidanceOpt);
		options.addOption(excludesOpt);
		options.addOption(logInputsOption);
		options.addOption(actOpt);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption(helpOption.getOpt())) {
			help(options);
			return;
		}

		File rootDir = null;
		if (cmd.hasOption(rootDirOpt.getOpt())) {
			rootDir = new File(cmd.getOptionValue(rootDirOpt.getOpt()));
		}

		PropertiesConfigurator config = initializeConfiguration(rootDir);

		String genai = config.get(GW_GENAI_PROP_NAME, null);
		if (cmd.hasOption(genaiOpt.getOpt())) {
			String opt = StringUtils.trimToNull(cmd.getOptionValue(genaiOpt.getOpt()));
			if (opt != null) {
				genai = opt;
			}
		}

		String instructions = config.get(GW_INSTRUCTIONS_PROP_NAME, null);
		if (cmd.hasOption(instructionsOpt.getOpt())) {
			instructions = cmd.getOptionValue(instructionsOpt.getOpt());

			if (instructions == null) {
				instructions = readText("No instructions were provided as an option value.\n"
						+ "Please enter the instructions text below.");
			}
		}

		String[] excludes = StringUtils.split(config.get(GW_EXCLUDES_PROP_NAME, null), ',');
		if (cmd.hasOption(excludesOpt.getOpt())) {
			excludes = StringUtils.split(cmd.getOptionValue(excludesOpt.getOpt()), ',');
		}

		boolean multiThread = config.getBoolean(GW_THREADS_PROP_NAME, false);
		if (cmd.hasOption(multiThreadOption.getOpt())) {
			String opt = cmd.getOptionValue(multiThreadOption.getOpt());
			if (opt == null) {
				multiThread = true;
			} else {
				multiThread = Boolean.parseBoolean(opt);
			}
		}

		boolean logInputs = config.getBoolean(GW_LOG_INPUTS_PROP_NAME, false);
		if (cmd.hasOption(logInputsOption.getOpt())) {
			logInputs = true;
		}

		if (rootDir == null) {
			rootDir = config.getFile(GW_ROOTDIR_PROP_NAME, null);
			if (rootDir == null) {
				rootDir = SystemUtils.getUserDir();
			}
		}

		String defaultPrompt;
		try {
			AIFileProcessor processor;
			if (cmd.hasOption(actOpt.getLongOpt())) {
				processor = new ActProcessor(rootDir, config, genai);
				defaultPrompt = cmd.getOptionValue(actOpt.getLongOpt());

				if (defaultPrompt == null) {
					defaultPrompt = readText("Act");
				}

				if (cmd.hasOption(actsDirOpt)) {
					((ActProcessor) processor).setActDir(new File(actsDirOpt.getValue()));
				}

				if (defaultPrompt != null) {
					logger.info("Act: {}", StringUtils.abbreviate(defaultPrompt, 60));
				}

			} else {
				processor = new GuidanceProcessor(rootDir, genai, config);

				defaultPrompt = config.get(GW_GUIDANCE_PROP_NAME, null);
				if (cmd.hasOption(guidanceOpt.getOpt())) {
					defaultPrompt = cmd.getOptionValue(guidanceOpt.getOpt());
					if (defaultPrompt == null) {
						defaultPrompt = readText("Please enter the guidance text below.");
					}
				}

				if (defaultPrompt != null) {
					logger.info("Default Prompt: {}", StringUtils.abbreviate(defaultPrompt, 60));
				}
			}

			Ghostwriter ghostwriter = new Ghostwriter(rootDir, genai, config, processor);

			ghostwriter.setInstructions(instructions);
			ghostwriter.setExcludes(excludes);
			ghostwriter.setMultiThread(multiThread);
			ghostwriter.setLogInputs(logInputs);

			ghostwriter.setDefaultPrompt(defaultPrompt);

			String[] scanDirs = cmd.getArgs();
			if (scanDirs == null || scanDirs.length == 0) {
				String gwScanDir = config.get("gw.scanDir", null);
				if (gwScanDir != null) {
					scanDirs = new String[] { gwScanDir };
				}
			}

			if (scanDirs == null || scanDirs.length == 0) {
				scanDirs = new String[] { rootDir.getAbsolutePath() };
			}

			int exitCode = ghostwriter.perform(scanDirs);
			if (exitCode != 0) {
				System.exit(exitCode);
			}

		} catch (IOException e) {
			logger.error("I/O error occurred during file processing: {}", e.getMessage(), e);
		}
	}
}
