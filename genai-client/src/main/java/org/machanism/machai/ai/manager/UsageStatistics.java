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
 * <p>
 * Usage entries are grouped by model identifier and can later be logged or
 * queried programmatically. All access to the internal storage is synchronized on
 * the shared map instance.
 * </p>
 */
public class UsageStatistics {

	private static final Logger logger = LoggerFactory.getLogger(UsageStatistics.class);

	private static final Map<String, List<Usage>> modelUsages = new HashMap<>();

	/**
	 * Adds a single {@link Usage} record for a specific model identifier.
	 *
	 * @param modelId model identifier (e.g., "OpenAI:gpt-4o-mini")
	 * @param usage usage to add
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
	 * @param modelId model identifier (e.g., "OpenAI:gpt-4o-mini")
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
	 * Returns the aggregated usage list for a specific model.
	 *
	 * @param modelId model identifier
	 * @return defensive copy of the recorded usage entries for the model; never
	 *         {@code null}
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
	 * @return shallow copy of the model-to-usage-list map
	 */
	public static Map<String, List<Usage>> getAllModelUsages() {
		synchronized (modelUsages) {
			return new HashMap<>(modelUsages);
		}
	}
}
