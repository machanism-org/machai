package org.machanism.machai.ai.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TypeConverterTest {
    static class Parameters {
        void values(String text, int number, boolean flag, List<String> list,
                Map<String, Integer> integers, Map<String, Double> decimals,
                Map<String, String> strings, Object object, byte byteValue, char charValue,
                float floatValue, long longValue, short shortValue) {
            // SonarQube java:S1186: this signature-only fixture supplies reflection metadata.
        }
    }

    private static Parameter parameter(int index) throws Exception {
        Method method = Parameters.class.getDeclaredMethod("values", String.class, int.class,
                boolean.class, List.class, Map.class, Map.class, Map.class, Object.class,
                byte.class, char.class, float.class, long.class, short.class);
        return method.getParameters()[index];
    }

    @Test
    void get_mapsSupportedAndUnknownTypes() {
        assertEquals("string", TypeConverter.get(String.class));
        assertEquals("integer", TypeConverter.get(int.class));
        assertEquals("number", TypeConverter.get(Double.class));
        assertEquals("boolean", TypeConverter.get(Boolean.class));
        assertEquals("array", TypeConverter.get(List.class));
        assertEquals("object", TypeConverter.get(Map.class));
        assertEquals("object", TypeConverter.get(null));
        assertEquals("object", TypeConverter.get(Object.class));
    }

    @Test
    void convertToType_handlesNullSentinelsAndSimpleValues() throws Exception {
        assertEquals("hello", TypeConverter.convertToType(parameter(0), "hello"));
        assertEquals(null, TypeConverter.convertToType(parameter(0), null));
        assertEquals(null, TypeConverter.convertToType(parameter(0), org.machanism.machai.ai.tools.Param.NULL));
        assertEquals(42, TypeConverter.convertToType(parameter(1), "42"));
        assertEquals(true, TypeConverter.convertToType(parameter(2), "true"));
    }

    @Test
    void convertToType_deserializesListsAndTypedMaps() throws Exception {
        assertEquals(Arrays.asList("a", "b"), TypeConverter.convertToType(parameter(3), "[\"a\",\"b\"]"));
        Map<String, Integer> integers = new HashMap<>(); integers.put("a", 2);
        Map<String, Double> decimals = new HashMap<>(); decimals.put("a", 2.5);
        Map<String, String> strings = new HashMap<>(); strings.put("a", "x");
        assertEquals(integers, TypeConverter.convertToType(parameter(4), "{\"a\":2}"));
        assertEquals(decimals, TypeConverter.convertToType(parameter(5), "{\"a\":2.5}"));
        assertEquals(strings, TypeConverter.convertToType(parameter(6), "{\"a\":\"x\"}"));
    }

    @Test
    void convertToType_handlesRawAndBlankMaps() throws Exception {
        assertEquals(Collections.emptyMap(), TypeConverter.convertToType(parameter(6), ""));
        assertInstanceOf(Map.class, TypeConverter.convertToType(parameter(6), " "));
    }

    @Test
    void convertToType_reportsMalformedJson() {
        Parameter listParameter = parameterUnchecked(3);
        assertThrows(IllegalArgumentException.class,
                () -> TypeConverter.convertToType(listParameter, "not-json"));
    }

    private static Parameter parameterUnchecked(int index) {
        try {
            return parameter(index);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void convertToType_mapsEverySupportedPrimitiveToItsWrapperConstructor() throws Exception {
        // Arrange / Act / Assert
        assertEquals(Byte.valueOf((byte) 7), TypeConverter.convertToType(parameter(8), "7"));
        assertEquals(Character.valueOf('x'), TypeConverter.convertToType(parameter(9), "\"x\""));
        assertEquals(Float.valueOf(1.5f), TypeConverter.convertToType(parameter(10), "1.5"));
        assertEquals(Long.valueOf(8L), TypeConverter.convertToType(parameter(11), "8"));
        assertEquals(Short.valueOf((short) 2), TypeConverter.convertToType(parameter(12), "2"));
    }
}
