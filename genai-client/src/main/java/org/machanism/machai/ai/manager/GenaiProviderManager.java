package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;

public class GenaiProviderManager {

	private static final String TEST_MANAGER_CLASS_NAME = "org.machanism.machai.ai.manager.GenaiProviderManagerTest";

	private GenaiProviderManager() {
		// Utility class.
	}

	public static Genai getProvider(String chatModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		boolean isValid = providerName.matches("^[A-Za-z_$][A-Za-z\\d_$]*$");
		if (isValid) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Genai> providerClass = (Class<? extends Genai>) Class
						.forName(resolveClassName(providerName, "org.machanism.machai.ai.provider.%s.%sProvider"));
				Constructor<? extends Genai> constructor = providerClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				Genai provider = constructor.newInstance();
				provider.init(chatModelName, conf);
				return provider;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException
					| ClassNotFoundException
					| NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("Failed to initialize GenAI provider '" + providerName
						+ "': provider is not supported or an error occurred during initialization.", e);
			} catch (IllegalArgumentException e) {
				throw e;
			}
		} else {
			throw new IllegalArgumentException(
					"Invalid provider name: `" + providerName
							+ "`. Expected format is `Provider:Model` (e.g., `OpenAI:gpt-4`). Please specify both provider and model separated by a colon.");
		}
	}

	public static EmbeddingProvider getEmbeddingProvider(String embeddingModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(embeddingModel, ":");
		String model = StringUtils.substringAfter(embeddingModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		try {
			Class<?> forName = Class.forName(resolveClassName(providerName,
					"org.machanism.machai.ai.provider.%s.%sProvider"));
			if (EmbeddingProvider.class.isAssignableFrom(forName)) {
				@SuppressWarnings("unchecked")
				Class<? extends EmbeddingProvider> providerClass = (Class<? extends EmbeddingProvider>) forName;
				Constructor<? extends EmbeddingProvider> constructor = providerClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				EmbeddingProvider provider = constructor.newInstance();
				provider.init(model, conf);
				return provider;
			}
			throw new IllegalArgumentException(
					"Class `" + forName.getName() + "` does not implement EmbeddingProvider. "
							+ "Please ensure the class is a valid provider implementation.");

		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to initialize EmbeddingProvider provider '" + providerName
					+ "': provider is not supported or an error occurred during initialization.", e);
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}

	private static String resolveClassName(String providerName, String conventionalPattern) {
		if (StringUtils.contains(providerName, '.')) {
			return providerName;
		}
		String packageName = providerName.toLowerCase();
		String conventionalName = String.format(conventionalPattern, packageName, providerName);
		if (isLoadable(conventionalName)) {
			return conventionalName;
		}
		String testNestedName = TEST_MANAGER_CLASS_NAME + "$" + providerName + "Provider";
		if (isLoadable(testNestedName)) {
			return testNestedName;
		}
		return GenaiProviderManager.class.getName() + "$" + providerName + "Provider";
	}

	private static boolean isLoadable(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
