package org.machanism.machai.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;
import org.machanism.machai.project.layout.DefaultProjectLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutManagerTest {
    @TempDir
    Path tempDir;

    private File rootDir;

    @BeforeEach
    void setup() {
        rootDir = tempDir.toFile();
    }

    @Test
    void testDetectMavenProjectLayout() throws IOException {
        // Arrange
        File pomFile = new File(rootDir, "pom.xml");
        assertTrue(pomFile.createNewFile());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(rootDir);

        // Assert
        assertTrue(layout instanceof MavenProjectLayout);
    }

    @Test
    void testDetectJScriptProjectLayout() throws IOException {
        // Arrange
        File packageJson = new File(rootDir, "package.json");
        assertTrue(packageJson.createNewFile());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(rootDir);

        // Assert
        assertTrue(layout instanceof JScriptProjectLayout);
    }

    @Test
    void testDetectPythonProjectLayout() throws IOException {
        // Arrange
        File pyprojectToml = new File(rootDir, "pyproject.toml");
        Files.write(pyprojectToml.toPath(), "project.name = 'example'".getBytes());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(rootDir);

        // Assert
        assertTrue(layout instanceof PythonProjectLayout);
    }

    @Test
    void testDetectDefaultProjectLayout() throws IOException {
        // Arrange: No special files present.

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(rootDir);

        // Assert
        assertTrue(layout instanceof DefaultProjectLayout);
    }

    @Test
    void testDetectThrowsWhenDirDoesNotExist() {
        // Arrange
        File nonExistent = new File(tempDir.toFile(), "does-not-exist");

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(nonExistent));
    }
}
