package org.machanism.machai.cli;

import java.io.IOException;
import java.util.List;

import org.jline.reader.LineReader;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.assembly.ApplicationAssembly;
import org.machanism.machai.core.embedding.EmbeddingProvider;
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
	private static final ChatModel CHAT_MODEL = ChatModel.GPT_5_1;

	private List<BIndex> bindexList;
	private String findQuery;

	LineReader reader;

	private GenAIProvider provider;

	public AssembyCommand() {
		super();
		provider = new GenAIProvider(CHAT_MODEL);
		provider.addDefaultTools();
	}

	@ShellMethod()
	public void find(
			@ShellOption(value = "The application assembly prompt.") String query,
			@ShellOption(help = "Max number of artifacts.", value = "limits", defaultValue = "10") int limits)
			throws IOException {

		findQuery = query;
		bindexList = getBricks(query, limits);
		printFindResult(bindexList, limits);
	}

	private void printFindResult(List<BIndex> bindexList, int limits) {
		logger.info("Search Context: Semantic search based on 'bindex' description embeddings.");

		if (!bindexList.isEmpty()) {
			logger.info("Matching Artifacts Found:");
			logger.info("---------------------------------------------------------------");

			int i = 1;
			for (BIndex bindex : bindexList) {
				logger.info(String.format("%2$3s. %1s", bindex.getId(), i++));
			}

			logger.info("---------------------------------------------------------------");
			logger.info("Number of Artifacts Found: " + bindexList.size() + ". Limits: " + limits);
		} else {
			logger.info("");
			logger.info("No Artifacts Found.");
			logger.info("---------------------------------------------------------------");
		}
	}

	@ShellMethod()
	public void assembly(
			@ShellOption(value = "query", defaultValue = ShellOption.NULL, help = "The application assembly prompt. If empty, an attempt will be made to use the result of the 'find' command, if one was specified previously.") String query,
			@ShellOption(help = "Max number of artifacts.", value = "limits", defaultValue = "10") int limits,
			@ShellOption(help = "The debug mode: no request is sent to OpenAI to create an index.", value = "debug") boolean debug)
			throws IOException {

		provider.setDebugMode(debug);
		String prompt = query;

		if (query == null) {
			if (bindexList == null) {
				throw new IllegalArgumentException("The query is empty.");
			}
			prompt = this.findQuery;
		} else {
			bindexList = getBricks(query, limits);

			for (BIndex bindex : bindexList) {
				logger.info("ArtifactId: " + bindex.getId());
			}
		}

		ApplicationAssembly assembly = new ApplicationAssembly(provider);
		assembly.assembly(prompt, bindexList);
	}

	@ShellMethod()
	public void prompt(
			@ShellOption(value = "prompt", help = "The user prompt to GenAI.") String prompt) {
		provider.prompt(prompt);
		String response = provider.perform();
		if (response != null) {
			logger.info(response);
		}
	}

	private List<BIndex> getBricks(String query, int limits) throws IOException {
		List<BIndex> bindexList = null;
		try (EmbeddingProvider provider = new EmbeddingProvider("machanism", "bindex")) {
			bindexList = provider.search(query, limits);
		}
		return bindexList;
	}

}
