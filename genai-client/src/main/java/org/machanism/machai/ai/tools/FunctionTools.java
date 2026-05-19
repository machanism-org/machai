package org.machanism.machai.ai.tools;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;

/**
 * Service-provider interface (SPI) for installing host-provided function tools
 * into a {@link Genai}.
 *
 * <p>
 * Implementations contribute one or more named tools via
 * {@link Genai#addTool(String, String, org.machanism.machai.ai.provider.Genai.ToolFunction, String...)}.
 * Tools are typically discovered via {@link java.util.ServiceLoader} and
 * applied at runtime.
 * </p>
 *
 * <p>
 * Implementations may optionally accept a {@link Configurator} via
 * {@link #setConfigurator(Configurator)} to resolve runtime configuration (for
 * example, API tokens for web calls).
 * </p>
 */
public interface FunctionTools {

	/**
	 * Registers this tool set with the given provider.
	 *
	 * @param provider provider to register tools with
	 */
	void applyTools(Genai provider);

	/**
	 * Provides a configurator instance to the tool set.
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 *
	 * @param configurator configurator to use for runtime value resolution
	 */
	default void setConfigurator(Configurator configurator) {
		// no-op
	}

}
