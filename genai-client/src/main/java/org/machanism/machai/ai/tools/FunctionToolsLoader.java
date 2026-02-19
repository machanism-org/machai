package org.machanism.machai.ai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers and applies {@link FunctionTools} implementations using {@link ServiceLoader}.
 *
 * <p>
 * This class acts as a host-side entry point for registering a curated set of local capabilities (for example,
 * file access, command execution, and HTTP retrieval) with a {@link GenAIProvider}. Implementations are
 * discovered from the classpath, typically via {@code META-INF/services} provider configuration, and then
 * applied to the provider in discovery order.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = ...;
 * FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
 * loader.setConfiguration(conf);
 * loader.applyTools(provider);
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public class FunctionToolsLoader {

	private static final Logger logger = LoggerFactory.getLogger(FunctionToolsLoader.class);

	private static final FunctionToolsLoader INSTANCE = new FunctionToolsLoader();

	private final List<FunctionTools> functionTools = new ArrayList<>();

	/**
	 * Private constructor to prevent external instantiation.
	 * <p>
	 * Discovers available {@link FunctionTools} implementations using {@link ServiceLoader}.
	 * </p>
	 */
	private FunctionToolsLoader() {
		ServiceLoader<FunctionTools> functionToolServiceLoader = ServiceLoader.load(FunctionTools.class);
		for (FunctionTools functionTool : functionToolServiceLoader) {
			functionTools.add(functionTool);
			logger.info("FunctionTool: {}", functionTool.getClass().getName());
		}
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return singleton loader
	 */
	public static FunctionToolsLoader getInstance() {
		return INSTANCE;
	}

	/**
	 * Applies all discovered tool installers to the given provider.
	 *
	 * @param provider provider instance to augment with tool functions
	 */
	public void applyTools(GenAIProvider provider) {
		for (FunctionTools functionTool : functionTools) {
			functionTool.applyTools(provider);
		}
	}

	/**
	 * Supplies configuration to all discovered tool installers.
	 *
	 * <p>
	 * Not all {@link FunctionTools} implementations use configuration, but the loader provides a centralized way to
	 * propagate a {@link Configurator} to all tool installers (for example, for resolving header placeholders in web
	 * requests).
	 * </p>
	 *
	 * @param configurator configuration source used by tool installers
	 */
	public void setConfiguration(Configurator configurator) {
		for (FunctionTools functionTool : functionTools) {
			functionTool.setConfigurator(configurator);
		}
	}
}
