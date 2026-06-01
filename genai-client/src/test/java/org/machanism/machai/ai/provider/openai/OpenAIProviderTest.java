package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.Usage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openai.client.OpenAIClient;
import com.openai.models.models.Model;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputFile;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.Tool;
import com.openai.models.responses.WebSearchTool;
import com.openai.services.blocking.ModelService;
import com.openai.services.blocking.ResponseService;

class OpenAIProviderTest {

    @Test
    void addTool_registersFunctionSchemaAndHandler() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        AtomicReference<JsonNode> capturedParams = new AtomicReference<JsonNode>();
        AtomicReference<File> capturedWorkingDir = new AtomicReference<File>();

        provider.addTool("sum", "Adds values", (params, workingDir) -> {
            capturedParams.set(params);
            capturedWorkingDir.set(workingDir);
            return Integer.valueOf(params.get("value").asInt() + 1);
        }, "value:integer:required:Number to increment", "note:string:optional:Optional note");

        assertEquals(1, provider.toolMap.size());
        Tool tool = provider.toolMap.keySet().iterator().next();
        assertTrue(tool.isFunction());
        assertEquals("sum", tool.asFunction().name());
        assertEquals("Adds values", tool.asFunction().description().orElse(null));
        JsonNode parameters = toJson(tool.asFunction().parameters().orElseThrow(() -> new IllegalStateException())._additionalProperties());
        assertEquals("object", parameters.get("type").asText());
        assertEquals("integer", parameters.get("properties").get("value").get("type").asText());
        assertEquals("Number to increment", parameters.get("properties").get("value").get("description").asText());
        assertEquals(1, parameters.get("required").size());
        assertEquals("value", parameters.get("required").get(0).asText());
        assertNotNull(provider.toolMap.get(tool));
        assertNull(capturedParams.get());
        assertNull(capturedWorkingDir.get());
    }

    @Test
    void addTool_withNullParamsDescRegistersEmptySchema() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        provider.addTool("noop", "No-op", (params, workingDir) -> "done", (String[]) null);

        Tool tool = provider.toolMap.keySet().iterator().next();
        JsonNode parameters = toJson(tool.asFunction().parameters().orElseThrow(() -> new IllegalStateException())._additionalProperties());
        assertTrue(parameters.get("properties").isObject() && !parameters.get("properties").fieldNames().hasNext());
        assertTrue(parameters.get("required").isArray() && parameters.get("required").size() == 0);
    }

    @Test
    void addWebSearch_usesPreviewTypeForDefaultAndStoresLocation() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        provider.addWebSearch("default", "Paris", "FR", "IDF");

        assertEquals(1, provider.toolMap.size());
        Tool tool = provider.toolMap.keySet().iterator().next();
        assertTrue(tool.isWebSearch());
        WebSearchTool webSearchTool = tool.asWebSearch();
        assertEquals("web_search_preview", webSearchTool.type().asString());
        assertTrue(webSearchTool.userLocation().isPresent());
        assertEquals("Paris", webSearchTool.userLocation().get().city().orElse(null));
        assertEquals("FR", webSearchTool.userLocation().get().country().orElse(null));
        assertEquals("IDF", webSearchTool.userLocation().get().region().orElse(null));
    }

    @Test
    void addWebSearch_preservesExplicitType() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        provider.addWebSearch("web_search_preview_2025_03_11", null, null, null);

        Tool tool = provider.toolMap.keySet().iterator().next();
        assertEquals("web_search_preview_2025_03_11", tool.asWebSearch().type().asString());
    }

    @Test
    void addMcpServer_registersConfiguredServer() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        provider.addMcpServer("files", "https://mcp.example", "Bearer token", "Read files");

        assertEquals(1, provider.toolMap.size());
        Tool tool = provider.toolMap.keySet().iterator().next();
        assertTrue(tool.isMcp());
        assertEquals("files", tool.asMcp().serverLabel());
        assertEquals("https://mcp.example", tool.asMcp().serverUrl().orElse(null));
        assertEquals("Bearer token", tool.asMcp().authorization().orElse(null));
        assertEquals("Read files", tool.asMcp().serverDescription().orElse(null));
    }

    @Test
    void prompt_addsUserMessageInput() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        provider.prompt("Hello provider");

        assertEquals(1, provider.inputs.size());
        ResponseInputItem input = provider.inputs.get(0);
        assertTrue(input.isMessage());
        assertEquals("Hello provider", input.asMessage().content().get(0).inputText().get().text());
    }

    @Test
    void clear_removesAccumulatedInputs() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        provider.prompt("one");
        provider.prompt("two");

        provider.clear();

        assertTrue(provider.inputs.isEmpty());
    }

    @Test
    void embedding_returnsNullWhenTextIsNull() {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        assertNull(provider.embedding(null, 128));
    }

    @Test
    void logInputsSpec_writesTextFileAndInvalidEntries() throws Exception {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        provider.prompt("Prompt body");
        provider.inputs.add(ResponseInputItem.ofMessage(com.openai.models.responses.ResponseInputItem.Message.builder()
                .role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
                .addContent(com.openai.models.responses.ResponseInputContent.ofInputFile(
                        ResponseInputFile.builder().fileUrl("https://example/file.txt").build()))
                .build()));
        provider.inputs.add(ResponseInputItem.ofComputerCallOutput(
                com.openai.models.responses.ResponseInputItem.ComputerCallOutput.builder().callId("c1").output(com.openai.models.responses.ResponseComputerToolCallOutputScreenshot.builder().type(com.openai.core.JsonValue.from("computer_screenshot")).build()).build()));
        StringWriter writer = new StringWriter();

        provider.logInputsSpec(writer);

        String log = writer.toString();
        assertTrue(log.contains("Prompt body"));
        assertTrue(log.contains("Add resource by URL: https://example/file.txt"));
        assertFalse(log.contains("ignored"));
    }

    @Test
    void callFunction_returnsNullWhenNoRegisteredFunctionMatches() throws Exception {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        ResponseFunctionToolCall toolCall = (ResponseFunctionToolCall) OpenAIResponseFakes.fakeFunctionCall("missing", "{}",
                "call-1");

        Object result = provider.invokeCallFunction(toolCall);

        assertNull(result);
    }

    @Test
    void callFunction_wrapsInvalidJsonInIllegalArgumentException() throws Exception {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        provider.addTool("tool", "desc", (params, workingDir) -> "ok");
        ResponseFunctionToolCall toolCall = (ResponseFunctionToolCall) OpenAIResponseFakes.fakeFunctionCall("tool", "{broken",
                "call-1");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> provider.invokeCallFunction(toolCall));

        assertNotNull(exception.getCause());
    }

    @Test
    void callFunction_returnsIoFailureMessageFromSafeInvocation() throws Exception {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        provider.addTool("tool", "desc", (params, workingDir) -> {
            throw new IOException("disk failed");
        });
        ResponseFunctionToolCall toolCall = (ResponseFunctionToolCall) OpenAIResponseFakes.fakeFunctionCall("tool", "{}",
                "call-1");

        Object result = provider.invokeCallFunction(toolCall);

        assertTrue(String.valueOf(result).contains("disk failed"));
        assertTrue(String.valueOf(result).contains("tool"));
    }

    @Test
    void getClient_throwsHelpfulErrorWhenModelMissing() throws Exception {
        RealClientOpenAIProvider provider = new RealClientOpenAIProvider();
        provider.setProtectedField("config", TestConfigurators.mapBacked());
        provider.setProtectedField("chatModel", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, provider::getClient);
        assertNotNull(ex.getMessage());
    }

    @Test
    void init_addsConfiguredWebSearchAndMcpServers() {
        TestConfigurators.MapBackedConfigurator config = (TestConfigurators.MapBackedConfigurator) TestConfigurators.mapBacked();
        config.set("MAX_OUTPUT_TOKENS", "321");
        config.set("MAX_TOOL_CALLS", "7");
        config.set("WebSearchTool.type", "default");
        config.set("WebSearchTool.city", "Berlin");
        config.set("WebSearchTool.country", "DE");
        config.set("WebSearchTool.region", "BE");
        config.set("MCP.name", "primary");
        config.set("MCP.url", "https://mcp1.example");
        config.set("MCP.authorization", "Auth1");
        config.set("MCP.description", "Desc1");
        config.set("MCP_1.name", "secondary");
        config.set("MCP_1.url", "https://mcp2.example");

        TestableOpenAIProvider provider = new TestableOpenAIProvider();

        provider.init("gpt-4.1", config);

        assertEquals(321L, provider.getProtectedLong("maxOutputTokens"));
        assertEquals(7L, provider.getProtectedLong("maxToolCalls"));
        assertEquals(3, provider.toolMap.size());
        long webSearchCount = provider.toolMap.keySet().stream().filter(Tool::isWebSearch).count();
        long mcpCount = provider.toolMap.keySet().stream().filter(Tool::isMcp).count();
        assertEquals(1L, webSearchCount);
        assertEquals(2L, mcpCount);
    }

    @Test
    void inputsLog_writesInstructionsAndPromptUsingLfSeparators() throws IOException {
        TestableOpenAIProvider provider = new TestableOpenAIProvider();
        provider.instructions("Rules");
        provider.prompt("Question");
        File logFile = Files.createTempFile("openai-provider", ".log").toFile();
        provider.inputsLog(logFile);

        provider.callProtectedLogInputs();

        String log = new String(Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(log.contains("Rules") && log.contains("Question"));
    }

    private static JsonNode toJson(Map<String, com.openai.core.JsonValue> values) {
        return new ObjectNode(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance,
                values.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().convert(JsonNode.class))));
    }

    private static class ReflectiveTestOpenAIProvider extends OpenAIProvider {

        void setProtectedField(String name, Object value) throws Exception {
            Field field = findField(name);
            field.setAccessible(true);
            field.set(this, value);
        }

        long getProtectedLong(String name) {
            try {
                Field field = findField(name);
                field.setAccessible(true);
                Object value = field.get(this);
                return value == null ? 0L : ((Long) value).longValue();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }

        private Field findField(String name) throws NoSuchFieldException {
            Class<?> type = getClass();
            while (type != null) {
                try {
                    return type.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            throw new NoSuchFieldException(name);
        }
    }

    private static final class TestableOpenAIProvider extends ReflectiveTestOpenAIProvider {

        private Response nextPerformResponse;
        private final ArrayDeque<Response> queuedResponses = new ArrayDeque<Response>();
        private int performCreateCount;
        private int responseCreateCount;
        private int loggedInputsCount;
        private int capturedRequestCount;

        void queueResponses(Response... responses) {
            for (Response response : responses) {
                queuedResponses.add(response);
            }
        }

        @Override
        protected void logInputs() {
            loggedInputsCount++;
            super.logInputs();
        }

        @Override
        public OpenAIClient getClient() {
            return (OpenAIClient) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { OpenAIClient.class },
                    (proxy, method, args) -> {
                        if ("responses".equals(method.getName())) {
                            return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ResponseService.class },
                                    (responseProxy, responseMethod, responseArgs) -> {
                                        if ("create".equals(responseMethod.getName())) {
                                            if (responseArgs != null && responseArgs.length == 1) {
                                                capturedRequestCount++;
                                            }
                                            if (nextPerformResponse != null) {
                                                performCreateCount++;
                                                Response response = nextPerformResponse;
                                                nextPerformResponse = null;
                                                return response;
                                            }
                                            responseCreateCount++;
                                            return queuedResponses.removeFirst();
                                        }
                                        throw new UnsupportedOperationException(responseMethod.getName());
                                    });
                        }
                        if ("models".equals(method.getName())) {
                            return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ModelService.class },
                                    (modelProxy, modelMethod, modelArgs) -> {
                                        if ("list".equals(modelMethod.getName())) {
                                            return (ModelPageStub) () -> java.util.Arrays.asList(Model.builder().id("available-model").build());
                                        }
                                        throw new UnsupportedOperationException(modelMethod.getName());
                                    });
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }

        String invokeParseResponse(Response response) throws Exception {
            return (String) invokePrivate("parseResponse", new Class<?>[] { Response.class }, response);
        }

        Object invokeCallFunction(ResponseFunctionToolCall call) throws Exception {
            return invokePrivate("callFunction", new Class<?>[] { ResponseFunctionToolCall.class }, call);
        }

        void callProtectedLogInputs() {
            super.logInputs();
        }

        private Object invokePrivate(String name, Class<?>[] parameterTypes, Object... args) throws Exception {
            Method method = OpenAIProvider.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            try {
                return method.invoke(this, args);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                }
                throw exception;
            }
        }
    }

    private static final class RealClientOpenAIProvider extends ReflectiveTestOpenAIProvider {
        @Override
        public OpenAIClient getClient() {
            return super.getClient();
        }
    }

    private interface ModelPageStub {
        List<Model> items();
    }
}
