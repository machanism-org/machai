package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional branch and boundary tests for the reviewer implementations. */
class ReviewerAdditionalQualityTest {

    @TempDir
    Path tempDir;

    @Test
    void everyReviewerAdvertisesOnlyItsSupportedExtension() {
        // Arrange
        Reviewer[] reviewers = { new HtmlReviewer(), new JavaReviewer(), new MarkdownReviewer(),
                new PumlReviewer(), new PythonReviewer(), new TextReviewer(), new TypeScriptReviewer() };
        String[][] expected = { { "html", "htm", "xml" }, { "java" }, { "md" }, { "puml" }, { "py" },
                { "txt" }, { "ts" } };

        // Act and Assert
        for (int i = 0; i < reviewers.length; i++) {
            assertArrayEquals(expected[i], reviewers[i].getSupportedFileExtensions());
        }
    }

    @Test
    void javaReviewer_returnsNullWhenTagOccursOutsideAComment() throws IOException {
        // Arrange
        Path project = project();
        Path file = write(project, "Example.java", "class Example { String value = \"@guidance\"; }\n");

        // Act
        String result = new JavaReviewer().perform(project.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void javaReviewer_acceptsMultilineBlockCommentGuidance() throws IOException {
        // Arrange
        Path project = project();
        Path file = write(project, "Example.java", "/*\n * @guidance: document this class\n */\nclass Example {}\n");

        // Act
        String result = new JavaReviewer().perform(project.toFile(), file.toFile());

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void extractPackageName_returnsDeclaredDottedPackageAndDefaultWhenAbsent() {
        // Arrange and Act
        String declared = JavaReviewer.extractPackageName("/* comment */\npackage a.b_2.c;\nclass X {}\n");
        String absent = JavaReviewer.extractPackageName("class X {}\n");

        // Assert
        assertEquals("a.b_2.c", declared);
        assertEquals("<default package>", absent);
    }

    @Test
    void htmlAndMarkdownReviewers_returnNullForPlainContent() throws IOException {
        // Arrange
        Path project = project();
        Path html = write(project, "page.html", "<!-- ordinary comment -->");
        Path markdown = write(project, "page.md", "# @guidance is not a comment");

        // Act
        String htmlResult = new HtmlReviewer().perform(project.toFile(), html.toFile());
        String markdownResult = new MarkdownReviewer().perform(project.toFile(), markdown.toFile());

        // Assert
        assertNull(htmlResult);
        assertNull(markdownResult);
    }

    @Test
    void pumlReviewer_returnsNullWhenTagIsAbsent() throws IOException {
        // Arrange
        Path project = project();
        Path file = write(project, "diagram.puml", "@startuml\nAlice -> Bob\n@enduml\n");

        // Act
        String result = new PumlReviewer().perform(project.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void pythonAndTypeScriptReviewers_returnNullForBlankGuidance() throws IOException {
        // Arrange
        Path project = project();
        Path python = write(project, "blank.py", "# @guidance:    \n");
        Path typescript = write(project, "blank.ts", "/* @guidance: \t */\n");

        // Act
        String pythonResult = new PythonReviewer().perform(project.toFile(), python.toFile());
        String typescriptResult = new TypeScriptReviewer().perform(project.toFile(), typescript.toFile());

        // Assert
        assertNull(pythonResult);
        assertNull(typescriptResult);
    }

    @Test
    void textReviewer_returnsNullForAFileWithAnotherName() throws IOException {
        // Arrange
        Path project = project();
        Path file = write(project, "notes.txt", "@guidance: ignore this file");

        // Act
        String result = new TextReviewer().perform(project.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    private Path project() throws IOException {
        Path project = tempDir.resolve("project");
        Files.createDirectories(project);
        return project;
    }

    private Path write(Path project, String relativePath, String content) throws IOException {
        Path file = project.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
