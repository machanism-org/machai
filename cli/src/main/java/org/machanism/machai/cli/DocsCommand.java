package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.documents.DocsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class DocsCommand {

	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

	@Autowired
	@Lazy
	LineReader reader;

	@ShellMethod("GenAI document processing command.")
	public void docs(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "Generates only the inputs.txt file; no request is sent to OpenAI to create a bindex.", value = "inputs") boolean inputs)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		DocsProcessor documents = new DocsProcessor();
		logger.info("Scanning documents has started...");
		documents.scanProjects(dir);
		logger.info("Scanning finished.");
	}

}
