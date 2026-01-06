package org.machanism.machai.ghostwriter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ghostwriter {
	public static final String CHAT_MODEL = "OpenAI:gpt-5-mini";
	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

	public static void main(String[] args) throws IOException {
		File dir = SystemUtils.getUserDir();

		GenAIProvider provider = GenAIProviderManager.getProvider(null);
		DocsProcessor documents = new DocsProcessor(provider);
		logger.info("Scanning documents in the root directory: {}", dir);
		documents.scanDocuments(dir);
		logger.info("Scanning finished.");
	}

}
