package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PythonProjectLayoutTest {

    @Test
    void isPythonProject_whenPyprojectHasNameAndNotPrivate_thenReturnsTrue() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-python-layout");
        String toml = "" +
                "[project]\n" +
                "name = 'demo'\n" +
                "classifiers = ['Programming Language :: Python']\n";
        Files.write(root.resolve("pyproject.toml"), toml.getBytes(StandardCharsets.UTF_8));

        // Act
        boolean result = PythonProjectLayout.isPythonProject(root.toFile());

        // Assert
        assertEquals(true, result);
    }

    @Test
    void isPythonProject_whenClassifierContainsPrivate_thenReturnsFalse() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-python-layout-private");
        String toml = "" +
                "[project]\n" +
                "name = 'demo'\n" +
                "classifiers = ['Private :: Do Not Publish']\n";
        Files.write(root.resolve("pyproject.toml"), toml.getBytes(StandardCharsets.UTF_8));

        // Act
        boolean result = PythonProjectLayout.isPythonProject(root.toFile());

        // Assert
        assertEquals(false, result);
    }

    @Test
    void isPythonProject_whenTomlMissing_thenReturnsFalse() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-python-layout-missing");

        // Act
        boolean result = PythonProjectLayout.isPythonProject(root.toFile());

        // Assert
        assertEquals(false, result);
    }
}
