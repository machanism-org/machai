package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.Tool;

public abstract class AbstractAIProvider implements Genai {

	/** Logger instance for this provider. */
	static Logger logger = LoggerFactory.getLogger(AbstractAIProvider.class);

	protected static final String MCP_PROP_NAME_PREFIX = "MCP";
	/** Default maximum number of tokens the model may generate. */
	public static final long MAX_OUTPUT_TOKENS = 18000;
	/** Active model identifier used in {@link #perform()}. */
	protected String chatModel;
	/** Optional log file for input data. */
	private File inputsLog;
	/** Working directory passed to tool handlers as contextual information. */
	protected File workingDir;
	/** Request timeout in seconds; {@code 0} means SDK defaults are used. */
	protected long timeoutSec;
	/**
	 * Latest usage metrics captured from the most recent {@link #perform()} call.
	 */
	private Usage lastUsage = new Usage(0, 0, 0);
	/** Optional instructions applied to the request. */
	protected String instructions;
	/** Maximum number of output tokens for responses. */
	protected Long maxOutputTokens;
	/** Maximum number of tool calls permitted per response. */
	protected Long maxToolCalls;
	/** Configuration source used to initialize clients and provider features. */
	protected Configurator config;
	/** Embedding model identifier used by {@link #embedding(String, long)}. */
	protected String embeddingModel;

	public AbstractAIProvider() {
		super();
	}

	/**
	 * Initializes the provider from the given configuration.
	 *
	 * @param config provider configuration source
	 */
	@Override
	public void init(Configurator config) {
		this.config = config;
		chatModel = config.get("chatModel");

		maxOutputTokens = config.getLong("MAX_OUTPUT_TOKENS", MAX_OUTPUT_TOKENS);
		maxToolCalls = config.getLong("MAX_TOOL_CALLS", 0L);
		embeddingModel = config.get("embedding.model", null);

		addWebSearch();
		addMcpServer();
	}

	protected abstract void addMcpServer();

	protected abstract void addWebSearch();

	/**
	 * Extracts token usage from the response and stores it as {@link #usage()}.
	 *
	 * @param usage optional usage information from the OpenAI response
	 */
	protected void captureUsage(Optional<ResponseUsage> usage) {
		if (usage.isPresent()) {
			ResponseUsage responseUsage = usage.get();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = responseUsage.inputTokensDetails().cachedTokens();
			long outputTokens = responseUsage.outputTokens();

			lastUsage = new Usage(inputTokens, inputCachedTokens, outputTokens);
			UsageStatistics.addUsage(chatModel, lastUsage);
		} else {
			lastUsage = new Usage(0, 0, 0);
		}
	}

	/**
	 * Normalizes a string for case-insensitive comparisons.
	 *
	 * @param value source value
	 * @return lower-cased value, or an empty string when the input is {@code null}
	 */
	protected String normalize(String value) {
		return StringUtils.defaultString(value).toLowerCase(Locale.ROOT);
	}

	/**
	 * Safely invokes a tool function and converts {@link IOException}s into a
	 * textual error payload suitable for the model conversation.
	 *
	 * @param name       tool name
	 * @param tool       tool handler
	 * @param params     parsed tool parameters
	 * @param workingDir working directory passed to the tool
	 * @return tool output or a formatted error message
	 */
	protected Object safelyInvokeTool(String name, ToolFunction tool, JsonNode params, File workingDir) {
		try {
			return tool.apply(params, workingDir);
		} catch (IOException e) {
			String errMsg = "Error: The functional tool call failed while executing '" + name + "'. Reason: "
					+ e.getMessage();
			logger.error(errMsg);
			return errMsg;
		}
	}

	/**
	 * Writes the current request inputs to {@link #inputsLog} when logging is
	 * enabled.
	 */
	protected void logInputs() {
		if (inputsLog != null) {
			File parentFile = inputsLog.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				parentFile.mkdirs();
			}
			try (Writer streamWriter = new FileWriter(inputsLog, false)) {
				logInputs(streamWriter);
			} catch (IOException e) {
				logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
			}
		}
	}

	/**
	 * Serializes the current instructions and input items to the supplied writer.
	 *
	 * @param streamWriter destination writer
	 * @throws IOException when writing fails
	 */
	private void logInputs(Writer streamWriter) throws IOException {
		streamWriter.write(StringUtils.defaultString(instructions));
		streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		streamWriter.write("-----------------------------------------");
		streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		logInputsSpec(streamWriter);
		logger.debug("LLM Inputs: {}", inputsLog);
	}

	protected abstract void logInputsSpec(Writer streamWriter) throws IOException;

	/**
	 * Registers a function tool for the current provider instance.
	 *
	 * <p>
	 * The {@code paramsDesc} entries must follow the format
	 * {@code name:type:required:description}. The parameter schema passed to OpenAI
	 * is a JSON Schema object of type {@code object}.
	 * </p>
	 *
	 * @param name        tool function name
	 * @param description tool description
	 * @param function    handler callback for tool execution
	 * @param paramsDesc  parameter descriptors in the format
	 *                    {@code name:type:required:description}
	 */
	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode requiredProps = mapper.createArrayNode();
		if (paramsDesc != null) {
			for (String pDesc : paramsDesc) {
				String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
				if (desc.length >= 3 && isRequiredParameter(desc[2])) {
					requiredProps.add(desc[0]);
				}
				Map<String, String> value = new HashMap<>();
				value.put("type", desc[1]);
				value.put("description", desc.length > 3 ? desc[3] : StringUtils.EMPTY);
				fromValue.put(desc[0], value);
			}
		}

		addTool(name, description, fromValue, mapper, requiredProps, function);
	}

	protected abstract void addTool(String name, String description, Map<String, Map<String, String>> fromValue,
			ObjectMapper mapper, ArrayNode requiredProps, ToolFunction function);

	/**
	 * Determines whether the supplied parameter flag marks a required parameter.
	 *
	 * @param parameterFlag descriptor flag value
	 * @return {@code true} when the flag equals {@code required}, ignoring case
	 */
	boolean isRequiredParameter(String parameterFlag) {
		// Sonar java:S1874: avoid deprecated StringUtils equality helpers.
		return normalize(parameterFlag).equals("required");
	}

	/**
	 * Compares a requested tool name with a registered tool name using normalized,
	 * case-insensitive matching.
	 *
	 * @param toolName requested tool name
	 * @param tool     registered tool
	 * @return {@code true} when both names match after normalization
	 */
	boolean hasSameToolName(String toolName, Tool tool) {
		// Sonar java:S1874: avoid deprecated StringUtils equality helpers.
		return normalize(toolName).equals(normalize(tool.asFunction().name()));
	}

	/**
	 * Sets system-level instructions applied to subsequent requests.
	 *
	 * @param instructions instruction text, or {@code null} to clear
	 */
	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Enables request input logging to the given file.
	 *
	 * @param inputsLog file for input logging, or {@code null} to disable
	 */
	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	/**
	 * Sets the working directory passed to tool handlers.
	 *
	 * @param workingDir working directory, or {@code null}
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Returns token usage metrics captured from the most recent {@link #perform()}
	 * call.
	 *
	 * @return usage metrics; never {@code null}
	 */
	@Override
	public Usage usage() {
		return lastUsage;
	}

	/**
	 * Returns the configured request timeout.
	 *
	 * @return timeout in seconds; {@code 0} indicates the SDK default
	 */
	public long getTimeout() {
		return timeoutSec;
	}

	/**
	 * Sets a request timeout in seconds for new clients created by this provider.
	 *
	 * @param timeout timeout in seconds; use {@code 0} to use SDK defaults
	 */
	public void setTimeout(long timeout, OpenAIProvider openAIProvider) {
		this.timeoutSec = timeout;
	}

}