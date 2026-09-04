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
    void actPrompts_returnsConfiguredPrompt() {
        assertNotNull(tools.actPrompts("any"));
    }

}
