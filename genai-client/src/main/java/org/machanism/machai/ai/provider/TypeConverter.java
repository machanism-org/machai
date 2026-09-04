package org.machanism.machai.ai.provider;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.ai.tools.Param;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * <p>
 * <b>Supported type mappings:</b>
 * </p>
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
public final class TypeConverter {

	private static final String OBJECT_TYPE = "object";
	private static final String BOOLEAN_TYPE = "boolean";
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<Class<?>, String> TYPE_MAP = createTypeMap();

	private TypeConverter() {
		// SonarQube java:S1118: utility class is intentionally non-instantiable.
	}

	private static Map<Class<?>, String> createTypeMap() {
		Map<Class<?>, String> types = new HashMap<>();
		types.put(String.class, "string");
		types.put(File.class, "string");
		types.put(Integer.class, "integer");
		types.put(int.class, "integer");
		types.put(Double.class, "number");
		types.put(double.class, "number");
		types.put(boolean.class, BOOLEAN_TYPE);
		types.put(Boolean.class, BOOLEAN_TYPE);
		types.put(com.fasterxml.jackson.databind.JsonNode.class, OBJECT_TYPE);
		types.put(Map.class, OBJECT_TYPE);
		types.put(List.class, "array");
		return Collections.unmodifiableMap(types);
	}

	/** Returns the simplified schema type for the supplied Java type. */
	public static String get(Class<?> type) {
		return ObjectUtils.getIfNull(TYPE_MAP.get(type), OBJECT_TYPE);
	}

	/**
	 * Converts an input value to the type declared by a reflected tool parameter.
	 *
	 * @throws IllegalArgumentException when JSON input cannot be converted
	 */
	public static Object convertToType(Parameter param, String input) {
		if (isUndefined(input)) {
			return null;
		}
		try {
			return convert(param, input);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Unable to convert tool argument", e);
		}
	}

	private static boolean isUndefined(String input) {
		return input == null || Strings.CS.equalsAny(input, Param.NULL, Param.NOT_DEFINED);
	}

	private static Object convert(Parameter param, String input) throws JsonProcessingException {
		Class<?> type = param.getType();
		if (List.class.isAssignableFrom(type)) {
			return MAPPER.readValue(input, new TypeReference<List<String>>() { });
		}
		if (Map.class.isAssignableFrom(type)) {
			return convertMap(param, input);
		}
		if (String.class.isAssignableFrom(type)) {
			return input;
		}
		if (File.class.isAssignableFrom(type)) {
			return input;
		}
		return convertObject(type, input);
	}

	private static Object convertMap(Parameter param, String input) throws JsonProcessingException {
		if (StringUtils.isBlank(input)) {
			return new HashMap<>();
		}
		String valueType = getMapValueType(param);
		if (Integer.class.getName().equals(valueType)) {
			return MAPPER.readValue(input, new TypeReference<Map<String, Integer>>() { });
		}
		if (Double.class.getName().equals(valueType)) {
			return MAPPER.readValue(input, new TypeReference<Map<String, Double>>() { });
		}
		return MAPPER.readValue(input, new TypeReference<Map<String, String>>() { });
	}

	private static String getMapValueType(Parameter param) {
		if (!(param.getParameterizedType() instanceof ParameterizedType)) {
			return String.class.getName();
		}
		ParameterizedType type = (ParameterizedType) param.getParameterizedType();
		return type.getActualTypeArguments()[1].getTypeName();
	}

	private static Object convertObject(Class<?> type, String input) throws JsonProcessingException {
		Class<?> targetType = type.isPrimitive() ? primitiveTypeMapping(type) : type;
		try {
			Constructor<?> constructor = targetType.getConstructor(String.class);
			return constructor.newInstance(input);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			return MAPPER.readValue(input, targetType);
		}
	}

	private static Class<?> primitiveTypeMapping(Class<?> type) {
		if (type == boolean.class) return Boolean.class;
		if (type == byte.class) return Byte.class;
		if (type == char.class) return Character.class;
		if (type == double.class) return Double.class;
		if (type == float.class) return Float.class;
		if (type == int.class) return Integer.class;
		if (type == long.class) return Long.class;
		if (type == short.class) return Short.class;
		return type;
	}
}
