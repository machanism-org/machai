package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServlet;

class HttpServerInfrastructureTest {

    private static final class TestHttpServer extends AbstractHttpMcpServer {
        @Override void tools(org.machanism.macha.core.commons.configurator.Configurator config) {
            // SonarQube S1186: this infrastructure test requires no tool registration.
        }

        @Override void start() {
            // SonarQube S1186: this infrastructure test must not start an HTTP server.
        }
    }

    @Test
    void httpBaseStoresPortAndTransportProvider() {
        TestHttpServer server = new TestHttpServer();
        HttpServlet servlet = new HttpServlet() { private static final long serialVersionUID = 1L; };

        server.setPort(8123);
        server.setTransportProvider(servlet);

        assertEquals(8123, server.getPort());
        assertSame(servlet, server.getTransportProvider());
    }

    @Test
    void httpBaseReportsJettyStartupFailureForAnInvalidPort() {
        // Arrange: a negative port makes Jetty fail before it can enter join().
        TestHttpServer server = new TestHttpServer();
        server.setPort(-1);
        server.setTransportProvider(new HttpServlet() { private static final long serialVersionUID = 1L; });

        // Act / Assert
        assertThrows(Exception.class, server::startHttpServer);
    }

    @Test
    void httpServersWrapInfrastructureStartupFailures() {
        // Arrange
        HttpStatelessMcpServer stateless = new HttpStatelessMcpServer("test", "1");
        HttpStreamableMcpServer streamable = new HttpStreamableMcpServer("test", "1");
        stateless.setPort(-1);
        streamable.setPort(-1);

        // Act / Assert
        assertThrows(McpServerStartupException.class, stateless::start);
        assertThrows(McpServerStartupException.class, streamable::start);
    }

    @Test
    void concreteServersExposeConfiguredTransportAndBuildToolSpecifications() {
        HttpStatelessMcpServer stateless = new HttpStatelessMcpServer("test", "1");
        HttpStreamableMcpServer streamable = new HttpStreamableMcpServer("test", "1");
        McpSchema.Tool tool = McpSchema.Tool.builder("tool", Map.of("type", "object")).build();

        assertNotNull(stateless.getTransportProvider());
        assertNotNull(streamable.getTransportProvider());
        assertSame(tool, stateless.new HttpStatelessToolSpecificationBuilder()
                .buildSpecification(tool, (exchange, request) -> McpSchema.CallToolResult.builder().build()).tool());
        assertSame(tool, streamable.new HttpStreamableToolSpecificationBuilder()
                .buildSpecification(tool, (exchange, request) -> McpSchema.CallToolResult.builder().build()).tool());
    }

    @Test
    void stdioToolBuilderRetainsToolAndHandler() {
        StdioMcpServer server = new StdioMcpServer("test", "1");
        McpSchema.Tool tool = McpSchema.Tool.builder("tool", new HashMap<>()).build();

        var specification = server.new StdioToolSpecificationBuilder()
                .buildSpecification(tool, (exchange, request) -> McpSchema.CallToolResult.builder().build());

        assertSame(tool, specification.tool());
        assertNotNull(specification.callHandler());
    }
}
