package org.machanism.machai.ai.tools;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Functional interface representing a tool callable by a provider during a run.
 */
@FunctionalInterface
public interface ToolFunction {

	/**
	 * Executes the tool.
	 *
	 * @param params provider-specific parameters
	 * @return tool result (provider-specific; commonly serialized to JSON)
	 * @throws IOException if tool execution fails
	 */
	Object apply(JsonNode params, File workingDir ) throws IOException;
}