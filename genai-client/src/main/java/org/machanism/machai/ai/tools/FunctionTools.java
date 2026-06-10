package org.machanism.machai.ai.tools;

import org.machanism.machai.ai.provider.Genai;

/**
 * Service-provider interface (SPI) for installing host-provided function tools
 * into a {@link Genai}.
 *
 * <p>
 * Implementations contribute one or more named tools via
 * {@link Genai#addTool(String, String, org.machanism.machai.ai.tools.ToolFunction, String...)}.
 * Tools are typically discovered via {@link java.util.ServiceLoader} and
 * applied at runtime.
 * </p>
 */
public interface FunctionTools {

}
