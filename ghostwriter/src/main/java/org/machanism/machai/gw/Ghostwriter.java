package org.machanism.machai.gw;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for document scanning and review automation.
 * <p>
 * Initializes the AI provider, configures the Ghostwriter,
 * and runs document scan over the user directory. Output is logged.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * {@code
 * java org.machanism.machai.gw.Ghostwriter
 * }
 * </pre>
 *
 * <p>
 * Usage is typically direct from command line or script/CI runner.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public final class Ghostwriter {

    /** Logger for the ghostwriter application. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Ghostwriter.class);

    private Ghostwriter() {
        // Utility class.
    }

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
        LOGGER.info("Scanning documents in the root directory: {}", dir);
        documents.scanDocuments(dir);
        LOGGER.info("Scanning finished.");
    }

}
