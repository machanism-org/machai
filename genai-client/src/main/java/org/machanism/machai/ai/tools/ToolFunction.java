package org.machanism.machai.ai.tools;

import java.io.File;
import java.io.IOException;

import org.machanism.macha.core.commons.configurator.Configurator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Functional interface representing a tool callable by a provider during a run.
 * <p>
 * Implementations of this interface encapsulate the execution logic for a tool,
 * typically invoked with structured arguments parsed from a model tool invocation request,
 * along with the provider's working directory context and configuration.
 * </p>
 *
 * <p>
 * The {@link #apply(JsonNode, File, Configurator)} method is the entry point for tool execution,
 * accepting provider-specific parameters, the working directory, and configuration,
 * and returning a result object (commonly serialized to JSON).
 * </p>
 *
 * <p>
 * The constant {@link #SESSION_ID_PARAM_NAME} defines the standard parameter name for session identification.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ToolFunction tool = ...;
 * JsonNode params = ...;
 * File projectDir = ...;
 * Configurator config = ...;
 * Object result = tool.apply(params, projectDir, config);
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
@FunctionalInterface
public interface ToolFunction {

    /**
     * Standard parameter name for session identification in tool requests.
     */
    String SESSION_ID_PARAM_NAME = "request.session.id";

    /**
     * Executes the tool with the given parameters, working directory, and configuration.
     *
     * @param params     provider-specific parameters, typically parsed from a JSON structure
     * @param projectDir provider working directory context; may be {@code null} if not applicable
     * @param config     provider configuration object, used for tool setup and execution
     * @return tool result (provider-specific; commonly serialized to JSON)
     * @throws IOException if tool execution fails due to I/O or processing errors
     */
    Object apply(JsonNode params, File projectDir, Configurator config) throws IOException;
}