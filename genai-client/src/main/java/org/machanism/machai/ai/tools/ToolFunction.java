package org.machanism.machai.ai.tools;

import java.io.IOException;

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
	Object apply(Object[] params) throws IOException;
}