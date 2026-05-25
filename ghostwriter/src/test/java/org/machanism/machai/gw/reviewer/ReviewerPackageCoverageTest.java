package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReviewerPackageCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void htmlReviewer_perform_returnsConfiguredPromptForHtmlCommentGuidance() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path projectDir = createProjectDir();
        String content = "<section>before</section>\n<!-- @guidance: review this page -->\n<div>after</div>\n";
        Path file = writeFile(projectDir, "web/pages/index.html", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nPath: `web/pages/index.html`.\n"), normalizeLineEndings(result));
    }

    @Test
    void htmlReviewer_perform_returnsNullForXmlFilesWithMultilineGuidanceCommentWithoutColon() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path projectDir = createProjectDir();
        String content = "<!-- @guidance\n keep xml in sync\n-->\n<root/>\n";
        Path file = writeFile(projectDir, "config/sample.xml", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void javaReviewer_perform_returnsConfiguredPromptForRegularJavaFile() throws IOException {
        // Arrange
        JavaReviewer reviewer = new JavaReviewer();
        Path projectDir = createProjectDir();
        String content = "package org.example;\n// @guidance: keep public api documented\npublic class Sample {}\n";
        Path file = writeFile(projectDir, "src/main/java/org/example/Sample.java", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Java source code.\n\nPath: `src/main/java/org/example/Sample.java`.\n\n"),
                normalizeLineEndings(result));
    }

    @Test
    void javaReviewer_perform_returnsConfiguredPromptForPackageInfoFile() throws IOException {
        // Arrange
        JavaReviewer reviewer = new JavaReviewer();
        Path projectDir = createProjectDir();
        String content = "/* @guidance: package contract */\npackage org.example.pkg;\n";
        Path file = writeFile(projectDir, "src/main/java/org/example/pkg/package-info.java", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nPath: `src/main/java/org/example/pkg/package-info.java`.\n\n"),
                normalizeLineEndings(result));
    }

    @Test
    void markdownReviewer_perform_returnsConfiguredPromptForMarkdownGuidance() throws IOException {
        // Arrange
        MarkdownReviewer reviewer = new MarkdownReviewer();
        Path projectDir = createProjectDir();
        String content = "<!-- @guidance: update docs -->\n# Title\n\nSome text\n";
        Path file = writeFile(projectDir, "docs/guide.md", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Markdown file.\n\nPath: `docs/guide.md`."),
                normalizeLineEndings(result));
    }

    @Test
    void pumlReviewer_perform_returnsConfiguredPromptWhenGuidanceTagExistsAnywhere() throws IOException {
        // Arrange
        PumlReviewer reviewer = new PumlReviewer();
        Path projectDir = createProjectDir();
        String content = "@startuml\n' @guidance: regenerate diagram\nAlice -> Bob\n@enduml\n";
        Path file = writeFile(projectDir, "architecture/flow.puml", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a PlantUML file.\n\nPath: `architecture/flow.puml`."),
                normalizeLineEndings(result));
    }

    @Test
    void pumlReviewer_perform_throwsNullPointerExceptionWhenGuidancesFileIsNull() {
        // Arrange
        PumlReviewer reviewer = new PumlReviewer();
        Path projectDir = tempDir.resolve("project");

        // Act + Assert
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> reviewer.perform(projectDir.toFile(), null));
        assertEquals("guidancesFile must not be null", exception.getMessage());
    }

    @Test
    void pythonReviewer_getSupportedFileExtensions_returnsPy() {
        // Arrange
        PythonReviewer reviewer = new PythonReviewer();

        // Act
        String[] result = reviewer.getSupportedFileExtensions();

        // Assert
        assertArrayEquals(new String[] { "py" }, result);
    }

    @Test
    void pythonReviewer_perform_returnsConfiguredPromptForLineCommentGuidance() throws IOException {
        // Arrange
        PythonReviewer reviewer = new PythonReviewer();
        Path projectDir = createProjectDir();
        String content = "# @guidance:   trim me   \nprint('ok')\n";
        Path file = writeFile(projectDir, "scripts/run.py", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Python source code.\n\nPath: `scripts/run.py`."),
                normalizeLineEndings(result));
    }

    @Test
    void pythonReviewer_perform_returnsConfiguredPromptForTripleQuotedGuidance() throws IOException {
        // Arrange
        PythonReviewer reviewer = new PythonReviewer();
        Path projectDir = createProjectDir();
        String content = "''' @guidance:   triple guidance\nwith more detail   '''\nprint('ok')\n";
        Path file = writeFile(projectDir, "scripts/doc.py", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Python source code.\n\nPath: `scripts/doc.py`."),
                normalizeLineEndings(result));
    }

    @Test
    void pythonReviewer_perform_usesTripleQuotedGuidanceWhenBlankLineCommentAppearsFirst() throws IOException {
        // Arrange
        PythonReviewer reviewer = new PythonReviewer();
        Path projectDir = createProjectDir();
        String content = "# @guidance\n''' @guidance: usable fallback '''\nprint('ok')\n";
        Path file = writeFile(projectDir, "scripts/blank-first.py", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Python source code.\n\nPath: `scripts/blank-first.py`."),
                normalizeLineEndings(result));
    }

    @Test
    void textReviewer_perform_returnsBlankContentUnchangedForGuidanceFile() throws IOException {
        // Arrange
        TextReviewer reviewer = new TextReviewer();
        Path projectDir = createProjectDir();
        Path file = writeFile(projectDir, "docs/@guidance.txt", "   \n\t");

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertEquals("   \n\t", result);
    }

    @Test
    void textReviewer_getPrompt_returnsConfiguredFolderPromptWhenGuidanceIsNotBlank() {
        // Arrange
        TextReviewer reviewer = new TextReviewer();
        File projectDir = tempDir.toFile();
        File guidanceFile = tempDir.resolve("docs").resolve("@guidance.txt").toFile();

        // Act
        String result = reviewer.getPrompt(projectDir, guidanceFile, "Review docs carefully");

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FOLDER\n\nFolder path: `docs`.\n\n# INSTRUCTION\n \nReview docs carefully\n\n"),
                normalizeLineEndings(result));
    }

    @Test
    void textReviewer_getPrompt_returnsSameReferenceForEmptyString() {
        // Arrange
        TextReviewer reviewer = new TextReviewer();
        String empty = "";

        // Act
        String result = reviewer.getPrompt(tempDir.toFile(), tempDir.toFile(), empty);

        // Assert
        assertSame(empty, result);
    }

    @Test
    void typeScriptReviewer_perform_returnsConfiguredPromptForLineCommentGuidance() throws IOException {
        // Arrange
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path projectDir = createProjectDir();
        String content = "// @guidance:   keep this exported api   \nexport const value = 1;\n";
        Path file = writeFile(projectDir, "ui/app.ts", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Typescript source code.\n\nPath: `app.ts`.\n\n\n\n\n"),
                normalizeLineEndings(result));
    }

    @Test
    void typeScriptReviewer_perform_returnsConfiguredPromptForBlockGuidance() throws IOException {
        // Arrange
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path projectDir = createProjectDir();
        String content = "/* @guidance:   preserve api surface   */\nexport const value = 1;\n";
        Path file = writeFile(projectDir, "ui/block.ts", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Typescript source code.\n\nPath: `block.ts`.\n\n\n\n\n"),
                normalizeLineEndings(result));
    }

    @Test
    void typeScriptReviewer_perform_usesBlockGuidanceWhenBlankLineCommentAppearsFirst() throws IOException {
        // Arrange
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path projectDir = createProjectDir();
        String content = "// @guidance\n/* @guidance: usable fallback */\nexport const value = 1;\n";
        Path file = writeFile(projectDir, "ui/blank-first.ts", content);

        // Act
        String result = reviewer.perform(projectDir.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertEquals(normalizeLineEndings("# THE CURRENT FILE\n\nThis is a Typescript source code.\n\nPath: `blank-first.ts`.\n\n\n\n\n"),
                normalizeLineEndings(result));
    }

    @Test
    void reviewer_contractSupportsCheckedIOExceptionDeclarations() {
        // Arrange
        Reviewer reviewer = new Reviewer() {
            @Override
            public String perform(File projectDir, File file) throws IOException {
                throw new IOException("io failure");
            }

            @Override
            public String[] getSupportedFileExtensions() {
                return new String[] { "custom" };
            }
        };

        // Act + Assert
        IOException exception = assertThrows(IOException.class,
                () -> reviewer.perform(new File("project"), new File("file.custom")));
        assertEquals("io failure", exception.getMessage());
        assertArrayEquals(new String[] { "custom" }, reviewer.getSupportedFileExtensions());
    }

    private String normalizeLineEndings(String value) {
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }

    private Path createProjectDir() throws IOException {
        Path projectDir = tempDir.resolve("project");
        Files.createDirectories(projectDir);
        return projectDir;
    }

    private Path writeFile(Path projectDir, String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
