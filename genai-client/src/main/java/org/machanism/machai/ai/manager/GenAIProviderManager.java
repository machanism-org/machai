package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.none.NoneProvider;

/**
 * Manages the instantiation of GenAIProvider implementations based on model
 * identifier strings.
 * <p>
 * This class supports dynamic loading of GenAIProvider classes using reflection
 * and resolves providers based on the model name format (e.g.
 * "OpenAI:gpt-3.5-turbo").
 * <p>
 * Usage example:
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

	private static Map<String, GenAIProvider> providerMap = new HashMap<>();

	/**
	 * Creates and returns the appropriate GenAIProvider based on the provided model
	 * string.
	 *
	 * @param chatModel the model string, formatted as "Provider:Model" or just
	 *                  "Model"
	 * @return the resolved GenAIProvider
	 * @throws IllegalArgumentException if the provider cannot be resolved or
	 *                                  instantiated
	 */
	public static GenAIProvider getProvider(String chatModel) {

		GenAIProvider provider = providerMap.get(chatModel);

		if (provider == null) {
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
				String packageName = providerName.substring(0, 1).toLowerCase() + providerName.substring(1);
				className = String.format("org.machanism.machai.ai.%s.%sProvider", packageName, providerName);
			}

			try {
				@SuppressWarnings("unchecked")
				Class<? extends GenAIProvider> providerClass = (Class<? extends GenAIProvider>) Class
						.forName(className);
				Constructor<? extends GenAIProvider> constructor = providerClass.getConstructor();
				provider = constructor.newInstance();
				provider.model(chatModelName);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ClassNotFoundException | NoSuchMethodException
					| SecurityException e) {
				throw new IllegalArgumentException("GenAI provider: " + providerName + " is not supported.", e);
			}

			providerMap.put(chatModel, provider);
		}

		return provider;
	}

}
