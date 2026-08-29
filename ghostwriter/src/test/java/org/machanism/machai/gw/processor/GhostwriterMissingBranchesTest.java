package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.lang.reflect.Method;
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

/** Tests CLI helper branches without launching the application or exiting the JVM. */
class GhostwriterMissingBranchesTest {

    @TempDir
    Path tempDir;

    @Test
    void inputHelpers_joinContinuationLinesAndReturnEmptyWhenNoInputExists() throws Exception {
        // Arrange
        String continued = "first" + GWConstants.MULTIPLE_LINES_BREAKER + "\nsecond\n";

        // Act
        String prompted = (String) invoke("promptForValue", new Class<?>[] { Scanner.class, String.class },
                new Scanner(continued), "Prompt: ");
        String actInput = (String) invoke("readActInput", new Class<?>[] { Scanner.class }, new Scanner(continued));
        String empty = (String) invoke("readActInput", new Class<?>[] { Scanner.class }, new Scanner(""));

        // Assert
        assertEquals("first" + AbstractAIProvider.LINE_SEPARATOR + "second", prompted);
        assertEquals("first" + AbstractAIProvider.LINE_SEPARATOR + "second", actInput);
        assertEquals("", empty);
    }

    @Test
    void resolutionHelpers_useBuiltInFallbacksAndIgnoreBlankModelOverride() throws Exception {
        // Arrange
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set(GWConstants.MODEL_PROP_NAME, "configured");
        CommandLine blankModel = parse("--model", "   ");
        CommandLine noValues = parse();

        // Act + Assert
        assertEquals("configured", invoke("resolveGenai",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, blankModel, config));
        assertArrayEquals(new String[] { "." }, (String[]) invoke("resolvePaths",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, noValues,
                new PropertiesConfigurator()));
        assertNull(invoke("resolveExcludes", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                noValues, new PropertiesConfigurator()));
        assertEquals(System.getProperty("user.dir"), ((File) invoke("resolveProjectDir",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, noValues,
                new PropertiesConfigurator())).getAbsolutePath());
    }

    @Test
    void processorCreation_selectsGuidanceOrConfiguredActMode() throws Exception {
        // Arrange
        PropertiesConfigurator config = new PropertiesConfigurator();
        Object settings = settings("model");

        // Act
        AIFileProcessor guidance = (AIFileProcessor) invoke("createProcessor",
                new Class<?>[] { Scanner.class, PropertiesConfigurator.class, CommandLine.class, settings.getClass() },
                new Scanner(""), config, parse(), settings);
        AIFileProcessor act = (AIFileProcessor) invoke("createProcessor",
                new Class<?>[] { Scanner.class, PropertiesConfigurator.class, CommandLine.class, settings.getClass() },
                new Scanner(""), new PropertiesConfigurator(), parse("--act", "help"), settings);

        // Assert
        assertInstanceOf(GuidanceProcessor.class, guidance);
        assertInstanceOf(ActProcessor.class, act);
    }

    @Test
    void configurationInitialization_loadsExplicitFileAndToleratesMissingDefault() throws Exception {
        // Arrange
        Path configFile = tempDir.resolve("gw.properties");
        Files.write(configFile, java.util.Collections.singletonList("sample=value"));
        PropertiesConfigurator explicit = new PropertiesConfigurator();
        PropertiesConfigurator absentDefault = new PropertiesConfigurator();

        // Act
        invoke("initializeConfiguration", new Class<?>[] { CommandLine.class, String.class, PropertiesConfigurator.class },
                parse("--config", configFile.toString()), tempDir.toString(), explicit);
        invoke("initializeConfiguration", new Class<?>[] { CommandLine.class, String.class, PropertiesConfigurator.class },
                parse(), tempDir.toString(), absentDefault);

        // Assert
        assertEquals("value", explicit.get("sample", null));
    }

    private Object settings(String model) throws Exception {
        Class<?> type = Class.forName("org.machanism.machai.gw.processor.Ghostwriter$RuntimeSettings");
        java.lang.reflect.Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object result = constructor.newInstance();
        java.lang.reflect.Field projectDir = type.getDeclaredField("projectDir");
        projectDir.setAccessible(true);
        projectDir.set(result, tempDir.toFile());
        java.lang.reflect.Field genai = type.getDeclaredField("genai");
        genai.setAccessible(true);
        genai.set(result, model);
        return result;
    }

    private CommandLine parse(String... arguments) throws Exception {
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        return new DefaultParser().parse(options, arguments);
    }

    private static Object invoke(String name, Class<?>[] types, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
