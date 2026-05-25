package org.machanism.machai.ai.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central registry for aggregated GenAI token-usage statistics.
 *
 * <p>Usage entries are grouped by model identifier and can later be logged or
 * queried programmatically. All access to the internal storage is synchronized on
 * the shared map instance.
 */
public class UsageStatistics {

	/**
	 * Logger used for reporting aggregated token usage.
	 */
	private static final Logger logger = LoggerFactory.getLogger(UsageStatistics.class);

	/**
	 * In-memory registry of usage entries grouped by model identifier.
	 */
	private static final Map<String, List<Usage>> modelUsages = new HashMap<>();

	/**
	 * Adds a single {@link Usage} record for a specific model identifier.
	 *
	 * @param modelId the model identifier, for example {@code OpenAI:gpt-4o-mini}
	 * @param usage   the usage entry to record
	 */
	public static void addUsage(String modelId, Usage usage) {
		synchronized (modelUsages) {
			List<Usage> list = modelUsages.get(modelId);
			if (list == null) {
				list = new ArrayList<>();
				modelUsages.put(modelId, list);
			}
			list.add(usage);
		}
	}

	/**
	 * Logs usage summaries for every model currently present in the registry.
	 */
	public static void logUsage() {
		modelUsages.keySet().stream().forEach(UsageStatistics::logUsageForModel);
	}

	/**
	 * Logs a summary of the aggregated {@link Usage} records for a specific model.
	 *
	 * @param modelId the model identifier, for example {@code OpenAI:gpt-4o-mini}
	 */
	public static void logUsageForModel(String modelId) {
		List<Usage> list;
		synchronized (modelUsages) {
			list = modelUsages.get(modelId);
		}
		if (list != null && !list.isEmpty()) {
			long inputTokens = 0;
			long inputCachedTokens = 0;
			long outputTokens = 0;

			for (Usage u : list) {
				inputTokens += u.getInputTokens();
				inputCachedTokens += u.getInputCachedTokens();
				outputTokens += u.getOutputTokens();
			}

			logger.info("Token usage summary for model '{}': Input = {}, Cached = {}, Output = {}.",
					modelId, inputTokens, inputCachedTokens, outputTokens);
		} else {
			logger.info("GenAI token usage information not found for model '{}'.", modelId);
		}
	}

	/**
	 * Returns the aggregated usage entries for a specific model.
	 *
	 * <p>A defensive copy is returned so callers can inspect the recorded values
	 * without modifying the internal registry state.
	 *
	 * @param modelId the model identifier to query
	 * @return a defensive copy of the recorded usage entries for the specified
	 *         model; never {@code null}
	 */
	public static List<Usage> getUsageForModel(String modelId) {
		synchronized (modelUsages) {
			List<Usage> list = modelUsages.get(modelId);
			return list != null ? new ArrayList<>(list) : new ArrayList<>();
		}
	}

	/**
	 * Returns the aggregated usage map for all models.
	 *
	 * <p>The returned map is a shallow copy of the registry. The map instance itself
	 * can be modified by the caller without affecting the registry, but the nested
	 * usage lists remain shared references.
	 *
	 * @return a shallow copy of the model-to-usage-list registry
	 */
	public static Map<String, List<Usage>> getAllModelUsages() {
		synchronized (modelUsages) {
			return new HashMap<>(modelUsages);
		}
	}
}
