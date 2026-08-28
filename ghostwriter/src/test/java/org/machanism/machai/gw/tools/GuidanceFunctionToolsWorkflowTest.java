package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class GuidanceFunctionToolsWorkflowTest {

    @TempDir
    Path projectDirectory;

    @Test
    void getProcessGuidanceTagFilesResultReturnsProcessingForUnknownId() throws Exception {
        // Arrange
        GuidanceFunctionTools tools = new GuidanceFunctionTools();
        String id = "missing-result-" + System.nanoTime();

        // Act
        Map<?, ?> result = (Map<?, ?>) tools.getProcessGuidanceTagFilesResult(id);

        // Assert
        assertEquals(id, result.get("process_id"));
        assertEquals("processing", result.get("status"));
        assertTrue(result.get("message").toString().contains("not ready"));
    }

    @Test
    void getProcessGuidanceTagFilesResultDeserializesCompletedResult() throws Exception {
        // Arrange
        GuidanceFunctionTools tools = new GuidanceFunctionTools();
        String id = "completed-result-" + System.nanoTime();
        File resultFile = new File(ProjectLayout.getTempDir(), "guidance/" + id + ".tmp");
        assertTrue(resultFile.getParentFile().mkdirs() || resultFile.getParentFile().isDirectory());
        List<String> expected = Arrays.asList("first", "second");
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(resultFile))) {
            output.writeObject(expected);
        }

        // Act
        Map<?, ?> result = (Map<?, ?>) tools.getProcessGuidanceTagFilesResult(id);

        // Assert
        assertEquals(id, result.get("process_id"));
        assertEquals("done", result.get("status"));
        assertEquals(expected, result.get("result"));
        assertTrue(resultFile.delete());
    }

    @Test
    void getProcessGuidanceTagFilesResultWrapsCorruptSerializedData() throws Exception {
        // Arrange
        GuidanceFunctionTools tools = new GuidanceFunctionTools();
        String id = "corrupt-result-" + System.nanoTime();
        File resultFile = new File(ProjectLayout.getTempDir(), "guidance/" + id + ".tmp");
        assertTrue(resultFile.getParentFile().mkdirs() || resultFile.getParentFile().isDirectory());
        try (FileOutputStream output = new FileOutputStream(resultFile)) {
            output.write(new byte[] { 1, 2, 3 });
        }

        // Act / Assert
        java.io.IOException exception = org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> tools.getProcessGuidanceTagFilesResult(id));
        assertTrue(exception.getMessage().contains("Error reading guidance"));
        resultFile.delete();
    }

    @Test
    void getGuidancePromptReturnsConfiguredTemplateRegardlessOfArguments() {
        // Arrange
        GuidanceFunctionTools tools = new GuidanceFunctionTools();

        // Act
        String prompt = tools.getGuidancePrompt(projectDirectory.toString(), "glob:**/*.java");

        // Assert
        assertInstanceOf(String.class, prompt);
        assertTrue(prompt.length() > 20);
    }
}
