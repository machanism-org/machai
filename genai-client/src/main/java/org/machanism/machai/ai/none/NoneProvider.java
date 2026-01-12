package org.machanism.machai.ai.none;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.openAI.OpenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code NoneProvider} class is an implementation of the {@link GenAIProvider} interface used to disable generative AI integrations
 * and log input requests locally when an external AI provider is not required or available.
 * <p>
 * <b>Purpose:</b>
 * Provides a stub implementation that stores requests in input files (in the {@code inputsLog} folder).
 * All GenAI operations are non-operative, or throw exceptions where necessary, making this useful for scenarios
 * where generative AI features must be disabled, simulated, or for fallback testing.
 * No calls are made to any external AI services or large language models (LLMs).
 * <p>
 * <b>Typical Use Cases:</b>
 * <ul>
 *   <li>Disabling generative AI features for security or compliance</li>
 *   <li>Implementing fallback logic when no provider is configured</li>
 *   <li>Logging requests for manual review or later processing</li>
 *   <li>Testing environments not connected to external services</li>
 * </ul>
 * <p>
 * <b>Example Usage:</b>
 * <pre>
 * {@code
 *   GenAIProvider provider = new NoneProvider();
 *   provider.prompt("Describe the weather.");
 *   provider.perform(); // No AI service is called; input may be logged locally
 * }
 * </pre>
 * <p>
 * <b>Notes:</b>
 * <ul>
 *   <li>Operations requiring GenAI services will throw exceptions when called.</li>
 *   <li>All prompts and instructions are cleared after performing.</li>
 *   <li>Refer to {@link GenAIProvider} interface for compatible methods.</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 */
public class NoneProvider implements GenAIProvider {
    /**
     * The name identifying this provider.
     */
    public static final String NAME = "None";

    private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

    /**
     * Accumulates AI prompts as a single string.
     */
    private StringBuilder prompts = new StringBuilder();
    
    /**
     * Instruction text (optional).
     */
    private String instructions;
    
    /**
     * File for storing input logs.
     */
    private File inputsLog;

    /**
     * Appends the given text to the prompt buffer.
     *
     * @param text the prompt text to add
     */
    @Override
    public void prompt(String text) {
        prompts.append(text);
        prompts.append("\r\n\r\n");
    }

    /**
     * Placeholder for adding a prompt from a file. No operation in this implementation.
     *
     * @param file the file containing prompt text
     * @param bundleMessageName unused, present for interface compatibility
     * @throws IOException never thrown in this implementation
     */
    @Override
    public void promptFile(File file, String bundleMessageName) throws IOException {
        // No-op in NoneProvider
    }

    /**
     * No operation for adding a file input in this implementation.
     *
     * @param file the file to add
     * @throws IOException never thrown in this implementation
     * @throws FileNotFoundException never thrown in this implementation
     */
    @Override
    public void addFile(File file) throws IOException, FileNotFoundException {
        // No-op in NoneProvider
    }

    /**
     * No operation for adding a file via URL in this implementation.
     *
     * @param fileUrl the URL to add
     * @throws IOException never thrown in this implementation
     * @throws FileNotFoundException never thrown in this implementation
     */
    @Override
    public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
        // No-op in NoneProvider
    }

    /**
     * Not supported for NoneProvider.
     *
     * @param text Input for embedding
     * @return never returns normally
     * @throws IllegalArgumentException always thrown for this provider
     */
    @Override
    public List<Float> embedding(String text) {
        throw new IllegalArgumentException("NoneProvider doesn't support embedding generation.");
    }

    /**
     * Clears all accumulated prompts.
     */
    @Override
    public void clear() {
        prompts = new StringBuilder();
    }

    /**
     * No-op for adding tools in this stub provider.
     *
     * @param name tool name
     * @param description tool description
     * @param function tool function implementation
     * @param paramsDesc optional parameter descriptions
     */
    @Override
    public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
        // No-op in NoneProvider
    }

    /**
     * Stores the instructions to be used by the provider (if any).
     *
     * @param instructions the instruction text
     */
    @Override
    public void instructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Writes the prompts and instructions (if present) to files if inputsLog is set.
     * Always returns {@code null}.
     *
     * @return {@code null} (No actual AI operation performed)
     */
    @Override
    public String perform() {
        if (inputsLog != null) {
            File parentFile = inputsLog.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
            } else {
                parentFile = SystemUtils.getUserDir();
            }

            if (instructions != null) {
                File file = new File(parentFile, "instructions.txt");
                try (Writer streamWriter = new FileWriter(file, false)) {
                    streamWriter.write(instructions);
                    logger.debug("LLM Instruction: {}", file);
                } catch (IOException e) {
                    logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
                }
            }

            try (Writer streamWriter = new FileWriter(inputsLog, false)) {
                streamWriter.write(prompts.toString());
                logger.info("LLM Inputs: {}", inputsLog);
            } catch (IOException e) {
                logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
            }
        }
        clear();
        return null;
    }

    /**
     * Sets the file to log inputs.
     *
     * @param inputsLog the file used for logging
     */
    @Override
    public void inputsLog(File inputsLog) {
        this.inputsLog = inputsLog;
    }

    /**
     * No operation for choosing a model in NoneProvider.
     *
     * @param chatModelName the model name (ignored)
     */
    @Override
    public void model(String chatModelName) {
        // No-op in NoneProvider
    }

    /**
     * Sets the working directory. No operation for NoneProvider.
     * @param workingDir working directory
     */
    @Override
    public void setWorkingDir(File workingDir) {
        // No-op in NoneProvider
    }

    /**
     * Gets all accumulated prompts as a string.
     * @return the prompt text
     */
    public String getPrompts() {
        return prompts.toString();
    }

}
