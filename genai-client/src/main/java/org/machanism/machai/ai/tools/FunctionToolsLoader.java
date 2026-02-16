package org.machanism.machai.ai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads {@link FunctionTools} implementations via {@link ServiceLoader} and
 * applies them to a {@link GenAIProvider}.
 *
 * <p>
 * This loader is a convenience entry point for host applications that want to
 * expose a curated set of local capabilities (file access, command execution,
 * HTTP retrieval, etc.) to an AI workflow. Implementations are discovered from
 * the classpath and must be registered as service providers.
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
	 * Discovers available {@link FunctionTools} implementations using
	 * {@link ServiceLoader}.
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
	 * @param configurator configuration source used by tool installers
	 */
	public void setConfiguration(Configurator configurator) {
		for (FunctionTools functionTool : functionTools) {
			functionTool.setConfigurator(configurator);
		}
	}
}
