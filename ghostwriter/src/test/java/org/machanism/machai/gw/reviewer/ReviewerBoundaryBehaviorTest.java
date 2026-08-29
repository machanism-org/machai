package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies boundary inputs that must not result in an actionable guidance prompt. */
class ReviewerBoundaryBehaviorTest {

    @TempDir
    Path tempDir;

    @Test
    void pythonReviewer_perform_returnsNullForGuidanceTagWithNoTextAtEndOfFile() throws IOException {
        // Arrange
        Path project = createProject();
        Path source = write(project, "empty.py", "# @guidance:");
        PythonReviewer reviewer = new PythonReviewer();

        // Act
        String result = reviewer.perform(project.toFile(), source.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void typeScriptReviewer_perform_returnsNullForGuidanceTagWithNoTextAtEndOfFile() throws IOException {
        // Arrange
        Path project = createProject();
        Path source = write(project, "empty.ts", "// @guidance:");
        TypeScriptReviewer reviewer = new TypeScriptReviewer();

        // Act
        String result = reviewer.perform(project.toFile(), source.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void pumlReviewer_perform_throwsIOExceptionWhenGuidanceFileIsMissing() throws IOException {
        // Arrange
        Path project = createProject();
        Path missingFile = project.resolve("missing.puml");
        PumlReviewer reviewer = new PumlReviewer();

        // Act and Assert
        assertThrows(IOException.class, () -> reviewer.perform(project.toFile(), missingFile.toFile()));
    }

    private Path createProject() throws IOException {
        Path project = tempDir.resolve("project");
        return Files.createDirectories(project);
    }

    private Path write(Path project, String name, String content) throws IOException {
        Path file = project.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
