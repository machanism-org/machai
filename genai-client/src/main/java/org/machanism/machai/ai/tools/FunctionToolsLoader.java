package org.machanism.machai.ai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers and applies {@link FunctionTools} implementations using Java's
 * {@link ServiceLoader} mechanism.
 *
 * <p>
 * This class serves as the host-side entry point for registering a curated set
 * of local capabilities (such as file access, command execution, and HTTP
 * retrieval) with a {@link Genai} provider. Implementations are discovered from
 * the classpath (typically via {@code META-INF/services} provider
 * configuration) and then applied to the provider in discovery order.
 * </p>
 *
 * <h2>Usage</h2>
 * 
 * <pre>{@code
 * Configurator conf = ...;
 * Genai provider = ...;
 * FunctionToolsLoader loader = new FunctionToolsLoader();
 * loader.applyTools(provider, conf);
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public class FunctionToolsLoader {

	private static final Logger logger = LoggerFactory.getLogger(FunctionToolsLoader.class);

	private final List<FunctionTools> functionTools = new ArrayList<>();

	/**
	 * Constructs a new {@code FunctionToolsLoader} and discovers available
	 * {@link FunctionTools} implementations using {@link ServiceLoader}.
	 */
	public FunctionToolsLoader() {
		ServiceLoader<FunctionTools> functionToolServiceLoader = ServiceLoader.load(FunctionTools.class);
		for (FunctionTools functionTool : functionToolServiceLoader) {
			functionTools.add(functionTool);
			logger.debug("FunctionTools: {}", functionTool.getClass().getName());
		}
	}

	/**
	 * Applies all discovered {@link FunctionTools} installers to the given
	 * provider.
	 *
	 * <p>
	 * A fresh instance of each discovered tool installer class is created before
	 * configuration and registration so that provider setup runs with isolated tool
	 * state.
	 * </p>
	 *
	 * @param provider the {@link Genai} provider instance to augment with tool
	 *                 functions
	 * @param configurator configurator passed to each discovered tool installer
	 * @throws IllegalArgumentException if a discovered installer cannot be
	 *                 instantiated
	 */
	public void applyTools(Genai provider, Configurator configurator) {
		for (FunctionTools functionTool : functionTools) {
			Class<? extends FunctionTools> functionToolsClass = functionTool.getClass();
			FunctionTools newInstance;
			try {
				newInstance = functionToolsClass.newInstance();
				newInstance.setConfigurator(configurator);
				newInstance.applyTools(provider);

			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException("FunctionTools class initialization failed: " + functionToolsClass,
						e);
			}
		}
	}

}
