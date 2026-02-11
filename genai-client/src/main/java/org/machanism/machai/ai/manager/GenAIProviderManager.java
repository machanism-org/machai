package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves and instantiates {@link GenAIProvider} implementations from a
 * provider/model identifier.
 *
 * <p>
 * The identifier is formatted as {@code Provider:Model} (for example,
 * {@code OpenAI:gpt-4o-mini}). If the provider portion is omitted (for example,
 * {@code gpt-4o-mini}), a default provider is used.
 *
 * <p>
 * Providers are instantiated via reflection. A provider can be referenced
 * either by a short provider name (mapped to the conventional class name
 * {@code org.machanism.machai.ai.provider.<provider>.<Provider>Provider}) or by
 * a fully-qualified class name.
 *
 * <h2>Usage</h2>
 *
 * {@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo", conf);
 * provider.prompt("Hello!");
 * }
 *
 * @author Viktor Tovstyi
 * @see GenAIProvider
 */
public class GenAIProviderManager {

	private static final Logger logger = LoggerFactory.getLogger(GenAIProviderManager.class);

	private static List<Usage> usages = new ArrayList<>();

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
	public static GenAIProvider getProvider(String chatModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(providerName)) {
			providerName = NoneProvider.NAME;
			chatModelName = chatModel;
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
			provider.init(conf);
			provider.model(chatModelName);

			return provider;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to initialize GenAI provider '" + providerName
					+ "': provider is not supported or an error occurred during initialization.", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("GenAI provider initialization failed for '" + providerName + "'", e);
		}
	}

	public static void addUsage(Usage usage) {
		usages.add(usage);
	}

	public static void logUsage() {

		if (!usages.isEmpty()) {
			long inputTokens = 0;
			long inputCachedTokens = 0;
			long outputTokens = 0;

			for (Usage u : usages) {
				inputTokens += u.getInputTokens();
				inputCachedTokens += u.getInputCachedTokens();
				outputTokens += u.getOutputTokens();
			}

			logger.info("GenAI token usage summary: Input = {}, Cached = {}, Output = {}.", inputTokens,
					inputCachedTokens, outputTokens);
		} else {
			logger.info("GenAI token usage information not found.");
		}
	}
}
