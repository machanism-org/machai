package org.machanism.machai.ai.provider;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Internal logging utility designed to manage logging of tool inputs, results,
 * and execution errors.
 * <p>
 * This class handles both compact informational logging (info) and fully
 * detailed JSON output mapping (debug) based on the current active logging
 * thresholds.
 * </p>
 */
class ToolLogger {

	/** Logger instance for this provider. */
	static Logger logger = LoggerFactory.getLogger(ToolLogger.class);

	enum Type {
		RESOURCE,
		PROMPT,
		TOOL
	}

	private static final String CALL_MSG = "[{}] <{}> is called with params: `{}`, projectDir: `{}`";
	private static final String RETURNS_MSG = "[{}] <{}> returns ({} bytes): `{}`, projectDir: `{}`";
	private static final String ERROR_MSG = "[{}] <{}> failed: `{}`, projectDir: `{}`";

	/** Categorization message descriptor. */
	private Type type;

	/**
	 * Creates an instance of ToolLogger.
	 *
	 * @param type  classification message text
	 * @param tools parent class tools instance
	 */
	public ToolLogger(Type type, FunctionTools tools) {
		this.type = type;
	}

	/**
	 * Logs errors that occurred during tool method execution.
	 *
	 * @param name            the name of the tool
	 * @param dir             the active project directory context
	 * @param targetException the exception thrown by the target tool
	 */
	void logError(String name, File dir, Throwable targetException) {
		if (logger.isDebugEnabled()) {
			logger.error(ERROR_MSG, type, name, targetException.getMessage(), dir, targetException);
		} else {
			logger.error(ERROR_MSG, type, name, targetException.getMessage(), dir);
		}
	}

	/**
	 * Logs tool invocation input details.
	 *
	 * @param name  the name of the tool
	 * @param props JSON node representing tool properties
	 * @param dir   the active project directory context
	 */
	void logInput(String name, JsonNode props, File dir) {
		if (logger.isDebugEnabled()) {
			String valueOf = writeValueAsString(props);
			logger.debug(CALL_MSG, type, name, valueOf, dir);
		} else if (logger.isInfoEnabled()) {
			String valueOf = writeValueAsString(props);
			logger.info(CALL_MSG, type, name, abbreviate(valueOf), dir);
		}
	}

	/**
	 * Abbreviates long log strings to keep logs concise and clean.
	 *
	 * @param valueOf the source string representation
	 * @return the abbreviated string trimmed to {@link #LOG_LINE_LENG} characters
	 */
	private String abbreviate(String valueOf) {
		return StringUtils.abbreviate(valueOf, AbstractAIProvider.LOG_LINE_LENG)
				.replace(AbstractAIProvider.LINE_SEPARATOR, " ").replace("\r", "");
	}

	/**
	 * Logs tool invocation execution results.
	 *
	 * @param name   the name of the tool
	 * @param dir    the active project directory context
	 * @param result the resulting object returned by the tool function
	 */
	void logResult(String name, File dir, Object result) {
		if (logger.isDebugEnabled()) {
			String valueOf = writeValueAsString(result);
			logger.debug(RETURNS_MSG, type, name, valueOf.length(), valueOf, dir);
		} else if (logger.isInfoEnabled()) {
			String valueOf = writeValueAsString(result);
			logger.info(RETURNS_MSG, type, name, valueOf.length(), abbreviate(valueOf), dir);
		}
	}

	/**
	 * Utility method that safely serializes any payload object into a JSON string
	 * format.
	 *
	 * @param result the object to serialize
	 * @return a serialized JSON string, or the fallback string value of the object
	 *         on failure
	 */
	private String writeValueAsString(Object result) {
		String valueOf;
		if (result instanceof String) {
			valueOf = (String) result;
		} else {
			try {
				valueOf = new ObjectMapper().writeValueAsString(result);
			} catch (JsonProcessingException e) {
				valueOf = String.valueOf(result);
			}
		}
		return valueOf;
	}

}
