package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class ActFunctionToolsTest {

    @TempDir
    Path tempDir;

    private final ActFunctionTools tools = new ActFunctionTools();

    @Test
    void getActDetails_returnsCustomAndBuiltinEntries() throws Exception {
        Path actsDir = Files.createDirectories(tempDir.resolve("acts"));
        Files.write(actsDir.resolve("help.toml"), "prompt = \"custom\"\n".getBytes(StandardCharsets.UTF_8));

        PropertiesConfigurator configurator = new PropertiesConfigurator();
        configurator.set(GWConstants.ACTS_LOCATION_PROP_NAME, actsDir.toString());

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) tools.getActDetails("help", tempDir.toFile(), configurator);

        assertTrue(response.containsKey("custom"), "Custom act data should be present");
        assertTrue(response.containsKey("built-in"), "Built-in act data should be present");
    }

    @Test
    void performAct_synchronous_invokesActProcessorWithResolvedProperties() throws Exception {
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        configurator.set(GWConstants.ACTS_LOCATION_PROP_NAME, tempDir.resolve("default-acts").toString());
        configurator.set(GWConstants.PATH_PROP_NAME, tempDir.resolve("default-path").toString());

        Map<String, String> overrides = new HashMap<>();
        overrides.put(GWConstants.ACTS_LOCATION_PROP_NAME, "custom-acts");
        overrides.put(GWConstants.PATH_PROP_NAME, tempDir.resolve("scan-path").toString());

        try (MockedConstruction<ActProcessor> mocked = Mockito.mockConstruction(ActProcessor.class, (mock, context) -> {
            Mockito.when(mock.getActProperties()).thenReturn(new HashMap<>());
            Mockito.when(mock.getResults()).thenReturn(Collections.singletonList("ok"));
        })) {
            Object result = tools.performAct("demo", tempDir.toFile(), overrides, false, configurator);

            assertEquals(Collections.singletonList("ok"), result);

            ActProcessor constructed = mocked.constructed().get(0);
            Mockito.verify(constructed).setActsLocation("custom-acts");
            Mockito.verify(constructed).setAct("demo");
            Mockito.verify(constructed).scanDocuments(tempDir.toFile(), overrides.get(GWConstants.PATH_PROP_NAME));

            @SuppressWarnings("unchecked")
            Map<String, Object> actProps = (Map<String, Object>) constructed.getActProperties();
            assertEquals(overrides.get(GWConstants.PATH_PROP_NAME), actProps.get(GWConstants.PATH_PROP_NAME));
        }
    }

    @Test
    void performAct_async_persistsResultsThatCanBeReadLater() throws Exception {
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        configurator.set(GWConstants.PATH_PROP_NAME, tempDir.resolve("async-path").toString());

        try (MockedConstruction<ActProcessor> mocked = Mockito.mockConstruction(ActProcessor.class, (mock, context) -> {
            Mockito.when(mock.getActProperties()).thenReturn(new HashMap<>());
            Mockito.when(mock.getResults()).thenReturn(Collections.singletonList("async-result"));
        })) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) tools.performAct("demo", tempDir.toFile(), null, true,
                    configurator);

            assertEquals("processing", response.get("status"));
            String processId = (String) response.get("process_id");
            assertNotNull(processId);

            Map<String, Object> finalResponse = waitForAsyncResult(processId);
            assertEquals("done", finalResponse.get("status"));
            assertEquals(Collections.singletonList("async-result"), finalResponse.get("result"));
        }
    }

    @Test
    void getActResult_whenFileMissing_reportsProcessing() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) tools.getActResult("unknown-id");
        assertEquals("processing", response.get("status"));
    }

    @Test
    void actPrompts_returnsConfiguredPrompt() {
        assertNotNull(tools.actPrompts("any"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> waitForAsyncResult(String processId) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000;
        Map<String, Object> response;
        do {
            response = (Map<String, Object>) tools.getActResult(processId);
            if ("done".equals(response.get("status"))) {
                return response;
            }
            Thread.sleep(50);
        } while (System.currentTimeMillis() < deadline);
        throw new IllegalStateException("Timed out waiting for async result");
    }
}
