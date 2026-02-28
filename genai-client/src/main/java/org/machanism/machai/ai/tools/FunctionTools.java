package org.machanism.machai.ai.tools;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;

/**
 * Service-provider interface (SPI) for installing host-provided function tools into a {@link GenAIProvider}.
 *
 * <p>
 * Implementations contribute one or more named tools via
 * {@link GenAIProvider#addTool(String, String, java.util.function.Function, String...)}. Tools are typically
 * discovered via {@link java.util.ServiceLoader} and applied at runtime by {@link FunctionToolsLoader}.
 * </p>
 *
 * <p>
 * Implementations may optionally accept a {@link Configurator} via {@link #setConfigurator(Configurator)} to
 * resolve runtime configuration (for example, API tokens for web calls).
 * </p>
 */
public interface FunctionTools {

	/**
	 * Registers this tool set with the given provider.
	 *
	 * @param provider provider to register tools with
	 */
	void applyTools(GenAIProvider provider);

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
