package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
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

/** Verifies non-interactive CLI input and configuration helper behavior. */
class GhostwriterInputWorkflowTest {

    @TempDir
    Path tempDir;

    @Test
    void promptForValue_andReadActInput_joinContinuationLines() throws Exception {
        // Sonar java:S2093: close scanners after exercising CLI input parsing.
        try (Scanner promptScanner = new Scanner("first\\\nsecond\n");
                Scanner actScanner = new Scanner("one\\\ntwo\n")) {
            // Act
            String prompt = (String) invoke("promptForValue", new Class<?>[] { Scanner.class, String.class },
                    promptScanner, "Input: ");
            String actInput = (String) invoke("readActInput", new Class<?>[] { Scanner.class }, actScanner);

            // Assert
            assertEquals("first\nsecond", prompt);
            assertEquals("one\ntwo", actInput);
        }
    }

    @Test
    void initializeConfiguration_loadsExplicitConfigurationAndMainHelpReturnsNormally() throws Exception {
        // Arrange
        Path configuration = tempDir.resolve("gw.properties");
        Files.write(configuration, "gw.model=test-model\n".getBytes("UTF-8"));
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        CommandLine commandLine = parse("--config", configuration.toString());

        // Act
        invoke("initializeConfiguration",
                new Class<?>[] { CommandLine.class, String.class, PropertiesConfigurator.class }, commandLine,
                tempDir.toString(), configurator);
        Ghostwriter.main(new String[] { "--help" });

        // Assert
        assertEquals("test-model", configurator.get(GWConstants.MODEL_PROP_NAME, null));
    }

    @Test
    void configurationAndFormattingHelpers_tolerateMissingDefaults() throws Exception {
        // Arrange
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        CommandLine commandLine = parse();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        // Act
        try (PrintStream redirectedOutput = new PrintStream(output)) {
            System.setOut(redirectedOutput);
            invoke("formatConsole", new Class<?>[] { java.io.Console.class, String.class }, null, "Prompt");
            invoke("logStartup", new Class<?>[] { File.class }, tempDir.toFile());
            invoke("logAbbreviatedMessage", new Class<?>[] { String.class, String.class }, "Value", "text");
            invoke("initializeConfiguration",
                    new Class<?>[] { CommandLine.class, String.class, PropertiesConfigurator.class }, commandLine,
                    tempDir.toString(), configurator);
        } finally {
            System.setOut(original);
        }

        // Assert
        assertTrue(output.toString().startsWith("Prompt: "));
        assertNull(configurator.get(GWConstants.MODEL_PROP_NAME, null));
    }

    private static CommandLine parse(String... arguments) throws Exception {
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        return new DefaultParser().parse(options, arguments);
    }

    private static Object invoke(String name, Class<?>[] types, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
