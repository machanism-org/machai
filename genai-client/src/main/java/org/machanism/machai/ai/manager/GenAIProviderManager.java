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
 * Factory/registry for resolving {@link GenAIProvider} implementations from a provider/model identifier and
 * aggregating per-run {@link Usage} metrics.
 *
 * <p>The model identifier is formatted as {@code Provider:Model} (for example, {@code OpenAI:gpt-4o-mini}). If the
 * provider portion is omitted (for example, {@code gpt-4o-mini}), the default provider is used.
 *
 * <p>Providers are instantiated via reflection. A provider can be referenced either by:
 * <ul>
 *   <li>a short provider name (mapped to
 *   {@code org.machanism.machai.ai.provider.&lt;provider&gt;.&lt;Provider&gt;Provider}), or</li>
 *   <li>a fully-qualified class name.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 * provider.prompt("Hello!");
 * String response = provider.perform();
 *
 * GenAIProviderManager.addUsage(provider.usage());
 * GenAIProviderManager.logUsage();
 * }</pre>
 *
 * <p><strong>Thread-safety:</strong> Usage aggregation is backed by a static {@link ArrayList} and is not
 * synchronized.
 *
 * @author Viktor Tovstyi
 * @see GenAIProvider
 */
public class GenAIProviderManager {

	private static final Logger logger = LoggerFactory.getLogger(GenAIProviderManager.class);

	private static final List<Usage> usages = new ArrayList<>();

	private GenAIProviderManager() {
		// Utility class.
	}

	/**
	 * Creates a provider instance for the given provider/model identifier and applies the selected model.
	 *
	 * @param chatModel the model identifier formatted as {@code Provider:Model} or just {@code Model}
	 * @param conf      configurator used to initialize the provider
	 * @return a new provider instance configured with the requested model
	 * @throws IllegalArgumentException if the provider cannot be resolved or instantiated
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
			provider.model(chatModelName);
			provider.init(conf);
			return provider;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to initialize GenAI provider '" + providerName
					+ "': provider is not supported or an error occurred during initialization.", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("GenAI provider initialization failed for '" + providerName + "'", e);
		}
	}

	/**
	 * Adds a single {@link Usage} record to the in-memory aggregation list.
	 *
	 * @param usage usage to add
	 */
	public static void addUsage(Usage usage) {
		usages.add(usage);
	}

	/**
	 * Logs a summary of the aggregated {@link Usage} records.
	 */
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
