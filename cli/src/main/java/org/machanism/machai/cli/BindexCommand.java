package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jline.reader.LineReader;
import org.machanism.machai.core.Register;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.assembly.ApplicationAssembly;
import org.machanism.machai.core.embedding.EmbeddingProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.openai.models.ChatModel;

@ShellComponent
public class BindexCommand {

	private static Logger logger = LoggerFactory.getLogger(BindexCommand.class);
	private static final ChatModel CHAT_MODEL = ChatModel.GPT_5_1;

	private List<BIndex> bindexList;
	private String findQuery;

	@Autowired
	@Lazy
	LineReader reader;

	@ShellMethod()
	public void bindex(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The overwrite mode: all saved data will be updated.", value = "overwrite") boolean overwrite,
			@ShellOption(help = "The debug mode: no request is sent to OpenAI to create an index.", value = "debug") boolean debug)
			throws IOException, XmlPullParserException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		GenAIProvider provider = new GenAIProvider(CHAT_MODEL);
		provider.setDebugMode(debug);

		try (Register register = new Register(provider)) {
			register.setRewriteMode(overwrite);
			register.regProjects(dir);
		}
	}

	@ShellMethod()
	public void find(
			@ShellOption(value = "The application assembly prompt.") String query,
			@ShellOption(help = "Max number of artifacts.", value = "limits", defaultValue = "10") int limits)
			throws IOException {

		findQuery = query;
		bindexList = getBricks(query, limits);
		printFindResult(bindexList);
	}

	private void printFindResult(List<BIndex> bindexList) {
		logger.info("Search Context: Semantic search based on 'bindex' description embeddings.");

		if (!bindexList.isEmpty()) {
			logger.info("Matching Artifacts Found:");
			logger.info("---------------------------------------------------------------");

			int i = 1;
			for (BIndex bindex : bindexList) {
				logger.info(String.format("%2$3s. %1s", bindex.getId(), i++));
			}

			logger.info("---------------------------------------------------------------");
			logger.info("Number of Artifacts Found: " + bindexList.size());
		} else {
			logger.info("");
			logger.info("No Artifacts Found:");
			logger.info("---------------------------------------------------------------");
		}
	}

	@ShellMethod()
	public void assembly(
			@ShellOption(value = "query", defaultValue = ShellOption.NULL, help = "The application assembly prompt. If empty, an attempt will be made to use the result of the 'find' command, if one was specified previously.") String query,
			@ShellOption(help = "Max number of artifacts.", value = "limits", defaultValue = "10") int limits,
			@ShellOption(help = "The debug mode: no request is sent to OpenAI to create an index.", value = "debug") boolean debug)
			throws IOException {

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

		ApplicationAssembly assembly = new ApplicationAssembly();

		GenAIProvider provider = new GenAIProvider(CHAT_MODEL);
		provider.setDebugMode(debug);
		assembly.provider(provider);

		assembly.assembly(prompt, bindexList);
	}

	private List<BIndex> getBricks(String query, int limits) throws IOException {
		List<BIndex> bindexList = null;
		try (EmbeddingProvider provider = new EmbeddingProvider("machanism", "bindex")) {
			bindexList = provider.search(query, limits);
		}
		return bindexList;
	}

}
