package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional edge-case tests for the layout package's shared utility behavior. */
class LayoutCoverageQualityTest {

    @TempDir
    Path tempDir;

    @Test
    void getTempDir_shouldInitializeAndCacheMachaiTemporaryDirectory() {
        // Arrange
        String expectedSuffix = ".machai";

        // Act
        String first = ProjectLayout.getTempDir();
        String second = ProjectLayout.getTempDir();

        // Assert
        assertNotNull(first);
        assertTrue(first.endsWith(expectedSuffix));
        assertSame(first, second);
    }

    @Test
    void isExcludedPath_shouldMatchOnlyExactExcludedDirectoryNames() {
        // Arrange
        String excluded = ProjectLayout.getExcludeDirs()[0];

        // Act
        boolean exactMatch = ProjectLayout.isExcludedPath(excluded);
        boolean prefixedMatch = ProjectLayout.isExcludedPath(excluded + "-child");
        boolean ordinaryName = ProjectLayout.isExcludedPath("ordinary-directory");

        // Assert
        assertTrue(exactMatch);
        assertFalse(prefixedMatch);
        assertFalse(ordinaryName);
    }

    @Test
    void listDirectories_shouldReturnOnlyDirectoriesAndSkipExcludedSubtrees() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("included/nested"));
        Files.createDirectories(tempDir.resolve("build/ignored"));
        Files.write(tempDir.resolve("included/file.txt"), "data".getBytes(StandardCharsets.UTF_8));

        // Act
        List<File> directories = ProjectLayout.listDirectories(tempDir.toFile());

        // Assert
        assertTrue(directories.stream().anyMatch(file -> file.getName().equals("included")));
        assertTrue(directories.stream().anyMatch(file -> file.getName().equals("nested")));
        assertFalse(directories.stream().anyMatch(file -> file.getPath().contains("build")));
        assertFalse(directories.stream().anyMatch(file -> file.getName().equals("file.txt")));
    }

    @Test
    void gradleLayout_shouldReturnEmptyModulesWhenProjectDirectoryIsUnset() {
        // Arrange
        GradleProjectLayout layout = new GradleProjectLayout();

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(ProjectLayout.NO_MODULES, modules);
        assertEquals("", layout.getProjectId());
        assertEquals("", layout.getProjectName());
    }

    @Test
    void javaScriptLayout_shouldRejectMalformedPackageJson() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("package.json"), "{not-valid-json".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act / Assert
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, layout::getModules);
    }
}
