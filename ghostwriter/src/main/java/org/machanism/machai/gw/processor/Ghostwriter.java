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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Ghostwriter {

	public static final String MULTIPLE_LINES_BREAKER = "\\";

	private static Logger logger;

	public static final String PROJECT_DIR_PROP_NAME = "project.dir";

	public static final String GW_PROPERTIES_FILE_NAME = "gw.properties";

	public static final String CONFIG_PROP_NAME = "gw.config";

	public static final String HOME_PROP_NAME = "gw.home";

	public static final String MODEL_PROP_NAME = "gw.model";

	public static final String INSTRUCTIONS_PROP_NAME = "instructions";

	public static final String EXCLUDES_PROP_NAME = "gw.excludes";

	public static final String ACTS_LOCATION_PROP_NAME = "gw.acts";

	public static final String ACT_PROP_NAME = "gw.act";

	public static final String THREADS_PROP_NAME = "gw.threads";

	public static final String SCAN_DIR_PROP_NAME = "gw.scanDir";

	public static final String NONRECURSIVE_PROP_NAME = "gw.nonRecursive";

	public static final String INPUTS_PROPERTY_NAME = "inputs";

	public static final String INTERACTIVE_MODE_PROP_NAME = "gw.interactive";

	private final AIFileProcessor processor;

	public Ghostwriter(String genai, AIFileProcessor processor) {
		if (StringUtils.isBlank(genai)) {
			throw new IllegalArgumentException("No GenAI provider/model configured. Set '" + MODEL_PROP_NAME
					+ "' in " + GW_PROPERTIES_FILE_NAME + " or pass -m/--model (e.g., OpenAI:gpt-5.1).");
		}

		this.processor = processor;
	}

	private static void logScanStart(String scanDir) {
		logger.info("Starting scan of path: {}", scanDir);
	}

	private static void logScanFinish(String scanDir) {
		logger.info("Finished scanning path: {}", scanDir);
	}

	private static void logInstructions(String instructions) {
		if (logger.isInfoEnabled()) {
			// Sonar java:S2629 - evaluate abbreviate only when INFO logging is enabled.
			logger.info("Instructions: {}", abbreviateInstructions(instructions));
		}
	}

	private static String abbreviateInstructions(String instructions) {
		return StringUtils.abbreviate(instructions, 60);
	}

	private static void applyActPrompt(CommandLine cmd, PropertiesConfigurator config, AIFileProcessor processor) {
		String defaultPrompt = resolveActPrompt(cmd, config);
		logDefaultPrompt("Act", defaultPrompt);
		processor.setDefaultPrompt(defaultPrompt);
	}

	private static File resolveProjectDir(CommandLine cmd, Option projectDirOpt, PropertiesConfigurator config) {
		File projectDir = null;
		if (cmd.hasOption(projectDirOpt)) {
			projectDir = new File(cmd.getOptionValue(projectDirOpt));
		}
		if (projectDir == null) {
			projectDir = config.getFile(PROJECT_DIR_PROP_NAME, null);
			if (projectDir == null) {
				projectDir = SystemUtils.getUserDir();
			}
		}
		return projectDir;
	}

	// Sonar java:S3776 - reduced Cognitive Complexity by extracting CLI resolution helpers.
	public int perform(String[] scanDirs) throws IOException {
		int exitCode = 0;
		try {
			for (String scanDir : scanDirs) {
				logScanStart(scanDir);
				File projectDir = processor.getProjectDir();

				processor.scanDocuments(projectDir, scanDir);
				logScanFinish(scanDir);
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

	static PropertiesConfigurator initializeConfiguration(File projectDir) {
		PropertiesConfigurator config = new PropertiesConfigurator();

		File gwHomeDir = config.getFile(HOME_PROP_NAME, null);
		if (gwHomeDir == null) {
			gwHomeDir = projectDir;
			if (gwHomeDir == null) {
				gwHomeDir = SystemUtils.getUserDir();
			}
		}

		System.setProperty(HOME_PROP_NAME, gwHomeDir.getAbsolutePath());
		logger = LoggerFactory.getLogger(Ghostwriter.class);

		String version = Ghostwriter.class.getPackage().getImplementationVersion();
		if (version != null) {
			logger.info("Ghostwriter {} (Machai project)", version);
		}
		logger.info("Home directory: {}", gwHomeDir);

		try {
			File configFile = resolveConfigFile(gwHomeDir);
			config.setConfiguration(configFile.getAbsolutePath());
		} catch (IOException e) {
			// The property file is not defined, ignore.
		} catch (RuntimeException e) {
			logger.warn("Failed to initialize configuration.", e);
		}

		return config;
	}

	private static File resolveConfigFile(File gwHomeDir) {
		String configFileName = System.getProperty(CONFIG_PROP_NAME, GW_PROPERTIES_FILE_NAME);
		return new File(gwHomeDir, configFileName);
	}

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

	@SuppressWarnings("java:S106")
	public static String readText(String prompt) {
		System.out.print(prompt);

		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				String nextLine = scanner.nextLine();
				if (Strings.CS.endsWith(nextLine, MULTIPLE_LINES_BREAKER)) {
					sb.append(StringUtils.substringBeforeLast(nextLine, MULTIPLE_LINES_BREAKER))
							.append(Genai.LINE_SEPARATOR);
					logger.info("\t");
				} else {
					sb.append(nextLine);
					break;
				}
			}
		}

		return sb.toString();
	}

	public void setExcludes(String[] excludes) {
		if (excludes != null) {
			logger.info("Excludes: {}", Arrays.toString(excludes));
			processor.setExcludes(excludes);
		}
	}

	public void setInstructions(String instructions) {
		if (instructions != null) {
			logInstructions(instructions);
			processor.setInstructions(instructions);
		}
	}

	public void setDefaultPrompt(String defaultGuidance) {
		if (defaultGuidance != null) {
			processor.setDefaultPrompt(defaultGuidance);
		}
	}

	public void setLogInputs(boolean logInputs) {
		processor.setLogInputs(logInputs);
	}

	public void setDegreeOfConcurrency(String multiThreadCount) {
		if (multiThreadCount != null) {
			int value = Integer.parseInt(multiThreadCount);
			processor.setDegreeOfConcurrency(value);
		}
	}

	static AIFileProcessor createProcessor(CommandLine cmd, File projectDir, PropertiesConfigurator config,
			String genai) {
		AIFileProcessor processor;
		if (cmd.hasOption("act")) {
			processor = createActProcessor(cmd, projectDir, config, genai);
			applyActPrompt(cmd, config, processor);
		} else {
			processor = new GuidanceProcessor(projectDir, genai, config);
		}
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
			String actsLocation = config.get(ACTS_LOCATION_PROP_NAME, null);
			if (actsLocation != null) {
				processor.setActsLocation(actsLocation);
			}
		}
		return processor;
	}

	static String resolveActPrompt(CommandLine cmd, PropertiesConfigurator config) {
		String defaultPrompt = config.get(ACT_PROP_NAME, null);
		if (cmd.hasOption("act")) {
			defaultPrompt = cmd.getOptionValue("act");
			if (defaultPrompt == null) {
				defaultPrompt = readText("Act");
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
			String gwScanDir = config.get(SCAN_DIR_PROP_NAME, null);
			if (gwScanDir != null) {
				scanDirs = new String[] { gwScanDir };
			}
		}
		if (scanDirs == null || scanDirs.length == 0) {
			scanDirs = new String[] { SystemUtils.getUserDir().getAbsolutePath() };
		}
		return scanDirs;
	}

	public static void main(String[] args) throws IOException, ParseException {
		Options options = new Options();
		Option helpOption = new Option("h", "help", false, "Show this help message and exit.");
		Option logInputsOption = new Option("l", Genai.LOG_INPUTS_PROP_NAME, false,
				"Log LLM request inputs to dedicated log files.");

		Option multiThreadOption = Option.builder("t").longOpt("threads")
				.desc("The degree of concurrency for the processing to improve performance.")
				.hasArg(true).build();

		Option projectDirOpt = new Option("d", PROJECT_DIR_PROP_NAME, true,
				"Specify the path to the root directory for file processing.");

		Option genaiOpt = new Option("m", "model", true,
				"Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1').");

		Option instructionsOpt = Option.builder("i").longOpt(INSTRUCTIONS_PROP_NAME)
				.desc("Specify system instructions as plain text, by URL, or by file path. "
						+ "Each line of input is processed: blank lines are preserved, lines starting with 'http://' or 'https://' are loaded from the specified URL, "
						+ "lines starting with 'file:' are loaded from the specified file path, and other lines are used as-is. "
						+ "If the option is used without a value, you will be prompted to enter instruction text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build();

		Option excludesOpt = new Option("e", "excludes", true,
				"Specify a comma-separated list of directories to exclude from processing.");

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

		PropertiesConfigurator config = initializeConfiguration(null);

		String genai = config.get(MODEL_PROP_NAME, null);
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

		String[] excludes = StringUtils.split(config.get(EXCLUDES_PROP_NAME, null), ',');
		if (cmd.hasOption(excludesOpt)) {
			excludes = StringUtils.split(cmd.getOptionValue(excludesOpt), ',');
		}

		String multiThread = config.get(THREADS_PROP_NAME, null);
		if (cmd.hasOption(multiThreadOption)) {
			multiThread = cmd.getOptionValue(multiThreadOption);
		}

		boolean logInputs = config.getBoolean(Genai.LOG_INPUTS_PROP_NAME, false);
		if (cmd.hasOption(logInputsOption)) {
			logInputs = true;
		}

		File projectDir = resolveProjectDir(cmd, projectDirOpt, config);
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
