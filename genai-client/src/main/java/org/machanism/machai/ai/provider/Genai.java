package org.machanism.machai.ai.provider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contract for a generative-AI provider integration.
 *
 * <p>
 * A {@code Genai} represents a concrete implementation (for example OpenAI,
 * Gemini, a local model, etc.) capable of:
 * <ul>
 * <li>collecting prompts and system instructions for a conversation,</li>
 * <li>attaching local or remote files for provider-side processing</li>
 * <li>registering tool functions that may be invoked during a run.</li>
 * </ul>
 *
 * <p>
 * Implementations may keep session state between calls. Use {@link #clear()} to
 * reset conversation state.
 *
 * <h2>Typical usage</h2>
 * 
 * <pre>{@code
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Hello!");
 * String response = provider.perform();
 *
 * provider.clear();
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public interface Genai {

	public static final int LINE_LENG = 160;

	public static final String PROJECT_DIR_PARAM_NAME = "projectDir";

	/** Logger for shell tool execution and diagnostics. */
	static final Logger logger = LoggerFactory.getLogger(Genai.class);

	/**
	 * Configuration property name indicating whether provider inputs should be
	 * logged.
	 */
	String LOG_INPUTS_PROP_NAME = "logInputs";

	/**
	 * Configuration property name for the target GenAI server identifier.
	 */
	String SERVERID_PROP_NAME = "genai.serverId";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	String USERNAME_PROP_NAME = "GENAI_USERNAME";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	String PASSWORD_PROP_NAME = "GENAI_PASSWORD";

	/**
	 * Line separator used when composing prompts.
	 */
	String LINE_SEPARATOR = "\n";

	/**
	 * Paragraph separator used when composing prompts.
	 */
	String PARAGRAPH_SEPARATOR = "\n\n";

	Map<Class<?>, String> typeMap = Collections.unmodifiableMap(
			new HashMap<Class<?>, String>() {
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
	 * Initializes the provider with application configuration.
	 * 
	 * @param model TODO
	 * @param conf  configuration source
	 */
	void init(String model, Configurator conf);

	/**
	 * Adds a user prompt to the current session.
	 *
	 * @param text the prompt text
	 */
	void prompt(String text);

	/**
	 * Clears any stored files and session/provider state.
	 */
	void clear();

	/**
	 * Sets system/session instructions for the current conversation.
	 *
	 * @param instructions instruction text
	 */
	void instructions(String instructions);

	/**
	 * Executes the provider to produce a response based on the accumulated prompts
	 * and state.
	 *
	 * @return the provider response
	 */
	String perform();

	/**
	 * Enables logging of provider inputs to the given directory.
	 *
	 * @param bindexTempDir directory used for writing log files
	 */
	void inputsLog(File bindexTempDir);

	/**
	 * Configures the working directory used for file and tool operations.
	 *
	 * @param projectDir the working directory
	 */
	void setWorkingDir(File projectDir);

	default void addTool(FunctionTools tools) {
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
						String defaultValue = paramAnn.defaultValue();

						boolean required = defaultValue.equals(Param.NULL_VALUE);
						Class<?> type = param.getType();

						String typeStr = typeMap.get(type);
						ParamDescriptor paramDescription = new ParamDescriptor(paramAnn.name(), typeStr, required,
								paramAnn.description());
						paramsDesc.add(paramDescription);
					}
				}

				addTool(name, description, (props, dir) -> {
					try {
						if (logger.isInfoEnabled()) {
							logger.info("Call function: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
									.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), dir);
						}

						List<Object> args = new ArrayList<>();

						Parameter[] params = method.getParameters();
						for (Parameter param : params) {
							Param paramAnn = param.getAnnotation(Param.class);
							if (paramAnn != null) {
								String defaultValue = paramAnn.defaultValue();

								Class<?> type = param.getType();

								String paramName = paramAnn.name();
								if (dir != null && PROJECT_DIR_PARAM_NAME.equals(paramName)) {
									defaultValue = dir.getAbsolutePath();
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
									StringUtils.abbreviate(String.valueOf(result), LINE_LENG)
											.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""),
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

	void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc);
}
