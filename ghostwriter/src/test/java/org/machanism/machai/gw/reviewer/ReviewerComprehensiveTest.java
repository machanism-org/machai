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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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

    @ParameterizedTest
    @MethodSource("htmlReviewerScenarios")
    void htmlReviewer_perform_handlesGuidanceScenarios(String relativePath, String content, boolean expectPrompt)
            throws IOException {
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
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

    @ParameterizedTest
    @MethodSource("javaGuidanceFiles")
    void javaReviewer_perform_handlesGuidanceScenarios(String relativePath, String content, boolean expectPrompt)
            throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
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

    @ParameterizedTest
    @MethodSource("markdownGuidanceFiles")
    void markdownReviewer_perform_handlesGuidanceScenarios(String relativePath, String content, boolean expectPrompt)
            throws IOException {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
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

    @ParameterizedTest
    @MethodSource("pumlGuidanceFiles")
    void pumlReviewer_perform_handlesGuidanceScenarios(String relativePath, String content, boolean expectPrompt)
            throws IOException {
        PumlReviewer reviewer = new PumlReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
    }

    @Test
    void pythonReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        PythonReviewer reviewer = new PythonReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "py" }, result);
    }

    @ParameterizedTest
    @MethodSource("pythonGuidanceFiles")
    void pythonReviewer_perform_handlesGuidanceScenarios(String relativePath, String content, boolean expectPrompt)
            throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
    }

    @Test
    void textReviewer_getSupportedFileExtensions_returnsExpectedExtensions() {
        TextReviewer reviewer = new TextReviewer();
        String[] result = reviewer.getSupportedFileExtensions();
        assertArrayEquals(new String[] { "txt" }, result);
    }

    @ParameterizedTest
    @MethodSource("textReviewerScenarios")
    void textReviewer_perform_handlesGuidanceScenarios(String relativePath, String content, boolean expectPrompt)
            throws IOException {
        TextReviewer reviewer = new TextReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
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

    @ParameterizedTest
    @MethodSource("typeScriptGuidanceFiles")
    void typeScriptReviewer_perform_handlesGuidanceScenarios(String relativePath, String content,
            boolean expectPrompt) throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        Path project = createProjectDirectory();
        Path file = writeFile(project, relativePath, content);
        String result = reviewer.perform(project.toFile(), file.toFile());
        assertPromptExpectation(expectPrompt, result);
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

    private void assertPromptExpectation(boolean expectPrompt, String result) {
        if (expectPrompt) {
            assertNotNull(result);
            return;
        }
        assertNull(result);
    }

    private static Stream<Arguments> htmlReviewerScenarios() {
        return Stream.of(
                Arguments.of("index.html", "<html><body>No guidance</body></html>\n", false),
                Arguments.of("page.xml", "<node>@guidance: ignore</node>\n", false),
                Arguments.of("web/index.html",
                        "<!-- @guidance: include section -->\n<html><body>Hi</body></html>\n", true));
    }

    private static Stream<Arguments> javaGuidanceFiles() {
        return Stream.of(
                Arguments.of("src/main/java/A.java", "public class A {}\n", false),
                Arguments.of("A.java",
                        "public class A { String value = \"" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\"; }\n",
                        false),
                Arguments.of("src/main/java/B.java", "// @guidance: keep this file\npublic class B {}\n", true),
                Arguments.of("B.java", "/* @guidance: keep this block */\npublic class B {}\n", true));
    }

    private static Stream<Arguments> markdownGuidanceFiles() {
        return Stream.of(
                Arguments.of("README.md", "# Title\n", false),
                Arguments.of("docs/guide.md", "<!-- @guidance: explain -->\n# Guide\n", true),
                Arguments.of("docs/open.md", "<!-- @guidance: explain\n# Guide\n", true));
    }

    private static Stream<Arguments> pumlGuidanceFiles() {
        return Stream.of(
                Arguments.of("diagram.puml", "@startuml\nAlice -> Bob\n@enduml\n", false),
                Arguments.of("docs/diagram.puml", "' @guidance: include\n@startuml\nAlice -> Bob\n@enduml\n", true));
    }

    private static Stream<Arguments> pythonGuidanceFiles() {
        return Stream.of(
                Arguments.of("a.py", "print('hi')\n", false),
                Arguments.of("a.py", "x = '@guidance: not a comment'\n", false),
                Arguments.of("empty-guidance.py", "# @guidance\nprint('x')\n", false),
                Arguments.of("src/a.py", "# @guidance: keep\nprint('hi')\n", true),
                Arguments.of("b.py", "\"\"\" @guidance: doc text \"\"\"\nprint('hi')\n", true));
    }

    private static Stream<Arguments> textReviewerScenarios() {
        return Stream.of(
                Arguments.of("notes.txt", "hello\n", false),
                Arguments.of("src/test/@guidance.txt", "Line1\nLine2", true));
    }

    private static Stream<Arguments> typeScriptGuidanceFiles() {
        String multipleGuidanceContent = String.join("\n",
                "// @guidance: first",
                "const x = 1;",
                "// @guidance: second",
                "const y = 2;",
                "");
        return Stream.of(
                Arguments.of("a.ts", "const x = 1;\n", false),
                Arguments.of("d.ts", "const tag = \"@guidance: not-a-comment\";\n", false),
                Arguments.of("empty-guidance.ts", "// @guidance\nconst x = 1;\n", false),
                Arguments.of("src/a.ts", "// @guidance: keep\nconst x = 1;\n", true),
                Arguments.of("b.ts", "/* @guidance: keep block */\nconst x = 1;\n", true),
                Arguments.of("c.ts", multipleGuidanceContent, true),
                Arguments.of("e.ts", "/* @guidance: keep\nconst x = 1;\n", false));
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
