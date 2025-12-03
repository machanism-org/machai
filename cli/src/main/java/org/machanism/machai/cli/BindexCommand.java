package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jline.reader.LineReader;
import org.machanism.machai.core.Register;
import org.machanism.machai.core.ai.GenAIProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.openai.models.ChatModel;

@ShellComponent
public class BindexCommand {

	private static final ChatModel CHAT_MODEL = ChatModel.GPT_5_1;

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

}
