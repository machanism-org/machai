package org.machanism.machai.ai.provider;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
 * <p><b>Supported type mappings:</b></p>
 * <ul>
 * <li>{@link String}, {@link File} &rarr; "string"</li>
 * <li>{@link Integer}, <code>int</code> &rarr; "integer"</li>
 * <li>{@link Double}, <code>double</code> &rarr; "number"</li>
 * <li>{@link Boolean}, <code>boolean</code> &rarr; "boolean"</li>
 * <li>{@link JsonNode}, {@link Map} &rarr; "object"</li>
 * <li>{@link List} &rarr; "array"</li>
 * </ul>
 * 
 * @since 1.2.0
 */
class TypeConverter {

	/**
	 * Creates a type converter utility instance.
	 */
	public TypeConverter() {
	}

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
	 * <p>
	 * If the specified Java class is not mapped in the internal type map, 
	 * the default type {@code "object"} is returned.
	 * </p>
	 *
	 * @param type the Java class to map; may be {@code null}
	 * @return the mapped simplified type name (e.g., {@code "string"}, {@code "integer"}, 
	 *         {@code "array"}), or {@code "object"} if the type is unknown or {@code null}
	 */
	public static String get(Class<?> type) {
		return ObjectUtils.getIfNull(typeMap.get(type), "object");
	}

	/**
	 * Converts a string input to an object of the type specified by the given
	 * {@link Parameter}.
	 * <p>
	 * This method supports conversion to various types, including {@link List}, 
	 * {@link Map} (with specialized value types of {@link Integer}, {@link Double}, or {@link String}), 
	 * primitives, or objects with a constructor taking a single {@link String} argument. 
	 * If direct constructor instantiation fails, it falls back to deserialization using Jackson's 
	 * {@link ObjectMapper}.
	 * </p>
	 *
	 * @param param the {@link Parameter} describing the target type and (for
	 *              generics) parameterized type information
	 * @param input the string input to convert; may be {@code null}
	 * @return the converted object, or {@code null} if the input is null, or the original 
	 *         input string if the target type is {@link String}
	 * @throws JsonProcessingException if the input is not empty and cannot be parsed 
	 *                                 as the specified target type
	 * @throws JsonMappingException    if the parsed input structure does not map successfully 
	 *                                 to the target parameter type layout
	 */
	public static Object convertToType(Parameter param, String input)
			throws JsonProcessingException, JsonMappingException {
		Object output = input;
		if (input != null) {
			Class<?> type = param.getType();
			if (List.class.isAssignableFrom(type)) {
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
					} else {
						output = new HashMap<>();
					}
				}
			} else if (!String.class.isAssignableFrom(type)) {
				try {
					if (type.isPrimitive()) {
						type = primitiveTypeMapping(type);
					}
					Constructor<?> constructor = type.getConstructor(String.class);
					output = constructor.newInstance(input);

				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					output = new ObjectMapper().readValue(input, type);
				}
			}
		}
		return output;
	}

	/**
	 * Maps a primitive Java {@link Class} type to its equivalent Object wrapper class representation.
	 * <p>
	 * For instance, maps {@code int.class} to {@code java.lang.Integer.class}, {@code boolean.class} 
	 * to {@code java.lang.Boolean.class}, etc. If the given type is not a recognized primitive name, 
	 * the type is returned unchanged.
	 * </p>
	 *
	 * @param type the primitive Java class type to map
	 * @return the wrapped Object class equivalent representation, or the original type if unmapped
	 */
	private static Class<?> primitiveTypeMapping(Class<?> type) {
		switch (type.getName()) {
		case "byte":
			type = java.lang.Byte.class;
			break;
		case "short":
			type = java.lang.Short.class;
			break;
		case "int":
			type = java.lang.Integer.class;
			break;
		case "long":
			type = java.lang.Long.class;
			break;
		case "float":
			type = java.lang.Float.class;
			break;
		case "double":
			type = java.lang.Double.class;
			break;
		case "char":
			type = java.lang.Character.class;
			break;
		case "boolean":
			type = java.lang.Boolean.class;
			break;
		}
		return type;
	}

}