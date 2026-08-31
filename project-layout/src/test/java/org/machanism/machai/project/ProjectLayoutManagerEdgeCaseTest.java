package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.GradleProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

/** Additional boundary and precedence tests for the package-level layout manager. */
class ProjectLayoutManagerEdgeCaseTest {

    @TempDir
    Path tempDirectory;

    @Test
    void detectProjectLayout_shouldPreferGradleOverNodeAndPythonMarkers() throws Exception {
        // Arrange
        Files.createFile(tempDirectory.resolve("build.gradle"));
        Files.createFile(tempDirectory.resolve("package.json"));
        Files.createFile(tempDirectory.resolve("pyproject.toml"));

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDirectory.toFile());

        // Assert
        assertInstanceOf(GradleProjectLayout.class, layout);
    }

    @Test
    void detectProjectLayout_shouldPreferNodeOverPythonMarker() throws Exception {
        // Arrange
        Files.createFile(tempDirectory.resolve("package.json"));
        Files.createFile(tempDirectory.resolve("pyproject.toml"));

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDirectory.toFile());

        // Assert
        assertInstanceOf(JScriptProjectLayout.class, layout);
    }

    @Test
    void detectProjectLayout_shouldUseDefaultLayoutForAnExistingRegularFile() throws Exception {
        // Arrange
        Path regularFile = tempDirectory.resolve("not-a-directory");
        Files.createFile(regularFile);

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(regularFile.toFile());

        // Assert
        assertInstanceOf(DefaultProjectLayout.class, layout);
    }

    @Test
    void detectProjectLayout_shouldRecognizePythonProjectWithPyprojectTomlMarker() throws Exception {
        // Arrange
        Files.write(tempDirectory.resolve("pyproject.toml"),
                "[project]\nname = 'demo'\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDirectory.toFile());

        // Assert
        assertInstanceOf(PythonProjectLayout.class, layout);
    }

    @Test
    void detectProjectLayout_shouldRecognizeMavenBeforeEveryOtherSupportedLayout() throws Exception {
        // Arrange
        Files.createFile(tempDirectory.resolve("pom.xml"));
        Files.createFile(tempDirectory.resolve("package.json"));
        Files.createFile(tempDirectory.resolve("pyproject.toml"));

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDirectory.toFile());

        // Assert
        assertInstanceOf(MavenProjectLayout.class, layout);
    }
}
