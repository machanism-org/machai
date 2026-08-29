package org.machanism.machai.mcp.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;

import org.junit.jupiter.api.Test;

class McpServerTest {

    @Test
    void getConfiguratorLoadsExplicitFileAndToleratesMissingDefaultFile() throws Exception {
        File properties = File.createTempFile("mcp", ".properties");
        try (FileWriter writer = new FileWriter(properties)) {
            writer.write("key=value\n");
        }

        assertNotNull(McpServer.getConfigurator(properties.getAbsolutePath()));
        assertNotNull(McpServer.getConfigurator(null));
        assertThrows(java.io.IOException.class, () -> McpServer.getConfigurator("missing-file.properties"));
    }
}
