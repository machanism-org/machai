package org.machanism.machai.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.Register;
import org.machanism.machai.core.embedding.EmbeddingProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class BindexCommand {

	@ShellMethod()
	public void bindex(
			@ShellOption(help = "The path to the project  directory.", value = "dir") File dir,
			@ShellOption(help = "The overwrite mode: all saved data will be updated.", value="overwrite") boolean overwrite,
			@ShellOption(help = "The debug mode: no request is sent to OpenAI to create an index.", value = "debug") boolean debug)
			throws IOException, XmlPullParserException {

		try (Register register = new Register(debug)) {
			register.setRewriteMode(overwrite);
			register.regProjects(dir);
		}
	}

	@ShellMethod()
	public void find(
			@ShellOption(value = "query") String query) throws IOException {

		try (EmbeddingProvider provider = new EmbeddingProvider("machanism", "bindex")) {
			List<String> bindexList = provider.search(query);
			FileWriter writer = new FileWriter(new File("inputs.txt"));
			IOUtils.write(StringUtils.join(bindexList, "\n\n").getBytes(), writer, "UTF8");
		}
	}

}
