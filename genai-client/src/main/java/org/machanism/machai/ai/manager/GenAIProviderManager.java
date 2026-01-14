package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.provider.none.NoneProvider;

/**
 * Manager for instantiating and resolving {@link GenAIProvider} implementations
 * by model identifier string.
 * <p>
 * Supports dynamic instantiation of provider implementations through
 * reflection, enabling flexible selection of AI models using provider/model
 * identifiers (e.g., "OpenAI:gpt-3.5-turbo"). Maintains a cache of available
 * provider instances and delegates model selection and configuration.
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
 * provider.prompt("Hello!");
 * </pre>
 *
 * @author Viktor Tovstyi
 * @see GenAIProvider
 */
public class GenAIProviderManager {

	/**
	 * Creates and returns the appropriate GenAIProvider instance based on a
	 * provider/model string.
	 *
	 * @param chatModel the model string, formatted as "Provider:Model" or just
	 *                  "Model"
	 * @return the resolved GenAIProvider
	 * @throws IllegalArgumentException if the provider cannot be resolved or
	 *                                  instantiated
	 */
	public static GenAIProvider getProvider(String chatModel) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(providerName)) {
			chatModelName = providerName;
			providerName = NoneProvider.NAME;
		}
		String className;
		if (StringUtils.contains(providerName, '.')) {
			className = providerName;
		} else {
			String packageName = providerName.toLowerCase();
			className = String.format("org.machanism.machai.ai.provider.%s.%sProvider", packageName, providerName);
		}
		try {
			@SuppressWarnings("unchecked")
			Class<? extends GenAIProvider> providerClass = (Class<? extends GenAIProvider>) Class.forName(className);
			Constructor<? extends GenAIProvider> constructor = providerClass.getConstructor();
			GenAIProvider provider = constructor.newInstance();
			provider.model(chatModelName);

			return provider;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("GenAI provider: " + providerName + " is not supported.", e);
		}
	}

}
