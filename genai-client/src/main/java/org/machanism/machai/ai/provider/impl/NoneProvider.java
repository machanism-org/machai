package org.machanism.machai.ai.provider.impl;

import java.io.File;

import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of {@link Genai} that performs no AI processing.
 *
 * <p>
 * Every method is a deliberate no-op: prompts, instructions, tools,
 * prompts/resources registration, project directory, error handling, and
 * enabled-tool configuration are all accepted and silently ignored, and
 * {@link #perform()} always returns {@code null}.
 * </p>
 *
 * <p>
 * This provider is typically used as a placeholder or "disabled" AI provider
 * &mdash; for example, to run Ghostwriter without configuring a real GenAI
 * provider/model, to skip AI calls during testing, or as a safe default when no
 * provider has been selected.
 * </p>
 *
 * <p>
 * When initialized with the {@code "log"} model, the operations that accept
 * input or perform work log an INFO-level message so callers can verify that
 * this no-op provider is active. All other model values leave this diagnostic
 * logging disabled.
 * </p>
 * 
 * @since 1.3.0
 */
public class NoneProvider implements Genai {

	/** Logger used when diagnostic logging is enabled with the {@code log} model. */
	private static final Logger logger = LoggerFactory.getLogger(NoneProvider.class);

	/** Whether provider calls should be logged at INFO level. */
	private boolean loggingOn;

	/**
	 * Does nothing; the given model and configurator are discarded.
	 *
	 * @param model model/provider identifier; ignored
	 * @param conf  configuration source; ignored
	 */
	@Override
	public void init(String model, Configurator conf) {
		loggingOn = Strings.CS.equals(model, "log");
		if (loggingOn) {
			logger.info("NoneProvider.init()");
		}
	}

	/**
	 * Does nothing; the given prompt text is discarded.
	 *
	 * @param text prompt text; ignored
	 */
	@Override
	public void prompt(String text) {
		if (loggingOn) {
			logger.info("Prompt text: {}", text);
		}
	}

	/**
	 * Clears the accumulated prompt buffer.
	 */
	@Override
	public void clear() {
		if (loggingOn) {
			logger.info("NoneProvider.clear()");
		}
	}

	/**
	 * Does nothing; the given instructions are discarded.
	 *
	 * @param instructions system-level instructions; ignored
	 */
	@Override
	public void instructions(String instructions) {
		if (loggingOn) {
			logger.info("Instructions: {}", instructions);
		}
	}

	/**
	 * Performs no AI processing.
	 *
	 * @return always {@code null}
	 */
	@Override
	public String perform() {
		if (loggingOn) {
			logger.info("NoneProvider.perform()");
		}
		return null;
	}

	/**
	 * Does nothing; no function tools are registered.
	 *
	 * @param tools        function tools to register; ignored
	 * @param enabledTools names of enabled tools; ignored
	 */
	@Override
	public void addTools(FunctionTools tools, String[] enabledTools) {
		if (loggingOn) {
			logger.info("Tools: {}", tools);
		}
	}

	/**
	 * Does nothing; no prompt tools are registered.
	 *
	 * @param tools function tools to register; ignored
	 */
	@Override
	public void addPrompts(FunctionTools tools) {
		// SonarQube java:S1186: prompts are deliberately unsupported by this no-op provider.
	}

	/**
	 * Does nothing; no resource tools are registered.
	 *
	 * @param tools function tools to register; ignored
	 */
	@Override
	public void addResources(FunctionTools tools) {
		// SonarQube java:S1186: resources are deliberately unsupported by this no-op provider.
	}

	/**
	 * Does nothing; the given project directory is discarded.
	 *
	 * @param projectDir project root directory; ignored
	 */
	@Override
	public void setProjectDir(File projectDir) {
		if (loggingOn) {
			logger.info("projectDir: {}", projectDir);
		}
	}

	/**
	 * Does nothing; the given error-handling flag is discarded.
	 *
	 * @param errorHandling whether error handling should be enabled; ignored
	 */
	@Override
	public void setErrorHandling(boolean errorHandling) {
		if (loggingOn) {
			logger.info("errorHandling: {}", errorHandling);
		}
	}

}
