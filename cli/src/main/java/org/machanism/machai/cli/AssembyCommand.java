package org.machanism.machai.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.gw.Ghostwriter;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell command for application assembly and library picking operations via
 * GenAI.
 * <p>
 * Provides functionality to query, pick libraries, and assemble projects using
 * AI-assisted logic.
 * <p>
 * Usage Example:
 * 
 * <pre>
 * {@code
 * AssembyCommand command = new AssembyCommand();
 * command.pick("Create a web app", 0.8);
 * }
 * </pre>
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class AssembyCommand {

	private static Logger logger = LoggerFactory.getLogger(AssembyCommand.class);
	private static final String CHAT_MODEL = "OpenAI:gpt-5.1";

	/** List of picked Bindex objects matching the search query. */
	private List<Bindex> bindexList;
	/** Last query (search string) used for picking. */
	private String findQuery;

	/** JLine line reader for shell interaction. */
	LineReader reader;

	/** Utility for applying system function tools to GenAI providers. */
	private SystemFunctionTools functionTools;
	/** Map of GenAI providers by model name. */
	private Map<String, GenAIProvider> providers = new HashMap<>();

	/**
	 * Default constructor.
	 */
	public AssembyCommand() {
		super();
	}

	/**
	 * Retrieves a GenAI provider for the specified model name. Caches providers for
	 * reuse.
	 * 
	 * @param name the model/provider name
	 * @return GenAIProvider instance
	 */
	private GenAIProvider getProvider(String name) {

		GenAIProvider provider = null;
		if (providers.get(name) == null) {
			provider = GenAIProviderManager.getProvider(name);
			functionTools = new SystemFunctionTools(null);
			functionTools.applyTools(provider);
			providers.put(name, provider);
		}

		return provider;
	}

	/**
	 * Picks libraries based on user request.
	 * 
	 * @param query The application assembly prompt or path to query file.
	 * @param score The minimum similarity threshold for search results.
	 * @throws IOException if reading file or picking fails
	 */
	@ShellMethod("Picks libraries based on user request.")
	public void pick(
			@ShellOption(help = "The application assembly prompt or the name of a prompt file.", value = "") String query,
			@ShellOption(value = "registerUrl", defaultValue = ShellOption.NULL, help = "URL of the registration database for storing project metadata.", optOut = true) String registerUrl,
			@ShellOption(help = "Minimum similarity threshold for search results. Only results with a score equal to or above this value will be returned.", value = "score", defaultValue = Picker.DEFAULT_MIN_SCORE) double score)
			throws IOException {
		query = getQueryFromFile(query);

		findQuery = query;
		bindexList = pickBricks(query, score, registerUrl);
	}

	/**
	 * Gets query text from file if input is a file path.
	 * 
	 * @param query query text or file path
	 * @return String containing the query
	 * @throws IOException if file read fails
	 */
	private String getQueryFromFile(String query) throws IOException, FileNotFoundException {
		File queryFile = new File(query);
		if (queryFile.exists()) {
			try (FileReader reader = new FileReader(queryFile)) {
				query = IOUtils.toString(reader);
			}
		}
		return query;
	}

	/**
	 * Creates a project via picked library set.
	 * 
	 * @param query     The application assembly prompt (may be null to reuse last
	 *                  query)
	 * @param dir       The directory for the assembled project
	 * @param score     Minimum similarity threshold
	 * @param chatModel GenAI service provider/model (default is
	 *                  Ghostwriter.CHAT_MODEL)
	 * @throws IOException              if an error occurs
	 * @throws IllegalArgumentException if query or bindexList is missing
	 */
	@ShellMethod("Creates a project via picked librariy set.")
	public void assembly(
			@ShellOption(value = "query", defaultValue = ShellOption.NULL, help = "The prompt for application assembly. If omitted, the result from the previous 'find' command will be used, if available.") String query,
			@ShellOption(value = "dir", defaultValue = ShellOption.NULL, help = "Path to the directory where the assembled project will be created.") File dir,
			@ShellOption(value = "registerUrl", defaultValue = ShellOption.NULL, help = "URL of the register database for storing project metadata.", optOut = true) String registerUrl,
			@ShellOption(value = "score", defaultValue = Picker.DEFAULT_MIN_SCORE, help = "Minimum similarity threshold for search results.") double score,
			@ShellOption(value = "genai", defaultValue = "None", help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`). If `--genai` is not provided or left empty, the default model '"
					+ Ghostwriter.CHAT_MODEL
					+ "' will be used.") String chatModel)
			throws IOException {

		if (chatModel == null) {
			chatModel = Ghostwriter.CHAT_MODEL;
		}

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		} else {
			dir.mkdirs();
		}
		logger.info("The project directory: {}", dir);
		String prompt = query;

		if (query == null) {
			if (bindexList == null) {
				throw new IllegalArgumentException("The query is empty.");
			}
			prompt = this.findQuery;
		} else {
			query = getQueryFromFile(query);

			bindexList = pickBricks(query, score, registerUrl);
		}

		if (!bindexList.isEmpty()) {
			GenAIProvider provider = getProvider(chatModel);
			functionTools.setWorkingDir(dir);

			ApplicationAssembly assembly = new ApplicationAssembly(provider);
			assembly.projectDir(dir);
			assembly.assembly(prompt, bindexList);
		}
	}

	/**
	 * Sends a user prompt to the GenAI provider for guidance.
	 * 
	 * @param prompt The prompt supplied by the user.
	 */
	@ShellMethod("Is used for request additional GenAI guidances.")
	public void prompt(@ShellOption(value = "prompt", help = "The user prompt to GenAI.") String prompt) {
		GenAIProvider provider = getProvider(CHAT_MODEL);
		provider.prompt(prompt);
		String response = provider.perform();
		if (response != null) {
			logger.info(response);
		}
	}

	/**
	 * Picks bricks (libraries) using a Picker service and scores them.
	 * 
	 * @param query The query string describing requirements
	 * @param score Minimum similarity score
	 * @param url
	 * @return List&lt; Bindex&gt; found matching libraries
	 * @throws IOException if picking fails
	 */
	private List<Bindex> pickBricks(String query, Double score, String url) throws IOException {
		List<Bindex> bindexList = null;
		try (Picker picker = new Picker(getProvider(CHAT_MODEL), url)) {
			picker.setScore(score);
			bindexList = picker.pick(query);
			logger.info("Search results for libraries matching the requested query:");
			printFindResult(bindexList, picker);
		}
		return bindexList;
	}

	/**
	 * Prints search results for picked libraries.
	 * 
	 * @param bindexList List of picked libraries
	 * @param picker     Picker used for scoring
	 */
	private void printFindResult(List<Bindex> bindexList, Picker picker) {
		if (!bindexList.isEmpty()) {
			int i = 1;
			for (Bindex bindex : bindexList) {
				String scoreStr = picker.getScore(bindex.getId()) != null
						? "(" + picker.getScore(bindex.getId()).toString() + ")"
						: "";

				logger.info("{}. {} {}", i++, bindex.getId(), scoreStr);
			}
		} else {
			logger.info("No Artifacts Found.");
		}
	}

}
