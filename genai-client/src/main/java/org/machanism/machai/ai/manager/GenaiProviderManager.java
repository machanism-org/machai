package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;

public class GenaiProviderManager {

	private GenaiProviderManager() {
		// Utility class.
	}

	/**
	 * Creates a provider instance for the given provider/model identifier and
	 * applies the selected model.
	 *
	 * @param chatModel the model identifier formatted as {@code Provider:Model} or
	 *                  just {@code Model}
	 * @param conf      configurator used to initialize the provider
	 * @return a new provider instance configured with the requested model
	 * @throws IllegalArgumentException if the provider cannot be resolved or
	 *                                  instantiated
	 */
	public static Genai getProvider(String chatModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
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
			Class<? extends Genai> providerClass = (Class<? extends Genai>) Class.forName(className);
			Constructor<? extends Genai> constructor = providerClass.getConstructor();
			Genai provider = constructor.newInstance();
			conf.set("chatModel", chatModelName);
			provider.init(conf);
			return provider;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to initialize GenAI provider '" + providerName
					+ "': provider is not supported or an error occurred during initialization.", e);
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}

	public static EmbeddingProvider getEmbeddingProvider(String embeddingModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(embeddingModel, ":");
		String model = StringUtils.substringAfter(embeddingModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
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
			Class<? extends EmbeddingProvider> providerClass = (Class<? extends EmbeddingProvider>) Class.forName(className);
			Constructor<? extends EmbeddingProvider> constructor = providerClass.getConstructor();
			EmbeddingProvider provider = constructor.newInstance();
			conf.set("embeddingModel", model);
			provider.init(conf);
			return provider;
			
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to initialize EmbeddingProvider provider '" + providerName
					+ "': provider is not supported or an error occurred during initialization.", e);
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}

}
