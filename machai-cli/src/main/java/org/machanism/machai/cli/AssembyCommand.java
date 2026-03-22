package org.machanism.machai.cli;

import java.io.File;
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
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that performs semantic search (“pick”) and assembles a
 * project based on the selected libraries.
 *
 * <p>
 * This command is a CLI facade over the bindex module: {@link Picker} is used
 * to retrieve candidate libraries ({@link Bindex}) for a query, and
 * {@link ApplicationAssembly} is used to generate/assemble an output project
 * from the selected set.
 *
 * <h2>Example</h2>
 * 
 * <pre>
 * pick --query "Create a web app" --score 0.8
 * assembly --dir .\\out
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class AssembyCommand {

	private static final Logger logger = LoggerFactory.getLogger(AssembyCommand.class);

	/** List of picked Bindex objects matching the last search query. */
	private List<Bindex> bindexList;
	/** Last query (search string) used for picking. */
	private String findQuery;

	/** JLine line reader for shell interaction (optional). */
	LineReader reader;

	private final PropertiesConfigurator config;

	private final LineReader lineReader;

	/**
	 * Creates a new command instance.
	 *
	 * @param lineReader JLine reader used to prompt the user in interactive mode
	 */
	public AssembyCommand(@Lazy LineReader lineReader) {
		super();
		this.lineReader = lineReader;

		config = new PropertiesConfigurator();
		String configFileName = "machai.properties";
		try {
			config.setConfiguration(configFileName);
		} catch (Exception e) {
			logger.debug("Configuration filr: `{}` not found.", configFileName);
		}
	}

	/**
	 * Picks libraries based on the user request.
	 *
	 * <p>
	 * The provided {@code query} may be a literal prompt or a path to a text file
	 * containing the prompt.
	 *
	 * @param query       the application assembly prompt or the name of a prompt
	 *                    file
	 * @param registerUrl optional URL of a registry service used by the picker
	 * @param score       minimum similarity threshold for search results; if
	 *                    {@code null}, uses the configured default
	 * @param model       optional GenAI provider/model identifier (for example,
	 *                    {@code OpenAI:gpt-5.1}); if {@code null}, uses the
	 *                    configured default
	 * @throws IOException if reading the query file or calling the picker fails
	 */
	@ShellMethod("Picks libraries based on user request.")
	public void pick(
			@ShellOption(value = { "-q",
					"--query" }, help = "The application assembly prompt or the name of a prompt file.", defaultValue = ShellOption.NULL) String query,
			@ShellOption(value = { "-r",
					"--registerUrl" }, defaultValue = ShellOption.NULL, help = "URL of the registration database for storing project metadata.") String registerUrl,
			@ShellOption(value = { "-s",
					"--score" }, help = "Minimum similarity threshold for search results. Only results with a score equal to or above this value will be returned.", defaultValue = ShellOption.NULL) Double score,
			@ShellOption(value = { "-g",
					ApplicationAssembly.MODEL_PROP_NAME }, help = "Specifies the GenAI service provider and model (e.g., `"
							+ ApplicationAssembly.DEFAULT_MODEL + "`).", defaultValue = ShellOption.NULL) String model)
			throws IOException {

		try {
			if (query == null) {
				query = lineReader.readLine("Prompt: ");
			}

			query = getQueryFromFile(query);

			findQuery = query;
			model = Optional.ofNullable(model)
					.orElse(ConfigCommand.config.get(ApplicationAssembly.MODEL_PROP_NAME,
							ApplicationAssembly.DEFAULT_MODEL));
			score = Optional.ofNullable(score)
					.orElse(ConfigCommand.config.getDouble("score", ApplicationAssembly.DEFAULT_SCORE_VALUE));
			try (Picker picker = new Picker(model, registerUrl, config)) {
				picker.setScore(score);
				bindexList = picker.pick(query);
				printFindResult(bindexList, picker);
			}

		} finally {
			GenAIProviderManager.logUsage();
		}
	}

	/**
	 * Gets query text from file if input is a file path.
	 *
	 * @param query query text or file path
	 * @return the resolved query text
	 * @throws IOException if the file cannot be read
	 */
	private String getQueryFromFile(String query) throws IOException {
		File queryFile = new File(query);
		if (queryFile.exists()) {
			try (FileReader fileReader = new FileReader(queryFile)) {
				query = IOUtils.toString(fileReader);
			}
		}
		return query;
	}

	/**
	 * Creates a project from the picked library set.
	 *
	 * <p>
	 * If {@code query} is omitted, the last query from
	 * {@link #pick(String, String, Double, String)} is reused.
	 *
	 * @param query       the application assembly prompt (or prompt file); may be
	 *                    {@code null}
	 * @param dir         the directory where the assembled project will be created;
	 *                    if {@code null}, uses the configured default or the
	 *                    current working directory
	 * @param registerUrl optional URL of a registry service used by the picker
	 * @param score       minimum similarity threshold; if {@code null}, uses the
	 *                    configured default
	 * @param model       optional GenAI provider/model identifier (for example,
	 *                    {@code OpenAI:gpt-5.1}); if {@code null}, uses the
	 *                    configured default
	 * @throws IOException              if picking or assembly fails
	 * @throws IllegalArgumentException if the query is missing and no previous pick
	 *                                  results exist
	 */
	@ShellMethod("Creates a project via picked librariy set.")
	public void assembly(@ShellOption(value = { "-q",
			"--query" }, defaultValue = ShellOption.NULL, help = "The prompt for application assembly. If omitted, the result from the previous 'find' command will be used, if available.") String query,
			@ShellOption(value = { "-d",
					ProjectLayout.PROJECT_DIR_PROP_NAME }, defaultValue = ShellOption.NULL, help = "Path to the directory where the assembled project will be created.") File dir,
			@ShellOption(value = { "-r",
					"--registerUrl" }, defaultValue = ShellOption.NULL, help = "URL of the register database for storing project metadata.") String registerUrl,
			@ShellOption(value = { "-s",
					"--score" }, help = "Minimum similarity threshold for search results.", defaultValue = ShellOption.NULL) Double score,
			@ShellOption(value = { "-m",
					ApplicationAssembly.MODEL_PROP_NAME }, help = "Specifies the GenAI service provider and model (e.g., `"
							+ ApplicationAssembly.DEFAULT_MODEL + "`).", defaultValue = ShellOption.NULL) String model)
			throws IOException {

		try {
			model = Optional.ofNullable(model)
					.orElse(ConfigCommand.config.get(ApplicationAssembly.MODEL_PROP_NAME,
							ApplicationAssembly.DEFAULT_MODEL));

			dir = Optional.ofNullable(dir).orElse(
					ConfigCommand.config.getFile(ProjectLayout.PROJECT_DIR_PROP_NAME, SystemUtils.getUserDir()));
			logger.info("The project directory: {}", dir);
			logger.info("GenAI model: {}", model);

			if (query == null) {
				query = lineReader.readLine("Project assembly prompt: ");
			}

			if (query == null) {
				if (bindexList == null) {
					throw new IllegalArgumentException("The query is empty.");
				}
				query = this.findQuery;
				logger.info("Project assembly prompt: {}", query);

			} else {
				query = getQueryFromFile(query);
				score = Optional.ofNullable(score)
						.orElse(ConfigCommand.config.getDouble("score", ApplicationAssembly.DEFAULT_SCORE_VALUE));
				try (Picker picker = new Picker(model, registerUrl, config)) {
					picker.setScore(score);
					bindexList = picker.pick(query);
					this.findQuery = query;
				}
			}

			if (!bindexList.isEmpty()) {
				ApplicationAssembly assembly = new ApplicationAssembly(model, config, dir);
				assembly.projectDir(dir);
				assembly.assembly(query, bindexList);
			} else {
				logger.error(
						"No libraries related to the user prompt were found. Please refine your query or ensure the relevant libraries are available.");
			}
		} finally {
			GenAIProviderManager.logUsage();
		}

	}

	/**
	 * Sends a user prompt to the GenAI provider.
	 *
	 * @param query     the prompt supplied by the user
	 * @param chatModel GenAI provider/model identifier (for example,
	 *                  {@code OpenAI:gpt-5.1}); if {@code null}, uses the
	 *                  configured default
	 * @param dir       working directory used by the provider; if {@code null},
	 *                  uses the configured default or the current working directory
	 */
	@ShellMethod("Is used for request additional GenAI guidances.")
	public void prompt(@ShellOption(value = { "-q", "--query" }, help = "The user prompt to GenAI.") String query,
			@ShellOption(value = { "-m",
					"--model" }, help = "Specifies the GenAI service provider and model (e.g., `"
							+ ApplicationAssembly.DEFAULT_MODEL + "`).") String chatModel,
			@ShellOption(value = { "-d",
					"--dir" }, defaultValue = ShellOption.NULL, help = "Path to the working directory.") File dir) {

		try {
			chatModel = Optional.ofNullable(chatModel)
					.orElse(ConfigCommand.config.get(ApplicationAssembly.MODEL_PROP_NAME,
							ApplicationAssembly.DEFAULT_MODEL));
			GenAIProvider provider = GenAIProviderManager.getProvider(chatModel, config);

			FunctionToolsLoader.getInstance().applyTools(provider);
			dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
			provider.setWorkingDir(dir);

			provider.prompt(query);
			String response = provider.perform();
			if (response != null) {
				logger.info(">>> {}", response);
			}
		} finally {
			GenAIProviderManager.logUsage();
		}

	}

	/**
	 * Prints search results for picked libraries.
	 *
	 * @param bindexList list of picked libraries
	 * @param picker     picker used for scoring
	 */
	private void printFindResult(List<Bindex> bindexList, Picker picker) {
		logger.debug("Search results for libraries matching the requested query:");
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
