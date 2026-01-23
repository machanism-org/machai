package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class ProjectLayoutManagerTest {

    @Test
    void detectProjectLayout_whenPomExists_thenReturnsMavenLayoutAndSetsProjectDir() throws Exception {
        // Arrange
        Path dir = Files.createTempDirectory("machai-maven");
        Files.write(dir.resolve("pom.xml"), "<project/>".getBytes());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir.toFile());

        // Assert
        assertInstanceOf(MavenProjectLayout.class, layout);
        assertEquals(dir.toFile().getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
    }

    @Test
    void detectProjectLayout_whenPackageJsonExists_thenReturnsJScriptLayoutAndSetsProjectDir() throws Exception {
        // Arrange
        Path dir = Files.createTempDirectory("machai-js");
        Files.write(dir.resolve("package.json"), "{}".getBytes());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir.toFile());

        // Assert
        assertInstanceOf(JScriptProjectLayout.class, layout);
        assertEquals(dir.toFile().getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
    }

    @Test
    void detectProjectLayout_whenPythonProject_thenReturnsPythonLayoutAndSetsProjectDir() throws Exception {
        // Arrange
        Path dir = Files.createTempDirectory("machai-py");
        Files.write(dir.resolve("pyproject.toml"), ("[project]\n" + "name='demo'\n").getBytes());

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir.toFile());

        // Assert
        assertInstanceOf(PythonProjectLayout.class, layout);
        assertEquals(dir.toFile().getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
    }

    @Test
    void detectProjectLayout_whenDirectoryExistsButNoKnownLayout_thenReturnsDefaultLayout() throws Exception {
        // Arrange
        Path dir = Files.createTempDirectory("machai-default");

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir.toFile());

        // Assert
        assertInstanceOf(DefaultProjectLayout.class, layout);
        assertEquals(dir.toFile().getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
    }

    @Test
    void detectProjectLayout_whenDirectoryDoesNotExist_thenThrowsFileNotFoundException() throws Exception {
        // Arrange
        Path parent = Files.createTempDirectory("machai-missing");
        File missing = parent.resolve("does-not-exist").toFile();

        // Act / Assert
        assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(missing));
    }
}
