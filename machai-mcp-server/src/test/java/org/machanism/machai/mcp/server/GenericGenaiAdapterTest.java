package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.mockito.Mockito;

import io.modelcontextprotocol.spec.McpSchema;

class GenericGenaiAdapterTest {

    @Test
    void toHumanReadableHandlesNullEmptyCamelCaseAndHyphens() {
        assertEquals("", GenericGenaiAdapter.toHumanReadable(null));
        assertEquals("", GenericGenaiAdapter.toHumanReadable(""));
        assertEquals("My Tool Name", GenericGenaiAdapter.toHumanReadable("myToolName"));
        assertEquals("Already Kebab", GenericGenaiAdapter.toHumanReadable("Already-Kebab"));
    }

    @Test
    void addToolBuildsSchemaAndHandlerReturnsStringResult() {
        List<Object> specifications = new ArrayList<>();
        CapturingBuilder builder = new CapturingBuilder();
        TestAdapter adapter = new TestAdapter(specifications, builder);
        ParamDescriptor required = new ParamDescriptor("query", "string", true, "Search text", null);
        ParamDescriptor optionalArray = new ParamDescriptor("tags", "array", false, "Tags", List.of("a"));

        adapter.register("findItems", "Find items", (params, ignored) -> "found:" + params.get("query").asText(),
                required, optionalArray);

        assertEquals(1, specifications.size());
        assertEquals("Find Items", builder.tool.title());
        assertEquals("Find items", builder.tool.description());
        assertEquals(List.of("query"), builder.tool.inputSchema().get("required"));
        assertEquals("object", builder.tool.inputSchema().get("type"));

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("query", "books");
        McpSchema.CallToolResult result = builder.handler.apply(null, callToolRequest("findItems", arguments));

        assertFalse(result.isError());
        assertEquals("found:books", ((McpSchema.TextContent) result.content().get(0)).text());
    }

    @Test
    void addToolSerializesNonStringResultsAndConvertsExceptionsToErrorResult() {
        CapturingBuilder builder = new CapturingBuilder();
        TestAdapter adapter = new TestAdapter(new ArrayList<>(), builder);
        adapter.register("object", "desc", (params, ignored) -> Map.of("answer", 42));

        McpSchema.CallToolResult serialized = builder.handler.apply(null,
                callToolRequest("object", new HashMap<>()));
        assertFalse(serialized.isError());
        assertTrue(((McpSchema.TextContent) serialized.content().get(0)).text().contains("\"answer\":42"));

        adapter.register("broken", "desc", (params, ignored) -> { throw new IllegalStateException("boom"); });
        McpSchema.CallToolResult failed = builder.handler.apply(null,
                callToolRequest("broken", new HashMap<>()));
        assertTrue(failed.isError());
        assertEquals("boom", ((McpSchema.TextContent) failed.content().get(0)).text());
        assertNull(adapter.perform());
    }

    @Test
    void abstractServerStoresProjectDirectoryAndSplitsConfiguredToolNames() {
        TestServer server = new TestServer();
        java.io.File directory = new java.io.File("project");
        server.setProjectDir(directory);
        Configurator config = Mockito.mock(Configurator.class);
        Mockito.when(config.get("enabledTools", null)).thenReturn("one, two;three\tfour");

        assertEquals(directory, server.getProjectDir());
        assertEquals(List.of("one", "two", "three", "four"), List.of(server.enabled(config)));
        Configurator emptyConfig = Mockito.mock(Configurator.class);
        Mockito.when(emptyConfig.get("enabledTools", null)).thenReturn(null);
        assertNull(server.enabled(emptyConfig));
    }

    private static final class CapturingBuilder implements AbstractMcpServer.ToolSpecificationBuilder<Object> {
        private McpSchema.Tool tool;
        private BiFunction<Object, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler;

        @Override
        public Object buildSpecification(Object tool,
                BiFunction<Object, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler) {
            this.tool = (McpSchema.Tool) tool;
            this.handler = callHandler;
            return tool;
        }
    }

    private static McpSchema.CallToolRequest callToolRequest(String name, Map<String, Object> arguments) {
        return McpSchema.CallToolRequest.builder(name).arguments(arguments).build();
    }

    private static final class TestAdapter extends GenericGenaiAdapter<Object, Object> {
        TestAdapter(List<Object> specs, AbstractMcpServer.ToolSpecificationBuilder<Object> builder) {
            super(specs, builder);
        }
        void register(String name, String description, ToolFunction function, ParamDescriptor... params) {
            addTool(name, description, function, params);
        }
    }

    private static final class TestServer extends AbstractMcpServer {
        String[] enabled(Configurator config) { return getEnabledTools(config); }
        @Override void tools(Configurator config) {
            // SonarQube S1186: this test double deliberately has no tools to register.
        }
        @Override void start() {
            // SonarQube S1186: this test double must not start an external server.
        }
    }
}
