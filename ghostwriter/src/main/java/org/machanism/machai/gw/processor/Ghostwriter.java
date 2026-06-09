package org.machanism.machai.gw.processor;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line entry point for the Ghostwriter application.
 */
public final class Ghostwriter {

	public static final String USER_INPUT_PREFIX = ">>>";

	private static final Logger LOGGER = LoggerFactory.getLogger(Ghostwriter.class);
	private static final String HELP_OPTION = "help";
	private static final String THREADS_OPTION = "threads";
	private static final String MODEL_OPTION = "model";
	private static final String EXCLUDES_OPTION = "excludes";
	private static final String ACT_OPTION = "act";
	private static final String ACTS_OPTION = "acts";
	private static final int EXIT_CODE_ERROR = 1;

	private Ghostwriter() {
	}

	/**
	 * Starts the Ghostwriter CLI.
	 *
	 * @param args command-line arguments
	 * @throws IOException    if configuration or processing fails with an I/O error
	 * @throws ParseException if command-line parsing fails
	 */
	public static void main(String[] args) throws IOException, ParseException {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		Options options = createOptions();
		CommandLine cmd = new DefaultParser().parse(options, args);

		if (cmd.hasOption(HELP_OPTION)) {
			printHelp(options);
			return;
		}

		PropertiesConfigurator config = new PropertiesConfigurator();
		File gwHomeDir = initializeHomeDirectory(config);
		initializeConfiguration(config);
		RuntimeSettings settings = loadRuntimeSettings(cmd, config, scanner);
		logStartup(gwHomeDir, settings.projectDir);
		execute(scanner, config, cmd, settings);
	}

	/**
	 * Creates the supported CLI option definitions.
	 *
	 * @return configured options
	 */
	private static Options createOptions() {
		Options options = new Options();
		options.addOption(new Option("h", HELP_OPTION, false, "Show this help message and exit."));
		options.addOption(new Option("d", GWConstants.PROJECT_DIR_PROP_NAME, true,
				"Specify the path to the root directory for file processing."));
		options.addOption(Option.builder("t").longOpt(THREADS_OPTION)
				.desc("The degree of concurrency for the processing to improve performance.")
				.hasArg(true).build());
		options.addOption(new Option("m", MODEL_OPTION, true,
				"Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1')."));
		options.addOption(Option.builder("i").longOpt(GWConstants.INSTRUCTIONS_PROP_NAME)
				.desc("Specify system instructions as plain text, by URL, or by file path. "
						+ "Each line of input is processed: blank lines are preserved, lines starting with 'http://' or "
						+ "'https://' are loaded from the specified URL, lines starting with 'file:' are loaded from "
						+ "the specified file path, and other lines are used as-is. If the option is used without a "
						+ "value, you will be prompted to enter instruction text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build());
		options.addOption(new Option("e", EXCLUDES_OPTION, true,
				"Specify a comma-separated list of directories to exclude from processing."));
		options.addOption(new Option("l", AbstractAIProvider.LOG_INPUTS_PROP_NAME, false,
				"Log LLM request inputs to dedicated log files."));
		options.addOption(new Option("as", ACTS_OPTION, true,
				"Specify the path to the directory containing predefined act prompt files for processing."));
		options.addOption(Option.builder("a").longOpt(ACT_OPTION)
				.desc("Run Ghostwriter in Act mode: an interactive mode for executing predefined prompts.")
				.hasArg(true).optionalArg(true).build());
		return options;
	}

	/**
	 * Prints CLI help text.
	 *
	 * @param options available command-line options
	 */
	private static void printHelp(Options options) {
		String header = "\nGhostwriter CLI - Scan and process directories or files using GenAI guidance.\n\n"
				+ "Usage:\n  java -jar gw.jar <paths> [options]\n\n"
				+ "  <paths> specifies the scanning path or pattern.\n"
				+ "    - Use a relative path with respect to the current project directory.\n"
				+ "    - If an absolute path is provided, it must be located within the root project directory.\n"
				+ "    - Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
				+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").\n\n"
				+ "Options:";
		String footer = "\nExamples:\n" + "  java -jar gw.jar C:\\\\projects\\project\n"
				+ "  java -jar gw.jar src\\project\n" + "  java -jar gw.jar \"glob:**/*.java\"\n"
				+ "  java -jar gw.jar \"regex:^.*/[^/]+\\.java$\"\n";
		new HelpFormatter().printHelp("java -jar gw.jar <paths> [options]", header, options, footer, true);
	}

	/**
	 * Initializes the Ghostwriter home directory.
	 *
	 * @param config configuration source
	 * @return resolved home directory
	 */
	private static File initializeHomeDirectory(PropertiesConfigurator config) {
		File gwHomeDir = config.getFile(GWConstants.HOME_PROP_NAME, null);
		if (gwHomeDir == null) {
			gwHomeDir = SystemUtils.getUserDir();
		}
		System.setProperty(GWConstants.HOME_PROP_NAME, gwHomeDir.getAbsolutePath());
		logVersion();
		return gwHomeDir;
	}

	/**
	 * Logs the application version when available from package metadata.
	 */
	private static void logVersion() {
		String version = Ghostwriter.class.getPackage().getImplementationVersion();
		if (version != null) {
			LOGGER.info("Ghostwriter {} (Machai Project)", version);
		}
	}

	/**
	 * Loads the external configuration file when present.
	 *
	 * @param config configuration source
	 */
	private static void initializeConfiguration(PropertiesConfigurator config) {
		try {
			String configFileName = System.getProperty(GWConstants.CONFIG_PROP_NAME,
					GWConstants.GW_PROPERTIES_FILE_NAME);
			File configFile = new File(System.getProperty(GWConstants.HOME_PROP_NAME), configFileName);
			config.setConfiguration(configFile.getAbsolutePath());
		} catch (IOException e) {
			// The property file is not defined, ignore.
		} catch (RuntimeException e) {
			LOGGER.warn("Failed to initialize configuration.", e);
		}
	}

	/**
	 * Resolves runtime settings from CLI arguments and persisted configuration.
	 *
	 * @param cmd     parsed command line
	 * @param config  configuration source
	 * @param scanner console scanner used for optional prompts
	 * @return populated runtime settings
	 */
	private static RuntimeSettings loadRuntimeSettings(CommandLine cmd, PropertiesConfigurator config,
			Scanner scanner) {
		RuntimeSettings settings = new RuntimeSettings();
		settings.genai = resolveGenai(cmd, config);
		settings.instructions = resolveInstructions(cmd, config, scanner);
		settings.excludes = resolveExcludes(cmd, config);
		settings.multiThread = resolveMultiThread(cmd, config);
		settings.logInputs = cmd.hasOption(AbstractAIProvider.LOG_INPUTS_PROP_NAME)
				|| config.getBoolean(AbstractAIProvider.LOG_INPUTS_PROP_NAME, false);
		settings.projectDir = resolveProjectDir(cmd, config);
		settings.pathss = resolvePathss(cmd, config);
		return settings;
	}

	/**
	 * Resolves the configured AI provider/model.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return provider/model identifier, or {@code null} if not configured
	 */
	private static String resolveGenai(CommandLine cmd, PropertiesConfigurator config) {
		String genai = config.get(GWConstants.MODEL_PROP_NAME, null);
		if (!cmd.hasOption(MODEL_OPTION)) {
			return genai;
		}
		String optionValue = StringUtils.trimToNull(cmd.getOptionValue(MODEL_OPTION));
		return optionValue == null ? genai : optionValue;
	}

	/**
	 * Resolves the instruction text from CLI input or configuration.
	 *
	 * @param cmd     parsed command line
	 * @param config  configuration source
	 * @param scanner console scanner used for prompting
	 * @return resolved instruction text
	 */
	private static String resolveInstructions(CommandLine cmd, PropertiesConfigurator config, Scanner scanner) {
		String instructions = config.get(GWConstants.INSTRUCTIONS_PROP_NAME, null);
		if (!cmd.hasOption(GWConstants.INSTRUCTIONS_PROP_NAME)) {
			return instructions;
		}
		String optionValue = cmd.getOptionValue(GWConstants.INSTRUCTIONS_PROP_NAME);
		return optionValue == null ? promptForValue(scanner, "Instructions: ") : optionValue;
	}

	/**
	 * Resolves the exclude list.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return exclude patterns split by comma, or {@code null}
	 */
	private static String[] resolveExcludes(CommandLine cmd, PropertiesConfigurator config) {
		String configuredValue = cmd.hasOption(EXCLUDES_OPTION) ? cmd.getOptionValue(EXCLUDES_OPTION)
				: config.get(GWConstants.EXCLUDES_PROP_NAME, null);
		return StringUtils.split(configuredValue, ',');
	}

	/**
	 * Resolves the concurrency setting.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return configured thread count as text, or {@code null}
	 */
	private static String resolveMultiThread(CommandLine cmd, PropertiesConfigurator config) {
		return cmd.hasOption(THREADS_OPTION) ? cmd.getOptionValue(THREADS_OPTION)
				: config.get(GWConstants.THREADS_PROP_NAME, null);
	}

	/**
	 * Resolves the project root directory.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return project root directory
	 */
	private static File resolveProjectDir(CommandLine cmd, PropertiesConfigurator config) {
		if (cmd.hasOption(GWConstants.PROJECT_DIR_PROP_NAME)) {
			return new File(cmd.getOptionValue(GWConstants.PROJECT_DIR_PROP_NAME));
		}
		File configuredProjectDir = config.getFile(GWConstants.PROJECT_DIR_PROP_NAME, null);
		return configuredProjectDir == null ? SystemUtils.getUserDir() : configuredProjectDir;
	}

	/**
	 * Resolves scan directories or patterns.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return scan paths/patterns to process
	 */
	private static String[] resolvePathss(CommandLine cmd, PropertiesConfigurator config) {
		String[] pathss = cmd.getArgs();
		if (pathss != null && pathss.length > 0) {
			return pathss;
		}
		String configuredPaths = config.get(GWConstants.SCAN_DIR_PROP_NAME, null);
		if (configuredPaths != null) {
			return new String[] { configuredPaths };
		}
		return new String[] { "." };
	}

	/**
	 * Prompts the user for a single line of input.
	 *
	 * @param scanner scanner reading standard input
	 * @param prompt  prompt text
	 * @return entered value
	 */
	private static String promptForValue(Scanner scanner, String prompt) {
		Console console = System.console();
		if (console != null) {
			console.format(prompt);
		} else {
			System.out.print(prompt);
		}

		StringBuilder sb = new StringBuilder();
		String line;
		int length = prompt.length();
		int maxlen = length;
		while ((line = scanner.nextLine()) != null) {
			prompt = "\t";
			length += line.length();
			if (length > maxlen) {
				maxlen = length;
			}
			if (Strings.CS.endsWith(line, GWConstants.MULTIPLE_LINES_BREAKER)) {
				sb.append(StringUtils.substringBeforeLast(line, GWConstants.MULTIPLE_LINES_BREAKER))
						.append(AbstractAIProvider.LINE_SEPARATOR);
			} else {
				sb.append(line);
				break;
			}
			length = 8;
		}
		String footer = StringUtils.leftPad("― ©" + SystemProperties.getUserName(), maxlen) + "\n";
		if (console != null) {
			console.format(footer);
		} else {
			System.out.print(footer);
		}

		return sb.toString();
	}

	/**
	 * Logs basic startup path information.
	 *
	 * @param gwHomeDir  Ghostwriter home directory
	 * @param projectDir project root directory
	 */
	private static void logStartup(File gwHomeDir, File projectDir) {
		LOGGER.info("Home directory: {}", gwHomeDir);
		LOGGER.info("Root directory: {}", projectDir);
	}

	/**
	 * Creates and runs the selected processor.
	 *
	 * @param scanner  console scanner
	 * @param config   configuration source
	 * @param cmd      parsed command line
	 * @param settings resolved runtime settings
	 * @throws IOException if processor creation or execution fails
	 */
	private static void execute(Scanner scanner, PropertiesConfigurator config, CommandLine cmd,
			RuntimeSettings settings) throws IOException {
		try {
			AIFileProcessor processor = createProcessor(scanner, config, cmd, settings);
			applyCommonSettings(processor, settings);
			handleExitCode(processPathsectories(processor, settings.pathss, settings.projectDir));
		} catch (ActNotFound e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("I/O error occurred during file processing: {}", e.getMessage(), e);
		}
	}

	/**
	 * Exits the JVM when the returned code is non-zero.
	 *
	 * @param exitCode exit code to apply
	 */
	private static void handleExitCode(int exitCode) {
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	/**
	 * Creates either a guidance processor or an act processor.
	 *
	 * @param scanner  console scanner
	 * @param config   configuration source
	 * @param cmd      parsed command line
	 * @param settings resolved runtime settings
	 * @return configured processor
	 * @throws IOException if act initialization fails
	 */
	private static AIFileProcessor createProcessor(Scanner scanner, PropertiesConfigurator config, CommandLine cmd,
			RuntimeSettings settings) throws IOException {
		if (!cmd.hasOption(ACT_OPTION)) {
			return new GuidanceProcessor(settings.projectDir, settings.genai, config);
		}
		ActProcessor actProcessor = createActProcessor(scanner, config, settings);
		configureActsLocation(cmd, config, actProcessor);
		configureDefaultAct(cmd, config, scanner, actProcessor);
		return actProcessor;
	}

	/**
	 * Creates an act processor with console-backed interactive input.
	 *
	 * @param scanner  console scanner
	 * @param config   configuration source
	 * @param settings resolved runtime settings
	 * @return act processor
	 */
	private static ActProcessor createActProcessor(Scanner scanner, PropertiesConfigurator config,
			RuntimeSettings settings) {
		String genai = settings.genai;
		if (genai != null) {
			LOGGER.info("Model: {}", genai);
		}
		return new ActProcessor(settings.projectDir, config, genai) {
			@Override
			protected String input() {
				return readActInput(scanner);
			}
		};
	}

	/**
	 * Reads possibly multi-line interactive act input.
	 *
	 * @param scanner scanner reading standard input
	 * @return collected input text
	 */
	private static String readActInput(Scanner scanner) {
		Console console = System.console();
		formatConsole(console, USER_INPUT_PREFIX);
		StringBuilder sb = new StringBuilder();
		while (scanner.hasNextLine()) {
			String nextLine = scanner.nextLine();
			if (!Strings.CS.endsWith(nextLine, GWConstants.MULTIPLE_LINES_BREAKER)) {
				sb.append(nextLine);
				break;
			}
			appendContinuedLine(sb, nextLine);
			formatConsole(console, "\t");
		}
		return sb.toString();
	}

	/**
	 * Writes a prompt message to the console when available.
	 *
	 * @param console console instance, may be {@code null}
	 * @param message message to print
	 */
	private static void formatConsole(Console console, String message) {
		if (console != null) {
			console.format(message + ": ");
		}
	}

	/**
	 * Appends a continued input line, removing the continuation marker.
	 *
	 * @param sb       buffer receiving the line
	 * @param nextLine line that ends with the continuation marker
	 */
	private static void appendContinuedLine(StringBuilder sb, String nextLine) {
		sb.append(StringUtils.substringBeforeLast(nextLine, GWConstants.MULTIPLE_LINES_BREAKER))
				.append(AbstractAIProvider.LINE_SEPARATOR);
	}

	/**
	 * Configures a custom acts location when provided.
	 *
	 * @param cmd          parsed command line
	 * @param config       configuration source
	 * @param actProcessor act processor to configure
	 */
	private static void configureActsLocation(CommandLine cmd, PropertiesConfigurator config,
			ActProcessor actProcessor) {
		String actsLocation = cmd.hasOption(ACTS_OPTION) ? cmd.getOptionValue(ACTS_OPTION)
				: config.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		if (actsLocation == null) {
			return;
		}
		LOGGER.info("Custom acts location specified: {}", actsLocation);
		actProcessor.setActsLocation(actsLocation);
	}

	/**
	 * Configures the default act when act mode is enabled.
	 *
	 * @param cmd          parsed command line
	 * @param config       configuration source
	 * @param scanner      scanner used for prompting
	 * @param actProcessor act processor to configure
	 * @throws IOException if act loading fails
	 */
	private static void configureDefaultAct(CommandLine cmd, PropertiesConfigurator config, Scanner scanner,
			ActProcessor actProcessor) throws IOException {
		String defaultPrompt = cmd.hasOption(ACT_OPTION) ? cmd.getOptionValue(ACT_OPTION)
				: config.get(GWConstants.ACT_PROP_NAME, null);
		if (cmd.hasOption(ACT_OPTION) && defaultPrompt == null) {
			defaultPrompt = promptForValue(scanner, "Act: ");
		}
		if (defaultPrompt == null) {
			return;
		}
		logAbbreviatedMessage("Act", defaultPrompt);
		actProcessor.setAct(defaultPrompt);
	}

	/**
	 * Applies shared processor settings.
	 *
	 * @param processor processor to configure
	 * @param settings  resolved runtime settings
	 */
	private static void applyCommonSettings(AIFileProcessor processor, RuntimeSettings settings) {
		applyInstructions(processor, settings.instructions);
		applyExcludes(processor, settings.excludes);
		applyConcurrency(processor, settings.multiThread);
		processor.setLogInputs(settings.logInputs);
	}

	/**
	 * Applies custom instructions when present.
	 *
	 * @param processor    processor to configure
	 * @param instructions instruction text
	 */
	private static void applyInstructions(AIFileProcessor processor, String instructions) {
		if (instructions == null) {
			return;
		}
		// Sonar java:S2629: avoid unnecessary abbreviation work unless INFO logging is
		// enabled.
		logAbbreviatedMessage("Instructions", instructions);
		processor.setInstructions(instructions);
	}

	/**
	 * Applies exclude patterns when configured.
	 *
	 * @param processor processor to configure
	 * @param excludes  exclude patterns
	 */
	private static void applyExcludes(AIFileProcessor processor, String[] excludes) {
		if (excludes == null) {
			return;
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Excludes: {}", Arrays.toString(excludes));
		}
		processor.setExcludes(excludes);
	}

	/**
	 * Applies the configured concurrency setting.
	 *
	 * @param processor   processor to configure
	 * @param multiThread thread count text
	 */
	private static void applyConcurrency(AIFileProcessor processor, String multiThread) {
		if (multiThread != null) {
			processor.setDegreeOfConcurrency(Integer.parseInt(multiThread));
		}
	}

	/**
	 * Logs an abbreviated value when INFO logging is enabled.
	 *
	 * @param label message label
	 * @param value message value
	 */
	private static void logAbbreviatedMessage(String label, String value) {
		if (LOGGER.isInfoEnabled()) {
			logAbbreviatedValue(label, value);
		}
	}

	/**
	 * Logs a value truncated to the configured maximum prompt log length.
	 *
	 * @param label message label
	 * @param value message value
	 */
	private static void logAbbreviatedValue(String label, String value) {
		String abbreviatedValue = StringUtils.abbreviate(value, AbstractAIProvider.LOG_LINE_LENG);
		LOGGER.info("{}: {}", label, abbreviatedValue);
	}

	/**
	 * Processes all requested scan directories.
	 *
	 * @param processor  configured processor
	 * @param pathss     scan directories or patterns
	 * @param projectDir project root directory
	 * @return resulting exit code
	 */
	private static int processPathsectories(AIFileProcessor processor, String[] pathss, File projectDir) {
		int exitCode = 0;
		try {
			for (String paths : pathss) {
				LOGGER.info("Starting scan of path: `{}`", paths);
				processor.scanDocuments(projectDir, paths);
				LOGGER.info("Finished scanning path: `{}`", paths);
			}
		} catch (ProcessTerminationException e) {
			exitCode = handleProcessTermination(e);
		} catch (IllegalArgumentException e) {
			exitCode = handleProcessingFailure("Error", e);
		} catch (Exception e) {
			exitCode = handleProcessingFailure("Unexpected error", e);
		} finally {
			UsageStatistics.logUsage();
			LOGGER.info("File processing completed.");
		}
		return exitCode;
	}

	/**
	 * Handles an explicit process termination request.
	 *
	 * @param exception termination exception
	 * @return exit code to use
	 */
	private static int handleProcessTermination(ProcessTerminationException exception) {
		LOGGER.error("Process terminated: {}, Exit code: {}", exception.getMessage(), exception.getExitCode());
		return exception.getExitCode();
	}

	/**
	 * Logs a processing failure and returns the generic error exit code.
	 *
	 * @param message   log prefix
	 * @param exception failure
	 * @return generic error exit code
	 */
	private static int handleProcessingFailure(String message, Exception exception) {
		LOGGER.error("{}: {}", message, exception.getMessage(), exception);
		return EXIT_CODE_ERROR;
	}

	/**
	 * Mutable holder for startup settings resolved before processor creation.
	 */
	private static final class RuntimeSettings {
		private String genai;
		private String instructions;
		private String[] excludes;
		private String multiThread;
		private boolean logInputs;
		private File projectDir;
		private String[] pathss;
	}
}
