package org.machanism.machai.ai.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;
import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base implementation of the {@link Genai} contract shared by concrete provider
 * integrations.
 *
 * <p>
 * This class centralizes common configuration handling, request input logging,
 * tool invocation safety, MCP/web-search bootstrap logic, and usage accounting
 * state used by subclasses such as OpenAI- and Claude-based providers.
 * </p>
 */
public abstract class AbstractAIProvider implements Genai {

	/** Logger instance for this provider. */
	static Logger logger = LoggerFactory.getLogger(AbstractAIProvider.class);

	public static final int LOG_LINE_LENG = 160;

	/**
	 * Configuration property name indicating whether provider inputs should be
	 * logged.
	 */
	public static final String LOG_INPUTS_PROP_NAME = "logInputs";

	/**
	 * Configuration property name for the target GenAI server identifier.
	 */
	public static final String SERVERID_PROP_NAME = "genai.serverId";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	public static final String USERNAME_PROP_NAME = "GENAI_USERNAME";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	public static final String PASSWORD_PROP_NAME = "GENAI_PASSWORD";

	/**
	 * Line separator used when composing prompts.
	 */
	public static final String LINE_SEPARATOR = "\n";

	/**
	 * Paragraph separator used when composing prompts.
	 */
	protected static final String PARAGRAPH_SEPARATOR = "\n\n";

	protected static final String MCP_PROP_NAME_PREFIX = "MCP";
	/** Default maximum number of tokens the model may generate. */
	public static final long MAX_OUTPUT_TOKENS = 18000;

	public static final String DEFAULT_WEBSEARCH_TYPE_NAME = "default";

	public static final String LOG_SECTION_SEPARATOR = PARAGRAPH_SEPARATOR
			+ "-----------------------------------------" + PARAGRAPH_SEPARATOR;

	public static final String PROJECT_DIR_PARAM_NAME = "projectDir";

	Map<Class<?>, String> typeMap = Collections.unmodifiableMap(new HashMap<Class<?>, String>() {
		{
			put(String.class, "string");
			put(File.class, "string");
			put(Integer.class, "integer");
			put(int.class, "integer");
			put(boolean.class, "boolean");
			put(Boolean.class, "boolean");
		}
	});

	/** Active model identifier used in {@link #perform()}. */
	protected String chatModel;
	/** Optional log file for input data. */
	private File inputsLog;
	/** Working directory passed to tool handlers as contextual information. */
	protected File projectDir;
	/** Request timeout in seconds; {@code 0} means SDK defaults are used. */
	protected Long timeoutSec;
	/**
	 * Latest usage metrics captured from the most recent {@link #perform()} call.
	 */
	protected Usage lastUsage = new Usage(0, 0, 0);
	/** Optional instructions applied to the request. */
	protected String instructions;
	/** Maximum number of output tokens for responses. */
	protected Long maxOutputTokens;
	/** Maximum number of tool calls permitted per response. */
	protected Long maxToolCalls;
	/** Configuration source used to initialize clients and provider features. */
	protected Configurator config;

	/**
	 * Creates a provider base instance.
	 *
	 * <p>
	 * Subclasses are expected to complete initialization in
	 * {@link #init(String, Configurator)}.
	 * </p>
	 */
	public AbstractAIProvider() {
		super();
	}

	/**
	 * Initializes the provider from the given configuration.
	 * 
	 * @param config provider configuration source
	 */
	@Override
	public void init(String model, Configurator config) {
		this.config = config;
		chatModel = model;

		maxOutputTokens = config.getLong("MAX_OUTPUT_TOKENS", MAX_OUTPUT_TOKENS);
		maxToolCalls = config.getLong("MAX_TOOL_CALLS", 0L);

		addWebSearch();
		addMcpServers();
	}

	/**
	 * Reads sequential MCP server configuration groups and registers them with the
	 * concrete provider implementation.
	 *
	 * <p>
	 * The method looks for configuration keys named {@code MCP.*}, then
	 * {@code MCP_1.*}, {@code MCP_2.*}, and so on until no further URL is found.
	 * </p>
	 */
	protected void addMcpServers() {
		int i = 0;
		String url = null;
		do {
			String id = "";

			if (i > 0) {
				id = "_" + i;
			}

			String propName = MCP_PROP_NAME_PREFIX + id;
			url = config.get(propName + ".url", null);
			String name = config.get(propName + ".name", null);
			String authorization = config.get(propName + ".authorization", null);
			String description = config.get(propName + ".description", null);

			if (name != null) {
				addMcpServer(name, url, authorization, description);
			}

		} while (i++ == 0 || url != null);
	}

	/**
	 * Registers one MCP server/tool with the underlying provider SDK.
	 *
	 * @param label         provider-visible MCP server label
	 * @param url           server endpoint URL
	 * @param authorization optional authorization token/value
	 * @param description   optional human-readable description
	 */
	protected void addMcpServer(String label, String url, String authorization, String description) {
	}

	/**
	 * Registers a web-search capability when enabled in configuration.
	 *
	 * <p>
	 * The default implementation reads configuration values and delegates the
	 * actual SDK-specific registration to
	 * {@link #addWebSearch(String, String, String, String)}.
	 * </p>
	 */
	protected void addWebSearch() {
		String type = config.get("WebSearchTool.type", null);
		String city = config.get("WebSearchTool.city", null);
		String country = config.get("WebSearchTool.country", null);
		String region = config.get("WebSearchTool.region", null);

		if (type != null) {
			addWebSearch(type, city, country, region);
		}
	}

	/**
	 * Registers a provider-specific web-search tool.
	 *
	 * @param type    provider-specific web-search tool type/version
	 * @param city    optional user city
	 * @param country optional user country
	 * @param region  optional user region
	 */
	protected void addWebSearch(String type, String city, String country, String region) {
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
	 * @param projectDir working directory passed to the tool
	 * @return tool output or a formatted error message
	 */
	protected String safelyInvokeTool(String name, ToolFunction tool, JsonNode params, File projectDir) {
		try {
			Object apply = tool.apply(params, projectDir);
			String result;
			if (apply instanceof String) {
				result = (String) apply;
			} else {
				result = new ObjectMapper().writeValueAsString(apply);
			}
			return result;

		} catch (IOException e) {
			String errMsg = "Error: The functional tool call failed while executing '" + name + "'. Reason: "
					+ e.getMessage();
			logger.error(errMsg);
			logger.debug(errMsg, e);
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
		streamWriter.write(PARAGRAPH_SEPARATOR);
		streamWriter.write("-----------------------------------------");
		streamWriter.write(PARAGRAPH_SEPARATOR);
		logInputsSpec(streamWriter);
		logger.debug("LLM Inputs: {}", inputsLog);
	}

	/**
	 * Writes provider-specific input items to the supplied log writer.
	 *
	 * @param streamWriter destination writer
	 * @throws IOException when writing fails
	 */
	protected void logInputsSpec(Writer streamWriter) throws IOException {
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
	 * @param projectDir working directory, or {@code null}
	 */
	public void setWorkingDir(File projectDir) {
		this.projectDir = projectDir;
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
	 * Sets the timeout value used by provider client creation.
	 *
	 * <p>
	 * The second parameter is unused and retained only for API compatibility.
	 * </p>
	 *
	 * @param timeout        timeout in seconds; use {@code 0} to use SDK defaults
	 * @param openAIProvider ignored compatibility parameter
	 */
	public void setTimeout(long timeout, OpenAIProvider openAIProvider) {
		this.timeoutSec = timeout;
	}

	abstract protected void addTool(String name, String description, ToolFunction function,
			ParamDescriptor... paramsDesc);

	public void addTool(FunctionTools tools) {
		Class<? extends FunctionTools> toolsClass = tools.getClass();
		Method[] methods = toolsClass.getMethods();
		for (Method method : methods) {
			Function annotation = method.getAnnotation(Function.class);
			if (annotation != null) {
				String description = annotation.description();
				String name = annotation.name();

				List<ParamDescriptor> paramsDesc = new ArrayList<>();

				Parameter[] parameters = method.getParameters();
				for (Parameter param : parameters) {
					Param paramAnn = param.getAnnotation(Param.class);
					if (paramAnn != null) {
						String paramName = paramAnn.name();

						if (!PROJECT_DIR_PARAM_NAME.equals(paramName) || projectDir == null) {
							Class<?> type = param.getType();
							String defaultValue = paramAnn.defaultValue();
							boolean required = defaultValue.equals(Param.NULL_VALUE);

							String typeStr = typeMap.get(type);
							ParamDescriptor paramDescription = new ParamDescriptor(paramName, typeStr, required,
									paramAnn.description());
							paramsDesc.add(paramDescription);
						}
					}
				}

				addTool(name, description, (props, dir) -> {
					try {
						if (logger.isInfoEnabled()) {
							logger.info("Call function: {}, {}", StringUtils.abbreviate(String.valueOf(props), LOG_LINE_LENG)
									.replace(LINE_SEPARATOR, " ").replace("\r", ""), dir);
						}

						List<Object> args = new ArrayList<>();

						Parameter[] params = method.getParameters();
						for (Parameter param : params) {
							Param paramAnn = param.getAnnotation(Param.class);
							if (paramAnn != null) {
								String defaultValue = paramAnn.defaultValue();

								Class<?> type = param.getType();

								String paramName = paramAnn.name();
								if (PROJECT_DIR_PARAM_NAME.equals(paramName)) {
									if (dir != null) {
										defaultValue = dir.getAbsolutePath();
									}
								}

								Object value = null;

								if (props.has(paramName)) {
									value = props.get(paramName).toString();
								}

								if (value == null) {
									value = defaultValue;
								}

								if (String.class.isAssignableFrom(type)) {
									value = props.get(paramName).asText(defaultValue);
								} else if (File.class.isAssignableFrom(type)) {
									value = new File(props.get(paramName).asText(defaultValue));
								} else if (int.class.isAssignableFrom(type)) {
									value = Integer.parseInt(props.get(paramName).asText(defaultValue));
								} else if (boolean.class.isAssignableFrom(type)) {
									value = Boolean.parseBoolean(props.get(paramName).asText(defaultValue));
								} else {
									value = new ObjectMapper().readValue(props.get(paramName).toString(), type);
								}

								args.add(value);
							}
						}

						Object result = method.invoke(tools, args.toArray());
						if (logger.isInfoEnabled()) {
							logger.info("Function returns: {}, {}",
									StringUtils.abbreviate(String.valueOf(result), LOG_LINE_LENG)
											.replace(LINE_SEPARATOR, " ").replace("\r", ""),
									dir);
						}

						return result;
					} catch (InvocationTargetException e) {
						throw new IllegalArgumentException(e.getTargetException());
					} catch (IllegalAccessException | IllegalArgumentException e) {
						throw new IllegalArgumentException(e);
					}
				}, paramsDesc.toArray(new ParamDescriptor[0]));
			}
		}

	}

	/**
	 * @return the projectDir
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * @param projectDir the projectDir to set
	 */
	public void setProjectDir(File projectDir) {
		this.projectDir = projectDir;
	}

	@Override
	public void prompt(String text) {
	}

	@Override
	public void clear() {
	}

}
