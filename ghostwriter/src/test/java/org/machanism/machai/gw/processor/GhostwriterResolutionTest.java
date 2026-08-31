package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

/** Tests command-line precedence for Ghostwriter's isolated setting resolvers. */
class GhostwriterResolutionTest {

    @Test
    void resolvers_preferMeaningfulCommandLineValuesOverConfiguration() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set(GWConstants.MODEL_PROP_NAME, "configured:model");
        configuration.set(GWConstants.EXCLUDES_PROP_NAME, "configured-one,configured-two");
        configuration.set(GWConstants.THREADS_PROP_NAME, "2");
        configuration.set(GWConstants.PROJECT_DIR_PROP_NAME, "configured-project");
        configuration.set(GWConstants.PATH_PROP_NAME, "configured-path");
        CommandLine commandLine = commandLine("--model", " cli:model ", "--excludes", "one,two", "--threads", "4",
                "--projectDir", "command-project", "command-path");

        // Act
        String model = (String) invoke("resolveGenai", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);
        String[] excludes = (String[]) invoke("resolveExcludes",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configuration);
        String threads = (String) invoke("resolveMultiThread",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configuration);
        File projectDir = (File) invoke("resolveProjectDir",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configuration);
        String[] paths = (String[]) invoke("resolvePaths", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);

        // Assert
        assertEquals("cli:model", model);
        assertArrayEquals(new String[] { "one", "two" }, excludes);
        assertEquals("4", threads);
        assertEquals(new File("command-project"), projectDir);
        assertArrayEquals(new String[] { "command-path" }, paths);
    }

    @Test
    void resolvers_fallBackToConfigurationWhenNoCommandLineOverrideExists() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set(GWConstants.MODEL_PROP_NAME, "configured:model");
        configuration.set(GWConstants.INSTRUCTIONS_PROP_NAME, "configured instructions");
        configuration.set(GWConstants.EXCLUDES_PROP_NAME, "one,two");
        configuration.set(GWConstants.THREADS_PROP_NAME, "3");
        configuration.set(GWConstants.PROJECT_DIR_PROP_NAME, "configured-project");
        configuration.set(GWConstants.PATH_PROP_NAME, "configured-path");
        CommandLine commandLine = commandLine();

        // Act
        String model = (String) invoke("resolveGenai", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);
        String instructions = (String) invoke("resolveInstructions",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class, Scanner.class }, commandLine, configuration,
                new Scanner("unused\n"));
        String[] excludes = (String[]) invoke("resolveExcludes",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configuration);
        String threads = (String) invoke("resolveMultiThread",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configuration);
        File projectDir = (File) invoke("resolveProjectDir",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, configuration);
        String[] paths = (String[]) invoke("resolvePaths", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);

        // Assert
        assertEquals("configured:model", model);
        assertEquals("configured instructions", instructions);
        assertArrayEquals(new String[] { "one", "two" }, excludes);
        assertEquals("3", threads);
        assertEquals(new File("configured-project"), projectDir);
        assertArrayEquals(new String[] { "configured-path" }, paths);
    }

    private static CommandLine commandLine(String... arguments) throws Exception {
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        return new DefaultParser().parse(options, arguments);
    }

    private static Object invoke(String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
