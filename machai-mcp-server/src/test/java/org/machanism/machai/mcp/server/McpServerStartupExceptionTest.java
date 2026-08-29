package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class McpServerStartupExceptionTest {

    @Test
    void retainsCauseAndProvidesAUsefulStartupMessage() {
        // Arrange
        Exception cause = new IllegalStateException("Jetty failed");

        // Act
        McpServerStartupException exception = new McpServerStartupException(cause);

        // Assert
        assertEquals("Unable to start the MCP server.", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
