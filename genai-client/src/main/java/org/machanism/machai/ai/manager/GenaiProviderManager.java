package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;

/**
 * Utility class for dynamically loading and initializing generative AI
 * providers and embedding providers.
 *
 * <p>
 * The {@code GenaiProviderManager} offers static methods to instantiate
 * {@link Genai} and {@link EmbeddingProvider} implementations based on a
 * provider/model string, using Java reflection. Providers are expected to
 * follow a conventional package and class naming pattern, or may be specified
 * by fully qualified class name.
 * </p>
 *
 * <h2>Provider Naming Convention</h2>
 * <ul>
 * <li>Provider and model are specified as {@code Provider:Model} (e.g.,
 * {@code OpenAI:gpt-4}).</li>
 * <li>If the provider name contains a dot ({@code .}), it is treated as a fully
 * qualified class name.</li>
 * <li>Otherwise, the provider is resolved using the pattern
 * {@code org.machanism.machai.ai.provider.{provider}.{Provider}Provider}.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4", conf);
 * EmbeddingProvider embeddingProvider = GenaiProviderManager.getEmbeddingProvider("OpenAI:embedding-model", conf);
 * }</pre>
 *
 * <p>
 * If the provider cannot be found or instantiated, an
 * {@link IllegalArgumentException} is thrown.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class GenaiProviderManager {

	private static final String AI_CLIENT_CLASS_NAME_PATTERN = "org.machanism.machai.ai.provider.impl.%sProvider";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private GenaiProviderManager() {
		// Utility class.
	}

	/**
	 * Dynamically loads and initializes a {@link Genai} provider based on the
	 * specified provider/model string.
	 *
	 * <p>
	 * The provider name and model are parsed from the input string (format:
	 * {@code Provider:Model}). The provider class is resolved using a conventional
	 * naming pattern or as a fully qualified class name. The provider is
	 * instantiated and initialized with the specified model and configuration.
	 * </p>
	 *
	 * @param chatModel the provider/model string (e.g., {@code OpenAI:gpt-4})
	 * @param conf      the configuration object for provider initialization
	 * @return the initialized {@link Genai} provider instance, or {@code null} if
	 *         the provider name is blank
	 * @throws IllegalArgumentException if the provider name is invalid, or if the
	 *                                  provider cannot be found or instantiated
	 */
	public static Genai getProvider(String chatModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		boolean isValid = providerName.matches("^[A-Za-z_$][A-Za-z\\d_$]*$");
		if (isValid) {
			try {
				String className = resolveClassName(providerName);
				@SuppressWarnings("unchecked")
				Class<? extends Genai> providerClass = (Class<? extends Genai>) Class
						.forName(className);
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

	/**
	 * Dynamically loads and initializes an {@link EmbeddingProvider} based on the
	 * specified provider/model string.
	 *
	 * <p>
	 * The provider name and model are parsed from the input string (format:
	 * {@code Provider:Model}). The provider class is resolved using a conventional
	 * naming pattern or as a fully qualified class name. The provider is
	 * instantiated and initialized with the specified model and configuration.
	 * </p>
	 *
	 * @param embeddingModel the provider/model string (e.g.,
	 *                       {@code OpenAI:embedding-model})
	 * @param conf           the configuration object for provider initialization
	 * @return the initialized {@link EmbeddingProvider} instance, or {@code null}
	 *         if the provider name is blank
	 * @throws IllegalArgumentException if the provider cannot be found, does not
	 *                                  implement {@link EmbeddingProvider}, or
	 *                                  cannot be instantiated
	 */
	public static EmbeddingProvider getEmbeddingProvider(String embeddingModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(embeddingModel, ":");
		String model = StringUtils.substringAfter(embeddingModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		try {
			Class<?> forName = Class.forName(resolveClassName(providerName));
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

	/**
	 * Resolves the provider class name based on the provider name and a
	 * conventional naming pattern.
	 *
	 * <p>
	 * If the provider name contains a dot ({@code .}), it is treated as a fully
	 * qualified class name. Otherwise, the provider is resolved using the specified
	 * pattern. If the class is not loadable, a fallback naming convention is used.
	 * </p>
	 *
	 * @param providerName        the provider name (e.g., {@code OpenAI})
	 * @param conventionalPattern the naming pattern (e.g.,
	 *                            {@code org.machanism.machai.ai.provider.%s.%sProvider})
	 * @return the resolved class name
	 */
	private static String resolveClassName(String providerName) {
		if (StringUtils.contains(providerName, '.')) {
			return providerName;
		}
		String conventionalName = String.format(AI_CLIENT_CLASS_NAME_PATTERN, providerName);
		if (isLoadable(conventionalName)) {
			return conventionalName;
		}
		return GenaiProviderManager.class.getName() + "$" + providerName + "Provider";
	}

	/**
	 * Checks whether the specified class name can be loaded.
	 *
	 * @param className the fully qualified class name to check
	 * @return {@code true} if the class can be loaded, {@code false} otherwise
	 */
	private static boolean isLoadable(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}