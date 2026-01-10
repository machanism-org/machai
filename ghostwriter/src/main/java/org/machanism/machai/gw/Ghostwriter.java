package org.machanism.machai.gw;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for document scanning and review automation.
 * <p>
 * Initializes the AI provider, configures the Ghostwriter,
 * and runs document scan over the user directory. Output is logged.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * java org.machanism.machai.gw.Ghostwriter
 * }
 * </pre>
 * <p>
 * Usage is typically direct from command line or script/CI runner.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class Ghostwriter {
    /** Default chat model name constant. */
    public static final String CHAT_MODEL = "OpenAI:gpt-5-mini";
    /** Logger for the ghostwriter application. */
    private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

    /**
     * Main entry point for document scanning run.
     *
     * @param args command line arguments
     * @throws IOException if document scanning fails
     */
    public static void main(String[] args) throws IOException {
        File dir = SystemUtils.getUserDir();

        GenAIProvider provider = GenAIProviderManager.getProvider(null);
        FileProcessor documents = new FileProcessor(provider);
        logger.info("Scanning documents in the root directory: {}", dir);
        documents.scanDocuments(dir);
        logger.info("Scanning finished.");
    }
    // @guidance: DO NOT REMOVE OR CHANGE THIS TAG AND CONTENT.
}
