package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.gw.DocsProcessor;
import org.machanism.machai.gw.Ghostwriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell command for GenAI document processing operations.
 * <p>
 * Provides CLI functionality to scan and process documents using the selected GenAI provider and model.
 * Usage Example:
 * <pre>
 * {@code
 * DocsCommand docsCmd = new DocsCommand();
 * docsCmd.docs(new File("/projects/"), "OpenAI:gpt-5.1");
 * }
 * </pre>
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class DocsCommand {

	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

	/** JLine line reader for shell interaction. */
	@Autowired
	@Lazy
	LineReader reader;

	/**
	 * Scans and processes documents in the given project directory using the specified GenAI chat model.
	 * @param dir The root directory to scan
	 * @param chatModel GenAI service provider/model (default is Ghostwriter.CHAT_MODEL)
	 * @throws IOException if scan or processing fails
	 */
	@ShellMethod("GenAI document processing command.")
	public void docs(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`). If `--genai` is empty, the default model '"
					+ Ghostwriter.CHAT_MODEL
					+ "' will be used.", value = "genai", defaultValue = "None") String chatModel)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		if (chatModel == null) {
			chatModel = Ghostwriter.CHAT_MODEL;
		}

		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);
		DocsProcessor documents = new DocsProcessor(provider);
		logger.info("Scanning documents in the root directory: {}", dir);
		documents.scanDocuments(dir);
		logger.info("Scanning finished.");
	}

}
