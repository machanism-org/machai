package org.machanism.machai.gw.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Episodes {
	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(Episodes.class);

	/** Ordered list of act episode prompts to execute. */
	private List<String> episodes = new ArrayList<>();

	/** Explicitly selected 1-based episode identifiers. */
	private List<Integer> selectedEpisodes = new ArrayList<>();

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
			String firstHeaderLine = StringUtils.trimToEmpty(StringUtils.substringBetween(episodes.get(i), "#", "\n"));
			if (firstHeaderLine != null) {
				if (firstHeaderLine.equals(episodeName)) {
					return i;
				}
			}
		}
		throw new EpisodeNotFoundException(episodeName);
	}

	/**
	 * Executes episodes sequentially, supporting repeat and move operations.
	 * 
	 * @param startId
	 *
	 * @param projectLayout active project layout
	 * @param projectDir    project root directory
	 * @param startId       zero-based starting episode index
	 */
	public void regularOrder(Integer startId, BiFunction<Integer, String, String> func) {
		Integer moveToEpisodeId = null;
		int i = 0;
		do {
			if (moveToEpisodeId != null) {
				startId = moveToEpisodeId;
			}
			try {
				for (i = startId; i < episodes.size(); i++) {
					int iteration = 1;
					boolean repeate;
					do {
						repeate = false;
						try {
							String episode = episodes.get(i);
							logEpisodeHeader(i, iteration++);
							execute(func, i, episode);

						} catch (RepeatEpisodeException e) {
							repeate = true;
						}
					} while (repeate);
				}
			} catch (MoveToEpisodeException e) {
				moveToEpisodeId = getEpisodeId(moveToEpisodeId, e);
			}
		} while (moveToEpisodeId != null);
	}

	public int requestedOrder(BiFunction<Integer, String, String> func) {
		String episodeIdStr = null;
		int i = 0;
		do {
			for (i = 0; i < selectedEpisodes.size(); i++) {
				int iteration = 1;
				boolean repeate;
				do {
					repeate = false;
					try {
						int id = selectedEpisodes.get(i) - 1;
						String episode = episodes.get(id);

						logEpisodeHeader(id, iteration++);
						execute(func, i, episode);
					} catch (RepeatEpisodeException e) {
						repeate = true;
					}
				} while (repeate);
			}
		} while (episodeIdStr != null);

		return i;
	}

	private void execute(BiFunction<Integer, String, String> func, int i, String episode) {
		String perform = func.apply(i, episode);

		if (perform != null) {
			logger.info(">>> {}", perform);
		}
	}

	/**
	 * Resolves the next episode index from a move request exception.
	 *
	 * @param requestedEpisodeId current fallback episode index
	 * @param e                  exception describing the requested move
	 * @return resolved zero-based episode index
	 */
	public Integer getEpisodeId(int requestedEpisodeId, MoveToEpisodeException e) {
		Integer episodeIdStr = e.getEpisodeId();
		if (episodeIdStr != null) {
			requestedEpisodeId = episodeIdStr - 1;
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
			String title = " Episode #" + (episodeId + 1) + iterationLabel;

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

	public boolean isRegularOrder() {
		return selectedEpisodes.isEmpty();
	}

	public int size() {
		return episodes.size();
	}
}
