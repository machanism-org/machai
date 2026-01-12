package org.machanism.machai.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutManagerTest {

    @Test
    void testDetectMavenProjectLayout(@TempDir File tempDir) throws IOException {
        // Arrange
        File pom = new File(tempDir, "pom.xml");
        assertTrue(pom.createNewFile());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);

        // Assert
        assertTrue(layout instanceof MavenProjectLayout);
        assertEquals(tempDir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
    }

    @Test
    void testDetectJScriptProjectLayout(@TempDir File tempDir) throws IOException {
        // Arrange
        File pkgJson = new File(tempDir, "package.json");
        assertTrue(pkgJson.createNewFile());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);

        // Assert
        assertTrue(layout instanceof JScriptProjectLayout);
    }

    @Test
    @Disabled
    void testDetectPythonProjectLayout(@TempDir File tempDir) throws IOException {
        // Arrange
        File pyFile = new File(tempDir, "main.py");
        assertTrue(pyFile.createNewFile());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);

        // Assert
        assertTrue(layout instanceof PythonProjectLayout);
    }

    @Test
    void testDetectDefaultProjectLayout(@TempDir File tempDir) throws IOException {
        // Arrange: no indicators present
        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);
        // Assert
        assertTrue(layout instanceof DefaultProjectLayout);
    }

    @Test
    void testDetectProjectLayoutThrowsIfNotFound() {
        // Arrange
        File notFound = new File("/does/not/exist/" + System.currentTimeMillis());
        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(notFound));
    }

}
