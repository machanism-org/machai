package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;

/** Tests CLI input collection and default-resolution branches without starting processing. */
class GhostwriterInputWorkflowTest {

    @Test
    void promptForValue_joinsContinuedScannerLinesAndRemovesContinuationMarkers() throws Exception {
        // Arrange
        Scanner scanner = new Scanner("first\\\nsecond\n");

        // Act
        String value = (String) invoke("promptForValue", new Class<?>[] { Scanner.class, String.class }, scanner,
                "Instructions: ");

        // Assert
        assertEquals("first" + AbstractAIProvider.LINE_SEPARATOR + "second", value);
    }

    @Test
    void readActInput_joinsContinuedLinesAndStopsAtFirstCompleteLine() throws Exception {
        // Arrange
        Scanner scanner = new Scanner("first\\\nsecond\nignored\n");

        // Act
        String value = (String) invoke("readActInput", new Class<?>[] { Scanner.class }, scanner);

        // Assert
        assertEquals("first" + AbstractAIProvider.LINE_SEPARATOR + "second", value);
        assertTrue(scanner.hasNextLine());
        assertEquals("ignored", scanner.nextLine());
    }

    @Test
    void resolutionHelpers_useBuiltInDefaultsWhenNoCommandLineOrConfigurationValueExists() throws Exception {
        // Arrange
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);
        PropertiesConfigurator configurator = new PropertiesConfigurator();

        // Act
        String[] excludes = (String[]) invoke("resolveExcludes",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configurator);
        String[] paths = (String[]) invoke("resolvePaths", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configurator);

        // Assert
        assertEquals(null, excludes);
        assertEquals(1, paths.length);
        assertEquals(".", paths[0]);
    }

    private static Object invoke(String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
