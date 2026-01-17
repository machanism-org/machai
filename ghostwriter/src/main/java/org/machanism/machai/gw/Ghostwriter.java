package org.machanism.machai.gw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.Config;
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
		Option helpOption = new Option("h", "help", false, "Displays help information for usage.");
		Option multiThreadOption = new Option("t", "threads", false, "Enable multi-threaded processing.");
		Option rootDirOpt = new Option("d", "dir", true, "The path fo the project directory.");
		Option genaiOpt = new Option("g", "genai", true,
				"Specifies the GenAI service provider and model (e.g. `OpenAI:gpt-5.1`).");
		Option instructionsOpt = new Option("i", "instructions", true,
				"Specifies additional file processing instructions. "
						+ "Provide either the instruction text directly or the path to a file containing the instructions.");

		options.addOption(helpOption);
		options.addOption(rootDirOpt);
		options.addOption(multiThreadOption);
		options.addOption(genaiOpt);
		options.addOption(instructionsOpt);

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
			genai = Config.getChatModel(genai);

			rootDir = Config.getWorkingDir(rootDir);

			String instructions = null;
			if (cmd.hasOption(instructionsOpt)) {
				instructions = cmd.getOptionValue(instructionsOpt);
				String instractionFromFile = getInstractionsFromFile(instructions);
				if (instractionFromFile != null) {
					instructions = instractionFromFile;
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
					dirs = new String[] { rootDir.getAbsolutePath() };
				}
			}

			LOGGER.info("Root directory: {}", rootDir);
			boolean multiThread = cmd.hasOption(multiThreadOption);

			for (String scanDir : dirs) {
				LOGGER.info("Scanning documents: {}", scanDir);

				FileProcessor documents = new FileProcessor(genai);
				documents.setInstructions(instructions);
				documents.setModuleMultiThread(multiThread);

				documents.scanDocuments(rootDir, new File(scanDir));
				LOGGER.info("Scanning finished.");
			}

		} catch (ParseException e) {
			System.err.println("Error parsing arguments: " + e.getMessage());
			help(options, formatter);
		}
	}

	private static String getInstractionsFromFile(String instructionsFile) {
		File file = new File(instructionsFile);
		String instructions = null;
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				instructions = IOUtils.toString(reader);
			} catch (IOException e) {
				LOGGER.info("Failed to read instructions from file: {}", file);
			}
		}
		return instructions;
	}

	private static void help(Options options, HelpFormatter formatter) {
		formatter.printHelp("java -jar gw.jar", options);
	}


}
