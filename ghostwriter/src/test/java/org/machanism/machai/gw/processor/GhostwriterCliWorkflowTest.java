package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;

/** Tests non-destructive command-line workflows and interactive input helpers. */
class GhostwriterCliWorkflowTest {

    @TempDir
    Path tempDir;

    @Test
    void mainWithHelpPrintsUsageAndReturnsWithoutStartingProcessing() throws Exception {
        // Arrange
        String[] arguments = { "--help" };

        // Act
        Ghostwriter.main(arguments);

        // Assert
        assertNotNull(Ghostwriter.class.getPackage());
    }

    @Test
    void interactiveInputHelpersJoinContinuedLinesAndKeepFinalLine() throws Exception {
        // Arrange
        Scanner promptScanner = new Scanner("first\\\nsecond\n");
        Scanner actScanner = new Scanner("one\\\ntwo\n");

        // Act
        String prompted = (String) invoke("promptForValue", new Class<?>[] { Scanner.class, String.class }, promptScanner,
                "Prompt: ");
        String actInput = (String) invoke("readActInput", new Class<?>[] { Scanner.class }, actScanner);

        // Assert
        String expected = "first" + AbstractAIProvider.LINE_SEPARATOR + "second";
        assertEquals(expected, prompted);
        assertEquals("one" + AbstractAIProvider.LINE_SEPARATOR + "two", actInput);
    }

    @Test
    void resolversUseDefaultsForBlankModelAndAbsentSettings() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set(GWConstants.MODEL_PROP_NAME, "configured-model");
        CommandLine commandLine = parse("--model", "   ");

        // Act
        String model = (String) invoke("resolveGenai", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);
        String[] excludes = (String[]) invoke("resolveExcludes",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, parse(), configuration);
        String[] paths = (String[]) invoke("resolvePaths", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                parse(), configuration);

        // Assert
        assertEquals("configured-model", model);
        assertEquals(null, excludes);
        assertArrayEquals(new String[] { "." }, paths);
    }

    @Test
    void loadRuntimeSettingsCombinesCliAndConfigurationValues() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set(GWConstants.INSTRUCTIONS_PROP_NAME, "configured instructions");
        configuration.set(GWConstants.EXCLUDES_PROP_NAME, "build,target");
        CommandLine commandLine = parse("--model", "provider:model", "--threads", "2", "--projectDir",
                tempDir.toString(), "src");

        // Act
        Object settings = invoke("loadRuntimeSettings",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class, Scanner.class }, commandLine, configuration,
                new Scanner("unused\n"));

        // Assert
        assertEquals("provider:model", field(settings, "genai"));
        assertEquals("configured instructions", field(settings, "instructions"));
        assertArrayEquals(new String[] { "build", "target" }, (String[]) field(settings, "excludes"));
        assertEquals("2", field(settings, "multiThread"));
        assertEquals(tempDir.toFile(), field(settings, "projectDir"));
        assertArrayEquals(new String[] { "src" }, (String[]) field(settings, "paths"));
    }

    @Test
    void initializeConfigurationUsesExplicitConfigurationFileRelativeToProject() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        Files.write(tempDir.resolve("custom.properties"), java.util.Collections.singletonList("gw.model=file-model"),
                StandardCharsets.UTF_8);
        CommandLine commandLine = parse("--config", "custom.properties");

        // Act
        invoke("initializeConfiguration", new Class<?>[] { CommandLine.class, String.class, PropertiesConfigurator.class },
                commandLine, tempDir.toString(), configuration);

        // Assert
        assertEquals("file-model", configuration.get(GWConstants.MODEL_PROP_NAME, null));
    }

    private static CommandLine parse(String... arguments) throws Exception {
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        return new DefaultParser().parse(options, arguments);
    }

    private static Object invoke(String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
