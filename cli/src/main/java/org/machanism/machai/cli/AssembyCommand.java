package org.machanism.machai.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
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

	private static final double DEFAULT_SCORE_VALUE = 0.90;
	private static final String DEFAULT_GENAI_VALUE = "OpenAI:gpt-5-mini";

	/** List of picked Bindex objects matching the search query. */
	private List<Bindex> bindexList;
	/** Last query (search string) used for picking. */
	private String findQuery;

	/** JLine line reader for shell interaction. */
	LineReader reader;

	private PropertiesConfigurator config;

	/**
	 * Default constructor.
	 */
	public AssembyCommand() {
		super();
		config = new PropertiesConfigurator();
		String configFileName = "machai.properties";
		try {
			config.setConfiguration(configFileName);
		} catch (Exception e) {
			logger.debug("Configuration filr: `{}` not found.", configFileName);
		}
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
			@ShellOption(value = { "-q",
					"--query" }, help = "The application assembly prompt or the name of a prompt file.") String query,
			@ShellOption(value = { "-r",
					"--registerUrl" }, defaultValue = ShellOption.NULL, help = "URL of the registration database for storing project metadata.") String registerUrl,
			@ShellOption(value = { "-s",
					"--score" }, help = "Minimum similarity threshold for search results. Only results with a score equal to or above this value will be returned.", defaultValue = ShellOption.NULL) Double score,
			@ShellOption(value = { "-g",
					"--genai" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", defaultValue = ShellOption.NULL) String chatModel)
			throws IOException {
		query = getQueryFromFile(query);

		findQuery = query;
		chatModel = Optional.ofNullable(chatModel).orElse(ConfigCommand.config.get("genai", DEFAULT_GENAI_VALUE));
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel, config);
		score = Optional.ofNullable(score).orElse(ConfigCommand.config.getDouble("score", 0.90));
		bindexList = pickBricks(provider, query, score, registerUrl, chatModel);
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
	public void assembly(@ShellOption(value = { "-q",
			"--query" }, defaultValue = ShellOption.NULL, help = "The prompt for application assembly. If omitted, the result from the previous 'find' command will be used, if available.") String query,
			@ShellOption(value = { "-d",
					"--dir" }, defaultValue = ShellOption.NULL, help = "Path to the directory where the assembled project will be created.") File dir,
			@ShellOption(value = { "-r",
					"--registerUrl" }, defaultValue = ShellOption.NULL, help = "URL of the register database for storing project metadata.") String registerUrl,
			@ShellOption(value = { "-s",
					"--score" }, help = "Minimum similarity threshold for search results.", defaultValue = ShellOption.NULL) Double score,
			@ShellOption(value = { "-g",
					"--genai" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", defaultValue = ShellOption.NULL) String chatModel)
			throws IOException {

		chatModel = Optional.ofNullable(chatModel).orElse(ConfigCommand.config.get("genai", DEFAULT_GENAI_VALUE));
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel, config);
		FunctionToolsLoader.getInstance().applyTools(provider);

		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
		logger.info("The project directory: {}", dir);
		provider.setWorkingDir(dir);

		String prompt = query;
		if (query == null) {
			if (bindexList == null) {
				throw new IllegalArgumentException("The query is empty.");
			}
			prompt = this.findQuery;
		} else {
			query = getQueryFromFile(query);
			score = Optional.ofNullable(score).orElse(ConfigCommand.config.getDouble("score", DEFAULT_SCORE_VALUE));
			bindexList = pickBricks(provider, query, score, registerUrl, chatModel);
		}

		if (!bindexList.isEmpty()) {

			ApplicationAssembly assembly = new ApplicationAssembly(provider);
			assembly.projectDir(dir);
			assembly.assembly(prompt, bindexList);
		}
	}

	/**
	 * Sends a user prompt to the GenAI provider for guidance.
	 * 
	 * @param query The prompt supplied by the user.
	 */
	@ShellMethod("Is used for request additional GenAI guidances.")
	public void prompt(@ShellOption(value = { "-q", "--query" }, help = "The user prompt to GenAI.") String query,
			@ShellOption(value = { "-g",
					"--genai" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).") String chatModel,
			@ShellOption(value = { "-d",
					"--dir" }, defaultValue = ShellOption.NULL, help = "Path to the working directory.") File dir) {

		chatModel = Optional.ofNullable(chatModel).orElse(ConfigCommand.config.get("genai", DEFAULT_GENAI_VALUE));
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel, config);

		FunctionToolsLoader.getInstance().applyTools(provider);
		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
		provider.setWorkingDir(dir);

		provider.prompt(query);
		String response = provider.perform();
		if (response != null) {
			logger.info(response);
		}
	}

	/**
	 * Picks bricks (libraries) using a Picker service and scores them.
	 * 
	 * @param provider
	 * @param query     The query string describing requirements
	 * @param score     Minimum similarity score
	 * @param url
	 * @param chatModel
	 * 
	 * @return List< Bindex> found matching libraries
	 * @throws IOException if picking fails
	 */
	private List<Bindex> pickBricks(GenAIProvider provider, String query, Double score, String url, String chatModel)
			throws IOException {
		List<Bindex> bindexList = null;
		try (Picker picker = new Picker(provider, url)) {
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
