package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.ToolFunction;

import com.anthropic.models.beta.messages.BetaToolUseBlock;
import com.anthropic.models.beta.messages.BetaToolUseBlockParam;

/** Covers token-refresh provider extensions and Anthropic local-tool dispatch. */
class CodeMieProviderExtensionAndAnthropicToolTest {

    @Test
    void openAiExtensionRefreshesTokenBeforeCreatingClient() throws Exception {
        // Arrange
        AtomicReference<String> requestBody = new AtomicReference<>();
        com.sun.net.httpserver.HttpServer server = tokenServer(requestBody);
        try {
            Object extension = extension("OpenAIProviderExtension", "service-client", endpoint(server), "secret");
            initialize((OpenAIProvider) extension, "gpt-test");

            // Act
            Object client = invokeGetClient(extension);

            // Assert
            assertNotNull(client);
            assertEquals("grant_type=client_credentials&client_id=service-client&client_secret=secret", requestBody.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void claudeExtensionRefreshesPasswordGrantTokenBeforeCreatingClient() throws Exception {
        // Arrange
        AtomicReference<String> requestBody = new AtomicReference<>();
        com.sun.net.httpserver.HttpServer server = tokenServer(requestBody);
        try {
            Object extension = extension("ClaudeProviderExtension", "person@example.com", endpoint(server), "secret value");
            initialize((AnthropicProvider) extension, "claude-test");

            // Act
            Object client = invokeGetClient(extension);

            // Assert
            assertNotNull(client);
            assertEquals("grant_type=password&client_id=codemie-sdk&username=person%40example.com&password=secret+value", requestBody.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void anthropicToolDispatchNormalizesNamesAndReturnsHandlerResult() throws Exception {
        // Arrange
        AnthropicProvider provider = new AnthropicProvider();
        provider.init("claude-test", TestConfigurators.mapBacked());
        Method addTool = AnthropicProvider.class.getDeclaredMethod("addTool", String.class, String.class,
                ToolFunction.class, org.machanism.machai.ai.tools.ParamDescriptor[].class);
        addTool.setAccessible(true);
        addTool.invoke(provider, "Lookup_Value", "description", (ToolFunction) (params, context) -> "handled",
                new org.machanism.machai.ai.tools.ParamDescriptor[0]);
        BetaToolUseBlock toolUse = mock(BetaToolUseBlock.class);
        BetaToolUseBlockParam parameter = mock(BetaToolUseBlockParam.class);
        when(toolUse.name()).thenReturn("lookup_value");
        when(toolUse.toParam()).thenReturn(parameter);
        when(parameter._input()).thenReturn(null);
        Method callFunction = AnthropicProvider.class.getDeclaredMethod("callFunction", BetaToolUseBlock.class);
        callFunction.setAccessible(true);

        // Act
        Object result = callFunction.invoke(provider, toolUse);

        // Assert
        assertEquals("handled", result);
    }

    private static Object extension(String simpleName, String username, String url, String password) throws Exception {
        Class<?> type = Class.forName(CodeMieProvider.class.getName() + "$" + simpleName);
        Constructor<?> constructor = type.getDeclaredConstructor(CodeMieProvider.class, String.class, String.class, String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(new CodeMieProvider(), username, url, password);
    }

    private static void initialize(org.machanism.machai.ai.provider.Genai provider, String model) {
        org.machanism.macha.core.commons.configurator.MutableConfigurator config = org.mockito.Mockito.mock(
                org.machanism.macha.core.commons.configurator.MutableConfigurator.class);
        when(config.get(OpenAIProvider.OPENAI_API_KEY)).thenReturn("initial-key");
        when(config.get(OpenAIProvider.OPENAI_BASE_URL_NAME)).thenReturn("http://localhost/api");
        when(config.get(AnthropicProvider.ANTHROPIC_API_KEY)).thenReturn("initial-token");
        when(config.get(AnthropicProvider.ANTHROPIC_BASE_URL, null)).thenReturn("http://localhost/api");
        when(config.getLong(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        provider.init(model, config);
    }

    private static Object invokeGetClient(Object extension) throws Exception {
        Method method = extension.getClass().getDeclaredMethod("getClient");
        method.setAccessible(true);
        return method.invoke(extension);
    }

    private static com.sun.net.httpserver.HttpServer tokenServer(AtomicReference<String> requestBody) throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/token", exchange -> {
            java.io.ByteArrayOutputStream request = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = exchange.getRequestBody().read(buffer)) != -1) {
                request.write(buffer, 0, read);
            }
            requestBody.set(new String(request.toByteArray(), StandardCharsets.UTF_8));
            byte[] response = "{\"access_token\":\"fresh-token\",}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return server;
    }

    private static String endpoint(com.sun.net.httpserver.HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort() + "/token";
    }
}
