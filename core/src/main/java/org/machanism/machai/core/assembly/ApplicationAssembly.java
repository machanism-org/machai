package org.machanism.machai.core.assembly;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ApplicationAssembly {
	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	private GenAIProvider provider;
	private File projectDir = SystemUtils.getUserDir();

	public ApplicationAssembly(GenAIProvider provider) {
		super();
		this.provider = provider;
	}

	public void assembly(final String prompt, List<BIndex> bindexList) {
		String systemPrompt = promptBundle.getString("system_instructions");
		provider.prompt(systemPrompt);
		provider.workingDir(projectDir);

		try {
			BIndexBuilder.bindexSchemaPrompt(provider);

			for (BIndex bindex : bindexList) {
				String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(bindex);
				String bindexPrompt = MessageFormat.format(promptBundle.getString("recommended_library_section"),
						bindex.getId(), bindexStr);
				provider.prompt(bindexPrompt);
			}

			provider.prompt(prompt);

			provider.saveInput(new File("inputs.txt"));

			String response = provider.perform();
			if (response != null) {
				logger.info(response);
			}

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public ApplicationAssembly projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

}
