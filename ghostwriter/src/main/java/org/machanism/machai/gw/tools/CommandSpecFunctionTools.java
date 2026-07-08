package org.machanism.machai.gw.tools;

import java.io.File;

import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides function tools for task and execution control within the
 * {@link AIFileProcessor} context.
 * <p>
 * This class registers tools for terminating execution and ending tasks in a
 * controlled manner. It is intended for use with {@link AIFileProcessor} and
 * integrates with the {@link Genai} provider.
 * </p>
 * 
 * @author Viktor Tovstyi
 */
@SupportedFor({ AIFileProcessor.class })
public class CommandSpecFunctionTools implements FunctionTools {

	private static final String TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE = "Execution terminated by function tool.";

	/**
	 * Terminates the application by throwing a {@link ProcessTerminationException}.
	 * <p>
	 * Reads {@code message} and {@code exitCode} from the supplied {@link JsonNode}
	 * and throws a {@link ProcessTerminationException}. This mechanism allows a
	 * tool invocation to abort the overall workflow with an explicit exit code.
	 * </p>
	 */
	@Tool(name = "terminate_execution", description = "Terminates the application by sending an exit code. This function tool should only be used when explicitly requested by the user.  "
			+ "Do not call this function automatically if task completed successfully.")
	public String terminateExecution(
			@Param(name = "message", description = "The exception message to use.", defaultValue = TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE) String message,
			@Param(name = "exit_code", description = "The exit code to return when terminating the execution. Defaults to 0 if not specified.", defaultValue = "0") int exitCode,
			@Param(name = "project_dir", description = "The project dir.") File projectDir) {
		throw new ProcessTerminationException(message, exitCode);
	}

	/**
	 * Completes the current task by throwing an {@link EndTaskException}.
	 * <p>
	 * This method is intended to be used as a function tool for terminating a
	 * process when requested by the user or dictated by process logic. It logs the
	 * task completion and uses a custom message if provided in the properties.
	 * </p>
	 */
	@Tool(name = "end_task", description = "Use this function if the user has requested to `end the task`. Ends the current task without terminating the application. "
			+ "Use this function to conclude an interactive session with the user, ensuring that only the current task is finished while the application remains active. "
			+ "This tool is ideal for gracefully completing user-driven tasks in interactive mode, "
			+ "allowing further operations or tasks to continue.")
	public String endTask(
			@Param(name = "message", description = "The message to use upon completion.", defaultValue = TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE) String message) {
		throw new EndTaskException(message);
	}

}