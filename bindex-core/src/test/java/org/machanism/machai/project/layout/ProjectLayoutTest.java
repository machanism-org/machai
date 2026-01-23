package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

    @Test
    void getRelatedPath_instanceMethod_stripsCurrentPathAndLeadingSlash() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-related-path-instance");
        Path file = Files.createDirectories(root.resolve("a")).resolve("b.txt");
        Files.write(file, "x".getBytes());

        ProjectLayout layout = new MavenProjectLayout().projectDir(root.toFile());
        String current = root.toFile().getAbsolutePath().replace("\\", "/");

        // Act
        String related = layout.getRelatedPath(current, file.toFile());

        // Assert
        assertEquals("a/b.txt", related);
    }

    @Test
    void getRelatedPath_static_whenFileIsDir_thenReturnsDot() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-related-path-dot");

        // Act
        String related = ProjectLayout.getRelatedPath(root.toFile(), root.toFile());

        // Assert
        assertEquals(".", related);
    }

    @Test
    void getRelatedPath_static_whenAddSingleDotAndNotDotPrefixed_thenPrefixesDotSlash() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-related-path-dot-prefix");
        Path f = Files.write(root.resolve("x.txt"), "x".getBytes());

        // Act
        String related = ProjectLayout.getRelatedPath(root.toFile(), f.toFile(), true);

        // Assert
        assertEquals("./x.txt", related);
    }

    @Test
    void getRelatedPath_static_whenFileNotUnderDir_thenReturnsNull() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-related-path-outside-root");
        Path other = Files.createTempDirectory("machai-related-path-outside-other");
        Path f = Files.write(other.resolve("x.txt"), "x".getBytes());

        // Act
        String related = ProjectLayout.getRelatedPath(root.toFile(), f.toFile(), false);

        // Assert
        assertNull(related);
    }

    @Test
    void getRelatedPath_static_whenAddSingleDotFalse_thenDoesNotPrefixDotSlash() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-related-path-no-dot-prefix");
        Path f = Files.write(root.resolve("x.txt"), "x".getBytes());

        // Act
        String related = ProjectLayout.getRelatedPath(root.toFile(), f.toFile(), false);

        // Assert
        assertEquals("x.txt", related);
    }
}
