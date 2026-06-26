package org.machanism.machai.ai.provider;

import java.io.File;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for converting between Java types and their string
 * representations, as well as mapping Java types to simplified type names
 * (e.g., "string", "integer", "array").
 * <p>
 * Supports conversion from string input to Java objects such as {@link File},
 * {@link Integer}, {@link Boolean}, {@link List}, {@link Map}, and JSON
 * objects. Also provides a mapping from Java classes to type names for use in
 * schema or metadata generation.
 * </p>
 *
 * <b>Supported type mappings:</b>
 * <ul>
 * <li>{@link String}, {@link File} &rarr; "string"</li>
 * <li>{@link Integer}, <code>int</code> &rarr; "integer"</li>
 * <li>{@link Double}, <code>double</code> &rarr; "number"</li>
 * <li>{@link Boolean}, <code>boolean</code> &rarr; "boolean"</li>
 * <li>{@link JsonNode}, {@link Map} &rarr; "object"</li>
 * <li>{@link List} &rarr; "array"</li>
 * </ul>
 */
public class TypeConverter {

	/**
	 * Immutable mapping from Java classes to simplified type names.
	 */
	protected static Map<Class<?>, String> typeMap = Collections.unmodifiableMap(new HashMap<Class<?>, String>() {
		{
			// string
			put(String.class, "string");
			put(File.class, "string");
			// integer
			put(Integer.class, "integer");
			put(int.class, "integer");
			// number
			put(Double.class, "number");
			put(double.class, "number");
			// boolean
			put(boolean.class, "boolean");
			put(Boolean.class, "boolean");
			// object: Nested dictionaries or JSON objects.
			put(JsonNode.class, "object");
			put(Map.class, "object");
			// array: Lists of items (e.g., ["item1", "item2"]).
			put(List.class, "array");
		}
	});

	/**
	 * Returns the simplified type name for the given Java class.
	 *
	 * @param type The Java class to map.
	 * @return The type name as a string (e.g., "string", "integer", "array"), or
	 *         {@code null} if not mapped.
	 */
	public static String get(Class<?> type) {
		return ObjectUtils.getIfNull(typeMap.get(type), "object");
	}

	/**
	 * Converts a string input to an object of the type specified by the given {@link Parameter}.
	 * <p>
	 * This method supports conversion to various types, including {@link File}, {@code int}, {@code boolean},
	 * {@link List}, and {@link Map} with generic value types ({@code String}, {@code Integer}, {@code Double}).
	 * For {@link Map} types, the value type is determined from the parameterized type information.
	 * If the type is not {@link String} and not one of the explicitly handled types, the input is deserialized
	 * using Jackson's {@link ObjectMapper} into the specified type.
	 * </p>
	 *
	 * @param param the {@link Parameter} describing the target type and (for generics) parameterized type information
	 * @param input the string input to convert; may be {@code null}
	 * @return the converted object, or the original input if {@code null} or if the type is {@link String}
	 * @throws JsonProcessingException if the input cannot be parsed as the specified type
	 * @throws JsonMappingException if the input cannot be mapped to the specified type
	 */
	public static Object converToType(Parameter param, String input)
			throws JsonProcessingException, JsonMappingException {
		Object output = input;
		if (input != null) {
			Class<?> type = param.getType();
			if (File.class.isAssignableFrom(type)) {
				output = new File(input);
			} else if (int.class.isAssignableFrom(type)) {
				output = Integer.parseInt(input);
			} else if (boolean.class.isAssignableFrom(type)) {
				output = Boolean.parseBoolean(input);
			} else if (List.class.isAssignableFrom(type)) {
				output = new ObjectMapper().readValue(input,
						new TypeReference<List<String>>() {
						});
			} else if (Map.class.isAssignableFrom(type)) {
				Type parameterizedType = param.getParameterizedType();
				if (parameterizedType instanceof ParameterizedType) {
					ParameterizedType typeName = (ParameterizedType) parameterizedType;
					String valueType = typeName.getActualTypeArguments()[1].getTypeName();
					if ("java.lang.Integer".equals(valueType)) {
						output = new ObjectMapper().readValue(input,
								new TypeReference<Map<String, Integer>>() {
								});
					} else if ("java.lang.Double".equals(valueType)) {
						output = new ObjectMapper().readValue(input,
								new TypeReference<Map<String, Double>>() {
								});
					} else {
						if (StringUtils.isNoneBlank(input)) {
							output = new ObjectMapper().readValue(input,
									new TypeReference<Map<String, String>>() {
									});
						} else {
							output = new HashMap<>();
						}
					}
				} else {
					if (StringUtils.isNoneBlank(input)) {
						output = new ObjectMapper().readValue(input,
								new TypeReference<Map<String, String>>() {
								});
					}else {
						output = new HashMap<>();
					}
				}
			} else if (!String.class.isAssignableFrom(type)) {
				output = new ObjectMapper().readValue(input, type);
			}
		}
		return output;
	}

}