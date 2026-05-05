package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.tools.ToolFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.responses.ResponseCreateParams;

class OpenAIProviderInternalsTest {

    @TempDir
    File tempDir;

    @Test
    void usageShouldReturnZeroesBeforeAnyRequest() {
        OpenAIProvider provider = new OpenAIProvider();

        Usage usage = provider.usage();

        assertEquals(0L, usage.getInputTokens());
        assertEquals(0L, usage.getInputCachedTokens());
        assertEquals(0L, usage.getOutputTokens());
    }

    @Test
    void safelyInvokeToolShouldReturnErrorMessageOnIOException() throws Exception {
        OpenAIProvider provider = initializedProvider();
        JsonNode params = new ObjectMapper().readTree("{\"value\":1}");
        ToolFunction tool = new ToolFunction() {
            @Override
            public Object apply(JsonNode input, File workingDir) throws IOException {
                throw new IOException("boom");
            }
        };

        Object result = invoke(provider, "safelyInvokeTool",
                new Class<?>[] { String.class, ToolFunction.class, JsonNode.class, File.class }, "name", tool, params,
                tempDir);

        assertEquals("Error: The functional tool call failed while executing 'name'. Reason: boom", result);
    }

    @Test
    void createResponseBuilderShouldIncludeInstructionsInputsAndTools() throws Exception {
        OpenAIProvider provider = initializedProvider();
        provider.instructions("system message");
        provider.prompt("hello");
        provider.addTool("Echo", "desc", new ToolFunction() {
            @Override
            public Object apply(JsonNode params, File workingDir) {
                return null;
            }
        }, "value:string:required:text", "optional:string:optional:desc");

        setPrivateField(provider, "maxToolCalls", Long.valueOf(2L));
        setPrivateField(provider, "maxOutputTokens", Long.valueOf(123L));

        @SuppressWarnings("unchecked")
        List<com.openai.models.responses.ResponseInputItem> inputs =
                (List<com.openai.models.responses.ResponseInputItem>) getPrivateField(provider, "inputs");

        ResponseCreateParams params = (ResponseCreateParams) invoke(provider, "createResponseBuilder",
                new Class<?>[] { List.class }, inputs);

        assertTrue(params.model().isPresent());
        assertEquals(Long.valueOf(123L), params.maxOutputTokens().get());
        assertEquals(Long.valueOf(2L), params.maxToolCalls().get());
        assertEquals("system message", params.instructions().get());
        assertEquals(1, params.tools().get().size());
        assertTrue(params.input().isPresent());
    }

    @Test
    void createResponseBuilderShouldSkipMaxToolCallsWhenZero() throws Exception {
        OpenAIProvider provider = initializedProvider();
        provider.prompt("hello");
        setPrivateField(provider, "maxToolCalls", Long.valueOf(0L));

        @SuppressWarnings("unchecked")
        List<com.openai.models.responses.ResponseInputItem> inputs =
                (List<com.openai.models.responses.ResponseInputItem>) getPrivateField(provider, "inputs");

        ResponseCreateParams params = (ResponseCreateParams) invoke(provider, "createResponseBuilder",
                new Class<?>[] { List.class }, inputs);

        assertTrue(!params.maxToolCalls().isPresent());
    }

    @Test
    void logInputsWriterShouldWritePromptAndInstructions() throws Exception {
        OpenAIProvider provider = initializedProvider();
        provider.instructions("sys");
        provider.prompt("hello world");

        StringWriter writer = new StringWriter();
        invoke(provider, "logInputs", new Class<?>[] { java.io.Writer.class }, writer);

        String text = writer.toString();
        assertTrue(text.contains("sys"));
        assertTrue(text.contains("hello world"));
    }

    @Test
    void firstNonBlankReasoningShouldReturnFirstNonBlankEntry() throws Exception {
        OpenAIProvider provider = initializedProvider();
        List<com.openai.models.responses.ResponseReasoningItem.Content> contents = Arrays.asList(
                com.openai.models.responses.ResponseReasoningItem.Content.builder().text(" ").build(),
                com.openai.models.responses.ResponseReasoningItem.Content.builder().text("reasoned").build(),
                com.openai.models.responses.ResponseReasoningItem.Content.builder().text("later").build());

        Object result = invoke(provider, "firstNonBlankReasoning", new Class<?>[] { List.class }, contents);

        assertEquals("reasoned", result);
    }

    @Test
    void firstNonBlankReasoningShouldReturnNullWhenNothingUseful() throws Exception {
        OpenAIProvider provider = initializedProvider();
        List<com.openai.models.responses.ResponseReasoningItem.Content> contents = Arrays.asList(
                com.openai.models.responses.ResponseReasoningItem.Content.builder().text("").build(),
                com.openai.models.responses.ResponseReasoningItem.Content.builder().text(" ").build());

        Object result = invoke(provider, "firstNonBlankReasoning", new Class<?>[] { List.class }, contents);

        assertNull(result);
    }

    @Test
    void normalizeShouldLowercaseAndHandleNull() throws Exception {
        OpenAIProvider provider = initializedProvider();

        assertEquals("echo", invoke(provider, "normalize", new Class<?>[] { String.class }, "EcHo"));
        assertEquals("", invoke(provider, "normalize", new Class<?>[] { String.class }, new Object[] { null }));
    }

    @Test
    void hasSameToolNameShouldIgnoreCase() throws Exception {
        OpenAIProvider provider = initializedProvider();
        provider.addTool("Echo", "desc", new ToolFunction() {
            @Override
            public Object apply(JsonNode params, File workingDir) {
                return null;
            }
        }, "value:string:required:text");

        @SuppressWarnings("unchecked")
        java.util.Map<com.openai.models.responses.Tool, ToolFunction> toolMap =
                (java.util.Map<com.openai.models.responses.Tool, ToolFunction>) getPrivateField(provider, "toolMap");
        com.openai.models.responses.Tool tool = toolMap.keySet().iterator().next();

        Object result = invoke(provider, "hasSameToolName",
                new Class<?>[] { String.class, com.openai.models.responses.Tool.class }, "echo", tool);

        assertEquals(Boolean.TRUE, result);
    }

    @Test
    void isRequiredParameterShouldMatchRequiredCaseInsensitively() throws Exception {
        OpenAIProvider provider = initializedProvider();

        assertEquals(Boolean.TRUE, invoke(provider, "isRequiredParameter", new Class<?>[] { String.class }, "ReQuIrEd"));
        assertEquals(Boolean.FALSE, invoke(provider, "isRequiredParameter", new Class<?>[] { String.class }, "optional"));
    }

    @Test
    void addToolShouldBuildSchemaWithRequiredAndOptionalParameters() throws Exception {
        OpenAIProvider provider = initializedProvider();
        provider.addTool("Echo", "desc", new ToolFunction() {
            @Override
            public Object apply(JsonNode params, File workingDir) {
                return null;
            }
        }, "name:string:required:person name", "age:number:optional:person age");

        @SuppressWarnings("unchecked")
        java.util.Map<com.openai.models.responses.Tool, ToolFunction> toolMap =
                (java.util.Map<com.openai.models.responses.Tool, ToolFunction>) getPrivateField(provider, "toolMap");
        assertEquals(1, toolMap.size());
        com.openai.models.responses.Tool tool = toolMap.keySet().iterator().next();
        com.openai.models.responses.FunctionTool functionTool = tool.asFunction();
        assertEquals("Echo", functionTool.name());
        assertEquals("desc", functionTool.description().get());
        String schema = String.valueOf(functionTool.parameters().get());
        assertTrue(schema.contains("name"));
        assertTrue(schema.contains("age"));
    }

    @Test
    void addToolShouldHandleNullParamsDescription() throws Exception {
        OpenAIProvider provider = initializedProvider();
        provider.addTool("Echo", "desc", new ToolFunction() {
            @Override
            public Object apply(JsonNode params, File workingDir) {
                return null;
            }
        }, (String[]) null);

        @SuppressWarnings("unchecked")
        java.util.Map<com.openai.models.responses.Tool, ToolFunction> toolMap =
                (java.util.Map<com.openai.models.responses.Tool, ToolFunction>) getPrivateField(provider, "toolMap");
        assertEquals(1, toolMap.size());
    }

    @Test
    void addToolShouldFailWhenDescriptorDoesNotContainEnoughParts() {
        OpenAIProvider provider = initializedProvider();

        ToolFunction toolFunction = new ToolFunction() {
            @Override
            public Object apply(JsonNode params, File workingDir) {
                return null;
            }
        };

        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> addBrokenTool(provider, toolFunction));
    }

    private static void addBrokenTool(OpenAIProvider provider, ToolFunction toolFunction) {
        provider.addTool("Echo", "desc", toolFunction, "broken");
    }

    private static OpenAIProvider initializedProvider() {
        OpenAIProvider provider = new OpenAIProvider();
        Configurator config = TestConfigurators.mapBacked();
        config.set("chatModel", "gpt-test");
        config.set("OPENAI_API_KEY", "dummy");
        provider.init(config);
        return provider;
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = OpenAIProvider.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = OpenAIProvider.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = OpenAIProvider.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
