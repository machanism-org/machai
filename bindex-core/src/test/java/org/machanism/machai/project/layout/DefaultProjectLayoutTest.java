package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class DefaultProjectLayoutTest {

    @Test
    void getModules_whenNoChildProjects_thenReturnsEmptyListAndCachesResult() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-default-layout-empty");
        Files.createDirectories(root.resolve("target"));
        Files.createDirectories(root.resolve(".git"));

        DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(root.toFile());

        // Act
        List<String> modules1 = layout.getModules();
        List<String> modules2 = layout.getModules();

        // Assert
        assertNotNull(modules1);
        assertEquals(0, modules1.size());
        assertEquals(modules1, modules2, "Expected module list to be cached");
    }

    @Test
    void getModules_whenContainsNonDefaultChildLayout_thenIncludesChildDirectoryAsModule() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-default-layout-modules");
        Files.createDirectories(root.resolve("module-a"));
        Files.write(root.resolve("module-a").resolve("pom.xml"), "<project/>".getBytes());

        DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(root.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(List.of("module-a"), modules);
    }

    @Test
    void getModules_whenChildDirectoryIsExcluded_thenDoesNotIncludeExcludedDirectory() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-default-layout-excluded");
        Files.createDirectories(root.resolve("target"));
        Files.write(root.resolve("target").resolve("pom.xml"), "<project/>".getBytes());

        DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(root.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(0, modules.size());
    }
}
