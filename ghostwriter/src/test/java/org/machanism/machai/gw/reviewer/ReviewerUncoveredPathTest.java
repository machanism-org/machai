package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests reviewer error handling and validation branches that require controlled input failures. */
class ReviewerUncoveredPathTest {

    @TempDir
    Path project;

    @Test
    void pythonReviewerReturnsNullForBlankTripleQuotedGuidance() throws Exception {
        // Arrange
        Path source = project.resolve("blank-guidance.py");
        Files.write(source, "''' @guidance:   \n\t '''".getBytes(StandardCharsets.UTF_8));
        PythonReviewer reviewer = new PythonReviewer();

        // Act
        String prompt = reviewer.perform(project.toFile(), source.toFile());

        // Assert
        assertNull(prompt);
    }

    @Test
    void javaReviewerWrapsMalformedInputExceptionWithFileContext() throws Exception {
        // Arrange
        File source = project.resolve("Unreadable.java").toFile();
        MalformedInputException malformedInput = new MalformedInputException(1);
        JavaReviewer reviewer = new JavaReviewer();

        // Act + Assert
        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.readAllBytes(source.toPath())).thenThrow(malformedInput);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewer.perform(project.toFile(), source));

            assertSame(malformedInput, exception.getCause());
            org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains(source.getAbsolutePath()));
        }
    }
}
