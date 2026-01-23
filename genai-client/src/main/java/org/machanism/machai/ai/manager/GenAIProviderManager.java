package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.provider.none.NoneProvider;

/**
 * Resolves and instantiates {@link GenAIProvider} implementations from a provider/model identifier.
 *
 * <p>The identifier is typically formatted as {@code Provider:Model} (for example
 * {@code OpenAI:gpt-4o-mini}). If the provider portion is omitted, a default provider is used.
 *
 * <p>Providers are instantiated via reflection. A provider can be referenced either by a short provider name
 * (mapped to the conventional class name {@code org.machanism.machai.ai.provider.<provider>.<Provider>Provider})
 * or by a fully-qualified class name.
 *
 * <h2>Usage</h2>
 * {@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
 * provider.prompt("Hello!");
 * }
 *
 * @author Viktor Tovstyi
 * @see GenAIProvider
 */
public class GenAIProviderManager {

	/**
	 * Creates a provider instance for the given provider/model identifier and applies the selected model.
	 *
	 * @param chatModel the model identifier formatted as {@code Provider:Model} or just {@code Model}
	 * @return a new provider instance configured with the requested model
	 * @throws IllegalArgumentException if the provider cannot be resolved or instantiated
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
