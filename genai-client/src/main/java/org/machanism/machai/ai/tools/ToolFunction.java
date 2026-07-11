package org.machanism.machai.ai.tools;

import java.io.File;

import org.machanism.macha.core.commons.configurator.Configurator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Functional interface representing a tool callable by a provider during a run.
 * <p>
 * Implementations of this interface encapsulate the execution logic for a tool,
 * typically invoked with structured arguments parsed from a model tool invocation request,
 * along with variable runtime context objects (such as the target working directory and configurators).
 * </p>
 *
 * <p>
 * The {@link #apply(JsonNode, Object...)} method is the entry point for tool execution,
 * accepting provider-specific parameters as a {@link JsonNode} and an array of contextual objects,
 * returning a result object (commonly serialized to JSON).
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
     * Executes the tool with the given parameters and variable context instances.
     *
     * @param params       provider-specific parameters, typically parsed from a JSON structure
     * @param paramsByType variable-arity parameter list representing runtime contextual environments 
     *                     (e.g. {@link File} for working directory, {@link Configurator} for lookups)
     * @return the tool execution result (commonly a string or object serializable to JSON)
     * @throws Exception if tool execution fails during processing
     */
    Object apply(JsonNode params, Object... paramsByType) throws Exception;
}