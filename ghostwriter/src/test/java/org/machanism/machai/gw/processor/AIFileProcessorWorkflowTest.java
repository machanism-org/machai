package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.LayeredConfigurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests prompt-front-matter handling and the provider workflow in isolation. */
class AIFileProcessorWorkflowTest {

    @TempDir
    Path tempDir;

    @Test
    void extractInputParams_resolvesStringsPreservesListsAndRemovesFrontMatter() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set("public.model", "test-model");
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configuration, "fallback");
        Map<String, Object> parameters = new HashMap<>();
        String prompt = "---\ngw.model: ${public.model}\nenabledTools:\n  - read_file\n  - web\nretries: 2\n---\nReview";

        // Act
        String content = invokeExtractInputParams(processor, prompt, parameters);

        // Assert
        assertEquals("\nReview", content);
        assertEquals("test-model", parameters.get("gw.model"));
        assertEquals(2, ((List<?>) parameters.get("enabledTools")).size());
        assertEquals(2, parameters.get("retries"));
    }

    @Test
    void removeFrontMatterData_handlesCompleteIncompleteAndPlainPrompts() {
        // Arrange + Act + Assert
        assertEquals("\nbody", AIFileProcessor.removeFrontMatterData("  ---\na: b\n---\nbody  "));
        assertEquals("---\na: b", AIFileProcessor.removeFrontMatterData("---\na: b"));
        assertEquals("plain", AIFileProcessor.removeFrontMatterData(" plain "));
    }

    @Test
    void getEnabledTools_acceptsStringMapListAndConfiguredFallback() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set(AIFileProcessor.ENABLED_TOOLS_PARAM_NAME, "configured-one, configured-two");
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configuration, "model");
        LayeredConfigurator layered = new LayeredConfigurator(configuration);

        // Act + Assert
        assertArrayEquals(new String[] { "configured-one", "configured-two" },
                invokeEnabledTools(processor, new HashMap<String, Object>(), layered));
        Map<String, Object> stringTools = new HashMap<>();
        stringTools.put("enabledTools", "one; two\tthree");
        assertArrayEquals(new String[] { "one", "two", "three" }, invokeEnabledTools(processor, stringTools, layered));
        Map<String, Object> listTools = new HashMap<>();
        listTools.put("enabledTools", java.util.Arrays.asList("one", null, 3));
        assertArrayEquals(new String[] { "one", null, "3" }, invokeEnabledTools(processor, listTools, layered));
        Map<String, Object> mapTools = new HashMap<>();
        mapTools.put("enabledTools", java.util.Collections.singletonMap("auto", "local"));
        assertEquals("{auto=local}", invokeEnabledTools(processor, mapTools, layered)[0]);
    }

    @Test
    void process_configuresProviderWithResolvedPromptAndReturnsItsResponse() {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set("public.name", "Ada");
        WorkflowProcessor processor = new WorkflowProcessor(tempDir.toFile(), configuration, "fallback-model");
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir.toFile());
        when(layout.getParentId()).thenReturn(null);
        when(layout.getSources()).thenReturn(new ArrayList<String>());
        when(layout.getTests()).thenReturn(new ArrayList<String>());
        when(layout.getDocuments()).thenReturn(new ArrayList<String>());
        when(layout.getModules()).thenReturn(new ArrayList<String>());
        when(layout.getProjectName()).thenReturn("Demo");
        when(layout.getProjectId()).thenReturn("demo");
        when(layout.getProjectLayoutType()).thenReturn("test");
        Genai provider = mock(Genai.class);
        when(provider.perform()).thenReturn("completed");
        File file = tempDir.resolve("Example.java").toFile();

        // Act
        String result;
        try (MockedStatic<GenaiProviderManager> providers = Mockito.mockStatic(GenaiProviderManager.class)) {
            providers.when(() -> GenaiProviderManager.getProvider(anyString(), any(LayeredConfigurator.class)))
                    .thenReturn(provider);
            result = processor.run(layout, file, "System ${public.name}",
                    "---\nenabledTools: read_file, web\n---\nHello ${public.name}");
        }

        // Assert
        assertEquals("completed", result);
        verify(provider).setProjectDir(tempDir.toFile());
        verify(provider).instructions("System Ada");
        verify(provider).prompt("Hello Ada");
        verify(provider).perform();
    }

    @Test
    void getDirInfoLine_andReferenceHandling_coverEmptyAndUnmarkedInputs() throws Exception {
        // Arrange
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "model");
        Files.write(tempDir.resolve("included.txt"), "included".getBytes("UTF-8"));

        // Act + Assert
        assertNull(processor.getDirInfoLine(null, tempDir.toFile()));
        assertEquals("ordinary", processor.tryToGetFromReference("ordinary", tempDir.toFile(), new PropertiesConfigurator()));
        assertEquals("included", processor.tryToGetFromReference(">>> file://included.txt", tempDir.toFile(), new PropertiesConfigurator()));
        assertTrue(processor.parseLines(null, tempDir.toFile(), new PropertiesConfigurator()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static String invokeExtractInputParams(AIFileProcessor processor, String prompt, Map<String, Object> parameters)
            throws Exception {
        Method method = AIFileProcessor.class.getDeclaredMethod("extractInputParams", String.class, Map.class);
        method.setAccessible(true);
        return (String) method.invoke(processor, prompt, parameters);
    }

    private static String[] invokeEnabledTools(AIFileProcessor processor, Map<String, Object> parameters,
            LayeredConfigurator configuration) throws Exception {
        Method method = AIFileProcessor.class.getDeclaredMethod("getEnabledTools", Map.class, LayeredConfigurator.class);
        method.setAccessible(true);
        return (String[]) method.invoke(processor, parameters, configuration);
    }

    private static final class WorkflowProcessor extends AIFileProcessor {
        private WorkflowProcessor(File rootDir, PropertiesConfigurator configuration, String model) {
            super(rootDir, configuration, model);
        }

        private String run(ProjectLayout layout, File file, String instructions, String prompt) {
            return process(layout, file, instructions, prompt);
        }
    }
}
