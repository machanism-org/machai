package org.machanism.machai.ai.tools;

import java.io.File;
import java.io.IOException;

import org.machanism.macha.core.commons.configurator.Configurator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Functional interface representing a tool callable by a provider during a run.
 *
 * <p>
 * Implementations typically receive structured arguments parsed from a model tool
 * invocation request together with the provider working directory context.
 * </p>
 */
@FunctionalInterface
public interface ToolFunction {

	String SESSION_ID_PARAM_NAME = "request.session.id";
	
	/**
	 * Executes the tool.
	 *
	 * @param params provider-specific parameters, typically parsed from JSON
	 * @param projectDir provider working directory context; may be {@code null}
	 * @param config TODO
	 * @return tool result (provider-specific; commonly serialized to JSON)
	 * @throws IOException if tool execution fails
	 */
	Object apply(JsonNode params, File projectDir, Configurator config) throws IOException;
}
