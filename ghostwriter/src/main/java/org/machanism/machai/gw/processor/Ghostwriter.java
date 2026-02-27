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
 * {@link GuidanceProcessor} to scan a directory tree for supported files. For each
 * file, Ghostwriter extracts embedded {@code @guidance:} directives and submits
 * the resulting prompt to the configured GenAI provider.
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
 * supported by {@link FileSystems#getPathMatcher(String)}.
 * </p>
 */
public final class Ghostwriter {

	public static final String GW_PROPERTIES_FILE_NAME = "gw.properties";
	public static final String GW_CONFIG_PROP_NAME = "gw.config";
	public static final String GW_HOME_PROP_NAME = "gw.home";
	public static final String GW_ROOTDIR_PROP_NAME = "gw.rootDir";
	public static final String GW_GENAI_PROP_NAME = "gw.genai";
	public static final String GW_INSTRUCTIONS_PROP_NAME = "gw.instructions";
	public static final String GW_EXCLUDES_PROP_NAME = "gw.excludes";
	public static final String GW_GUIDANCE_PROP_NAME = "gw.guidance";
	public static final String GW_THREADS_PROP_NAME = "gw.threads";
	public static final String GW_LOG_INPUTS_PROP_NAME = "gw.logInputs";

	/** Logger for the Ghostwriter application. */
	private static Logger logger;

	/** Properties-based configuration used by the CLI. */
	private static final PropertiesConfigurator config = new PropertiesConfigurator();

	/** Directory used as the execution base for relative configuration files. */
	private static File gwHomeDir;

	private Ghostwriter() {
		// Utility class.
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

		Option rootDirOpt = new Option("r", "root", true, "Specify the path to the root directory for file processing.");

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

		options.addOption(helpOption);
		options.addOption(rootDirOpt);
		options.addOption(multiThreadOption);
		options.addOption(genaiOpt);
		options.addOption(instructionsOpt);
		options.addOption(guidanceOpt);
		options.addOption(excludesOpt);
		options.addOption(logInputsOption);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		int exitCode = 0;
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption(helpOption.getOpt())) {
			help(options, formatter);
			return;
		}

		File rootDir = config.getFile(GW_ROOTDIR_PROP_NAME, null);
		if (cmd.hasOption(rootDirOpt.getOpt())) {
			rootDir = new File(cmd.getOptionValue(rootDirOpt.getOpt()));
		}

		initializeConfiguration(rootDir);

		try {
			String version = Ghostwriter.class.getPackage().getImplementationVersion();
			if (version != null) {
				logger.info("Ghostwriter {} (Machai project)", version);
			}
			logger.info("Home directory: {}", gwHomeDir);

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
							+ "Please enter the instructions text below. When you are done, press "
							+ (SystemUtils.IS_OS_WINDOWS ? "Ctrl + Z" : "Ctrl + D")
							+ " to finish and signal end of input (EOF):");
				}
			}

			String[] scanDirs = cmd.getArgs();
			if (scanDirs == null || scanDirs.length == 0) {
				scanDirs = new String[] {
						rootDir != null ? rootDir.getAbsolutePath() : SystemUtils.getUserDir().getAbsolutePath() };
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

			String defaultGuidance = config.get(GW_GUIDANCE_PROP_NAME, null);
			if (cmd.hasOption(guidanceOpt.getOpt())) {
				defaultGuidance = cmd.getOptionValue(guidanceOpt.getOpt());
				if (defaultGuidance == null) {
					defaultGuidance = readText("Please enter the guidance text below. When finished, press "
							+ (SystemUtils.IS_OS_WINDOWS ? "Ctrl + Z" : "Ctrl + D")
							+ " to signal end of input (EOF):");
				}
			}

			boolean logInputs = config.getBoolean(GW_LOG_INPUTS_PROP_NAME, false);
			if (cmd.hasOption(logInputsOption.getOpt())) {
				logInputs = true;
			}

			if (StringUtils.isBlank(genai)) {
				throw new IllegalArgumentException("No GenAI provider/model configured. Set '" + GW_GENAI_PROP_NAME
						+ "' in " + GW_PROPERTIES_FILE_NAME + " or pass -a/--genai (e.g., OpenAI:gpt-5.1).");
			}

			if (rootDir == null) {
				rootDir = SystemUtils.getUserDir();
			}

			for (String scanDir : scanDirs) {
				GuidanceProcessor processor = new GuidanceProcessor(rootDir, genai, config);
				if (excludes != null) {
					logger.info("Excludes: {}", Arrays.toString(excludes));
					processor.setExcludes(excludes);
				}

				if (instructions != null) {
					logger.info("Instructions: {}", StringUtils.abbreviate(instructions, 60));
					processor.setInstructions(instructions);
				}

				processor.setModuleMultiThread(multiThread);

				if (defaultGuidance != null) {
					logger.info("Default Guidance: {}", StringUtils.abbreviate(defaultGuidance, 60));
					processor.setDefaultGuidance(defaultGuidance);
				}

				processor.setLogInputs(logInputs);

				processor.scanDocuments(rootDir, scanDir);
				logger.info("Finished scanning directory: {}", scanDir);
			}

		} catch (ProcessTerminationException e) {
			if (logger != null) {
				logger.error("Process terminated: {}", e.getMessage());
			}
			exitCode = e.getExitCode();
		} catch (IllegalArgumentException e) {
			if (logger != null) {
				logger.error("Error: {}", e.getMessage());
			} else {
				System.err.println("Error: " + e.getMessage());
			}
			exitCode = 1;
		} catch (Exception e) {
			if (logger != null) {
				logger.error("Unexpected error: {}", e.getMessage(), e);
			} else {
				e.printStackTrace(System.err);
			}
			exitCode = 1;
		} finally {
			GenAIProviderManager.logUsage();
			if (logger != null) {
				logger.info("File processing completed.");
			}
			if (exitCode != 0) {
				System.exit(exitCode);
			}
		}
	}

	private static void initializeConfiguration(File rootDir) {
		try {
			gwHomeDir = config.getFile(GW_HOME_PROP_NAME, null);

			if (gwHomeDir == null) {
				gwHomeDir = rootDir;
				if (gwHomeDir == null) {
					gwHomeDir = SystemUtils.getUserDir();
				}
			}

			System.setProperty(GW_HOME_PROP_NAME, gwHomeDir.getAbsolutePath());
			logger = LoggerFactory.getLogger(Ghostwriter.class);

			try {
				File configFile = new File(gwHomeDir, System.getProperty(GW_CONFIG_PROP_NAME, GW_PROPERTIES_FILE_NAME));
				config.setConfiguration(configFile.getAbsolutePath());
			} catch (IOException e) {
				// The property file is not defined, ignore.
			}
		} catch (Exception e) {
			// Configuration file not found. An alternative configuration method will be used.
			if (logger == null) {
				logger = LoggerFactory.getLogger(Ghostwriter.class);
			}
		}
	}

	/**
	 * Prints the Ghostwriter CLI help text, including usage instructions, option
	 * descriptions, and example commands.
	 *
	 * @param options   the configured CLI options to display in the help message
	 * @param formatter the formatter instance used to print the help text
	 */
	private static void help(Options options, HelpFormatter formatter) {
		String header = "\nGhostwriter CLI - Scan and process directories or files using GenAI guidance.\n\n"
				+ "Usage:\n  java -jar gw.jar <scanDir> [options]\n\n"
				+ "  <scanDir> specifies the scanning path or pattern.\n"
				+ "    - Use a relative path with respect to the current project directory.\n"
				+ "    - If an absolute path is provided, it must be located within the root project directory.\n"
				+ "    - Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex patterns (e.g., \"regex:^.*/[^/]+\\.java$\").\n\n"
				+ "Options:";
		String footer = "\nExamples:\n"
				+ "  java -jar gw.jar C:\\\\projects\\\\project\n"
				+ "  java -jar gw.jar src\\project\n"
				+ "  java -jar gw.jar \"glob:**/*.java\"\n"
				+ "  java -jar gw.jar \"regex:^.*/[^/]+\\.java$\"\n";
		formatter.printHelp("java -jar gw.jar <scanDir> [options]", header, options, footer, true);
	}

	/**
	 * Reads multi-line text from standard input until EOF.
	 *
	 * @param prompt prompt to display before reading
	 * @return the entered text, or {@code null} if no content was entered
	 */
	private static String readText(String prompt) {
		System.out.println(prompt);
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append("\n");
			}
		}

		System.out.println("Input complete. Processing your text...");
		return sb.length() > 0 ? sb.deleteCharAt(sb.length() - 1).toString() : null;
	}
}
