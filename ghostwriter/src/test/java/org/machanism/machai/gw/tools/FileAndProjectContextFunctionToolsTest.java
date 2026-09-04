package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kotlin.Pair;

class FileAndProjectContextFunctionToolsTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void fileToolsWriteReadListAndRecursivelyDiscoverProjectFiles() throws Exception {
        // Arrange
        FileFunctionTools tools = new FileFunctionTools();
        File root = temporaryDirectory.toFile();

        // Act
        String created = tools.writeFile(new File("nested/note.txt"), "hello", "UTF-8", root);
        String updated = tools.writeFile(new File("nested/note.txt"), "updated", "UTF-8", root);
        String content = tools.readFile(new File("nested/note.txt"), "UTF-8", root);
        List<Pair<String, String>> immediate = tools.listFiles(new File("nested"), root);
        Object recursiveFiles = tools.getRecursiveFiles(new File("."), 10, root);
        Object recursiveFolders = tools.getRecursiveFolders(new File("."), 10, root);

        // Assert
        assertEquals("File written successfully: nested\\note.txt", created);
        assertEquals("File updated successfully: nested\\note.txt", updated);
        assertEquals("updated", content);
        assertEquals(1, immediate.size());
        assertTrue(immediate.get(0).getFirst().endsWith("nested/note.txt"));
        assertTrue(recursiveFiles instanceof List);
        assertTrue(((List<?>) recursiveFiles).get(0).toString().endsWith("nested/note.txt"));
        assertTrue(recursiveFolders instanceof List);
        assertTrue(((List<?>) recursiveFolders).get(0).toString().endsWith("nested"));
    }

    @Test
    void fileToolsRejectOutsidePathsAndReportMissingAndEmptyDirectories() throws Exception {
        // Arrange
        FileFunctionTools tools = new FileFunctionTools();
        File root = temporaryDirectory.toFile();
        Path outside = Files.createTempFile("outside", ".txt");

        // Act and Assert
        assertEquals("No files found in directory.", tools.getRecursiveFiles(new File("missing"), 2, root));
        assertEquals("No folders found in directory.", tools.getRecursiveFolders(new File("missing"), 2, root));
        IOException missing = assertThrows(IOException.class,
                () -> tools.readFile(new File("missing.txt"), "UTF-8", root));
        assertTrue(missing.getMessage().contains("does not exist"));
        assertOutsidePathIsRejected(tools, outside, root);
        assertEscapingWriteIsRejected(tools, root);
        assertFalse(Files.exists(temporaryDirectory.getParent().resolve("escaped.txt")));
    }

    @Test
    void fileToolsCanonicalizeRelativePathsBeforeAuthorizingAccess() throws Exception {
        // Arrange
        FileFunctionTools tools = new FileFunctionTools();
        File root = temporaryDirectory.toFile();
        Files.createDirectories(temporaryDirectory.resolve("nested"));

        // Act
        File resolved = tools.getFile(new File("nested/../nested"), root);

        // Assert
        assertEquals(temporaryDirectory.resolve("nested").toRealPath().toFile(), resolved);
        assertEscapingPathIsRejected(tools, root);
    }

    @Test
    void fileToolsEnforceResultLimitAndApplyPatches() throws Exception {
        // Arrange
        FileFunctionTools tools = new FileFunctionTools();
        File root = temporaryDirectory.toFile();
        Files.write(temporaryDirectory.resolve("one.txt"), "one\n".getBytes(StandardCharsets.UTF_8));
        Files.write(temporaryDirectory.resolve("two.txt"), "two\n".getBytes(StandardCharsets.UTF_8));

        // Act
        IllegalArgumentException tooMany = assertTooManyFiles(tools, root);
        String patchResult = tools.applyPatchToFile(new File("one.txt"), "@@\n-one\n+changed", "UTF-8", root);
        String badPatchResult = tools.applyPatchToFile(new File("one.txt"), "@@\n-missing\n+x", "UTF-8", root);

        // Assert
        assertTrue(tooMany.getMessage().contains("exceeds the allowed limit"));
        assertEquals("Patch applied successfully.", patchResult);
        assertEquals("changed", new String(Files.readAllBytes(temporaryDirectory.resolve("one.txt")), StandardCharsets.UTF_8).trim());
        assertTrue(badPatchResult.startsWith("Failed to apply patch:"));
    }

    @Test
    void relativePathHandlesDescendantsSameDirectoryAndNullArguments() {
        // Arrange
        File root = temporaryDirectory.toFile();
        File child = temporaryDirectory.resolve("child.txt").toFile();

        // Act and Assert
        assertEquals(".", FileFunctionTools.getRelativePath(root, root, true));
        assertEquals("./child.txt", FileFunctionTools.getRelativePath(root, child, true));
        assertEquals("child.txt", FileFunctionTools.getRelativePath(root, child, false));
        assertEquals(null, FileFunctionTools.getRelativePath(null, child, true));
    }

    @Test
    void projectContextSupportsPutPushAndBothPopOrders() {
        // Arrange
        File project = temporaryDirectory.resolve("context-project").toFile();

        // Act
        String put = ProjectContextFunctionTools.putProjectContextVariable("name", "first", project);
        Object pushFirst = ProjectContextFunctionTools.pushProjectContextVariable("name", "second", project);
        Object pushSecond = ProjectContextFunctionTools.pushProjectContextVariable("name", "third", project);
        Object fifo = ProjectContextFunctionTools.popProjectContextVariable("name", "FIFO", project);
        Object lifo = ProjectContextFunctionTools.popProjectContextVariable("name", "LIFO", project);
        Map<String, Object> remaining = ProjectContextFunctionTools.getProjectContextVariables(
                Collections.singletonList("name"), project);
        Object finalPop = ProjectContextFunctionTools.popProjectContextVariable("name", "", project);

        // Assert
        assertTrue(put.contains("set to 'first'"));
        assertTrue(pushFirst.toString().contains("Pushed value 'second'"));
        assertTrue(pushSecond.toString().contains("Pushed value 'third'"));
        assertEquals("first", fifo);
        assertEquals("third", lifo);
        assertEquals("second", remaining.get("name"));
        assertEquals("second", finalPop);
        assertEquals(null, ProjectContextFunctionTools.getProjectContextVariables(
                Collections.singletonList("name"), project).get("name"));
    }

    @Test
    void projectContextSerializesObjectsAndReportsMissingContext() throws Exception {
        // Arrange
        File project = temporaryDirectory.resolve("serialized-project").toFile();

        // Act
        ProjectContextFunctionTools.put(project, "number", Integer.valueOf(7));
        Map<String, Object> serialized = ProjectContextFunctionTools.getProjectContextVariables(
                Collections.singletonList("number"), project);
        IllegalArgumentException noContext = assertMissingContext();

        // Assert
        assertEquals("7", serialized.get("number"));
        assertTrue(noContext.getMessage().contains("No context found"));
        assertFalse(ProjectContextFunctionTools.popProjectContextVariable("number", "", project).toString().isEmpty());
    }

    @Test
    void projectContextCreatesListsAndHandlesUnsupportedValuesAndMissingPop() throws Exception {
        // Arrange
        File project = temporaryDirectory.resolve("branch-context-project").toFile();

        // Act
        Object firstPush = ProjectContextFunctionTools.pushProjectContextVariable("items", "one", project);
        Object secondPush = ProjectContextFunctionTools.pushProjectContextVariable("items", "two", project);
        Object missingPop = ProjectContextFunctionTools.popProjectContextVariable("absent", "LIFO", project);

        Map<File, Map<String, Object>> contextMap = getContextProjectMap();
        Map<String, Object> values = new ConcurrentHashMap<>();
        values.put("unsupported", Integer.valueOf(3));
        contextMap.put(project, values);
        Object unsupportedPush = ProjectContextFunctionTools.pushProjectContextVariable("unsupported", "x", project);
        Object unsupportedPop = ProjectContextFunctionTools.popProjectContextVariable("unsupported", "", project);

        // Assert
        assertTrue(firstPush.toString().contains("Pushed value 'one'"));
        assertTrue(secondPush.toString().contains("Pushed value 'two'"));
        assertTrue(missingPop.toString().contains("not found"));
        assertTrue(unsupportedPush.toString().contains("Unsupported variable type"));
        assertTrue(unsupportedPop.toString().contains("Unsupported variable type"));
    }

    private void assertOutsidePathIsRejected(FileFunctionTools tools, Path outside, File root) {
        assertThrows(IllegalArgumentException.class, () -> readOutsideFile(tools, outside, root));
    }

    private void readOutsideFile(FileFunctionTools tools, Path outside, File root) {
        try {
            tools.readFile(outside.toFile(), "UTF-8", root);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void assertEscapingWriteIsRejected(FileFunctionTools tools, File root) {
        assertThrows(IllegalArgumentException.class, () -> writeEscapingFile(tools, root));
    }

    private void writeEscapingFile(FileFunctionTools tools, File root) {
        tools.writeFile(new File("../escaped.txt"), "blocked", "UTF-8", root);
    }

    private void assertEscapingPathIsRejected(FileFunctionTools tools, File root) {
        assertThrows(IllegalArgumentException.class, () -> getEscapingFile(tools, root));
    }

    private void getEscapingFile(FileFunctionTools tools, File root) {
        tools.getFile(new File("nested/../../outside"), root);
    }

    private IllegalArgumentException assertTooManyFiles(FileFunctionTools tools, File root) {
        return assertThrows(IllegalArgumentException.class, () -> getTooManyFiles(tools, root));
    }

    private void getTooManyFiles(FileFunctionTools tools, File root) {
        tools.getRecursiveFiles(new File("."), 1, root);
    }

    private IllegalArgumentException assertMissingContext() {
        return assertThrows(IllegalArgumentException.class, this::getMissingContext);
    }

    private void getMissingContext() {
        ProjectContextFunctionTools.getProjectContextVariables(
                Collections.singletonList("unknown"), temporaryDirectory.resolve("unknown").toFile());
    }

    @SuppressWarnings("unchecked")
    private Map<File, Map<String, Object>> getContextProjectMap() throws ReflectiveOperationException {
        java.lang.reflect.Field contexts = ProjectContextFunctionTools.class.getDeclaredField("contextProjectMap");
        contexts.setAccessible(true);
        return (Map<File, Map<String, Object>>) contexts.get(null);
    }
}
