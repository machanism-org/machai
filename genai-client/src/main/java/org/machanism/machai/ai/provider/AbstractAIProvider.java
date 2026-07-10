package org.machanism.machai.ai.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.impl.OpenAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Resource;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.SpecialException;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JacksonException;
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
	 * Optional instructions applied to the request.
	 */
	protected String instructions;

	/** Maximum number of output tokens for responses. */
	protected Long maxOutputTokens;

	/** Maximum number of tool calls permitted per response. */
	protected Long maxToolCalls;

	/** Configuration source used to initialize clients and provider features. */
	private Configurator config;

	private boolean errorHandling = true;

	private String[] enabledTools;

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
			url = getConfigurator().get(propName + ".url", null);
			String name = getConfigurator().get(propName + ".name", null);
			String authorization = getConfigurator().get(propName + ".authorization", null);
			String description = getConfigurator().get(propName + ".description", null);

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
		String type = getConfigurator().get("WebSearchTool.type", null);
		String city = getConfigurator().get("WebSearchTool.city", null);
		String country = getConfigurator().get("WebSearchTool.country", null);
		String region = getConfigurator().get("WebSearchTool.region", null);

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
			Object apply = tool.apply(params, projectDir, getConfigurator());
			String result;
			if (apply instanceof String) {
				result = (String) apply;
			} else {
				result = new ObjectMapper().writeValueAsString(apply);
			}
			return result;

		} catch (Exception e) {
			if (!isErrorHandling()) {
				throw new SpecialException(e);
			}

			Throwable rootException = ExceptionUtils.getRootCause(e);
			String message;
			if (rootException instanceof SpecialException) {
				throw (SpecialException) rootException;

			} else {
				message = "Error: The functional tool call failed while executing '" + name + "'. Reason: "
						+ e.getMessage();
				logger.error(message);
				if (logger.isDebugEnabled()) {
					logger.debug(message, ExceptionUtils.getRootCause(e));
				}
			}

			return message;
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
	 * <p>
	 * This method inspects the public methods of the provided {@link FunctionTools}
	 * class. For any method annotated with {@link Tool}, it extracts its name and
	 * description (falling back to the method's Java name if no explicit name is
	 * defined in the annotation) and registers it as an active tool.
	 * </p>
	 *
	 * @param tools the {@link FunctionTools} instance containing the annotated
	 *              methods to register
	 */
	public void addTools(FunctionTools tools) {
		Class<? extends FunctionTools> toolsClass = tools.getClass();
		Method[] methods = toolsClass.getMethods();
		for (Method method : methods) {
			Tool toolAnnotation = method.getAnnotation(Tool.class);
			if (toolAnnotation != null) {
				String description = toolAnnotation.description();
				String name;
				if (Tool.NOT_DEFINED.equals(toolAnnotation.name())) {
					name = method.getName();
				} else {
					name = toolAnnotation.name();
				}

				addTool(tools, method, name, description);
			}
		}
	}

	/**
	 * Registers all annotated prompt methods from the given {@link FunctionTools}
	 * instance.
	 * <p>
	 * This method inspects the public methods of the provided {@link FunctionTools}
	 * class. For any method annotated with {@link Prompt}, it extracts its
	 * configured metadata—such as the name, description, and target {@link Role}.
	 * If no explicit name is defined in the annotation, it falls back to using the
	 * method's Java name, before registering it as an active prompt.
	 * </p>
	 *
	 * @param tools the {@link FunctionTools} instance containing the annotated
	 *              prompt methods to register
	 */
	public void addPrompts(FunctionTools tools) {
		Class<? extends FunctionTools> toolsClass = tools.getClass();
		Method[] methods = toolsClass.getMethods();
		for (Method method : methods) {
			Prompt promptAnnotation = method.getAnnotation(Prompt.class);
			if (promptAnnotation != null) {
				String description = promptAnnotation.description();
				String name;
				if (Prompt.NOT_DEFINED.equals(promptAnnotation.name())) {
					name = method.getName();
				} else {
					name = promptAnnotation.name();
				}

				Role role = promptAnnotation.role();
				addPrompt(tools, method, name, description, role);
			}
		}
	}

	public void addResources(FunctionTools tools) {
		Class<? extends FunctionTools> toolsClass = tools.getClass();
		Method[] methods = toolsClass.getMethods();
		for (Method method : methods) {
			Resource resourceAnnotation = method.getAnnotation(Resource.class);
			if (resourceAnnotation != null) {
				String description = resourceAnnotation.description();
				String[] uri = resourceAnnotation.uri();
				String mimeType = resourceAnnotation.mimeType();
				addResource(tools, method, uri, description, mimeType);
			}
		}
	}

	private void addResource(FunctionTools tools, Method method, String[] uris, String description,
			String mimeType) {
		ParamDescriptor[] paramsDesc = fillParamDesc(method);

		for (String uri : uris) {
			addResource(uri, description, mimeType, (props, dir, config) -> {
				String name;
				try {
					name = StringUtils.substringAfterLast(new URI(uri).getPath(), "/");
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
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

				} catch (JacksonException | IllegalAccessException | IllegalArgumentException e) {
					logger.error("Prompt: `{}`, exception: `{}`, projectDir: `{}`", name,
							e.getMessage(), dir);
					throw new IllegalArgumentException(e);
				}

			}, paramsDesc);
		}
	}

	/**
	 * Registers a resource callback for providers that support resource management
	 * tools.
	 * <p>
	 * This method configures an executable resource tool with its associated
	 * meta-information, parameter constraints, and the callback handler used to
	 * fetch or compute the resource's payload.
	 * </p>
	 *
	 * @param name        the unique resource tool name exposed to the provider
	 * @param description a description of the resource tool and its purpose, used
	 *                    by the provider to decide when to call it
	 * @param function    the callback execution handler used to resolve or generate
	 *                    the resource content
	 * @param paramsDesc  variable-arity array of parameter descriptors defining the
	 *                    input schema expected by the tool
	 */
	protected void addResource(String uri, String description, String mimeType, ToolFunction function,
			ParamDescriptor... paramsDesc) {
	}

	private void addPrompt(FunctionTools tools, Method method, String name, String description, Role role) {
		ParamDescriptor[] paramsDesc = fillParamDesc(method);

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

			} catch (JacksonException | IllegalAccessException | IllegalArgumentException e) {
				logger.error("Prompt: `{}`, exception: `{}`, projectDir: `{}`", name,
						e.getMessage(), dir);
				throw new IllegalArgumentException(e);
			}

		}, role, paramsDesc);
	}

	/**
	 * Registers a prompt callback for providers that support prompt enabledTools.
	 *
	 * @param name        prompt name exposed to the provider
	 * @param description prompt description used by the provider
	 * @param function    callback used to resolve the prompt content
	 * @param role        role associated with the generated prompt
	 * @param paramsDesc  descriptors for prompt input parameters
	 */
	protected void addPrompt(String name, String description, ToolFunction function, Role role,
			ParamDescriptor... paramsDesc) {
	}

	private void addTool(FunctionTools tools, Method method, String name, String description) {
		ParamDescriptor[] paramsDesc = fillParamDesc(method);

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
				if (targetException instanceof SpecialException) {
					throw (SpecialException) targetException;

				} else {
					logger.error("Tool: `{}`, error: `{}`, projectDir: `{}`", name,
							targetException.getMessage(), dir);
					logger.debug("Tool: `{}`, error: `{}`, projectDir: `{}`", name,
							targetException.getMessage(), dir, targetException);
					throw new IllegalArgumentException(targetException);
				}
			} catch (JacksonException | IllegalAccessException e) {
				logger.error("Tool: `{}`, exception: `{}`, projectDir: `{}`", name,
						e.getMessage(), dir);
				throw new IllegalArgumentException(e);
			}

		}, paramsDesc);
	}

	private ParamDescriptor[] fillParamDesc(Method method) {
		List<ParamDescriptor> paramsDesc = new ArrayList<>();
		Parameter[] parameters = method.getParameters();
		for (Parameter param : parameters) {
			Param paramAnn = param.getAnnotation(Param.class);
			if (paramAnn != null) {
				String paramName = paramAnn.name();
				if (Param.NOT_DEFINED.equals(paramName)) {
					paramName = param.getName();
				}

				if (!PROJECT_DIR_PARAM_NAME.equals(paramName) || projectDir == null) {
					Class<?> type = param.getType();
					String defaultValue = paramAnn.defaultValue();
					boolean required = defaultValue.equals(Param.NOT_DEFINED);
					String typeStr = TypeConverter.get(type);
					String description = paramAnn.description();

					ParamDescriptor paramDescription = new ParamDescriptor(paramName, typeStr, required,
							description, defaultValue);
					paramsDesc.add(paramDescription);
				}
			}
		}
		return paramsDesc.toArray(new ParamDescriptor[0]);
	}

	private Object invoke(FunctionTools tools, Method method, JsonNode props, File dir, Configurator config)
			throws JacksonException, IllegalAccessException, InvocationTargetException {
		List<Object> args = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();

		Parameter[] params = method.getParameters();
		for (Parameter param : params) {
			Param paramAnn = param.getAnnotation(Param.class);
			if (paramAnn != null) {
				String defaultValue = paramAnn.defaultValue();
				if (Strings.CS.containsAny(defaultValue, Param.NULL, Param.NOT_DEFINED)) {
					defaultValue = null;
				}

				String paramName = paramAnn.name();
				if (Param.NOT_DEFINED.equals(paramName)) {
					paramName = param.getName();
				}

				if (PROJECT_DIR_PARAM_NAME.equals(paramName)) {
					if (dir != null) {
						defaultValue = dir.getAbsolutePath();
					}
				}

				defaultValue = StringSubstitutor.replace(defaultValue, map);
				String valueStr = getParamValue(props, paramName, defaultValue);

				Object value = TypeConverter.converToType(param, valueStr);

				map.put(paramName, value);
				args.add(value);

			} else {
				Object value = null;
				if (Configurator.class.isAssignableFrom(param.getType())) {
					value = config;
				} else if (File.class.isAssignableFrom(param.getType())) {
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

	/**
	 * Returns whether runtime tool errors are handled conversationally.
	 *
	 * @return the errorHandling
	 */
	public boolean isErrorHandling() {
		return errorHandling;
	}

	/**
	 * Configures how runtime tool errors are handled by the invocation logic.
	 * <p>
	 * Use this setter to toggle between conversational error recovery and strict,
	 * fail-fast exception reporting.
	 * </p>
	 * <h4>Behavior Summary:</h4>
	 * <ul>
	 * <li>{@code setErrorHandling(true)} (Default): Captures all standard runtime
	 * tool exceptions and returns them in a text payload (e.g.
	 * {@code "Error: The functional tool call failed..."}). This permits
	 * conversational LLM agents to review the failure description and attempt
	 * self-correction.</li>
	 * <li>{@code setErrorHandling(false)}: Re-throws all invocation exceptions as a
	 * wrapped {@link SpecialException} up the current thread execution. Use this
	 * setting to debug and fail execution immediately upon the first unhandled
	 * exception.</li>
	 * </ul>
	 *
	 * @param errorHandling {@code true} to enable conversational intercept and
	 *                      recovery; {@code false} to disable intercept and trigger
	 *                      strict stack propagation.
	 */
	public void setErrorHandling(boolean errorHandling) {
		this.errorHandling = errorHandling;
	}

	/**
	 * @return the config
	 */
	public Configurator getConfigurator() {
		return config;
	}

	@Override
	public void setEnabledTools(String[] tools) {
		this.enabledTools = tools;
	}

	public String[] getEnabledTools() {
		return enabledTools;
	}
}
