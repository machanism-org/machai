package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class JScriptProjectLayoutTest {

    @Test
    void getModules_whenNoWorkspaces_thenReturnsNull() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-js-layout-no-workspaces");
        Files.write(root.resolve("package.json"), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));

        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(root.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNull(modules);
    }

    @Test
    void getModules_whenWorkspacesContainGlob_thenFindsNestedPackageJsonAndExcludesNodeModules() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-js-layout-workspaces");
        Files.write(root.resolve("package.json"), "{\"workspaces\":[\"packages/**\"]}".getBytes(StandardCharsets.UTF_8));

        Path packages = Files.createDirectories(root.resolve("packages"));
        Path a = Files.createDirectories(packages.resolve("a"));
        Files.write(a.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

        Path nodeModules = Files.createDirectories(packages.resolve("node_modules").resolve("ignored"));
        Files.write(nodeModules.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(root.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(List.of("packages/a"), modules);
    }
}
