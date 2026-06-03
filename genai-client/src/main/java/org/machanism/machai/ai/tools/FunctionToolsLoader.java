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
 * loader.applyTools(provider, conf, appClass);
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
	 * provider, filtered by application class compatibility.
	 *
	 * <p>
	 * A fresh instance of each discovered tool installer class is created before
	 * configuration and registration so that provider setup runs with isolated tool
	 * state.
	 * </p>
	 *
	 * @param provider     the {@link Genai} provider instance to augment with tool functions
	 * @param configurator configurator passed to each discovered tool installer
	 * @param appClass     the application class requesting tool assignment; only tools compatible with this class are applied
	 * @throws IllegalArgumentException if a discovered installer cannot be instantiated
	 */
	public void applyTools(Genai provider, Configurator configurator, Class<?> appClass) {
		for (FunctionTools functionTool : functionTools) {
			Class<? extends FunctionTools> functionToolsClass = functionTool.getClass();
			boolean supported = isSupportedFor(appClass, functionToolsClass);

			if (supported) {
				FunctionTools newInstance;
				try {
					newInstance = functionToolsClass.newInstance();
					newInstance.setConfigurator(configurator);
					newInstance.applyTools(provider);

				} catch (InstantiationException | IllegalAccessException e) {
					throw new IllegalArgumentException(
							"FunctionTools class initialization failed: " + functionToolsClass,
							e);
				}
			}
		}
	}

	/**
	 * Checks whether the given FunctionTools implementation supports assignment to the specified application class.
	 * <p>
	 * If the {@link SupportedFor} annotation is present, only classes listed in its value are considered compatible.
	 * If the annotation is absent, compatibility is assumed.
	 * </p>
	 *
	 * @param appClass            the application class requesting tool assignment
	 * @param functionToolsClass  the FunctionTools implementation class
	 * @return {@code true} if the tool is compatible with the application class, {@code false} otherwise
	 */
	private boolean isSupportedFor(Class<?> appClass, Class<? extends FunctionTools> functionToolsClass) {
		SupportedFor supportedApplications = functionToolsClass.getAnnotation(SupportedFor.class);
		boolean supported = false;
		if (supportedApplications != null) {
			for (Class<?> supportedClass : supportedApplications.value()) {
				if (supportedClass.isAssignableFrom(appClass)) {
					supported = true;
					break;
				}
			}
		} else {
			supported = true;
		}
		return supported;
	}

}