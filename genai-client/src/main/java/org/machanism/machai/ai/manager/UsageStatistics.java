package org.machanism.machai.ai.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages and aggregates usage statistics for GenAI models.
 */
public class UsageStatistics {

	private static final Logger logger = LoggerFactory.getLogger(UsageStatistics.class);

	private static final Map<String, List<Usage>> modelUsages = new HashMap<>();

	/**
	 * Adds a single {@link Usage} record for a specific model identifier.
	 *
	 * @param modelId model identifier (e.g., "OpenAI:gpt-4o-mini")
	 * @param usage   usage to add
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
	 * Logs a summary of the aggregated {@link Usage} records (all models).
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
	 * @return list of Usage records, or empty list if none
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
	 * @return map of modelId to list of Usage records
	 */
	public static Map<String, List<Usage>> getAllModelUsages() {
		synchronized (modelUsages) {
			return new HashMap<>(modelUsages);
		}
	}
}