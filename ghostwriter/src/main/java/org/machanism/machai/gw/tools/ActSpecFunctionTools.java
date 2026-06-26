package org.machanism.machai.gw.tools;

import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.ActProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides functional tools for episode navigation and control within the
 * ActProcessor context.
 * <p>
 * This class registers tools for moving between episodes and repeating episodes
 * in a project workflow. It is intended for use with {@link ActProcessor} and
 * integrates with the {@link Genai} provider.
 * </p>
 *
 * @author Viktor Tovstyi
 */
@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ActSpecFunctionTools.class);

	/**
	 * Moves to the next episode, or to the episode specified by 'id' or 'name' if
	 * provided.
	 * <p>
	 * This method always throws a {@link MoveToEpisodeException} to signal episode
	 * navigation.
	 * </p>
	 */
	@Tool(name = "move_to_episode", description = "Moves to the next episode, or to the episode specified by 'id' or 'name' if provided. Use this to control "
			+ "episode navigation in the project context.")
	public void moveToEpisode(@Param(name = "id", description = "The ID of the episode to move to.") int targetId,
			@Param(name = "name", description = "The name of the episode to move to.") String name) {
		throw new MoveToEpisodeException(targetId, name);
	}

	/**
	 * Repeats the current episode by terminating the current execution and restarting the same episode,
	 * preserving the context.
	 * <p>
	 * This method can be used to re-execute the current episode, for example, after a validation failure
	 * or when additional user input is required. If a custom message is provided, it is logged before
	 * the episode is repeated.
	 * </p>
	 *
	 * @param message A custom response message to output before repeating the episode. If empty, no message is logged.
	 * @throws RepeatEpisodeException always thrown to signal the episode should be repeated
	 */
	@Tool(name = "repeate_episode", description = "Repeats the current episode. This function terminates the current execution and restarts the same "
			+ "episode, preserving the context.")
	public void repeateEpisode(
			@Param(name = "message", description = "A custom response message to output before repeating the episode.", defaultValue = "") String message) {
		if (!message.isEmpty()) {
			logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, message);
		}
		throw new RepeatEpisodeException();
	}

}