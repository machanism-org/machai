package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests exclusion matching independently of filesystem traversal. */
class ProjectLayoutExclusionTest {

    @TempDir
    Path tempDir;

    @Test
    void isExcludedPath_shouldMatchGlobAgainstFileNameAndLeaveOtherPathsIncluded() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("*.log"));
        File logFile = tempDir.resolve("build.log").toFile();
        File sourceFile = tempDir.resolve("Main.java").toFile();

        // Act
        boolean logIsExcluded = layout.isExcludedPath(logFile);
        boolean sourceIsExcluded = layout.isExcludedPath(sourceFile);

        // Assert
        assertTrue(logIsExcluded);
        assertFalse(sourceIsExcluded);
    }

    @Test
    void isExcludedPath_shouldFallBackToExactMatchWhenGlobPatternIsInvalid() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("["));
        File exactName = new File("[");
        File differentName = new File("different");

        // Act
        boolean exactNameIsExcluded = layout.isExcludedPath(exactName);
        boolean differentNameIsExcluded = layout.isExcludedPath(differentName);

        // Assert
        assertTrue(exactNameIsExcluded);
        assertFalse(differentNameIsExcluded);
    }

    @Test
    void isExcludedPath_shouldTreatNullPathAsIncluded() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("*"));

        // Act
        boolean excluded = layout.isExcludedPath(null);

        // Assert
        assertFalse(excluded);
    }
}
