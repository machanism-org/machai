package org.machanism.machai.ai.tools;

/**
 * Service-provider interface (SPI) for installing host-provided function tools.
 *
 * <p>
 * Implementations of this interface define a set of local capabilities (such as file access,
 * command execution, or HTTP retrieval) that can be registered with an AI provider (e.g., {@link org.machanism.machai.ai.provider.Genai}).
 * </p>
 *
 * <p>
 * Function tool implementations are typically discovered via Java's {@link java.util.ServiceLoader}
 * mechanism and applied to the provider at runtime. This enables dynamic extension of the provider's
 * functionality based on available tool implementations in the classpath.
 * </p>
 *
 * <p>
 * The interface serves as a marker for tool installer classes; concrete implementations may expose
 * tool functions, prompts, or other callable capabilities.
 * </p>
 */
public interface FunctionTools {

}