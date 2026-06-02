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

@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ActSpecFunctionTools.class);

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
	 * Moves to the next episode, or to the episode specified by 'id' if provided.
	 * 
	 * @param params The first argument is expected to be a JsonNode containing an
	 *               optional 'id' property. The second argument is a File
	 *               representing the project directory.
	 * @return Never returns normally; always throws MoveToEpisodeException.
	 */
	public Object moveToEpisode(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Move to episode: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}

		Integer targetId = props.has("id") ? props.get("id").asInt() : null;
		String name = props.has("name") ? props.get("name").asText() : null;

		throw new MoveToEpisodeException(targetId, name);
	}

	/**
	 * Repeats the current episode by throwing a RepeatEpisodeException.
	 * 
	 * @param props      The first argument is expected to be a JsonNode (can be
	 *                   empty or contain context).
	 * @param workingDir The project directory.
	 * @return Never returns normally; always throws RepeatEpisodeException.
	 */
	public Object repeateEpisode(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Repeat episode: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
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
