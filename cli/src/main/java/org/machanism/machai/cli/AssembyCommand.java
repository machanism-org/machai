package org.machanism.machai.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.ai.GenAIProviderManager;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.openai.models.ChatModel;

@ShellComponent
public class AssembyCommand {

	private static Logger logger = LoggerFactory.getLogger(AssembyCommand.class);
	private static final String CHAT_MODEL = ChatModel.GPT_5_1.toString();

	private List<BIndex> bindexList;
	private String findQuery;

	LineReader reader;

	private GenAIProvider provider;

	public AssembyCommand() {
		super();
		provider = GenAIProviderManager.getProvider(CHAT_MODEL);
		provider.addDefaultTools();
	}

	@ShellMethod()
	public void pick(
			@ShellOption(value = "The application assembly prompt.") String query,
			@ShellOption(help = "The minimum similarity threshold for search results.", value = "score", defaultValue = Picker.DEFAULT_MIN_SCORE) double score)
			throws IOException {

		query = getQueryFromFile(query);

		findQuery = query;
		bindexList = pickBricks(query, score);
	}

	private String getQueryFromFile(String query) throws IOException, FileNotFoundException {
		File queryFile = new File(query);
		if (queryFile.exists()) {
			try (FileReader reader = new FileReader(queryFile)) {
				query = IOUtils.toString(reader);
			}
		}
		return query;
	}

	@ShellMethod()
	public void assembly(
			@ShellOption(value = "query", defaultValue = ShellOption.NULL, help = "The application assembly prompt. If empty, an attempt will be made to use the result of the 'find' command, if one was specified previously.") String query,
			@ShellOption(help = "The path to the assembled project directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The minimum similarity threshold for search results.", value = "score", defaultValue = Picker.DEFAULT_MIN_SCORE) double score,
			@ShellOption(help = "The debug mode: no request is sent to OpenAI to create the project.", value = "inputs") boolean inputs)
			throws IOException {

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
			bindexList = pickBricks(query, score);
		}

		ApplicationAssembly assembly = new ApplicationAssembly(provider);
		assembly.projectDir(dir);
		assembly.assembly(prompt, bindexList, !inputs);
	}

	@ShellMethod()
	public void prompt(
			@ShellOption(value = "prompt", help = "The user prompt to GenAI.") String prompt) {
		provider.prompt(prompt);
		String response = provider.perform(true);
		if (response != null) {
			logger.info(response);
		}
	}

	private List<BIndex> pickBricks(String query, Double score) throws IOException {
		List<BIndex> bindexList = null;
		try (Picker picker = new Picker(provider)) {
			picker.setScore(score);
			bindexList = picker.pick(query);
			logger.info("Search results for libraries matching the requested query:");
			printFindResult(bindexList, picker);
		}
		return bindexList;
	}

	private void printFindResult(List<BIndex> bindexList, Picker picker) {
		if (!bindexList.isEmpty()) {
			int i = 1;
			for (BIndex bindex : bindexList) {
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
