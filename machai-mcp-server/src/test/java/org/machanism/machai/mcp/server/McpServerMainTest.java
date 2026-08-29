package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileWriter;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

/** Verifies command-line mode selection without starting real transports. */
class McpServerMainTest {

    @Test
    void mainCreatesAndConfiguresStdioServerWhenPortIsAbsent() throws Exception {
        // Arrange
        File config = configurationFile();
        try (MockedConstruction<StdioMcpServer> construction = Mockito.mockConstruction(StdioMcpServer.class)) {
            // Act
            McpServer.main(new String[] { "-c", config.getAbsolutePath(), "-n", "named", "-v", "2" });

            // Assert
            StdioMcpServer server = construction.constructed().get(0);
            verify(server).setProjectDir(null);
            verify(server).tools(any());
            verify(server).start();
        }
    }

    @Test
    void mainCreatesStatelessHttpServerAndAppliesPortAndProjectDirectory() throws Exception {
        // Arrange
        File config = configurationFile();
        File project = new File(".");
        try (MockedConstruction<HttpStatelessMcpServer> construction = Mockito.mockConstruction(HttpStatelessMcpServer.class)) {
            // Act
            McpServer.main(new String[] { "-c", config.getAbsolutePath(), "-p", "8123", "-d", project.getPath() });

            // Assert
            HttpStatelessMcpServer server = construction.constructed().get(0);
            verify(server).setPort(8123);
            verify(server).setProjectDir(any(File.class));
            verify(server).tools(any());
            verify(server).start();
        }
    }

    @Test
    void mainCreatesStreamableHttpServerWhenSessionOptionIsPresent() throws Exception {
        // Arrange
        File config = configurationFile();
        try (MockedConstruction<HttpStreamableMcpServer> construction = Mockito.mockConstruction(HttpStreamableMcpServer.class)) {
            // Act
            McpServer.main(new String[] { "-c", config.getAbsolutePath(), "--port", "8124", "--session" });

            // Assert
            HttpStreamableMcpServer server = construction.constructed().get(0);
            verify(server).setPort(8124);
            verify(server).tools(any());
            verify(server).start();
        }
    }

    @Test
    void mainAcceptsHelpOptionWhileContinuingWithDefaultMode() throws Exception {
        // Arrange
        File config = configurationFile();
        try (MockedConstruction<StdioMcpServer> construction = Mockito.mockConstruction(StdioMcpServer.class)) {
            // Act / Assert
            assertDoesNotThrow(() -> McpServer.main(new String[] { "--help", "-c", config.getAbsolutePath() }));
            verify(construction.constructed().get(0)).start();
        }
    }

    private static File configurationFile() throws Exception {
        File file = File.createTempFile("mcp-main", ".properties");
        file.deleteOnExit();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# test configuration\n");
        }
        return file;
    }
}
