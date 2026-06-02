package org.machanism.machai.gw.tools;

import java.io.File;

import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

@SupportedFor({ AIFileProcessor.class })
public class CommandSpecFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandSpecFunctionTools.class);

	private static final String TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE = "Execution terminated by function tool.";

	public void applyTools(Genai provider) {
		provider.addTool(
				"terminate_execution",
				"Terminates the application by sending an exit code. This function tool should only be used when explicitly requested by the user.  "
						+ "Do not call this function automatically if task completed successfully.",
				this::terminateExecution,
				"message:string:optional:The exception message to use. Defaults to '"
						+ TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE + "'",
				"exitCode:integer:optional:The exit code to return when terminating the execution. Defaults to 0 if not specified.");
		provider.addTool(
				"end_task",
				"Use this function if the user has requested to `end the task`. Ends the current task without terminating the application. "
						+ "Use this function to conclude an interactive session with the user, ensuring that only the current task is finished while the application remains active. "
						+ "This tool is ideal for gracefully completing user-driven tasks in interactive mode, "
						+ "allowing further operations or tasks to continue.",
				this::endTask,
				"message:string:optional:The message to use upon completion.");
	}

	/**
	 * Implements the {@code terminate_task} tool.
	 *
	 * <p>
	 * Reads {@code message}, {@code cause}, and {@code exitCode} from the supplied
	 * {@link JsonNode} and throws a {@link ProcessTerminationException}. This
	 * mechanism allows a tool invocation to abort the overall workflow with an
	 * explicit exit code.
	 * </p>
	 *
	 * @param params tool invocation parameters (expects a single {@link JsonNode}
	 *               argument)
	 * @return never returns; always throws
	 * @throws ProcessTerminationException always thrown to terminate execution
	 */
	public String terminateExecution(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Terminate the task: {}, {}", props, projectDir);
		}

		String message = props.has("message") ? props.get("message").asText(TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE)
				: TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE;
		int exitCode = props.has("exitCode") ? props.get("exitCode").asInt(0) : 0;

		throw new ProcessTerminationException(message, exitCode);
	}

	/**
	 * Completes the current task by throwing a {@link EndTaskException} exception.
	 * <p>
	 * This method is intended to be used as a function tool for terminating a
	 * process when requested by the user or dictated by process logic. It logs the
	 * task completion and uses a custom message if provided in the properties.
	 * </p>
	 *
	 * @param props      JSON node containing optional properties, such as a custom
	 *                   completion message.
	 * @param projectDir The project directory associated with the task.
	 * @return This method does not return normally; it always throws
	 *         {@link EndTaskException}.
	 * @throws EndTaskException Always thrown to signal task completion.
	 */
	public String endTask(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Ending task: {}, {}", props, projectDir);
		}

		String message = TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE;
		if (props != null && props.has("message")) {
			message = props.get("message").asText(null);
		}

		throw new EndTaskException(message);
	}

}
