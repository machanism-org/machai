package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.tomlj.TomlParseResult;

/** Tests error-prone local and remote act-definition resolution branches. */
class ActProcessorMissingBranchQualityTest {

    @TempDir
    Path tempDir;

    @Test
    void tryLoadActFromDirectory_whenExplicitTomlFileExists_loadsItsProperties() throws Exception {
        // Arrange
        Path actFile = tempDir.resolve("explicit.toml");
        Files.write(actFile, java.util.Collections.singletonList("instructions = \"from file\""),
                StandardCharsets.UTF_8);
        Map<String, Object> properties = new HashMap<>();

        // Act
        TomlParseResult result = ActProcessor.tryLoadActFromDirectory(properties, actFile.toString(), null,
                tempDir.toFile());

        // Assert
        assertEquals("from file", properties.get(ActProcessor.INSTRUCTIONS_PROPERTY_NAME));
        assertTrue(result.contains(ActProcessor.INSTRUCTIONS_PROPERTY_NAME));
    }

    @Test
    void tryLoadActFromDirectory_whenExplicitTomlFileDoesNotExist_returnsNull() throws Exception {
        // Arrange
        Map<String, Object> properties = new HashMap<>();

        // Act
        TomlParseResult result = ActProcessor.tryLoadActFromDirectory(properties,
                tempDir.resolve("absent.toml").toString(), null, tempDir.toFile());

        // Assert
        assertNull(result);
        assertTrue(properties.isEmpty());
    }

    @Test
    void setActsLocation_whenNull_leavesPreviouslyConfiguredLocationUntouched() {
        // Arrange
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        ActProcessor processor = new ActProcessor(tempDir.toFile(), "test:model", configurator);
        File actsDirectory = tempDir.resolve("acts").toFile();
        assertTrue(actsDirectory.mkdirs());
        processor.setActsLocation("acts");
        String configuredLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);

        // Act
        processor.setActsLocation(null);

        // Assert
        assertEquals(configuredLocation, configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null));
    }

    @Test
    void applyActData_whenStringSettingsProvided_updatesProcessorAndConfigurator() {
        // Arrange
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        ActProcessor processor = new ActProcessor(tempDir.toFile(), null, configurator);
        Map<String, Object> properties = new HashMap<>();
        properties.put(ActProcessor.INSTRUCTIONS_PROPERTY_NAME, "act instructions");
        properties.put(ActProcessor.INPUTS_PROPERTY_NAME, "single episode");
        properties.put(GWConstants.THREADS_PROP_NAME, "2");
        properties.put(GWConstants.EXCLUDES_PROP_NAME, "build,target");
        properties.put(GWConstants.NONRECURSIVE_PROP_NAME, "true");
        properties.put(GWConstants.INTERACTIVE_MODE_PROP_NAME, "true");
        properties.put(GWConstants.MODEL_PROP_NAME, "configured:model");
        properties.put("custom.property", "custom value");

        // Act
        processor.applyActData(properties);

        // Assert
        assertEquals("act instructions", processor.getInstructions());
        assertEquals("single episode", processor.getDefaultPrompt());
        assertEquals("configured:model", processor.getModel());
        assertTrue(processor.isNonRecursive());
        assertTrue(processor.isInteractive());
        assertEquals("custom value", processor.getConfigurator().get("custom.property", null));
    }

    @Test
    void getAbsolutePath_whenAbsoluteFileIsMissing_reportsTheMissingAct() throws Exception {
        // Arrange
        Method method = ActProcessor.class.getDeclaredMethod("getAbsolutePath", String.class, String.class, File.class);
        method.setAccessible(true);
        String missing = tempDir.resolve("missing.toml").toAbsolutePath().toString();

        // Act + Assert
        java.lang.reflect.InvocationTargetException exception = assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(null, missing, "ignored", tempDir.toFile()));
        assertTrue(exception.getCause() instanceof IOException);
        assertFalse(exception.getCause().getMessage().isEmpty());
    }
}
