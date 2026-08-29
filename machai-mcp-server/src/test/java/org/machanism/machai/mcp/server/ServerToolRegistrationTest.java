package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.mockito.Mockito;

/** Exercises the transport-specific registration paths with an empty tool set. */
class ServerToolRegistrationTest {

    @Test
    void stdioServerRegistersEmptyToolAndPromptCollections() {
        // Arrange
        StdioMcpServer server = new StdioMcpServer("test", "1");
        Configurator config = Mockito.mock(Configurator.class);
        Mockito.when(config.get("enabledTools", null)).thenReturn(null);
        server.setProjectDir(new File("."));

        // Act / Assert
        assertDoesNotThrow(() -> server.tools(config));
    }

    @Test
    void statelessHttpServerRegistersEmptyToolPromptAndResourceCollections() {
        // Arrange
        HttpStatelessMcpServer server = new HttpStatelessMcpServer("test", "1");
        Configurator config = Mockito.mock(Configurator.class);
        Mockito.when(config.get("enabledTools", null)).thenReturn("  ");
        server.setProjectDir(new File("."));

        // Act / Assert
        assertDoesNotThrow(() -> server.tools(config));
    }

    @Test
    void streamableHttpServerRegistersEmptyToolAndPromptCollections() {
        // Arrange
        HttpStreamableMcpServer server = new HttpStreamableMcpServer("test", "1");
        Configurator config = Mockito.mock(Configurator.class);
        Mockito.when(config.get("enabledTools", null)).thenReturn("toolOne, toolTwo");
        server.setProjectDir(new File("."));

        // Act / Assert
        assertDoesNotThrow(() -> server.tools(config));
    }
}
