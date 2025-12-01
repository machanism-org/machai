package org.machanism.machai.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.Register;
import org.machanism.machai.core.embedding.EmbeddingProvider;
import org.machanism.machai.schema.BIndex;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.databind.ObjectMapper;

@ShellComponent
public class BindexCommand {

	private List<BIndex> bindexList;
	private String findQuery;

	@ShellMethod()
	public void bindex(
			@ShellOption(help = "The path to the project  directory.", value = "dir") File dir,
			@ShellOption(help = "The overwrite mode: all saved data will be updated.", value = "overwrite") boolean overwrite,
			@ShellOption(help = "The debug mode: no request is sent to OpenAI to create an index.", value = "debug") boolean debug)
			throws IOException, XmlPullParserException {

		try (Register register = new Register(debug)) {
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
		for (BIndex bindex : bindexList) {
			System.out.println("ArtifactId: " + bindex.getId());
		}
	}

	@ShellMethod()
	public void assembly(
			@ShellOption(value = "query", defaultValue = "", help = "The application assembly prompt. If empty, an attempt will be made to use the result of the 'find' command, if one was specified previously.") String query,
			@ShellOption(help = "Max number of artifacts.", value = "limits", defaultValue = "10") int limits)
			throws IOException {

		String prompt = query;

		if (StringUtils.isBlank(query)) {
			if (bindexList == null) {
				throw new IllegalArgumentException("The query is empty.");
			}
			prompt = this.findQuery;
		} else {
			bindexList = getBricks(query, limits);
		}

		URL systemResource = getClass().getResource("/schema/bindex-schema-v1.json");
		String schema = IOUtils.toString(systemResource, "UTF8");

		StringBuilder assemblPromptBuilder = new StringBuilder(
				"The bindex schema https://machanism.org/machai/schema/bindex-schema-v1.json:\n" + schema + "\n\n");
		for (BIndex bindex : bindexList) {
			System.out.println("ArtifactId: " + bindex.getId());
			String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(bindex);
			assemblPromptBuilder.append("```json\n" + bindexStr + "\n```\n\n");
		}

		assemblPromptBuilder.append(prompt);

		try (Writer writer = new FileWriter(new File("assemble.txt"))) {
			IOUtils.write(assemblPromptBuilder.toString().getBytes(), writer, "UTF8");
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
