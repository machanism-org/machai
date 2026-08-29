package org.machanism.machai.ai.tools;

import org.apache.commons.lang3.Strings;

/**
 * Descriptor for a parameter used in tool or prompt definitions within the AI
 * provider framework.
 *
 * <p>
 * The {@code ParamDescriptor} class encapsulates metadata about a parameter,
 * including its name, type, whether it is required, and a description of its
 * purpose or usage.
 * </p>
 *
 * <ul>
 * <li><b>name</b>: The parameter's name.</li>
 * <li><b>type</b>: The parameter's data type (e.g., "String", "Integer",
 * "JsonNode").</li>
 * <li><b>required</b>: Indicates whether the parameter is mandatory.</li>
 * <li><b>description</b>: A brief description of the parameter's purpose or
 * usage.</li>
 * </ul>
 *
 * <p>
 * This class is typically used to document and validate parameters for tool
 * functions, prompts, or other callable entities within the framework.
 * </p>
 */
public class ParamDescriptor {

	/** The parameter's name. */
	String name;

	/** The parameter's data type. */
	String type;

	/** Indicates whether the parameter is required. */
	boolean required;

	/** Description of the parameter's purpose or usage. */
	String description;

	/**
	 * Default value supplied when the corresponding parameter is omitted.
	 */
	private Object defaultValue;

	/**
	 * Constructs a new {@code ParamDescriptor} with the specified properties.
	 *
	 * @param name        the parameter's name
	 * @param type        the parameter's data type
	 * @param required    whether the parameter is mandatory
	 * @param description a brief description of the parameter's purpose or usage
	 * @param defaultValue default value supplied when the parameter is omitted
	 */
	public ParamDescriptor(String name, String type, boolean required, String description, Object defaultValue) {
		super();
		this.name = name;
		this.type = type;
		this.required = required;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the parameter's name.
	 *
	 * @return the name of the parameter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameter's data type.
	 *
	 * @return the data type of the parameter
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns a description of the parameter's purpose or usage.
	 *
	 * @return the parameter description
	 */
	public String getDescription() {
		return Strings.CS.equalsAny(description, Param.NULL, Param.NOT_DEFINED) ? null : description;
	}

	/**
	 * Indicates whether the parameter is required.
	 *
	 * @return {@code true} if the parameter is mandatory; {@code false} otherwise
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Returns the configured default value, translating the {@link Param#NULL}
	 * and {@link Param#NOT_DEFINED} sentinel values to {@code null}.
	 *
	 * @return the default value, or {@code null} when no default is defined
	 */
	public Object getDefaultValue() {
		if (defaultValue instanceof String
				&& Strings.CS.equalsAny((String) defaultValue, Param.NULL, Param.NOT_DEFINED)) {
			return null;
		}
		return defaultValue;
	}

	/**
	 * Sets the value used when the corresponding parameter is omitted.
	 *
	 * @param defaultValue the default value to store
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

}
