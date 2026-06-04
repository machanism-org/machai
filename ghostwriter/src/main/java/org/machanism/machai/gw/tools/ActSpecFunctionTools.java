package org.machanism.machai.gw.tools;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.ActProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides functional tools for episode navigation and control within the ActProcessor context.
 * <p>
 * This class registers tools for moving between episodes and repeating episodes in a project workflow.
 * It is intended for use with {@link ActProcessor} and integrates with the {@link Genai} provider.
 * </p>
 *
 * @author Viktor Tovstyi
 */
@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ActSpecFunctionTools.class);

	/**
	 * Registers episode navigation tools with the specified Genai provider.
	 * <ul>
	 *   <li><b>move_to_episode</b>: Moves to the next episode or to a specified episode by ID or name.</li>
	 *   <li><b>repeate_episode</b>: Repeats the current episode, preserving context.</li>
	 * </ul>
	 *
	 * @param provider the Genai provider to register tools with
	 */
	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"move_to_episode",
				"Moves to the next episode, or to the episode specified by 'id' or 'name' if provided. Use this to control "
						+ "episode navigation in the project context.",
				this::moveToEpisode,
				"id:integer:optional:The ID of the episode to move to.",
				"name:string:optional:The name of the episode to move to.");

		provider.addTool(
				"repeate_episode",
				"Repeats the current episode. This function terminates the current execution and restarts the same "
						+ "episode, preserving the context.",
				this::repeateEpisode,
				"message:string:optional:A custom response message to output before repeating the episode.");
	}

	/**
	 * Moves to the next episode, or to the episode specified by 'id' or 'name' if provided.
	 * <p>
	 * This method always throws a {@link MoveToEpisodeException} to signal episode navigation.
	 * </p>
	 *
	 * @param props      JSON node containing optional 'id' (integer) and 'name' (string) properties
	 * @param projectDir the project directory
	 * @return           never returns normally; always throws {@link MoveToEpisodeException}
	 * @throws MoveToEpisodeException to signal episode navigation
	 */
	public Object moveToEpisode(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Move to episode: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), projectDir);
		}

		Integer targetId = props.has("id") ? props.get("id").asInt() : null;
		String name = props.has("name") ? props.get("name").asText() : null;

		throw new MoveToEpisodeException(targetId, name);
	}

	/**
	 * Repeats the current episode by throwing a {@link RepeatEpisodeException}.
	 * <p>
	 * Optionally logs a custom message if provided in the 'message' property of the input JSON node.
	 * </p>
	 *
	 * @param props      JSON node containing optional 'message' property
	 * @param projectDir the project directory
	 * @return           never returns normally; always throws {@link RepeatEpisodeException}
	 * @throws RepeatEpisodeException to signal episode repetition
	 */
	public Object repeateEpisode(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Repeat episode: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), projectDir);
		}
		if (props != null && props.has("message")) {
			String message = props.get("message").asText();
			if (StringUtils.isNotBlank(message)) {
				logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, message);
			}
		}
		throw new RepeatEpisodeException();
	}

}