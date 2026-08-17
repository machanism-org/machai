package org.machanism.machai.ai.provider;

import java.io.File;
import java.io.IOException;
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
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.ToolLogger.Type;
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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Base implementation of the {@link Genai} contract shared by concrete provider
 * integrations.
 * <p>
 * This class centralizes common configuration handling, request input logging,
 * tool invocation safety, MCP/web-search bootstrap logic, and usage accounting
 * state used by subclasses such as OpenAI- and Claude-based providers.
 * </p>
 */
public abstract class AbstractAIProvider implements Genai {

	/** Logger instance for this provider. */
	static Logger logger = LoggerFactory.getLogger(AbstractAIProvider.class);

	/**
	 * Prefix prepended to tool invocation error messages that are returned back to
	 * the LLM.
	 */
	public static final String ERROR_TOOL_RESULT_PREFIX = "Error: The functional tool call failed";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	public static final String USERNAME_PROP_NAME = "GENAI_USERNAME";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	public static final String PASSWORD_PROP_NAME = "GENAI_PASSWORD";

	/**
	 * Maximum length for log lines. Configures the truncation threshold when
	 * logging tool parameters and result strings.
	 */
	public static final int LOG_LINE_LENG = 160;

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

	/** Name of the project directory parameter. */
	public static final String PROJECT_DIR_PARAM_NAME = "project_dir";

	/** Active model identifier used in {@link #perform()}. */
	protected String chatModel;

	/** Working directory passed to tool handlers as contextual information. */
	protected File projectDir;

	/** Request timeout in seconds; {@code 0} means SDK defaults are used. */
	protected Long timeoutSec;

	/** Optional instructions applied to the request. */
	protected String instructions;

	/** Maximum number of output tokens for responses. */
	protected Long maxOutputTokens;

	/** Maximum number of tool calls permitted per response. */
	protected Long maxToolCalls;

	/** Configuration source used to initialize clients and provider features. */
	private Configurator config;

	/**
	 * Flag indicating if standard runtime exceptions should be wrapped or handled
	 * conversationally.
	 */
	private boolean errorHandling = true;

	/** List of specific tool names that are enabled on this provider instance. */
	private String[] enabledTools;

	/**
	 * Creates a provider base instance.
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
	 * <p>
	 * This is an optional lifecycle method intended to be overridden by subclasses
	 * that natively support the Model Context Protocol (MCP).
	 * </p>
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
	 * <p>
	 * This is an optional capability hook intended to be overridden by subclasses
	 * whose underlying models natively support real-time web-search tools.
	 * </p>
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
	 * @return tool output or a formatted error message string
	 * @throws SpecialException if a non-recoverable error occurs or when
	 *                          error-handling is disabled
	 */
	protected Object safelyInvokeTool(String name, ToolFunction tool, JsonNode params, File projectDir) {
		try {
			Object result = tool.apply(params, projectDir, getConfigurator());
			return result;

		} catch (Exception e) {
			if (e instanceof SpecialException) {
				throw (SpecialException) e;
			}

			if (!isErrorHandling()) {
				throw new SpecialException(e);
			}

			Throwable rootException = ExceptionUtils.getRootCause(e);
			if (rootException instanceof SpecialException) {
				throw (SpecialException) rootException;

			} else {
				String message = ERROR_TOOL_RESULT_PREFIX + " while executing '" + name + "'. Reason: "
						+ e.getMessage();

				if (logger.isDebugEnabled()) {
					logger.error(message, ExceptionUtils.getRootCause(e));
				} else {
					logger.error(message);
				}
				return message;
			}
		}
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
	 * Returns the configured request timeout.
	 *
	 * @return timeout in seconds; {@code 0} indicates the SDK default
	 */
	public long getTimeout() {
		return timeoutSec;
	}

	/**
	 * Sets the timeout value used by provider client creation.
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
	 * <p>
	 * Implemented by concrete subclasses to register the functional tool definition
	 * with the provider's specific API schemas (such as OpenAI's Tool schema or
	 * Claude's Tool definition).
	 * </p>
	 *
	 * @param name        the tool name
	 * @param description the tool description
	 * @param function    the tool function implementation callback
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
	@Override
	public void addTools(FunctionTools tools) {
		Class<? extends FunctionTools> toolsClass = tools.getClass();
		Method[] methods = toolsClass.getMethods();
		for (Method method : methods) {
			Tool toolAnnotation = method.getAnnotation(Tool.class);
			if (toolAnnotation != null) {
				String description = toolAnnotation.description();

				description = interpolateDescription(description);

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
	 * Interpolates system metadata variables (e.g., OS Name) inside the
	 * annotation's tool description text before registering it with the LLM.
	 *
	 * @param description the raw tool description from the annotation
	 * @return the interpolated description string
	 */
	private String interpolateDescription(String description) {
		HashMap<String, String> valueMap = new HashMap<String, String>();
		valueMap.put("OS_NAME", SystemUtils.OS_NAME);
		description = StringSubstitutor.replace(description, valueMap);
		return description;
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
	@Override
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

	/**
	 * Scans the provided {@link FunctionTools} instance for methods annotated with
	 * {@link Resource}, and registers each resource for use during a run.
	 *
	 * @param tools the {@link FunctionTools} instance whose methods will be scanned
	 *              for {@link Resource} annotations
	 */
	@Override
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

	/**
	 * Resolves resource endpoints from declared arrays and binds them to the
	 * implementation context.
	 *
	 * @param tools       target instance container
	 * @param method      reflective execution handle
	 * @param uris        URIs declared in the annotation config
	 * @param description resource description text
	 * @param mimeType    the mime type associated with the resource
	 * @throws IllegalArgumentException if any URIs have incorrect syntax or
	 *                                  execution fails
	 */
	private void addResource(FunctionTools tools, Method method, String[] uris, String description,
			String mimeType) {
		ParamDescriptor[] paramsDesc = fillParamDesc(method);

		for (String uriStr : uris) {
			URI uri;
			try {
				uri = new URI(uriStr);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}

			addResource(uri, description, mimeType, (props, paramsByType) -> {
				File dir = getParamByType(File.class, paramsByType);
				String name = StringUtils.substringAfterLast(uri.getPath(), "/");
				ToolLogger toolLogger = new ToolLogger(Type.RESOURCE, tools);
				try {
					toolLogger.logInput(name, props, dir);
					Object result = invoke(tools, method, props, dir, config, uri);
					toolLogger.logResult(name, dir, result);

					return result;

				} catch (InvocationTargetException e) {
					Throwable targetException = e.getTargetException();
					toolLogger.logError(name, dir, targetException);
					if (targetException instanceof IllegalArgumentException) {
						throw (IllegalArgumentException) targetException;
					}
					throw new IllegalArgumentException(targetException);
				}

			}, paramsDesc);
		}
	}

	/**
	 * Registers a resource callback for providers that support resource management
	 * tools.
	 * <p>
	 * Intended to be implemented by concrete subclasses to natively expose
	 * local/remote resources to the LLM model context.
	 * </p>
	 *
	 * @param uri         resource URI
	 * @param description a description of the resource tool
	 * @param mimeType    the mime type format (e.g. application/json)
	 * @param function    the callback execution handler
	 * @param paramsDesc  variable-arity array of parameter descriptors
	 */
	protected void addResource(URI uri, String description, String mimeType, ToolFunction function,
			ParamDescriptor... paramsDesc) {
	}

	/**
	 * Configures and registers a single prompt method.
	 *
	 * @param tools       instance container
	 * @param method      reflection handle
	 * @param name        the prompt name
	 * @param description descriptive purpose
	 * @param role        role level instructions
	 */
	private void addPrompt(FunctionTools tools, Method method, String name, String description, Role role) {
		ParamDescriptor[] paramsDesc = fillParamDesc(method);

		addPrompt(name, description, (props, paramsByType) -> {
			File dir = getParamByType(File.class, paramsByType);

			ToolLogger toolLogger = new ToolLogger(Type.PROMPT, tools);
			try {
				toolLogger.logInput(name, props, dir);
				Object result = invoke(tools, method, props, dir, config);
				toolLogger.logResult(name, dir, result);

				return result;

			} catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				toolLogger.logError(name, dir, targetException);
				if (targetException instanceof IllegalArgumentException) {
					throw (IllegalArgumentException) targetException;
				}
				throw new IllegalArgumentException(targetException);
			}

		}, role, paramsDesc);
	}

	/**
	 * Registers a prompt callback for providers that support prompt tools.
	 * <p>
	 * Intended to be implemented by concrete subclasses to natively expose dynamic
	 * prompt templates to the underlying model execution context.
	 * </p>
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

	/**
	 * Configures and registers a single tool method.
	 *
	 * @param tools       instance container
	 * @param method      reflection handle
	 * @param name        the tool name
	 * @param description tool capabilities description
	 */
	private void addTool(FunctionTools tools, Method method, String name, String description) {
		ParamDescriptor[] paramsDesc = fillParamDesc(method);

		addTool(name, description, (props, paramsByType) -> {
			File dir = getParamByType(File.class, paramsByType);

			ToolLogger toolLogger = new ToolLogger(Type.TOOL, tools);
			try {
				toolLogger.logInput(name, props, dir);
				Object result = invoke(tools, method, props, paramsByType);
				toolLogger.logResult(name, dir, result);

				return result;

			} catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				if (targetException instanceof SpecialException) {
					throw (SpecialException) targetException;

				} else {
					toolLogger.logError(name, dir, targetException);
					if (targetException instanceof RuntimeException) {
						throw (RuntimeException) targetException;
					}
					throw new IllegalArgumentException(targetException);
				}
			}

		}, paramsDesc);
	}

	/**
	 * Analyzes method signature annotations to build an array of parameter
	 * descriptions.
	 *
	 * @param method the method to inspect
	 * @return an array of {@link ParamDescriptor} instances detailing parameters
	 */
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
					Object defaultValue = paramAnn.defaultValue();
					boolean required = defaultValue.equals(Param.NOT_DEFINED);
					String typeStr = TypeConverter.get(type);
					String description = paramAnn.description();
					defaultValue = TypeConverter.convertToType(param, (String) defaultValue);
					ParamDescriptor paramDescription = new ParamDescriptor(paramName, typeStr, required,
							description, defaultValue);
					paramsDesc.add(paramDescription);
				}
			}
		}
		return paramsDesc.toArray(new ParamDescriptor[0]);
	}

	/**
	 * Dynamically invokes the specified method on the given tools instance,
	 * matching arguments dynamically.
	 *
	 * @param tools        target instance
	 * @param method       method handle
	 * @param props        the incoming JSON attributes
	 * @param paramsByType variable array of parameter context constraints
	 * @return execution output
	 * @throws ReflectiveOperationException if the invoked target throws an
	 *                                      exception
	 */
	private Object invoke(FunctionTools tools, Method method, JsonNode props, Object... paramsByType)
			throws ReflectiveOperationException {
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
					File dir = getParamByType(File.class, paramsByType);

					if (dir != null) {
						defaultValue = dir.getAbsolutePath();
					}
				}

				defaultValue = StringSubstitutor.replace(defaultValue, map);
				String valueStr = getParamValue(props, paramName, defaultValue);

				Object value = TypeConverter.convertToType(param, valueStr);

				map.put(paramName, value);
				args.add(value);

			} else {
				Object value = getParamByType(param.getType(), paramsByType);
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
	 * Filters parameter arrays to find the first assignment-compatible instance of
	 * the specified type.
	 *
	 * @param <T>          the generic class type to filter for
	 * @param type         the class definition type representing the constraint
	 * @param paramsByType variable array containing contextual params
	 * @return the mapped parameter matching the target class, or {@code null} if
	 *         not found
	 */
	private <T> T getParamByType(Class<T> type, Object[] paramsByType) {
		T value = null;
		if (paramsByType != null) {
			for (Object object : paramsByType) {
				if (object != null && type.isInstance(object)) {
					value = type.cast(object);
				}
			}
		}
		return value;
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
			if (value.isEmpty()) {
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
	 * @return the active projectDir File context
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Sets the project directory.
	 *
	 * @param projectDir the projectDir to set
	 */
	@Override
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
	 * @return {@code true} if errors are returned as text payload to the model;
	 *         {@code false} if exceptions propagate and fail immediately.
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
	@Override
	public void setErrorHandling(boolean errorHandling) {
		this.errorHandling = errorHandling;
	}

	/**
	 * Returns the configurator context source.
	 * 
	 * @return the config configuration source
	 */
	public Configurator getConfigurator() {
		return config;
	}

	/**
	 * Configures the list of tool names that are enabled and allowed to be used by
	 * the AI provider.
	 *
	 * @param tools the array of unique tool names to enable; if {@code null} or
	 *              empty, all tools are enabled
	 */
	@Override
	public void setEnabledTools(String[] tools) {
		this.enabledTools = tools;
	}

	/**
	 * Returns the array of currently active tool names.
	 *
	 * @return the array of enabled tool identifiers, or {@code null} if no filter
	 *         is applied
	 */
	public String[] getEnabledTools() {
		return enabledTools;
	}

}