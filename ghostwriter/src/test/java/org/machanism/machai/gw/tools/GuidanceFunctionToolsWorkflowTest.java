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
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

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

    @Test
    void processGuidanceTagFilesSynchronouslyAppliesPropertiesAndReturnsProcessorReport() throws Exception {
        // Arrange
        GuidanceFunctionTools tools = new GuidanceFunctionTools();
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set(GWConstants.MODEL_PROP_NAME, "configured-model");
        Map<String, String> properties = new java.util.HashMap<>();
        properties.put(GWConstants.MODEL_PROP_NAME, "requested-model");
        properties.put("custom", "value");
        List<Map<String, Object>> expected = java.util.Collections.singletonList(
                java.util.Collections.<String, Object>singletonMap("file", "example.java"));
        java.util.concurrent.atomic.AtomicReference<org.mockito.MockedConstruction.Context> context = new java.util.concurrent.atomic.AtomicReference<>();

        try (MockedConstruction<GuidanceProcessor> construction = Mockito.mockConstruction(GuidanceProcessor.class,
                (mock, constructionContext) -> {
                    context.set(constructionContext);
                    Mockito.when(mock.getReport()).thenReturn(expected);
                })) {
            // Act
            Object result = tools.processGuidanceTagFiles(projectDirectory.toFile(), properties, "glob:**/*.java", false,
                    config);

            // Assert
            assertEquals(expected, result);
            GuidanceProcessor processor = construction.constructed().get(0);
            Mockito.verify(processor).scanDocuments(projectDirectory.toFile(), "glob:**/*.java");
            Mockito.verify(processor).getReport();
            assertEquals("requested-model", context.get().arguments().get(1));
        }
    }

    @Test
    void processGuidanceTagFilesAsynchronouslySerializesReportForLaterRetrieval() throws Exception {
        // Arrange
        GuidanceFunctionTools tools = new GuidanceFunctionTools();
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set(GWConstants.MODEL_PROP_NAME, "configured-model");
        List<Map<String, Object>> expected = java.util.Collections.singletonList(
                java.util.Collections.<String, Object>singletonMap("status", "processed"));

        try (MockedConstruction<GuidanceProcessor> construction = Mockito.mockConstruction(GuidanceProcessor.class,
                (mock, context) -> Mockito.when(mock.getReport()).thenReturn(expected))) {
            // Act
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) tools.processGuidanceTagFiles(projectDirectory.toFile(),
                    null, "glob:**/*.txt", true, config);

            // Assert
            assertEquals("processing", response.get("status"));
            String processId = (String) response.get("process_id");
            Map<?, ?> completed = waitForGuidanceResult(tools, processId);
            assertEquals("done", completed.get("status"));
            assertEquals(expected, completed.get("result"));
            Mockito.verify(construction.constructed().get(0)).scanDocuments(projectDirectory.toFile(), "glob:**/*.txt");
            new File(ProjectLayout.getTempDir(), "guidance/" + processId + ".tmp").delete();
        }
    }

    private Map<?, ?> waitForGuidanceResult(GuidanceFunctionTools tools, String processId) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        Map<?, ?> result;
        do {
            result = (Map<?, ?>) tools.getProcessGuidanceTagFilesResult(processId);
            if ("done".equals(result.get("status"))) {
                return result;
            }
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(25));
        } while (System.currentTimeMillis() < deadline);
        throw new AssertionError("Asynchronous guidance result was not written in time");
    }
}
