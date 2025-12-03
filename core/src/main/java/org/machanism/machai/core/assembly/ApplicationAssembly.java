package org.machanism.machai.core.assembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class ApplicationAssembly {
	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

	private final String SYSTEM_INSTRUCTIONS = "You are smart software engineer and developer. "
			+ "You are expert in all popular programming languages, frameworks, platforms.\r\n"
			+ "You must implement user tasks.\r\n"
			+ "\r\n"
			+ "You MUST follow the following steps:\r\n"
			+ "1. Analyse user request to implement task.\r\n"
			+ "2. Search relevant context using repo tree and search tools. Plan your actions to implement task.\r\n"
			+ "3. Write all relevant changes in file system using file system tools (before making changes you must read content from file and generate new content).\r\n"
			+ "\r\n"
			+ "Constraints:\r\n"
			+ "1. You must implement comprehensive, correct code.\r\n"
			+ "2. You must strictly follow the plan to implement correct code.\r\n"
			+ "\r\n"
			+ "Important:\r\n"
			+ "1. You have ability to work with local file system and command line.\r\n"
			+ "2. If possible, you should use the recommended and described by bindex artifacts.";

	private GenAIProvider provider;

	public ApplicationAssembly(GenAIProvider provider) {
		super();
		this.provider = provider;
	}

	public void assembly(String prompt, List<BIndex> bindexList) {
		provider.addDefaultTools();

		provider.prompt(SYSTEM_INSTRUCTIONS);

		try {
			URL systemResource = getClass().getResource("/schema/bindex-schema-v1.json");
			String schema = IOUtils.toString(systemResource, "UTF8");
			provider.prompt("The bindex schema https://machanism.org/machai/schema/bindex-schema-v1.json:\n" + schema);

			for (BIndex bindex : bindexList) {
				String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(bindex);
				provider.prompt("Recommended to use artifact: " + bindex.getId() + "\n"
						+ "Bindex json for this artifact:\n"
						+ "```json\n" + bindexStr + "\n```\n\n");
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

}
