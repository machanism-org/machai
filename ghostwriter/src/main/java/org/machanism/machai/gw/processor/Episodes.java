package org.machanism.machai.gw.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance:
 * Class javadoc description should describe supported functionality and provide examples to use it.
 * If the method used as Javadoc documentation is not public or protected, the method name should not be specified.
 */
/**
 * Maintains an ordered collection of act episode prompts and provides execution
 * helpers that support several playback strategies.
 *
 * <p>
 * Supported functionality:
 * </p>
 * <ul>
 * <li>Holds an ordered list of episode prompts, each optionally starting with a
 * markdown heading ({@code "# Name"}) that names the episode.</li>
 * <li>Executes episodes in their natural order via
 * {@link #regularOrder(Integer, BiFunction)}, honoring repeat requests
 * ({@link RepeatEpisodeException}) and jump/redirect requests
 * ({@link MoveToEpisodeException}).</li>
 * <li>Executes an explicitly selected subset of episodes in the requested order
 * via {@link #requestedOrder(BiFunction)}.</li>
 * <li>Resolves episode indices either by 1-based ID or by heading name when a
 * {@link MoveToEpisodeException} requests a jump to a named episode.</li>
 * <li>Exposes act/episode metadata for reporting via
 * {@link #getActInformation(int)}.</li>
 * </ul>
 *
 * <p>
 * <b>Example: regular order execution</b>
 * </p>
 *
 * <pre>
 * Episodes episodes = new Episodes(actProcessor);
 * episodes.setName("demo-act");
 * episodes.setEpisodes(List.of(
 * 		"# Introduction\nWelcome to the show!",
 * 		"# Recap\nLast time on our show..."));
 *
 * episodes.regularOrder(1, (id, prompt) -&gt; executor.run(id, prompt));
 * </pre>
 *
 * <p>
 * <b>Example: executing only a selected subset</b>
 * </p>
 *
 * <pre>
 * episodes.setSelectedEpisodes(List.of(2));
 * if (!episodes.isRegularOrder()) {
 * 	episodes.requestedOrder((id, prompt) -&gt; executor.run(id, prompt));
 * }
 * </pre>
 *
 */
public class Episodes {
    private static final String HEADER_MARKER = "# ";

    /** Logger for documentation input processing events. */
    private static final Logger logger = LoggerFactory.getLogger(Episodes.class);

    /** Ordered list of act episode prompts to execute. */
    private List<String> episodePrompts = new ArrayList<>();

    /** Explicitly selected 1-based episode identifiers. */
    private List<Integer> selectedEpisodes = new ArrayList<>();

    /** Logical act name associated with the episodes. */
    private String name;

    private ActProcessor actProcessor;

    public Episodes(ActProcessor actProcessor) {
        this.actProcessor = actProcessor;
    }

    /**
     * Sets the list of explicitly requested episode identifiers.
     *
     * @param selectedEpisodeIds 1-based episode identifiers to execute
     * @throws IllegalArgumentException if any identifier is outside the available
     *                                  episode range
     */
    public void setSelectedEpisodes(List<Integer> selectedEpisodeIds) {
        int numberOfEpisodes = episodePrompts.size();
        boolean hasInvalidId = selectedEpisodeIds.stream().anyMatch(id -> id <= 0 || id > numberOfEpisodes);
        if (hasInvalidId) {
            throw new IllegalArgumentException(
                    "All episode IDs must be between 1 and " + numberOfEpisodes + "  (inclusive).");
        }
        this.selectedEpisodes = selectedEpisodeIds;
    }

    private int getEpisodeIdByName(String episodeName) {
        for (int id = 1; id <= episodePrompts.size(); id++) {
            String firstHeaderLine = getEpisodeName(id);
            if (episodeName.equals(firstHeaderLine)) {
                return id;
            }
        }
        throw new EpisodeNotFoundException(episodeName);
    }

    private String getEpisodeName(int episodeId) {
        String episode = StringUtils.trim(episodePrompts.get(episodeId - 1));
        if (Strings.CS.startsWith(episode, "---")) {
            episode = StringUtils.substringAfter(StringUtils.substring(episode, 3), "---").trim();
        }
        String header = episode != null && episode.startsWith(HEADER_MARKER)
                ? StringUtils.substringBetween(episode, HEADER_MARKER, "\n")
                : null;
        return StringUtils.trimToNull(header);
    }

    /**
     * Executes episodes in regular order starting from the supplied 1-based index
     * while honoring repeat and move requests.
     *
     * @param startEpisodeId starting 1-based episode index
     * @param func callback used to execute an episode
     */
    public void regularOrder(Integer startEpisodeId, BiFunction<Integer, String, String> func) {
        Integer moveToEpisodeId = startEpisodeId;
        while (moveToEpisodeId != null) {
            moveToEpisodeId = executeRegularEpisodes(moveToEpisodeId, func);
        }
    }

    private Integer executeRegularEpisodes(int startEpisodeId, BiFunction<Integer, String, String> func) {
        try {
            for (int episodeId = startEpisodeId; episodeId <= episodePrompts.size(); episodeId++) {
                executeEpisodeWithRepeats(episodeId, func);
            }
            return null;
        } catch (MoveToEpisodeException exception) {
            return getEpisodeId(null, exception);
        }
    }

    /**
     * Executes only the explicitly selected episodes in their requested order.
     *
     * @param func callback used to execute an episode
     * @return the last processed episode identifier
     */
    public int requestedOrder(BiFunction<Integer, String, String> func) {
        int episodeId = 0;
        for (Integer selectedEpisodeId : selectedEpisodes) {
            episodeId = selectedEpisodeId;
            executeEpisodeWithRepeats(episodeId, func);
        }
        return episodeId;
    }

    private void executeEpisodeWithRepeats(int episodeId, BiFunction<Integer, String, String> func) {
        int iteration = 1;
        boolean repeat;
        do {
            repeat = !executeEpisode(episodeId, iteration++, func);
        } while (repeat);
    }

    private boolean executeEpisode(int episodeId, int iteration, BiFunction<Integer, String, String> func) {
        try {
            String episode = episodePrompts.get(episodeId - 1);
            logEpisodeHeader(episodeId, iteration, "Start");
            String perform = func.apply(episodeId, episode);
            logEpisodeHeader(episodeId, iteration, "End");
            actProcessor.addResults(perform);
            logResult(perform);
            return true;
        } catch (RepeatEpisodeException exception) {
            return false;
        }
    }

    private void logResult(String perform) {
        if (StringUtils.isNoneBlank(perform)) {
            logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, perform);
        }
    }

    /**
     * Resolves the next episode index from a move request exception.
     *
     * @param requestedEpisodeId current fallback episode index
     * @param exception exception describing the requested move
     * @return resolved 1-based episode index
     */
    public Integer getEpisodeId(Integer requestedEpisodeId, MoveToEpisodeException exception) {
        Integer episodeId = exception.getEpisodeId();
        if (episodeId != null) {
            return episodeId;
        }
        if (exception.getName() != null) {
            return getEpisodeIdByName(exception.getName());
        }
        return requestedEpisodeId;
    }

    private void logEpisodeHeader(int episodeId, int iteration, String msg) {
        if ((episodePrompts.size() > 1 || iteration > 1) && logger.isInfoEnabled()) {
            String iterationLabel = iteration > 1 ? " [Iteration: " + iteration + "]) " : " ";
            String episodeName = getEpisodeName(episodeId);
            String displayName = episodeName == null ? StringUtils.EMPTY : " \"" + episodeName + "\"";
            String title = " " + msg + " Episode #" + episodeId + displayName + iterationLabel + " ";
            logger.info("{}", StringUtils.center(title, GWConstants.LOG_LINE_LENGTH, "-"));
        }
    }

    public void setEpisodes(List<String> episodes) {
        this.episodePrompts = episodes;
    }

    public List<String> getEpisodes() {
        return episodePrompts;
    }

    public boolean isRegularOrder() {
        return selectedEpisodes.isEmpty();
    }

    public int size() {
        return episodePrompts.size();
    }

    public Map<String, Object> getActInformation(int episodeId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> episodesArray = new ArrayList<>();
        for (int id = 1; id <= episodePrompts.size(); id++) {
            Map<String, String> episodeObj = new HashMap<>();
            episodeObj.put("ID", Objects.toString(id));
            episodeObj.put("EPISODE_NAME", getEpisodeName(id));
            episodesArray.add(episodeObj);
        }
        result.put("EPISODES", episodesArray);
        result.put("CURRENT_EPISODE_ID", episodeId);
        result.put("ACT_INFORMATION", episodesArray);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
