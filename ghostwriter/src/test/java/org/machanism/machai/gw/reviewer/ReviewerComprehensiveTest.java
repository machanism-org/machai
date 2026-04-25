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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.machanism.machai.gw.processor.GuidanceProcessor;

class ReviewerComprehensiveTest {

    @TempDir
    Path tempDir;

    @Test
    void htmlReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        HtmlReviewer reviewer = new HtmlReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "html", "htm", "xml" }, result);
    }

    @Test
    void htmlReviewer_perform_returnsNullWhenGuidanceCommentIsMissing() throws IOException {
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "index.html", "<html><body>No guidance</body></html>\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void htmlReviewer_perform_returnsNullWhenGuidanceTagIsOutsideHtmlComment() throws IOException {
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "page.xml", "<node>@guidance: ignore</node>\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void htmlReviewer_perform_formatsPromptWhenGuidanceCommentExists() throws IOException {
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProjectDirectory();
        String content = "<!-- @guidance: include section -->\n<html><body>Hi</body></html>\n";
        Path file = writeFile(project, "web/index.html", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void javaReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        JavaReviewer reviewer = new JavaReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "java" }, result);
    }

    @ParameterizedTest
    @CsvSource({
            "'/* header */\npackage org.example.test;\nclass A {}', org.example.test",
            "'package\n org.example.deep.sub;\nclass A {}', org.example.deep.sub",
            "'class A {}', <default package>",
            "'package 123.invalid;\nclass A {}', <default package>"
    })
    void javaReviewer_extractPackageName_returnsExpectedValue(String content, String expected) {
        String result = JavaReviewer.extractPackageName(content);
        assertEquals(expected, result);
    }

    @Test
    void javaReviewer_perform_returnsNullWhenGuidanceTagIsMissing() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "src/main/java/A.java", "public class A {}\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void javaReviewer_perform_returnsNullWhenGuidanceTagAppearsOnlyInStringLiteral() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        Path project = createProjectDirectory();
        String content = "public class A { String value = \"" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\"; }\n";
        Path file = writeFile(project, "A.java", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void javaReviewer_perform_formatsPromptForLineCommentGuidance() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        Path project = createProjectDirectory();
        String content = "// @guidance: keep this file\npublic class A {}\n";
        Path file = writeFile(project, "src/main/java/A.java", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void javaReviewer_perform_formatsPromptForBlockCommentGuidance() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        Path project = createProjectDirectory();
        String content = "/* @guidance: keep this block */\npublic class A {}\n";
        Path file = writeFile(project, "A.java", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void javaReviewer_perform_formatsPackageInfoPromptWithoutEmbeddingSourceContent() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        Path project = createProjectDirectory();
        String content = "/* @guidance: package docs */\npackage org.example;\n";
        Path file = writeFile(project, "src/main/java/org/example/package-info.java", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
        assertEquals(false, result.contains(content));
    }

    @Test
    void javaReviewer_perform_throwsIOExceptionWhenFileDoesNotExist() {
        JavaReviewer reviewer = new JavaReviewer();
        File project = tempDir.toFile();
        File missing = new File(project, "missing.java");
        assertThrows(IOException.class, () -> reviewer.perform(project, missing));
    }

    @Test
    void markdownReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "md" }, result);
    }

    @Test
    void markdownReviewer_perform_returnsNullWhenGuidanceCommentIsMissing() throws IOException {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "README.md", "# Title\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void markdownReviewer_perform_formatsPromptWhenGuidanceCommentExists() throws IOException {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        Path project = createProjectDirectory();
        String content = "<!-- @guidance: explain -->\n# Guide\n";
        Path file = writeFile(project, "docs/guide.md", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void markdownReviewer_perform_formatsPromptWhenCommentRunsToEndOfFile() throws IOException {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        Path project = createProjectDirectory();
        String content = "<!-- @guidance: explain\n# Guide\n";
        Path file = writeFile(project, "docs/open.md", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void pumlReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        PumlReviewer reviewer = new PumlReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "puml" }, result);
    }

    @Test
    void pumlReviewer_perform_throwsNullPointerExceptionWhenProjectDirIsNull() {
        PumlReviewer reviewer = new PumlReviewer();
        File file = tempDir.resolve("diagram.puml").toFile();
        assertThrows(NullPointerException.class, () -> reviewer.perform(null, file));
    }

    @Test
    void pumlReviewer_perform_returnsNullWhenGuidanceTagIsMissing() throws IOException {
        PumlReviewer reviewer = new PumlReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "diagram.puml", "@startuml\nAlice -> Bob\n@enduml\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void pumlReviewer_perform_formatsPromptWhenGuidanceTagExists() throws IOException {
        PumlReviewer reviewer = new PumlReviewer();
        Path project = createProjectDirectory();
        String content = "' @guidance: include\n@startuml\nAlice -> Bob\n@enduml\n";
        Path file = writeFile(project, "docs/diagram.puml", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void pythonReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        PythonReviewer reviewer = new PythonReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "py" }, result);
    }

    @Test
    void pythonReviewer_perform_returnsNullWhenGuidanceTagIsMissing() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "a.py", "print('hi')\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void pythonReviewer_perform_returnsNullWhenGuidanceTagAppearsOnlyInRegularString() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "a.py", "x = '@guidance: not a comment'\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void pythonReviewer_perform_formatsPromptForLineCommentGuidance() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "src/a.py", "# @guidance: keep\nprint('hi')\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void pythonReviewer_perform_formatsPromptForTripleQuotedGuidance() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "b.py", "\"\"\" @guidance: doc text \"\"\"\nprint('hi')\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void pythonReviewer_perform_returnsNullWhenGuidanceTextIsEmpty() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "empty-guidance.py", "# @guidance\nprint('x')\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void textReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        TextReviewer reviewer = new TextReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "txt" }, result);
    }

    @Test
    void textReviewer_perform_returnsNullWhenFileNameDoesNotMatchGuidanceFile() throws IOException {
        TextReviewer reviewer = new TextReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "notes.txt", "hello\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void textReviewer_perform_formatsPromptWhenGuidanceFileNameMatches() throws IOException {
        TextReviewer reviewer = new TextReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "src/test/@guidance.txt", "Line1\nLine2");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void textReviewer_getPrompt_returnsSameReferenceWhenGuidanceIsBlank() {
        TextReviewer reviewer = new TextReviewer();
        String blank = "  \n\t";
        String result = reviewer.getPrompt(tempDir.toFile(), tempDir.toFile(), blank);
        assertSame(blank, result);
    }

    @Test
    void textReviewer_getPrompt_formatsPromptWhenGuidanceIsNotBlank() {
        TextReviewer reviewer = new TextReviewer();
        File project = tempDir.toFile();
        File child = tempDir.resolve("child").toFile();
        String result = reviewer.getPrompt(project, child, "Hello");
        assertNotNull(result);
    }

    @Test
    void typeScriptReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "ts" }, result);
    }

    @Test
    void typeScriptReviewer_perform_returnsNullWhenGuidanceTagIsMissing() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "a.ts", "const x = 1;\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void typeScriptReviewer_perform_returnsNullWhenGuidanceTagAppearsOnlyInStringLiteral() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "d.ts", "const tag = \"@guidance: not-a-comment\";\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void typeScriptReviewer_perform_formatsPromptForLineCommentGuidance() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "src/a.ts", "// @guidance: keep\nconst x = 1;\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void typeScriptReviewer_perform_formatsPromptForBlockCommentGuidance() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "b.ts", "/* @guidance: keep block */\nconst x = 1;\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void typeScriptReviewer_perform_formatsPromptUsingFirstMatchingGuidanceWhenMultipleExist() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        String content = String.join("\n",
                "// @guidance: first",
                "const x = 1;",
                "// @guidance: second",
                "const y = 2;",
                "");
        Path file = writeFile(project, "c.ts", content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNotNull(result);
    }

    @Test
    void typeScriptReviewer_perform_returnsNullWhenBlockCommentIsNotClosed() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "e.ts", "/* @guidance: keep\nconst x = 1;\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void typeScriptReviewer_perform_returnsNullWhenGuidanceTextIsEmpty() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, "empty-guidance.ts", "// @guidance\nconst x = 1;\n");
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertNull(result);
    }

    @Test
    void typeScriptReviewer_perform_throwsIOExceptionWhenFileDoesNotExist() {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = tempDir.resolve("project");
        File missing = project.resolve("missing.ts").toFile();
        assertThrows(IOException.class, () -> reviewer.perform(project.toFile(), missing));
    }

    @Test
    void reviewer_contractCanBeImplementedByCustomType() throws IOException {
        Reviewer reviewer = new Reviewer() {
            @Override
            public String perform(File projectDir, File file) {
                return projectDir.getName() + ":" + file.getName();
            }

            @Override
            public String[] getSupportedFileExtensions() {
                return new String[] { "a", "b" };
            }
        };
        File project = new File("project");
        File file = new File("sample.txt");
        String result = reviewer.perform(project, file);
        String[] extensions = reviewer.getSupportedFileExtensions();
        assertEquals("project:sample.txt", result);
        assertArrayEquals(new String[] { "a", "b" }, extensions);
    }

    private Path createProjectDirectory() throws IOException {
        Path project = tempDir.resolve("project");
        Files.createDirectories(project);
        return project;
    }

    private Path writeFile(Path project, String relativePath, String content) throws IOException {
        Path file = project.resolve(relativePath);
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
