package org.machanism.machai.gw.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains ordered act episodes and provides execution helpers for normal,
 * selected, repeated, and redirected episode flow.
 */
public class Episodes {
	private static final String HEADER_MARKER = "# ";

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(Episodes.class);

	/** Ordered list of act episode prompts to execute. */
	private List<String> episodes = new ArrayList<>();

	/** Explicitly selected 1-based episode identifiers. */
	private List<Integer> selectedEpisodes = new ArrayList<>();

	/** Logical act name associated with the episodes. */
	private String name;

	/**
	 * Sets the list of explicitly requested episode identifiers.
	 *
	 * @param selectedEpisodeIds 1-based episode identifiers to execute
	 * @throws IllegalArgumentException if any identifier is outside the available
	 *                                  episode range
	 */
	public void setSelectedEpisodes(List<Integer> selectedEpisodeIds) {
		int numberOfEpisodes = episodes.size();
		boolean hasInvalidId = selectedEpisodeIds.stream().anyMatch(id -> id <= 0 || id > numberOfEpisodes);
		if (hasInvalidId) {
			throw new IllegalArgumentException(
					"All episode IDs must be between 1 and " + numberOfEpisodes + "  (inclusive).");
		}

		this.selectedEpisodes = selectedEpisodeIds;
	}

	/**
	 * Returns the zero-based index of the episode whose prompt text contains a
	 * heading that matches the specified episode name.
	 * <p>
	 * The method scans each episode's prompt text, extracts the first line that
	 * appears between the heading marker "# " and the next newline character, trims
	 * any leading or trailing whitespace, and compares it to the provided
	 * {@code episodeName}. If a match is found, the corresponding episode index is
	 * returned.
	 * </p>
	 *
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre>
	 * episodes.get(0): "# Introduction\nWelcome to the show!"
	 * episodes.get(1): "# Recap\nLast time on our show..."
	 *
	 * getEpisodeIdByName("Recap") returns 1
	 * getEpisodeIdByName("Introduction") returns 0
	 * </pre>
	 *
	 * @param episodeName the heading text to match (e.g., "Recap")
	 * @return the zero-based index of the matching episode
	 * @throws EpisodeNotFoundException if no episode with the specified heading
	 *                                  exists
	 */
	public int getEpisodeIdByName(String episodeName) {
		for (int i = 0; i < episodes.size(); i++) {
			String firstHeaderLine = getEpisodeName(i);
			if (firstHeaderLine != null) {
				if (firstHeaderLine.equals(episodeName)) {
					return i;
				}
			}
		}
		throw new EpisodeNotFoundException(episodeName);
	}

	/**
	 * Extracts and returns the episode name (heading) from the prompt text of the
	 * episode at the specified index.
	 * <p>
	 * The method retrieves the episode prompt at index {@code i}, extracts the
	 * substring between the first occurrence of the heading marker "#" and the next
	 * newline character, and trims any leading or trailing whitespace. If the
	 * extracted heading is empty after trimming, {@code null} is returned.
	 * </p>
	 *
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre>
	 * episodes.get(0): "# Introduction\nWelcome to the show!"
	 * getEpisodeName(0) returns "Introduction"
	 * </pre>
	 *
	 * @param episodeId the zero-based index of the episode
	 * @return the trimmed episode name, or {@code null} if no heading is found or
	 *         the heading is empty
	 */
	public String getEpisodeName(int episodeId) {
		String episode = StringUtils.trim(episodes.get(episodeId - 1));
		String header = null;
		if (episode != null && episode.startsWith(HEADER_MARKER)) {
			header = StringUtils.substringBetween(episode, HEADER_MARKER, "\n");
		}
		return StringUtils.trimToNull(header);
	}

	/**
	 * Executes episodes in regular order starting from the supplied zero-based
	 * index while honoring repeat and move requests.
	 *
	 * @param startEpisodeId starting zero-based episode index
	 * @param func    callback used to execute an episode
	 */
	public void regularOrder(Integer startEpisodeId, BiFunction<Integer, String, String> func) {
		Integer moveToEpisodeId = null;
		int episodeId = 0;
		do {
			if (moveToEpisodeId != null) {
				startEpisodeId = moveToEpisodeId;
			}
			try {
				for (episodeId = startEpisodeId; episodeId <= episodes.size(); episodeId++) {
					int iteration = 1;
					boolean repeate;
					do {
						repeate = false;
						try {
							String episode = episodes.get(episodeId - 1);
							logEpisodeHeader(episodeId, iteration++);
							execute(func, episodeId, episode);

						} catch (RepeatEpisodeException e) {
							repeate = true;
						}
					} while (repeate);
				}
				moveToEpisodeId = null;

			} catch (MoveToEpisodeException e) {
				moveToEpisodeId = getEpisodeId(moveToEpisodeId, e);
			}
		} while (moveToEpisodeId != null);
	}

	/**
	 * Executes only the explicitly selected episodes in their requested order.
	 *
	 * @param func callback used to execute an episode
	 * @return the last processed selection index
	 */
	public int requestedOrder(BiFunction<Integer, String, String> func) {
		String episodeIdStr = null;
		int episodeId = 0;
		do {
			for (int i = 0; i < selectedEpisodes.size(); i++) {
				int iteration = 1;
				boolean repeate;
				do {
					repeate = false;
					try {
						episodeId = selectedEpisodes.get(i);
						String episode = episodes.get(episodeId - 1);

						logEpisodeHeader(episodeId, iteration++);
						execute(func, episodeId, episode);
					} catch (RepeatEpisodeException e) {
						repeate = true;
					}
				} while (repeate);
			}
		} while (episodeIdStr != null);

		return episodeId;
	}

	/**
	 * Executes a single episode callback and logs any returned output.
	 *
	 * @param func    callback used to execute an episode
	 * @param i       zero-based episode index supplied to the callback
	 * @param episode episode prompt text
	 */
	private void execute(BiFunction<Integer, String, String> func, int i, String episode) {
		String perform = func.apply(i, episode);

		if (perform != null) {
			logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, perform);
		}
	}

	/**
	 * Resolves the next episode index from a move request exception.
	 *
	 * @param requestedEpisodeId current fallback episode index
	 * @param e                  exception describing the requested move
	 * @return resolved zero-based episode index
	 */
	public Integer getEpisodeId(Integer requestedEpisodeId, MoveToEpisodeException e) {
		Integer episodeIdStr = e.getEpisodeId();
		if (episodeIdStr != null) {
			requestedEpisodeId = episodeIdStr;
		} else if (e.getName() != null) {
			requestedEpisodeId = getEpisodeIdByName(e.getName());
		}
		return requestedEpisodeId;
	}

	/**
	 * Logs a formatted episode banner for the current execution step.
	 *
	 * @param episodeId zero-based episode index
	 * @param iteration current iteration number for the same episode
	 */
	private void logEpisodeHeader(int episodeId, int iteration) {
		if ((episodes.size() > 1 || iteration > 1) && logger.isInfoEnabled()) {
			String iterationLabel = iteration > 1 ? " [Iteration: " + iteration + "]) " : " ";
			String episodeName = getEpisodeName(episodeId);
			if (episodeName != null) {
				episodeName = " \"" + episodeName + "\"";
			} else {
				episodeName = StringUtils.EMPTY;
			}

			String title = " Episode #" + episodeId + episodeName + iterationLabel;

			logger.info("{}", StringUtils.center(title, 80, "-"));
		}
	}

	/**
	 * Replaces the current ordered episode list.
	 *
	 * @param episodes episode prompts to execute
	 */
	public void setEpisodes(List<String> episodes) {
		this.episodes = episodes;
	}

	/**
	 * Returns the configured episode prompts.
	 *
	 * @return configured episode prompt list
	 */
	public List<String> getEpisodes() {
		return episodes;
	}

	/**
	 * Returns whether no explicit episode subset has been selected.
	 *
	 * @return {@code true} when regular order should be used
	 */
	public boolean isRegularOrder() {
		return selectedEpisodes.isEmpty();
	}

	/**
	 * Returns the number of configured episodes.
	 *
	 * @return episode count
	 */
	public int size() {
		return episodes.size();
	}

	/**
	 * Builds act metadata that is prepended to an episode prompt.
	 *
	 * @param episodeId zero-based current episode index
	 * @return formatted act metadata block
	 */
	public String getEpisodeInformation(int episodeId) {
		StringBuilder promptBuilder = new StringBuilder("# Act Information\n\n");
		promptBuilder.append("- Act Name: `" + getName() + "`\n");

		if (!episodes.isEmpty()) {
			promptBuilder.append("Episodes:\n\n");
			promptBuilder.append("| ID | EPISODE NAME |\n");
			promptBuilder.append("|----|--------------|\n");
			for (int i = 1; i <= episodes.size(); i++) {
				promptBuilder.append("| " + (i + 1) + " | "
						+ StringUtils.defaultIfBlank(getEpisodeName(i), "<not defined>") + " |\n");
			}

			promptBuilder.append("\n- Current Episode Id: " + (episodeId + 1) + "\n\n");
			promptBuilder.append("---\n\n");
		}
		return promptBuilder.toString();
	}

	/**
	 * Returns the act name associated with these episodes.
	 *
	 * @return act name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the act name associated with these episodes.
	 *
	 * @param name act name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
