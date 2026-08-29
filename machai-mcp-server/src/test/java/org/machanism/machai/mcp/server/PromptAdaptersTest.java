package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.ToolFunction;

import io.modelcontextprotocol.spec.McpSchema;

class PromptAdaptersTest {

    @Test
    void stdioPromptSupportsStringListJsonAndFailureResults() {
        StdioGenaiAdapter adapter = new StdioGenaiAdapter(new ArrayList<>(), (tool, handler) -> tool);
        ParamDescriptor parameter = new ParamDescriptor("userName", "string", true, "User", null);

        registerAndAssertStreamable(adapter, parameter, (params, ignored) -> "hello");
        registerAndAssertStreamable(adapter, parameter, (params, ignored) -> List.of("one", "two"));
        registerAndAssertStreamable(adapter, parameter, (params, ignored) -> Map.of("answer", 42));
        registerAndAssertStreamable(adapter, parameter, (params, ignored) -> { throw new IllegalArgumentException("bad input"); });
    }

    @Test
    void httpStreamablePromptAddsSessionIdAndProducesMessages() {
        HttpStreamableGenericGenaiAdapter adapter = new HttpStreamableGenericGenaiAdapter(new ArrayList<>(), (tool, handler) -> tool);
        adapter.addPrompt("welcomeUser", "Welcome", (params, ignored) -> "hello " + params.get("userName").asText(),
                Role.USER, new ParamDescriptor("userName", "string", true, "User", null));

        Map<String, Object> arguments = new HashMap<>(Map.of("userName", "Ada"));
        McpSchema.GetPromptResult result = adapter.getPrompts().get(0).promptHandler().apply(null,
                promptRequest("welcomeUser", arguments));

        assertEquals("Welcome User", adapter.getPrompts().get(0).prompt().title());
        assertEquals("hello Ada", text(result, 0));
        assertEquals("Ada", arguments.get("userName"));
    }

    @Test
    void httpStreamablePromptHandlesListJsonAndExceptions() {
        // Arrange
        HttpStreamableGenericGenaiAdapter adapter = new HttpStreamableGenericGenaiAdapter(new ArrayList<>(),
                (tool, handler) -> tool);
        adapter.addPrompt("listPrompt", "Description", (params, ignored) -> List.of("one", "two"), Role.USER);
        adapter.addPrompt("jsonPrompt", "Description", (params, ignored) -> Map.of("answer", 42), Role.USER);
        adapter.addPrompt("failedPrompt", "Description", (params, ignored) -> {
            throw new IllegalStateException("failed");
        }, Role.USER);

        // Act / Assert
        assertEquals("one", text(adapter.getPrompts().get(0).promptHandler().apply(null,
                promptRequest("listPrompt", new HashMap<>())), 0));
        assertEquals("two", text(adapter.getPrompts().get(0).promptHandler().apply(null,
                promptRequest("listPrompt", new HashMap<>())), 1));
        assertEquals("{\"answer\":42}", text(adapter.getPrompts().get(1).promptHandler().apply(null,
                promptRequest("jsonPrompt", new HashMap<>())), 0));
        assertEquals("failed", text(adapter.getPrompts().get(2).promptHandler().apply(null,
                promptRequest("failedPrompt", new HashMap<>())), 0));
    }

    @Test
    void statelessPromptAndResourceHandleValuesAndResourceFailure() {
        HttpStatelessGenericGenaiAdapter adapter = new HttpStatelessGenericGenaiAdapter(new ArrayList<>(), (tool, handler) -> tool);
        adapter.addPrompt("welcomeUser", "Welcome", (params, ignored) -> List.of("one", "two"), Role.ASSISTANT,
                new ParamDescriptor("userName", "string", false, "User", null));
        McpSchema.GetPromptResult prompt = adapter.getPrompts().get(0).promptHandler().apply(null,
                promptRequest("welcomeUser", new HashMap<>()));
        assertEquals("one", text(prompt, 0));
        assertEquals("two", text(prompt, 1));

        URI uri = URI.create("file:///docs/readme.txt");
        adapter.addResource(uri, "description", "text/plain", (params, ignored) -> "content");
        McpSchema.ReadResourceResult resource = adapter.getResources().get(0).readHandler().apply(null,
                resourceRequest(uri));
        assertEquals("content", ((McpSchema.TextResourceContents) resource.contents().get(0)).text());

        adapter.addResource(uri, "description", "text/plain", (params, ignored) -> { throw new Exception("failed"); });
        try {
            adapter.getResources().get(1).readHandler().apply(null, resourceRequest(uri));
        } catch (IllegalArgumentException exception) {
            assertEquals("failed", exception.getCause().getMessage());
            return;
        }
        throw new AssertionError("Expected resource handler failure");
    }

    private static void registerAndAssertStreamable(StdioGenaiAdapter adapter, ParamDescriptor parameter,
            ToolFunction function) {
        adapter.addPrompt("prompt", "Description", function, Role.USER, parameter);
        McpSchema.GetPromptResult result = adapter.getPrompts().get(adapter.getPrompts().size() - 1).promptHandler()
                .apply(null, promptRequest("prompt", new HashMap<>(Map.of("userName", "Ada"))));
        assertTrue(result.messages().size() >= 1);
    }

    private static String text(McpSchema.GetPromptResult result, int index) {
        return ((McpSchema.TextContent) result.messages().get(index).content()).text();
    }

    private static McpSchema.GetPromptRequest promptRequest(String name, Map<String, Object> arguments) {
        return McpSchema.GetPromptRequest.builder(name).arguments(arguments).build();
    }

    private static McpSchema.ReadResourceRequest resourceRequest(URI uri) {
        return McpSchema.ReadResourceRequest.builder(uri.toString()).build();
    }
}
