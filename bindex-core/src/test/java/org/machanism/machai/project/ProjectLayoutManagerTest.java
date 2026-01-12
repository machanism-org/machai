package org.machanism.machai.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutManagerTest {

    @Test
    void detectsMavenProjectLayout(@TempDir Path tempDir) throws Exception {
        // Arrange
        File projectDir = tempDir.toFile();
        new PrintWriter(new File(projectDir, "pom.xml"), "UTF-8").close();

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

        // Assert
        assertTrue(layout instanceof MavenProjectLayout);
        assertEquals(projectDir, layout.getProjectDir());
    }

    @Test
    void detectsJScriptProjectLayout(@TempDir Path tempDir) throws Exception {
        // Arrange
        File projectDir = tempDir.toFile();
        new PrintWriter(new File(projectDir, "package.json"), "UTF-8").close();

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

        // Assert
        assertTrue(layout instanceof JScriptProjectLayout);
        assertEquals(projectDir, layout.getProjectDir());
    }

    @Test
    void detectsPythonProjectLayout(@TempDir Path tempDir) throws Exception {
        // Arrange
        File projectDir = tempDir.toFile();
        PrintWriter writer = new PrintWriter(new File(projectDir, "pyproject.toml"), "UTF-8");
        writer.println("project.name = \"test\"");
        writer.close();

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

        // Assert
        assertTrue(layout instanceof PythonProjectLayout);
        assertEquals(projectDir, layout.getProjectDir());
    }

    @Test
    void usesDefaultLayoutIfNoKnownMarkers(@TempDir Path tempDir) throws Exception {
        // Arrange
        File projectDir = tempDir.toFile();

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

        // Assert
        assertTrue(layout instanceof DefaultProjectLayout);
        assertEquals(projectDir, layout.getProjectDir());
    }

    @Test
    void throwsFileNotFoundIfDirDoesNotExist() {
        // Arrange
        File missingDir = new File("/some/invalid/123456dir");

        // Act & Assert
        assertThrows(java.io.FileNotFoundException.class, () ->
            ProjectLayoutManager.detectProjectLayout(missingDir));
    }
}
