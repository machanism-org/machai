package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell command for GenAI document processing operations.
 * <p>
 * Provides CLI functionality to scan and process documents using the selected
 * GenAI provider and model. Usage Example:
 * 
 * <pre>
 * {@code
 * ProcessCommand docsCmd = new ProcessCommand();
 * docsCmd.docs(new File("/projects/"), "OpenAI:gpt-5.1");
 * }
 * </pre>
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class GWCommand {

	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);
	private static final String DEFAULT_GENAI_VALUE = "OpenAI:gpt-5-mini";

	private PropertiesConfigurator config;

	/**
	 * Scans and processes documents in the given project directory using the
	 * specified GenAI chat model.
	 * 
	 * @param scan      The root directory to scan
	 * @param chatModel GenAI service provider/model
	 * @throws IOException if scan or processing fails
	 */
	@ShellMethod("Processing files using the Ghostwriter command.")
	public void gw(
			@ShellOption(help = "The path fo the project directory.", value = { "-d",
					"--dir" }, defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The path to the directory to be processed.", value = { "-s",
					"--scan" }, defaultValue = ShellOption.NULL) File scan,
			@ShellOption(help = "Enable multi-threaded processing.", value = { "-t",
					"--threads" }, defaultValue = "false") boolean threads,
			@ShellOption(help = "Specifies additional file processing instructions. "
					+ "Provide either the instruction text directly or the path to a file containing the instructions.", value = {
							"-i", "--instructions" }, defaultValue = ShellOption.NULL, arity = 1) String instructions,
			@ShellOption(help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", value = {
					"-g", "--genai" }, defaultValue = ShellOption.NULL, arity = 1) String chatModel)
			throws IOException {

		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
		scan = Optional.ofNullable(scan).orElse(dir);

		String relativePath = ProjectLayout.getRelativePath(dir, scan);
		if (relativePath == null) {
			logger.warn(
					"The starting document scan directory: `{}` is not located within the root directory or the current directory path: `{}`",
					scan, dir);
			dir = scan;
		} else {
			logger.info("Root project directory: {}", dir);
			logger.info("Starting document scan in directory: {}", scan);
		}

		chatModel = Optional.ofNullable(chatModel).orElse(ConfigCommand.config.get("genai", DEFAULT_GENAI_VALUE));
		FileProcessor documents = new FileProcessor(chatModel, config);

		if (instructions != null) {
			File instructionsFile = new File(instructions);
			if (instructionsFile.exists()) {
				try {
					instructions = Files.readString(instructionsFile.toPath());
				} catch (IOException e) {
					logger.info("Failed to read instructions from file: {}", instructions);
				}
			}
		}
		documents.setInstructions(instructions);
		documents.setModuleMultiThread(threads);
		documents.scanDocuments(dir, scan.getAbsolutePath());
		logger.info("Scanning finished.");
	}

}
