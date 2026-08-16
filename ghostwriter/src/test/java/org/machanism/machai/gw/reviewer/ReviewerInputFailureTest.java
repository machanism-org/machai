package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies consistent failure behavior for reviewers when their input cannot be read. */
class ReviewerInputFailureTest {

    @TempDir
    Path tempDir;

    @Test
    void htmlReviewer_perform_throwsIOExceptionForMissingFile() {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        File project = tempDir.toFile();
        File missing = tempDir.resolve("missing.html").toFile();

        // Act + Assert
        assertThrows(IOException.class, () -> reviewer.perform(project, missing));
    }

    @Test
    void markdownReviewer_perform_throwsIOExceptionForMissingFile() {
        // Arrange
        MarkdownReviewer reviewer = new MarkdownReviewer();
        File project = tempDir.toFile();
        File missing = tempDir.resolve("missing.md").toFile();

        // Act + Assert
        assertThrows(IOException.class, () -> reviewer.perform(project, missing));
    }

    @Test
    void pumlReviewer_perform_throwsNullPointerExceptionWhenProjectDirectoryIsNull() {
        // Arrange
        PumlReviewer reviewer = new PumlReviewer();
        File file = tempDir.resolve("diagram.puml").toFile();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> reviewer.perform(null, file));
    }

    @Test
    void pythonReviewer_perform_throwsIOExceptionForMissingFile() {
        // Arrange
        PythonReviewer reviewer = new PythonReviewer();
        File project = tempDir.toFile();
        File missing = tempDir.resolve("missing.py").toFile();

        // Act + Assert
        assertThrows(IOException.class, () -> reviewer.perform(project, missing));
    }

    @Test
    void textReviewer_perform_throwsIOExceptionForMissingGuidanceFile() {
        // Arrange
        TextReviewer reviewer = new TextReviewer();
        File project = tempDir.toFile();
        File missing = tempDir.resolve("@guidance.txt").toFile();

        // Act + Assert
        assertThrows(IOException.class, () -> reviewer.perform(project, missing));
    }
}
