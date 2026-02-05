package org.machanism.machai.gw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line entry point for the Ghostwriter document scanning tool.
 *
 * <p>
 * This class:
 * </p>
 * <ul>
 * <li>parses CLI options</li>
 * <li>loads configuration from {@code gw.properties} (or
 * {@code -Dgw.config})</li>
 * <li>configures and runs {@link FileProcessor} over one or more scan
 * roots</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * java -jar gw.jar C:\projects\my-project
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public final class Ghostwriter {

	/** Logger for the Ghostwriter application. */
	private static Logger logger;

	/** Default provider/model identifier used when none is configured. */
	private static final String DEFAULT_GENAI_VALUE = "OpenAI:gpt-5-mini";

	/** Properties-based configuration used by the CLI. */
	private static PropertiesConfigurator config = new PropertiesConfigurator();

	/** Directory used as the execution base for relative configuration files. */
	private static File execDir;

	static {
		try {
			execDir = new File(Ghostwriter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (execDir.isFile()) {
				execDir = execDir.getParentFile();
			}

			org.machanism.machai.log.FileAppender.setExecutionDir(execDir);
			logger = LoggerFactory.getLogger(Ghostwriter.class);
			logger.info("Executing in directory: {}", execDir);

			try {
				File configFile = new File(execDir, System.getProperty("gw.config", "gw.properties"));
				config.setConfiguration(configFile.getAbsolutePath());
			} catch (IOException e) {
				// The property file is not defined, ignore.
			}
		} catch (Exception e) {
			// Configuration file not found. An alternative configuration method will be
			// used.
		}
	}

	private Ghostwriter() {
		// Utility class.
	}

	/**
	 * Main entry point.
	 *
	 * @param args command line arguments
	 * @throws IOException if scanning fails while reading files
	 */
	public static void main(String[] args) throws IOException {

		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "Show this help message and exit.");
		Option logInputsOption = new Option("l", "logInputs", false, "Log LLM request inputs to dedicated log files.");

		Option multiThreadOption = Option.builder("t").longOpt("threads")
				.desc("Enable multi-threaded processing to improve performance (default: true).").hasArg(true)
				.optionalArg(true).build();

		Option rootDirOpt = new Option("r", "root", true,
				"Specify the path to the root directory for file processing.");
		Option genaiOpt = new Option("a", "genai", true, "Set the GenAI provider and model (e.g., 'OpenAI:gpt-5.1').");

		Option instructionsOpt = Option.builder("i").longOpt("instructions")
				.desc("Specify additional instructions by URL or file path. "
						+ "To provide multiple locations, separate them with a comma (`,`). "
						+ "If the option is used without a value, you will be prompted to enter instruction text via standard input (stdin).")
				.hasArg(true).optionalArg(true).build();

		Option excludesOpt = new Option("e", "excludes", true,
				"Specify a list of directories to exclude from processing. You can provide multiple directories separated by commas or by repeating the option.");

		Option guidanceOpt = Option.builder("g").longOpt("guidance")
				.desc("Specify the default guidance file to apply as a final step for the current directory. "
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

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(helpOption)) {
				help(options, formatter);
				return;
			}

			File rootDir = config.getFile("root", null);
			if (cmd.hasOption(rootDirOpt)) {
				rootDir = new File(cmd.getOptionValue(rootDirOpt));
			}

			String genai = config.get("genai", DEFAULT_GENAI_VALUE);
			if (cmd.hasOption(genaiOpt)) {
				genai = cmd.getOptionValue(genaiOpt);
			}

			String[] instructionLocations = StringUtils.split(config.get("instructions", null), ",");
			String instructions = null;
			if (cmd.hasOption(instructionsOpt)) {
				String optionValue = cmd.getOptionValue(instructionsOpt);
				if (optionValue != null) {
					instructionLocations = StringUtils.split(optionValue, ",");
				} else {
					instructions = readText("No instructions were provided as an option value.\n"
							+ "Please enter the instructions text below. When you are done, press "
							+ (SystemUtils.IS_OS_WINDOWS ? "Ctrl + Z" : "Ctrl + D")
							+ " to finish and signal end of input (EOF):");
				}
			}

			if (instructionLocations != null && instructionLocations.length > 0) {
				instructionLocations = Arrays.stream(instructionLocations).map(StringUtils::trimToNull)
						.map(location -> {
							if (location != null && !location.startsWith("http://") && !location.startsWith("https://")
									&& !(new File(location).isAbsolute())) {
								return new File(execDir, location).getAbsolutePath();
							}
							return location;
						}).toArray(String[]::new);
			}

			String[] dirs = cmd.getArgs();

			if (rootDir == null) {
				rootDir = SystemUtils.getUserDir();
				if (dirs.length == 0) {
					dirs = new String[] { rootDir.getAbsolutePath() };
				}
			} else {
				if (dirs.length == 0) {
					dirs = new String[] { SystemUtils.getUserDir().getPath() };
				}
			}

			String[] excludes = StringUtils.split(config.get("excludes", null), ",");
			if (cmd.hasOption(excludesOpt)) {
				excludes = StringUtils.split(cmd.getOptionValue(excludesOpt), ",");
			}

			logger.info("Root directory: {}", rootDir);
			boolean multiThread = Boolean.parseBoolean(cmd.getOptionValue(multiThreadOption, "true"));

			String defaultGuidance = config.get("guidance", null);
			if (cmd.hasOption(guidanceOpt)) {
				String guidanceFileName = cmd.getOptionValue(guidanceOpt);
				if (guidanceFileName != null) {
					defaultGuidance = getInstructionsFromFile(guidanceFileName);
					if (defaultGuidance == null) {
						throw new FileNotFoundException("Guidance file '" + guidanceFileName
								+ "' not found. Please verify that the file exists at the expected location and is accessible.");
					}
					logger.info("Default guidance: {}", guidanceFileName);

				} else {
					defaultGuidance = readText("Please enter the guidance text below. When finished, press "
							+ (SystemUtils.IS_OS_WINDOWS ? "Ctrl + Z" : "Ctrl + D") + " to signal end of input (EOF):");
				}
			}

			boolean logInputs = cmd.hasOption(logInputsOption);

			for (String scanDir : dirs) {
				logger.info("Starting scan of directory: {}", scanDir);

				String currentFile = ProjectLayout.getRelatedPath(rootDir, new File(scanDir));
				if (currentFile != null) {

					FileProcessor processor = new FileProcessor(genai, config);
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
						processor.setDefaultGuidance(defaultGuidance);
					}

					processor.setLogInputs(logInputs);

					processor.scanDocuments(rootDir, scanDir);
					logger.info("Finished scanning directory: {}", scanDir);

				} else {
					logger.error("The directory '{}' must be located within the root directory '{}'.", scanDir,
							rootDir);
				}
			}

		} catch (ParseException e) {
			System.err.println("Error parsing arguments: " + e.getMessage());
			help(options, formatter);
		} finally {
			logger.info("Finished processing files.");
		}
	}

	private static String getInstructionsFromFile(String instructionsFile) {
		String result = null;
		if (instructionsFile != null) {
			File file = new File(instructionsFile);

			if (!file.isAbsolute()) {
				file = new File(execDir, instructionsFile);
			}

			if (file.exists()) {
				try (FileReader reader = new FileReader(file)) {
					result = IOUtils.toString(reader);

				} catch (IOException e) {
					logger.error("Failed to read instructions file: {}", file.getAbsolutePath(), e);
				}
			}
		}
		return result;
	}

	private static void help(Options options, HelpFormatter formatter) {
		String header = "\nGhostwriter CLI - Scan and process directories or files using GenAI guidance.\n" + "Usage:\n"
				+ "  java -jar gw.jar <path | path_pattern> [options]\n\n" + "Options:";
		String footer = "\nExamples:\n" + "  java -jar gw.jar C:\\projects\\project\n"
				+ "  java -r C:\\projects\\project -jar gw.jar src/project\n"
				+ "  java -r C:\\projects\\project -jar gw.jar \"glob:**/*.java\"\n"
				+ "  java -r C:\\projects\\project -jar gw.jar \"regex:^.*\\/[^\\/]+\\.java$\"\n";
		formatter.printHelp("java -jar gw.jar <path | pattern>", header, options, footer, true);
	}

	private static String readText(String prompt) {
		System.out.println(prompt);
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append("\n");
			}
		}
		return sb.length() > 0 ? sb.deleteCharAt(sb.length() - 1).toString() : null;
	}
}
