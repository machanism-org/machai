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
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.gw.tools.CommandFunctionTools.ProcessTerminationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Ghostwriter {

	public static void main(String[] args) throws IOException, ParseException {
		Logger logger = LoggerFactory.getLogger(Ghostwriter.class);

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);

		Options options = new Options();
		Option helpOption = new Option("h", "help", false, "Show this help message and exit.");
		Option logInputsOption = new Option("l", Genai.LOG_INPUTS_PROP_NAME, false,
				"Log LLM request inputs to dedicated log files.");
		Option multiThreadOption = Option.builder("t").longOpt("threads")
				.desc("The degree of concurrency for the processing to improve performance.")
				.hasArg(true).build();
		Option projectDirOpt = new Option("d", GWConstants.PROJECT_DIR_PROP_NAME, true,
				"Specify the path to the root directory for file processing.");
		Option genaiOpt = new Option("m", "model", true,
				"Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1').");
		Option instructionsOpt = Option.builder("i").longOpt(GWConstants.INSTRUCTIONS_PROP_NAME)
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
			return;
		}

		PropertiesConfigurator config = new PropertiesConfigurator();

		File gwHomeDir = config.getFile(GWConstants.HOME_PROP_NAME, null);
		if (gwHomeDir == null) {
			gwHomeDir = SystemUtils.getUserDir();
		}
		System.setProperty(GWConstants.HOME_PROP_NAME, gwHomeDir.getAbsolutePath());
		String version = Ghostwriter.class.getPackage().getImplementationVersion();
		if (version != null) {
			logger.info("Ghostwriter {} (Machai Project)", version);
		}
		logger.info("Home directory: {}", gwHomeDir);

		try {
			String configFileName = System.getProperty(GWConstants.CONFIG_PROP_NAME,
					GWConstants.GW_PROPERTIES_FILE_NAME);
			File configFile = new File(gwHomeDir, configFileName);
			config.setConfiguration(configFile.getAbsolutePath());
		} catch (IOException e) {
			// The property file is not defined, ignore.
		} catch (RuntimeException e) {
			logger.warn("Failed to initialize configuration.", e);
		}

		String genai = config.get(GWConstants.MODEL_PROP_NAME, null);
		if (cmd.hasOption(genaiOpt)) {
			String opt = StringUtils.trimToNull(cmd.getOptionValue(genaiOpt));
			if (opt != null) {
				genai = opt;
			}
		}

		String instructions = config.get(GWConstants.INSTRUCTIONS_PROP_NAME, null);
		if (cmd.hasOption(instructionsOpt)) {
			instructions = cmd.getOptionValue(instructionsOpt);
			if (instructions == null) {
				System.out.print("Instructions: ");
				instructions = scanner.nextLine();
			}
		}

		String[] excludes = StringUtils.split(config.get(GWConstants.EXCLUDES_PROP_NAME, null), ',');
		if (cmd.hasOption(excludesOpt)) {
			excludes = StringUtils.split(cmd.getOptionValue(excludesOpt), ',');
		}

		String multiThread = config.get(GWConstants.THREADS_PROP_NAME, null);
		if (cmd.hasOption(multiThreadOption)) {
			multiThread = cmd.getOptionValue(multiThreadOption);
		}

		boolean logInputs = config.getBoolean(Genai.LOG_INPUTS_PROP_NAME, false);
		if (cmd.hasOption(logInputsOption)) {
			logInputs = true;
		}

		File projectDir = null;
		if (cmd.hasOption(projectDirOpt)) {
			projectDir = new File(cmd.getOptionValue(projectDirOpt));
		}
		if (projectDir == null) {
			projectDir = config.getFile(GWConstants.PROJECT_DIR_PROP_NAME, null);
			if (projectDir == null) {
				projectDir = SystemUtils.getUserDir();
			}
		}
		logger.info("Root directory: {}", projectDir);

		try {
			AIFileProcessor processor;
			if (cmd.hasOption("act")) {
				ActProcessor actProcessor = new ActProcessor(projectDir, config, genai) {
					@Override
					protected String input() {
						System.out.print(">>>: ");
						StringBuilder sb = new StringBuilder();
						while (scanner.hasNextLine()) {
							String nextLine = scanner.nextLine();
							if (Strings.CS.endsWith(nextLine, GWConstants.MULTIPLE_LINES_BREAKER)) {
								sb.append(StringUtils.substringBeforeLast(nextLine, GWConstants.MULTIPLE_LINES_BREAKER))
										.append(Genai.LINE_SEPARATOR);
								System.out.print("\t");
							} else {
								sb.append(nextLine);
								break;
							}
						}
						return sb.toString();
					}
				};

				if (cmd.hasOption("acts")) {
					String acts = cmd.getOptionValue("acts");
					logger.info("Custom acts location specified: {}", acts);
					actProcessor.setActsLocation(acts);
				} else {
					String actsLocation = config.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
					if (actsLocation != null) {
						actProcessor.setActsLocation(actsLocation);
					}
				}

				String defaultPrompt = config.get(GWConstants.ACT_PROP_NAME, null);
				if (cmd.hasOption("act")) {
					defaultPrompt = cmd.getOptionValue("act");
					if (defaultPrompt == null) {
						System.out.print("Act: ");
						defaultPrompt = scanner.nextLine();
					}
				}
				if (defaultPrompt != null) {
					logger.info("Act: {}", StringUtils.abbreviate(defaultPrompt, GWConstants.LOG_PROMPT_MAX_LENGTH));
					actProcessor.setAct(defaultPrompt);
				}
				processor = actProcessor;
			} else {
				processor = new GuidanceProcessor(projectDir, genai, config);
			}

			if (instructions != null) {
				logger.info("Instructions: {}", StringUtils.abbreviate(instructions, GWConstants.LOG_PROMPT_MAX_LENGTH));
				processor.setInstructions(instructions);
			}
			if (excludes != null) {
				logger.info("Excludes: {}", Arrays.toString(excludes));
				processor.setExcludes(excludes);
			}
			if (multiThread != null) {
				int value = Integer.parseInt(multiThread);
				processor.setDegreeOfConcurrency(value);
			}
			processor.setLogInputs(logInputs);

			String[] scanDirs = cmd.getArgs();
			if (scanDirs == null || scanDirs.length == 0) {
				String gwScanDir = config.get(GWConstants.SCAN_DIR_PROP_NAME, null);
				if (gwScanDir != null) {
					scanDirs = new String[] { gwScanDir };
				}
			}
			if (scanDirs == null || scanDirs.length == 0) {
				scanDirs = new String[] { "." };
			}

			int exitCode = 0;
			try {
				for (String scanDir : scanDirs) {
					logger.info("Starting scan of path: {}", scanDir);
					processor.scanDocuments(projectDir, scanDir);
					logger.info("Finished scanning path: {}", scanDir);
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

			if (exitCode != 0) {
				System.exit(exitCode);
			}
		} catch (ActNotFound e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error("I/O error occurred during file processing: {}", e.getMessage(), e);
		}
	}
}