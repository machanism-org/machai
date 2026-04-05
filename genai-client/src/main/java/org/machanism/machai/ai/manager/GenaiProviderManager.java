package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.provider.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory/registry for resolving {@link Genai} implementations from a provider/model identifier and
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
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 * provider.prompt("Hello!");
 * String response = provider.perform();
 *
 * GenaiProviderManager.addUsage(provider.usage());
 * GenaiProviderManager.logUsage();
 * }</pre>
 *
 * <p><strong>Thread-safety:</strong> Usage aggregation is backed by a static {@link ArrayList} and is not
 * synchronized.
 *
 * @author Viktor Tovstyi
 * @see Genai
 */
public class GenaiProviderManager {

	private static final Logger logger = LoggerFactory.getLogger(GenaiProviderManager.class);

	private static final List<Usage> usages = new ArrayList<>();

	private GenaiProviderManager() {
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
	public static Genai getProvider(String chatModel, Configurator conf) {
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
