package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ToolsMetadataTest {

    @Test
    void paramDescriptor_exposesMetadataAndNormalizesPlaceholders() {
        ParamDescriptor descriptor = new ParamDescriptor("id", "String", true, Param.NULL, Param.NOT_DEFINED);
        assertEquals("id", descriptor.getName());
        assertEquals("String", descriptor.getType());
        assertTrue(descriptor.isRequired());
        assertNull(descriptor.getDescription());
        assertNull(descriptor.getDefaultValue());

        descriptor.setDefaultValue(42);
        assertEquals(42, descriptor.getDefaultValue());
        assertEquals("description", new ParamDescriptor("n", "t", false, "description", "default").getDescription());
        assertEquals("default", new ParamDescriptor("n", "t", false, "description", "default").getDefaultValue());
        assertNull(new ParamDescriptor("n", "t", false, Param.NOT_DEFINED, Param.NULL).getDescription());
        assertNull(new ParamDescriptor("n", "t", false, null, null).getDescription());
    }

    @Test
    void paramDescriptor_preservesNullAndNonStringDefaultValues() {
        // Arrange
        ParamDescriptor descriptor = new ParamDescriptor("n", "Object", false, "description", 0L);

        // Act
        descriptor.setDefaultValue(null);
        Object nullDefault = descriptor.getDefaultValue();
        descriptor.setDefaultValue(new StringBuilder("value"));
        Object objectDefault = descriptor.getDefaultValue();

        // Assert
        assertNull(nullDefault);
        assertTrue(objectDefault instanceof StringBuilder);
        assertEquals("value", objectDefault.toString());
    }

    private static boolean contains(Target target, ElementType expected) {
        for (ElementType actual : target.value()) {
            if (actual == expected) {
                return true;
            }
        }
        return false;
    }

    static class CyclicPayload {
        public CyclicPayload self;
    }

    @Test
    void annotations_areRuntimeVisibleWithDeclaredAndDefaultValues() throws Exception {
        Method method = MetadataFixture.class.getDeclaredMethod("annotated", String.class, int.class);
        Tool tool = method.getAnnotation(Tool.class);
        Prompt prompt = method.getAnnotation(Prompt.class);
        Resource resource = method.getAnnotation(Resource.class);
        assertEquals("tool", tool.name());
        assertEquals("tool description", tool.description());
        assertEquals(Prompt.NOT_DEFINED, prompt.name());
        assertEquals(Role.ASSISTANT, prompt.role());
        assertArrayEquals(new String[] {"urn:a", "urn:b"}, resource.uri());
        assertEquals("text/plain", resource.mimeType());

        Parameter parameter = method.getParameters()[0];
        Param param = parameter.getAnnotation(Param.class);
        assertEquals("value", param.name());
        assertEquals("a value", param.description());
        assertEquals("fallback", param.defaultValue());
        assertEquals(Param.NOT_DEFINED, method.getParameters()[1].getAnnotation(Param.class).name());
    }

    @Test
    void constantsAndRoleValues_areStable() {
        assertEquals("request.session.id", ToolFunction.SESSION_ID_PARAM_NAME);
        assertEquals("___NULL___", Param.NULL);
        assertEquals("___NOT_DEFINED___", Tool.NOT_DEFINED);
        assertEquals(2, Role.values().length);
        assertSame(Role.USER, Role.valueOf("USER"));
        assertFalse(Role.USER == Role.ASSISTANT);
    }

    @Test
    void annotations_exposeDefaultsRetentionAndTargets() throws Exception {
        // Arrange
        Method defaults = MetadataFixture.class.getDeclaredMethod("defaults", String.class);

        // Act
        Tool tool = defaults.getAnnotation(Tool.class);
        Prompt prompt = defaults.getAnnotation(Prompt.class);
        Resource resource = defaults.getAnnotation(Resource.class);
        Param param = defaults.getParameters()[0].getAnnotation(Param.class);

        // Assert
        assertEquals(Tool.NOT_DEFINED, tool.name());
        assertEquals(Prompt.NOT_DEFINED, prompt.name());
        assertEquals(Role.USER, prompt.role());
        assertEquals(Resource.NOT_DEFINED, resource.mimeType());
        assertEquals(Param.NOT_DEFINED, param.name());
        assertEquals(Param.NOT_DEFINED, param.defaultValue());
        assertEquals(RetentionPolicy.RUNTIME, Tool.class.getAnnotation(Retention.class).value());
        assertEquals(RetentionPolicy.RUNTIME, Param.class.getAnnotation(Retention.class).value());
        assertTrue(contains(Target.class, Tool.class, ElementType.METHOD));
        assertTrue(contains(Target.class, Param.class, ElementType.PARAMETER));
    }

    @Test
    void allToolMetadataAnnotations_haveRuntimeContracts() {
        // Arrange
        SupportedFor supportedFor = SupportedFixture.class.getAnnotation(SupportedFor.class);

        // Act
        Retention toolRetention = Tool.class.getAnnotation(Retention.class);
        Retention promptRetention = Prompt.class.getAnnotation(Retention.class);
        Retention resourceRetention = Resource.class.getAnnotation(Retention.class);
        Retention supportedForRetention = SupportedFor.class.getAnnotation(Retention.class);
        Target resourceTarget = Resource.class.getAnnotation(Target.class);

        // Assert
        assertEquals(RetentionPolicy.RUNTIME, toolRetention.value());
        assertEquals(RetentionPolicy.RUNTIME, promptRetention.value());
        assertEquals(RetentionPolicy.RUNTIME, resourceRetention.value());
        assertEquals(RetentionPolicy.RUNTIME, supportedForRetention.value());
        assertTrue(contains(resourceTarget, ElementType.METHOD));
        assertNull(SupportedFor.class.getAnnotation(Target.class));
        assertArrayEquals(new Class<?>[] {MetadataFixture.class, String.class}, supportedFor.value());
    }

    @Test
    void markerInterfaces_andResourceDefaults_areUsable() throws Exception {
        // Arrange
        FunctionTools tools = new FunctionTools() { };
        Method defaults = MetadataFixture.class.getDeclaredMethod("defaults", String.class);

        // Act
        Resource resource = defaults.getAnnotation(Resource.class);

        // Assert
        assertTrue(tools instanceof FunctionTools);
        assertEquals("default resource", resource.description());
        assertArrayEquals(new String[] {"urn:default"}, resource.uri());
        assertEquals(Resource.NOT_DEFINED, resource.mimeType());
    }

    private static boolean contains(Class<Target> ignored, Class<?> annotation, ElementType expected) {
        for (ElementType actual : annotation.getAnnotation(Target.class).value()) {
            if (actual == expected) {
                return true;
            }
        }
        return false;
    }

    @Test
    void errorResultException_serializesObjectsAndPreservesCauses() {
        ErrorResultException objectError = new ErrorResultException(new ErrorPayload("bad", 7));
        assertEquals("{\"code\":\"bad\",\"count\":7}", objectError.getMessage());

        Exception cause = new IllegalStateException("broken");
        ErrorResultException detailed = new ErrorResultException(new ErrorPayload("bad", 7), cause);
        assertTrue(detailed.getMessage().contains("Error: java.lang.IllegalStateException: broken"));
        assertTrue(detailed.getMessage().contains("Details: {\"code\":\"bad\",\"count\":7}"));
        assertSame(cause, detailed.getCause());
        assertEquals("already formatted", new ErrorResultException("already formatted").getMessage());
    }

    @Test
    void errorResultException_fallsBackWhenPayloadCannotBeSerialized() {
        CyclicPayload cyclic = new CyclicPayload();
        cyclic.self = cyclic;
        ErrorResultException exception = new ErrorResultException(cyclic);
        assertTrue(exception.getMessage().contains("@"));
    }

    @Test
    void errorResultException_serializesNullPayloadAndSupportsNullCause() {
        // Arrange
        ErrorResultException exception = new ErrorResultException((Object) null, null);

        // Act
        String message = exception.getMessage();

        // Assert
        assertEquals("null", message);
        assertNull(exception.getCause());
    }

    @Test
    void errorResultException_preservesStringPayloadWhenWrappingCauseThroughObjectConstructor() {
        // Arrange
        Object payload = "plain details";
        Exception cause = new IllegalArgumentException("invalid input");

        // Act
        ErrorResultException exception = new ErrorResultException(payload, cause);

        // Assert
        assertEquals("Error: java.lang.IllegalArgumentException: invalid input\nDetails: plain details",
                exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void specialException_supportsMessageAndCauseConstructors() {
        assertEquals("stop", new SpecialException("stop").getMessage());
        Exception cause = new Exception("cause");
        SpecialException wrapped = new SpecialException(cause);
        assertSame(cause, wrapped.getCause());
        assertEquals(cause.toString(), wrapped.getMessage());
    }

    @Test
    void toolFunction_canThrowCheckedExceptions() {
        ToolFunction function = (JsonNode ignored, Object[] context) -> { throw new Exception("failure"); };
        Exception exception = assertThrows(Exception.class, () -> function.apply(new ObjectMapper().createObjectNode()));
        assertEquals("failure", exception.getMessage());
    }

    static class ErrorPayload {
        public String code;
        public int count;
        ErrorPayload(String code, int count) { this.code = code; this.count = count; }
    }

    static class MetadataFixture {
        @Tool(name = "tool", description = "tool description")
        @Prompt(description = "prompt description", role = Role.ASSISTANT)
        @Resource(uri = {"urn:a", "urn:b"}, description = "resource", mimeType = "text/plain")
        void annotated(@Param(name = "value", description = "a value", defaultValue = "fallback") String value,
                       @Param(description = "second") int second) { }

        @Tool(description = "default tool")
        @Prompt(description = "default prompt")
        @Resource(description = "default resource", uri = {"urn:default"})
        void defaults(@Param(description = "default parameter") String value) { }
    }

    @SupportedFor({MetadataFixture.class, String.class})
    static class SupportedFixture implements FunctionTools { }
}
