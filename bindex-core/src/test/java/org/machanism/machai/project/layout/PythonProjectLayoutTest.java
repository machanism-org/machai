package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for PythonProjectLayout.
 */
class PythonProjectLayoutTest {
    
    @Test
    void isPythonProject_returnsFalse_forDirectoryWithoutModelFile(@TempDir Path tempDir) {
        // Arrange
        File dir = tempDir.toFile();
        // Act
        boolean result = PythonProjectLayout.isPythonProject(dir);
        // Assert
        assertFalse(result);
    }

    @Test
    void isPythonProject_returnsFalse_forPyprojectWithoutProjectName(@TempDir Path tempDir) throws IOException {
        // Arrange
        String pyproject = "[project]\n";
        Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes());
        File dir = tempDir.toFile();
        // Act
        boolean result = PythonProjectLayout.isPythonProject(dir);
        // Assert
        assertFalse(result);
    }

    @Test
    void isPythonProject_returnsTrue_forValidNonPrivateProject(@TempDir Path tempDir) throws IOException {
        // Arrange
        String pyproject = "[project]\nname = 'testapp'\n";
        Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes());
        File dir = tempDir.toFile();
        // Act
        boolean result = PythonProjectLayout.isPythonProject(dir);
        // Assert
        assertTrue(result);
    }

    @Test
    void isPythonProject_returnsFalse_forPrivateProjectClassifier(@TempDir Path tempDir) throws IOException {
        // Arrange
        String pyproject = "[project]\nname = 'testapp'\nclassifiers = ['Private', 'Other']\n";
        Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes());
        File dir = tempDir.toFile();
        // Act
        boolean result = PythonProjectLayout.isPythonProject(dir);
        // Assert
        assertFalse(result);
    }

    @Test
    void getSources_returnsNull() {
        // Act & Assert
        assertNull(new PythonProjectLayout().getSources());
    }

    @Test
    void getDocuments_returnsNull() {
        // Act & Assert
        assertNull(new PythonProjectLayout().getDocuments());
    }

    @Test
    void getTests_returnsNull() {
        // Act & Assert
        assertNull(new PythonProjectLayout().getTests());
    }
}
