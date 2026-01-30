package org.machanism.machai.gw;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

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
 * Entry point for document scanning and review automation.
 * <p>
 * Initializes the AI provider, configures the Ghostwriter, and runs document
 * scan over the user directory. Output is logged.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * {@code
 * java org.machanism.machai.gw.Ghostwriter
 * }
 * </pre>
 *
 * <p>
 * Usage is typically direct from command line or script/CI runner.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public final class Ghostwriter {

	/** Logger for the ghostwriter application. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Ghostwriter.class);

	private static final String DEFAULT_GENAI_VALUE = "OpenAI:gpt-5-mini";
	private static PropertiesConfigurator config = new PropertiesConfigurator();
	private static File execDir;

	static {
		try {
			execDir = new File(Ghostwriter.class.getProtectionDomain().getCodeSource().getLocation()
					.toURI());
			if (execDir.isFile()) {
				execDir = execDir.getParentFile();
			}
			LOGGER.info("Executing in directory: {}", execDir);
			File configFile = new File(execDir, "gw.properties");
			config.load(configFile.getAbsolutePath());
		} catch (Exception e) {
			// configuration file not found.
		}
	}

	private Ghostwriter() {
		// Utility class.
	}

	/**
	 * Main entry point for document scanning run.
	 *
	 * @param args command line arguments
	 * @throws IOException if document scanning fails
	 */
	public static void main(String[] args) throws IOException {

		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "Show this help message and exit.");
		Option multiThreadOption = new Option("t", "threads", false,
				"Enable multi-threaded processing for improved performance.");
		Option rootDirOpt = new Option("d", "dir", true, "Specify the path to the project directory.");
		Option genaiOpt = new Option("g", "genai", true,
				"Set the GenAI service provider and model (e.g., 'OpenAI:gpt-5.1').");
		Option instructionsOpt = new Option("i", "instructions", true,
				"Provide the file name containing additional processing instructions.");
		Option guidanceOpt = Option.builder("u")
				.longOpt("guidance")
				.desc("Specify the default guidance file to apply as a final step for the current directory.")
				.hasArg(true)
				.optionalArg(true)
				.build();

		options.addOption(helpOption);
		options.addOption(rootDirOpt);
		options.addOption(multiThreadOption);
		options.addOption(genaiOpt);
		options.addOption(instructionsOpt);
		options.addOption(guidanceOpt);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(helpOption)) {
				help(options, formatter);
				return;
			}

			File rootDir = null;
			if (cmd.hasOption(rootDirOpt)) {
				rootDir = new File(cmd.getOptionValue(rootDirOpt));
			}

			String genai = cmd.getOptionValue(genaiOpt);
			String defaultGenai = config.get("genai", DEFAULT_GENAI_VALUE);
			genai = Optional.ofNullable(genai).orElse(defaultGenai);

			File defaultRootDir = config.getFile("dir");
			rootDir = Optional.ofNullable(rootDir).orElse(defaultRootDir);

			String instructionsFileName = null;
			if (cmd.hasOption(instructionsOpt)) {
				instructionsFileName = cmd.getOptionValue(instructionsOpt);
			} else {
				instructionsFileName = config.get("instructions");
			}

			String instructions = null;
			if (instructionsFileName != null) {
				instructions = getInstractionsFromFile(instructionsFileName);
				if (instructions == null) {
					LOGGER.warn("Guidance file not found: {}", instructionsFileName);
				}
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

			LOGGER.info("Root directory: {}", rootDir);
			boolean multiThread = cmd.hasOption(multiThreadOption);

			String defaultGuidance = null;
			if (cmd.hasOption(guidanceOpt)) {
				String guidanceFileName = cmd.getOptionValue(guidanceOpt);
				guidanceFileName = StringUtils.defaultIfBlank(guidanceFileName, "@guidance.txt");
				defaultGuidance = getInstractionsFromFile(guidanceFileName);
			}

			for (String scanDir : dirs) {
				LOGGER.info("Starting scan of directory: {}", scanDir);

				String currentFile = ProjectLayout.getRelatedPath(rootDir, new File(scanDir));
				if (currentFile != null) {

					FileProcessor documents = new FileProcessor(genai);
					documents.setInstructions(instructions);
					documents.setModuleMultiThread(multiThread);
					documents.setDefaultGuidance(defaultGuidance);

					documents.scanDocuments(rootDir, new File(scanDir));
					LOGGER.info("Scan completed for directory: {}", scanDir);
				} else {
					LOGGER.error("The directory '{}' must be located within the root directory '{}'.", scanDir,
							rootDir);
				}
			}

		} catch (ParseException e) {
			System.err.println("Error parsing arguments: " + e.getMessage());
			help(options, formatter);
		}
	}

	private static String getInstractionsFromFile(String instructionsFile) {
		String instructions = null;
		if (instructionsFile != null) {
			File file = new File(execDir, instructionsFile);
			if (file.exists()) {
				try (FileReader reader = new FileReader(file)) {
					instructions = IOUtils.toString(reader);
				} catch (IOException e) {
					LOGGER.error("Failed to read file: {}", file);
				}
			}
		}
		return instructions;
	}

	private static void help(Options options, HelpFormatter formatter) {
		formatter.printHelp("java -jar gw.jar", options);
	}

}
