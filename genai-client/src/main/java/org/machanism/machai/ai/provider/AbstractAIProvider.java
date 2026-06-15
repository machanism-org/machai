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
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

	/** Maximum length for log lines. */
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

	/** Line separator used when composing prompts. */
	public static final String LINE_SEPARATOR = "\n";

	/** Paragraph separator used when composing prompts. */
	public static final String PARAGRAPH_SEPARATOR = "\n\n";

	/** Prefix for MCP property names. */
	protected static final String MCP_PROP_NAME_PREFIX = "MCP";

	/** Default maximum number of tokens the model may generate. */
	public static final long MAX_OUTPUT_TOKENS = 18000;

	/** Default web search type name. */
	public static final String DEFAULT_WEBSEARCH_TYPE_NAME = "default";

	/** Separator for log sections. */
	public static final String LOG_SECTION_SEPARATOR = PARAGRAPH_SEPARATOR
			+ "-----------------------------------------" + PARAGRAPH_SEPARATOR;

	/** Name of the project directory parameter. */
	public static final String PROJECT_DIR_PARAM_NAME = "project_dir";

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

	/**
	 * Optional instructions applied to the request.
	 */
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
	 * @param model  the model identifier to use
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
		// To be implemented by subclasses if needed
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
		// To be implemented by subclasses if needed
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
			Object apply = tool.apply(params, projectDir, config);
			String result;
			if (apply instanceof String) {
				result = (String) apply;
			} else {
				result = new ObjectMapper().writeValueAsString(apply);
			}
			return result;

		} catch (Exception e) {
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
		// To be implemented by subclasses if needed
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

	/**
	 * Adds a tool to the provider.
	 *
	 * @param name        the tool name
	 * @param description the tool description
	 * @param function    the tool function implementation
	 * @param paramsDesc  descriptors for the tool parameters
	 */
	abstract protected void addTool(String name, String description, ToolFunction function,
			ParamDescriptor... paramsDesc);

	/**
	 * Registers all annotated tool methods from the given {@link FunctionTools}
	 * instance.
	 *
	 * @param tools the tools instance containing annotated methods
	 */
	public void addTool(FunctionTools tools) {
		Class<? extends FunctionTools> toolsClass = tools.getClass();
		Method[] methods = toolsClass.getMethods();
		for (Method method : methods) {
			Tool toolAnnotation = method.getAnnotation(Tool.class);
			if (toolAnnotation != null) {
				String description = toolAnnotation.description();
				String name;
				if (Tool.NULL_VALUE.equals(toolAnnotation.name())) {
					name = method.getName();
				} else {
					name = toolAnnotation.name();
				}

				addTool(tools, method, name, description);
			}
			Prompt promptAnnotation = method.getAnnotation(Prompt.class);
			if (promptAnnotation != null) {
				String description = promptAnnotation.description();
				String name = promptAnnotation.name();
				Role role = promptAnnotation.role();
				addPrompt(tools, method, name, description, role);
			}
		}
	}

	private void addPrompt(FunctionTools tools, Method method, String name, String description, Role role) {
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

		addPrompt(name, description, (props, dir, config) -> {
			try {
				if (logger.isInfoEnabled()) {
					logger.info("Request prompt: `{}`, params: `{}`, projectDir: `{}`", name,
							StringUtils.abbreviate(String.valueOf(props), LOG_LINE_LENG)
									.replace(LINE_SEPARATOR, " ").replace("\r", ""),
							dir);
				}

				Object result = invoke(tools, method, props, dir, config);

				if (logger.isInfoEnabled()) {
					logger.info("Prompt: `{}`, returns: `{}`, projectDir: `{}`",
							name,
							StringUtils.abbreviate(String.valueOf(result), LOG_LINE_LENG)
									.replace(LINE_SEPARATOR, " ").replace("\r", ""),
							dir);
				}

				return result;

			} catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				logger.error("Prompt: `{}`, error: `{}`, projectDir: `{}`", name,
						targetException.getMessage(), dir);
				throw new IllegalArgumentException(targetException);

			} catch (IllegalAccessException | IllegalArgumentException e) {
				logger.error("Prompt: `{}`, exception: `{}`, projectDir: `{}`", name,
						e.getMessage(), dir);
				throw new IllegalArgumentException(e);
			}

		}, role, paramsDesc.toArray(new ParamDescriptor[0]));
	}

	protected void addPrompt(String name, String description, ToolFunction function, Role role,
			ParamDescriptor... paramsDesc) {
	}

	private void addTool(FunctionTools tools, Method method, String name, String description) {
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

		addTool(name, description, (props, dir, config) -> {
			try {
				if (logger.isInfoEnabled()) {
					logger.info("Call function: `{}`, params: `{}`, projectDir: `{}`", name,
							StringUtils.abbreviate(String.valueOf(props), LOG_LINE_LENG)
									.replace(LINE_SEPARATOR, " ").replace("\r", ""),
							dir);
				}

				Object result = invoke(tools, method, props, dir, config);

				if (logger.isInfoEnabled()) {
					logger.info("Tool: `{}`, returns: `{}`, projectDir: `{}`",
							name,
							StringUtils.abbreviate(String.valueOf(result), LOG_LINE_LENG)
									.replace(LINE_SEPARATOR, " ").replace("\r", ""),
							dir);
				}

				return result;

			} catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				logger.error("Tool: `{}`, error: `{}`, projectDir: `{}`", name,
						targetException.getMessage(), dir);
				throw new IllegalArgumentException(targetException);

			} catch (IllegalAccessException | IllegalArgumentException e) {
				logger.error("Tool: `{}`, exception: `{}`, projectDir: `{}`", name,
						e.getMessage(), dir);
				throw new IllegalArgumentException(e);
			}

		}, paramsDesc.toArray(new ParamDescriptor[0]));
	}

	private Object invoke(FunctionTools tools, Method method, JsonNode props, File dir, Configurator config)
			throws JsonProcessingException, JsonMappingException, IllegalAccessException, InvocationTargetException {
		List<Object> args = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();

		Parameter[] params = method.getParameters();
		for (Parameter param : params) {
			Class<?> type = param.getType();
			Param paramAnn = param.getAnnotation(Param.class);
			if (paramAnn != null) {
				String defaultValue = paramAnn.defaultValue();
				if (Param.NULL_VALUE.equals(defaultValue)) {
					defaultValue = null;
				}

				String paramName = paramAnn.name();
				if (PROJECT_DIR_PARAM_NAME.equals(paramName)) {
					if (dir != null) {
						defaultValue = dir.getAbsolutePath();
					}
				}

				defaultValue = StringSubstitutor.replace(defaultValue, map);
				String valueStr = getParamValue(props, paramName, defaultValue);

				Object value = converToType(type, valueStr);

				map.put(paramName, value);
				args.add(value);

			} else {
				Object value = null;
				if (Configurator.class.isAssignableFrom(type)) {
					value = config;
				} else if (File.class.isAssignableFrom(type)) {
					value = dir;
				}
				args.add(value);
			}
		}

		Object result = method.invoke(tools, args.toArray());

		if (result instanceof String) {
			result = StringSubstitutor.replace((String) result, map);
		}

		return result;
	}

	/**
	 * Retrieves the value for a parameter from the given JSON node, or returns the
	 * default value if not present.
	 *
	 * @param props        the JSON node containing parameters
	 * @param paramName    the parameter name
	 * @param defaultValue the default value to use if not present
	 * @return the parameter value as a string
	 */
	protected String getParamValue(JsonNode props, String paramName, String defaultValue) {
		String value;
		if (props.has(paramName)) {
			value = props.get(paramName).asText();
			if (((String) value).isEmpty()) {
				value = props.get(paramName).toString();
			}
		} else {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Mapping from Java types to string representations for parameter descriptors.
	 */
	protected Map<Class<?>, String> typeMap = Collections.unmodifiableMap(new HashMap<Class<?>, String>() {
		{
			put(String.class, "string");
			put(File.class, "string");
			put(Integer.class, "integer");
			put(int.class, "integer");
			put(boolean.class, "boolean");
			put(Boolean.class, "boolean");
		}
	});

	/**
	 * Converts a value to the specified Java type.
	 *
	 * @param type  the target type
	 * @param value the value to convert
	 * @return the converted value
	 * @throws JsonProcessingException if JSON parsing fails
	 * @throws JsonMappingException    if mapping fails
	 */
	protected Object converToType(Class<?> type, Object value) throws JsonProcessingException, JsonMappingException {
		if (value != null) {
			if (File.class.isAssignableFrom(type)) {
				value = new File((String) value);
			} else if (int.class.isAssignableFrom(type)) {
				value = Integer.parseInt((String) value);
			} else if (boolean.class.isAssignableFrom(type)) {
				value = Boolean.parseBoolean((String) value);
			} else if (!String.class.isAssignableFrom(type)) {
				value = new ObjectMapper().readValue((String) value, type);
			}
		}
		return value;
	}

	/**
	 * Returns the current project directory.
	 *
	 * @return the projectDir
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Sets the project directory.
	 *
	 * @param projectDir the projectDir to set
	 */
	public void setProjectDir(File projectDir) {
		this.projectDir = projectDir;
	}

	/**
	 * Sets the prompt text for the provider.
	 *
	 * @param text the prompt text
	 */
	@Override
	public void prompt(String text) {
		// To be implemented by subclasses if needed
	}

	/**
	 * Clears the provider state.
	 */
	@Override
	public void clear() {
		// To be implemented by subclasses if needed
	}

}