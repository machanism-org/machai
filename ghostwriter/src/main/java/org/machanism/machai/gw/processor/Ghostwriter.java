package org.machanism.machai.gw.processor;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line entry point for the Ghostwriter application.
 *
 * <p>
 * Ghostwriter scans a project's files or directories and processes them with
 * GenAI guidance. It supports two processing modes:
 * </p>
 * <ul>
 * <li><b>Guidance mode</b> (default) &mdash; processes files using inline
 * guidance comments found in the scanned content, via
 * {@link GuidanceProcessor}.</li>
 * <li><b>Act mode</b> (enabled with {@code --act}) &mdash; runs a predefined,
 * possibly interactive, prompt ("act") against the project, via
 * {@link ActProcessor}.</li>
 * </ul>
 *
 * <p>
 * Runtime behavior can be configured through command-line options, a persisted
 * properties file (see {@link PropertiesConfigurator}), or a combination of
 * both, with command-line options taking precedence. This class is responsible
 * for parsing CLI arguments, resolving effective settings, wiring up the
 * appropriate processor, and mapping processing outcomes to JVM exit codes.
 * </p>
 *
 * <p>
 * This class is not intended to be instantiated; it exposes only a static
 * {@link #main(String[])} entry point.
 * </p>
 */
public final class Ghostwriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ghostwriter.class);

	public static final String USER_INPUT_PREFIX = ">>>";

	private static final String HELP_OPTION = "help";
	private static final String THREADS_OPTION = "threads";
	private static final String PROJECT_DIR_PROP_NAME = "projectDir";
	private static final String INSTRUCTIONS_PROP_NAME = "instructions";
	private static final String MODEL_OPTION = "model";
	private static final String EXCLUDES_OPTION = "excludes";
	private static final String ACT_OPTION = "act";
	private static final String ACTS_OPTION = "acts";
	private static final int EXIT_CODE_ERROR = 1;

	public static final String DEFAULT_MODEL_MSG = "Using default model: {}";

	private Ghostwriter() {
	}

	/**
	 * Starts the Ghostwriter CLI.
	 *
	 * <p>
	 * Parses command-line arguments, loads configuration, resolves runtime
	 * settings, and delegates to the appropriate processor. If {@code --help} is
	 * specified, prints usage information and returns without processing anything.
	 * </p>
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
	 * @return configured options describing every flag accepted by the CLI
	 */
	private static Options createOptions() {
		Options options = new Options();
		options.addOption(new Option("h", HELP_OPTION, false, "Show this help message and exit."));
		options.addOption(new Option("d", PROJECT_DIR_PROP_NAME, true,
				"Specify the path to the project directory for file processing."));
		options.addOption(Option.builder("t").longOpt(THREADS_OPTION)
				.desc("Number of concurrent threads to use for processing (e.g., 4). Higher values can improve "
						+ "performance on multi-core systems but increase resource and AI provider usage.")
				.hasArg(true).get());
		options.addOption(new Option("m", MODEL_OPTION, true,
				"Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1')."));
		options.addOption(Option.builder("i").longOpt(INSTRUCTIONS_PROP_NAME)
				.desc("Specify system instructions. If the option is used without a "
						+ "value, you will be prompted to enter instruction text via standard input (stdin).")
				.hasArg(true).optionalArg(true).get());
		options.addOption(new Option("e", EXCLUDES_OPTION, true,
				"Specify a comma-separated list of directories to exclude from processing."));
		options.addOption(new Option("as", ACTS_OPTION, true,
				"Specify the path to the directory containing predefined act prompt files for processing."));
		options.addOption(Option.builder("a").longOpt(ACT_OPTION)
				.desc("Run Ghostwriter in Act mode: an interactive mode for executing predefined prompts.")
				.hasArg(true).optionalArg(true).get());
		return options;
	}

	/**
	 * Prints CLI help text, including usage syntax, all available options, and
	 * example invocations, to standard output.
	 *
	 * @param options available command-line options
	 * @throws IOException
	 */
	private static void printHelp(Options options) throws IOException {
		String header = "\nGhostwriter CLI - Scan and process directories or files using GenAI guidance.\n\n"
				+ "Usage:\n  java -jar gw.jar <path> [options]\n\n"
				+ "  <path> specifies the scanning path or pattern.\n"
				+ "    - Use a relative path with respect to the current project directory.\n"
				+ "    - If an absolute path is provided, it must be located within the project directory.\n"
				+ "    - Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
				+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").\n\n"
				+ "Options:";
		String footer = "\nExamples:\n"
				+ "  java -jar gw.jar C:\\\\projects\\project\n"
				+ "  java -jar gw.jar src\\project\n"
				+ "  java -jar gw.jar \"glob:**/*.java\"\n"
				+ "  java -jar gw.jar \"regex:^.*/[^/]+\\.java$\"\n";
		HelpFormatter.builder().setShowSince(false).get().printHelp("java -jar gw.jar <path> [options]", header,
				options, footer, true);
	}

	/**
	 * Initializes the Ghostwriter home directory and registers it as a system
	 * property so it is visible to other components. Also triggers a one-time
	 * version log entry.
	 *
	 * @param config configuration source
	 * @return resolved home directory; falls back to the current user directory
	 *         when not explicitly configured
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
	 * Logs the application version when available from package metadata. If no
	 * implementation version is present (e.g., when running from an IDE without a
	 * packaged manifest), no log entry is produced.
	 */
	private static void logVersion() {
		String version = Objects.toString(Ghostwriter.class.getPackage().getImplementationVersion(), "DEVELOPMENT");
		LOGGER.info(StringUtils.center(" Starting Ghostwriter CLI " + version + " (Machanism.org/Machai) ",
				GWConstants.LOG_LINE_LENGTH, "-"));
	}

	/**
	 * Loads the external configuration properties file when present, using either
	 * the {@code GWConstants.CONFIG_PROP_NAME} system property or the default
	 * Ghostwriter properties file name, resolved relative to the home directory.
	 *
	 * <p>
	 * Failures to locate or load the file are tolerated: a missing file is silently
	 * ignored, while unexpected runtime errors are logged as warnings so startup
	 * can continue.
	 * </p>
	 *
	 * @param config configuration source to populate
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
	 * <p>
	 * For every setting, an explicit command-line value takes precedence over the
	 * corresponding value from {@code config}, which in turn takes precedence over
	 * any built-in default.
	 * </p>
	 *
	 * @param cmd     parsed command line
	 * @param config  configuration source
	 * @param scanner console scanner used for optional interactive prompts
	 * @return populated runtime settings ready to be applied to a processor
	 */
	private static RuntimeSettings loadRuntimeSettings(CommandLine cmd, PropertiesConfigurator config,
			Scanner scanner) {
		RuntimeSettings settings = new RuntimeSettings();
		settings.genai = resolveGenai(cmd, config);
		settings.instructions = resolveInstructions(cmd, config, scanner);
		settings.excludes = resolveExcludes(cmd, config);
		settings.multiThread = resolveMultiThread(cmd, config);
		settings.projectDir = resolveProjectDir(cmd, config);
		settings.paths = resolvePaths(cmd, config);
		return settings;
	}

	/**
	 * Resolves the configured AI provider/model, preferring the {@code --model}
	 * command-line option over the corresponding configuration value.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return provider/model identifier (e.g. {@code "OpenAI:gpt-5.1"}), or
	 *         {@code null} if not configured
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
	 * Resolves the system instruction text from CLI input or configuration.
	 *
	 * <p>
	 * If the {@code --instructions} option is present without a value, the user is
	 * interactively prompted (via {@code scanner}) to enter the instruction text.
	 * </p>
	 *
	 * @param cmd     parsed command line
	 * @param config  configuration source
	 * @param scanner console scanner used for prompting when no value is supplied
	 * @return resolved instruction text, or {@code null} if not configured
	 */
	private static String resolveInstructions(CommandLine cmd, PropertiesConfigurator config, Scanner scanner) {
		String instructions = config.get(GWConstants.INSTRUCTIONS_PROP_NAME, null);
		if (!cmd.hasOption(INSTRUCTIONS_PROP_NAME)) {
			return instructions;
		}
		String optionValue = cmd.getOptionValue(INSTRUCTIONS_PROP_NAME);
		return optionValue == null ? promptForValue(scanner, "Instructions: ") : optionValue;
	}

	/**
	 * Resolves the exclude list from the {@code --excludes} command-line option or
	 * configuration, splitting the comma-separated value into individual patterns.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return exclude patterns split by comma, or {@code null} if not configured
	 */
	private static String[] resolveExcludes(CommandLine cmd, PropertiesConfigurator config) {
		String configuredValue = cmd.hasOption(EXCLUDES_OPTION) ? cmd.getOptionValue(EXCLUDES_OPTION)
				: config.get(GWConstants.EXCLUDES_PROP_NAME, null);
		return StringUtils.split(configuredValue, ',');
	}

	/**
	 * Resolves the concurrency (thread count) setting from the {@code --threads}
	 * command-line option or configuration.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return configured thread count as text, or {@code null} if not configured
	 */
	private static String resolveMultiThread(CommandLine cmd, PropertiesConfigurator config) {
		return cmd.hasOption(THREADS_OPTION) ? cmd.getOptionValue(THREADS_OPTION)
				: config.get(GWConstants.THREADS_PROP_NAME, null);
	}

	/**
	 * Resolves the project directory from the {@code -d} command-line option or
	 * configuration.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return project directory; falls back to the current user directory when not
	 *         explicitly configured
	 */
	private static File resolveProjectDir(CommandLine cmd, PropertiesConfigurator config) {
		if (cmd.hasOption(PROJECT_DIR_PROP_NAME)) {
			return new File(cmd.getOptionValue(PROJECT_DIR_PROP_NAME));
		}
		File configuredProjectDir = config.getFile(GWConstants.PROJECT_DIR_PROP_NAME, null);
		return configuredProjectDir == null ? SystemUtils.getUserDir() : configuredProjectDir;
	}

	/**
	 * Resolves the scan directories or patterns to process, in order of precedence:
	 * positional command-line arguments, then the configured path property, then
	 * the current directory ({@code "."}) as a final fallback.
	 *
	 * @param cmd    parsed command line
	 * @param config configuration source
	 * @return scan path(s) or pattern(s) to process; never {@code null} or empty
	 */
	private static String[] resolvePaths(CommandLine cmd, PropertiesConfigurator config) {
		String[] paths = cmd.getArgs();
		if (paths != null && paths.length > 0) {
			return paths;
		}
		String configuredPath = config.get(GWConstants.PATH_PROP_NAME, null);
		if (configuredPath != null) {
			return new String[] { configuredPath };
		}
		return new String[] { "." };
	}

	/**
	 * Prompts the user for a single (possibly multi-line) piece of input on
	 * standard input, printing the prompt via {@link Console} when available or
	 * plain {@code System.out} otherwise.
	 *
	 * <p>
	 * Lines ending with {@code GWConstants.MULTIPLE_LINES_BREAKER} are treated as
	 * continued: the breaker is stripped and a line separator is appended, and
	 * reading continues on the next line. Reading stops at the first line that does
	 * not end with the breaker. After input is collected, a right-aligned signature
	 * footer with the current user name is printed.
	 * </p>
	 *
	 * @param scanner scanner reading standard input
	 * @param prompt  prompt text to display before reading
	 * @return the entered value, with continuation markers removed and multiple
	 *         lines joined by the platform line separator
	 */
	private static String promptForValue(Scanner scanner, String prompt) {
		String input;
		Console console = System.console();
		if (console != null) {
			console.format(prompt);

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = console.readLine()) != null) {
				if (Strings.CS.endsWith(line, String.valueOf(GWConstants.MULTIPLE_LINES_BREAKER))) {
					sb.append(StringUtils.substringBeforeLast(line, String.valueOf(GWConstants.MULTIPLE_LINES_BREAKER)))
							.append(AbstractAIProvider.LINE_SEPARATOR);
				} else {
					sb.append(line);
					break;
				}
			}
			input = sb.toString();

		} else {
			System.out.print(prompt);

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = scanner.nextLine()) != null) {
				if (Strings.CS.endsWith(line, String.valueOf(GWConstants.MULTIPLE_LINES_BREAKER))) {
					sb.append(StringUtils.substringBeforeLast(line, String.valueOf(GWConstants.MULTIPLE_LINES_BREAKER)))
							.append(AbstractAIProvider.LINE_SEPARATOR);
				} else {
					sb.append(line);
					break;
				}
			}
			input = sb.toString();
		}

		return input;
	}

	/**
	 * Logs basic startup path information at INFO level: the resolved Ghostwriter
	 * home directory and the resolved project directory.
	 *
	 * @param gwHomeDir  Ghostwriter home directory
	 * @param projectDir project directory
	 */
	private static void logStartup(File gwHomeDir, File projectDir) {
		LOGGER.info("Home directory: {}", gwHomeDir);
		LOGGER.info("Project directory: {}", projectDir);
	}

	/**
	 * Creates and runs the selected processor (guidance or act mode) against the
	 * resolved scan paths, then applies the resulting exit code.
	 *
	 * <p>
	 * Recognized failures are logged and translated into a non-zero exit code via
	 * {@link #handleExitCode(int)} rather than propagating as uncaught exceptions,
	 * except for I/O errors during processor creation, which are rethrown.
	 * </p>
	 *
	 * @param scanner  console scanner
	 * @param config   configuration source
	 * @param cmd      parsed command line
	 * @param settings resolved runtime settings
	 * @throws IOException if processor creation fails with an I/O error
	 */
	private static void execute(Scanner scanner, PropertiesConfigurator config, CommandLine cmd,
			RuntimeSettings settings) throws IOException {
		try {
			AIFileProcessor processor = createProcessor(scanner, config, cmd, settings);
			applyCommonSettings(processor, settings);
			handleExitCode(processPathectories(processor, settings.paths, settings.projectDir));
		} catch (ActNotFound e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("I/O error occurred during file processing: {}", e.getMessage(), e);
		}
	}

	/**
	 * Terminates the JVM with the given exit code when it is non-zero. A zero exit
	 * code is treated as success and does not trigger {@link System#exit(int)}.
	 *
	 * @param exitCode exit code to apply
	 */
	private static void handleExitCode(int exitCode) {
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	/**
	 * Creates either a {@link GuidanceProcessor} (default mode) or an
	 * {@link ActProcessor} (when {@code --act} is specified), fully configured for
	 * the current run.
	 *
	 * @param scanner  console scanner
	 * @param config   configuration source
	 * @param cmd      parsed command line
	 * @param settings resolved runtime settings
	 * @return configured processor ready for use
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
	 * Creates an {@link ActProcessor} with console-backed interactive input,
	 * overriding {@link ActProcessor#input()} to read from the CLI scanner instead
	 * of the default input source. Logs the resolved AI model, if any, at INFO
	 * level.
	 *
	 * @param scanner  console scanner
	 * @param config   configuration source
	 * @param settings resolved runtime settings
	 * @return act processor configured for CLI-driven interactive input
	 */
	private static ActProcessor createActProcessor(Scanner scanner, PropertiesConfigurator config,
			RuntimeSettings settings) {
		String genai = settings.genai;
		if (genai != null) {
			LOGGER.info(DEFAULT_MODEL_MSG, genai);
		}
		return new ActProcessor(settings.projectDir, genai, config) {
			@Override
			protected String input() {
				return readActInput(scanner);
			}
		};
	}

	/**
	 * Reads possibly multi-line interactive act input from standard input, printing
	 * the {@link #USER_INPUT_PREFIX} prompt before each line and supporting the
	 * same continuation-marker convention as
	 * {@link #promptForValue(Scanner, String)}.
	 *
	 * @param scanner scanner reading standard input
	 * @return collected input text, with continuation markers removed and multiple
	 *         lines joined by the platform line separator
	 */
	private static String readActInput(Scanner scanner) {
		Console console = System.console();
		formatConsole(console, USER_INPUT_PREFIX);
		StringBuilder sb = new StringBuilder();
		while (scanner.hasNextLine()) {
			String nextLine = scanner.nextLine();
			if (!Strings.CS.endsWith(nextLine, String.valueOf(GWConstants.MULTIPLE_LINES_BREAKER))) {
				sb.append(nextLine);
				break;
			}
			appendContinuedLine(sb, nextLine);
			formatConsole(console, "\t");
		}
		return sb.toString();
	}

	/**
	 * Writes a prompt message to the console when available. If no {@link Console}
	 * is attached (e.g., input is redirected), no output is produced.
	 *
	 * @param console console instance, may be {@code null}
	 * @param message message to print, followed by {@code ": "}
	 */
	private static void formatConsole(Console console, String message) {
		if (console != null) {
			console.format(message + ": ");
		} else {
			System.out.print(message + ": ");
		}
	}

	/**
	 * Appends a continued input line to the buffer, stripping the trailing
	 * continuation marker and replacing it with the platform line separator.
	 *
	 * @param sb       buffer receiving the line
	 * @param nextLine line that ends with the continuation marker
	 */
	private static void appendContinuedLine(StringBuilder sb, String nextLine) {
		sb.append(StringUtils.substringBeforeLast(nextLine, String.valueOf(GWConstants.MULTIPLE_LINES_BREAKER)))
				.append(AbstractAIProvider.LINE_SEPARATOR);
	}

	/**
	 * Configures a custom acts location on the given processor when one is provided
	 * via the {@code --acts} command-line option or configuration. Leaves the
	 * processor's default acts location unchanged if none is configured.
	 *
	 * @param cmd          parsed command line
	 * @param config       configuration source
	 * @param actProcessor act processor to configure
	 */
	private static void configureActsLocation(CommandLine cmd, PropertiesConfigurator config,
			ActProcessor actProcessor) {
		String actsLocation = null;

		if (cmd.hasOption(ACTS_OPTION)) {
			actsLocation = cmd.getOptionValue(ACTS_OPTION);
		} else {
			actsLocation = config.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		}

		if (actsLocation == null) {
			return;
		}
		LOGGER.info("Custom acts location specified: {}", actsLocation);
		actProcessor.setActsLocation(actsLocation);
	}

	/**
	 * Configures the default act to run when act mode is enabled.
	 *
	 * <p>
	 * The act name/prompt is resolved from the {@code --act} command-line option
	 * value or configuration. If {@code --act} is present without a value, the user
	 * is interactively prompted (via {@code scanner}) to enter the act name.
	 * </p>
	 *
	 * @param cmd          parsed command line
	 * @param config       configuration source
	 * @param scanner      scanner used for prompting when no value is supplied
	 * @param actProcessor act processor to configure
	 * @throws IOException if act loading fails
	 */
	private static void configureDefaultAct(CommandLine cmd, PropertiesConfigurator config, Scanner scanner,
			ActProcessor actProcessor) throws IOException {
		String defaultPrompt = cmd.hasOption(ACT_OPTION) ? cmd.getOptionValue(ACT_OPTION)
				: config.get(GWConstants.ACT_PROP_NAME, null);
		if (cmd.hasOption(ACT_OPTION) && defaultPrompt == null) {
			defaultPrompt = promptForValue(scanner, "Act: ");
		} else {
			logAbbreviatedMessage("Act", defaultPrompt);
		}
		LOGGER.info("Requested act: {}", defaultPrompt);
		actProcessor.setAct(defaultPrompt);
	}

	/**
	 * Applies shared processor settings &mdash; instructions, excludes, and
	 * concurrency &mdash; to the given processor, skipping any setting that was not
	 * resolved.
	 *
	 * @param processor processor to configure
	 * @param settings  resolved runtime settings
	 */
	private static void applyCommonSettings(AIFileProcessor processor, RuntimeSettings settings) {
		applyInstructions(processor, settings.instructions);
		applyExcludes(processor, settings.excludes);
		applyConcurrency(processor, settings.multiThread);
	}

	/**
	 * Applies custom instructions to the processor when present, logging an
	 * abbreviated version of the text at INFO level.
	 *
	 * @param processor    processor to configure
	 * @param instructions instruction text, or {@code null} to leave unset
	 */
	private static void applyInstructions(AIFileProcessor processor, String instructions) {
		if (instructions == null) {
			return;
		}
		logAbbreviatedMessage("Instructions", instructions);
		processor.setInstructions(instructions);
	}

	/**
	 * Applies exclude patterns to the processor when configured, logging the full
	 * list at INFO level.
	 *
	 * @param processor processor to configure
	 * @param excludes  exclude patterns, or {@code null} to leave unset
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
	 * Applies the configured concurrency (thread count) setting to the processor
	 * when present, logging the resolved value at INFO level.
	 *
	 * @param processor processor to configure
	 * @param threads   thread count as text, or {@code null} to leave unset; parsed
	 *                  with {@link Integer#parseInt(String)}
	 */
	private static void applyConcurrency(AIFileProcessor processor, String threads) {
		if (threads != null) {
			int threadCount = Integer.parseInt(threads);
			LOGGER.info("Threads: {}", threadCount);
			processor.setThreads(threadCount);
		}
	}

	/**
	 * Logs an abbreviated value at INFO level, skipping the abbreviation work
	 * entirely when INFO logging is disabled.
	 *
	 * @param label message label
	 * @param value message value
	 */
	private static void logAbbreviatedMessage(String label, String value) {
		if (LOGGER.isInfoEnabled()) {
			String abbreviatedValue = StringUtils.abbreviate(value, AbstractAIProvider.LOG_LINE_LENG);
			LOGGER.info("{}: {}", label, abbreviatedValue);
		}
	}

	/**
	 * Processes all requested scan paths using the given processor, converting
	 * recognized failures into an appropriate exit code instead of propagating them
	 * further, and always logging usage statistics and completion.
	 *
	 * @param processor  configured processor
	 * @param paths      scan directories or patterns to process, in order
	 * @param projectDir project directory
	 * @return {@code 0} on success; the termination exception's exit code if
	 *         processing was explicitly terminated; {@link #EXIT_CODE_ERROR} for
	 *         any other handled failure
	 */
	private static int processPathectories(AIFileProcessor processor, String[] paths, File projectDir) {
		int exitCode = 0;
		try {
			for (String path : paths) {
				LOGGER.info("Starting scan of path: `{}`", path);
				processor.scanDocuments(projectDir, path);
				LOGGER.info("Finished scanning path: `{}`", path);
			}
		} catch (ProcessTerminationException e) {
			exitCode = handleProcessTermination(e);
		} catch (IllegalArgumentException e) {
			exitCode = handleProcessingFailure("Error", e);
		} catch (Exception e) {
			exitCode = handleProcessingFailure("Unexpected error", e);
		} finally {
			UsageStatistics.logUsage();
			LOGGER.info("File processing finished.");
		}
		return exitCode;
	}

	/**
	 * Handles an explicit process termination request raised by a processor or
	 * tool, logging the termination message and reason before returning the exit
	 * code requested by the exception.
	 *
	 * @param exception termination exception carrying the requested exit code
	 * @return exit code to use, as specified by {@code exception}
	 */
	private static int handleProcessTermination(ProcessTerminationException exception) {
		int exitCode = exception.getExitCode();
		if (exitCode != 0) {
			if (StringUtils.isBlank(exception.getMessage())) {
				LOGGER.error("Process terminated. Exit code: {}", exception.getExitCode());
			} else {
				LOGGER.error("Process terminated: {}, Exit code: {}", exception.getMessage(), exception.getExitCode());
			}
		}
		return exitCode;
	}

	/**
	 * Logs a processing failure with the given message prefix and returns the
	 * generic error exit code.
	 *
	 * @param message   log prefix describing the failure category
	 * @param exception failure that occurred during processing
	 * @return {@link #EXIT_CODE_ERROR}
	 */
	private static int handleProcessingFailure(String message, Exception exception) {
		LOGGER.error("{}: {}", message, exception.getMessage(), exception);
		return EXIT_CODE_ERROR;
	}

	/**
	 * Mutable holder for startup settings resolved before processor creation.
	 *
	 * <p>
	 * Instances are populated once by
	 * {@link Ghostwriter#loadRuntimeSettings(CommandLine, PropertiesConfigurator, Scanner)}
	 * and then read by the various {@code apply*}/{@code create*} helper methods
	 * while wiring up the processor for the current run.
	 * </p>
	 */
	private static final class RuntimeSettings {
		/**
		 * Configured AI provider/model identifier (e.g. {@code "OpenAI:gpt-5.1"}), or
		 * {@code null}.
		 */
		private String genai;
		/** Resolved system instruction text, or {@code null} if not configured. */
		private String instructions;
		/**
		 * Exclude patterns to skip during scanning, or {@code null} if not configured.
		 */
		private String[] excludes;
		/** Configured thread count as text, or {@code null} if not configured. */
		private String multiThread;
		/** Resolved project directory. */
		private File projectDir;
		/** Scan directories or patterns to process. */
		private String[] paths;
	}
}