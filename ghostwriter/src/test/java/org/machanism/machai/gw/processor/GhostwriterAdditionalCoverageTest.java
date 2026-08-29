package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.tools.ProcessTerminationException;

/** Exercises CLI setting resolution without starting the command-line application. */
class GhostwriterAdditionalCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void resolutionHelpers_preferCommandLineValuesAndApplyConfigurationFallbacks() throws Exception {
        // Arrange
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set(GWConstants.MODEL_PROP_NAME, "configured-model");
        config.set(GWConstants.INSTRUCTIONS_PROP_NAME, "configured instructions");
        config.set(GWConstants.EXCLUDES_PROP_NAME, "build,target");
        config.set(GWConstants.THREADS_PROP_NAME, "3");
        config.set(GWConstants.PROJECT_DIR_PROP_NAME, tempDir.toString());
        config.set(GWConstants.PATH_PROP_NAME, "configured-path");
        CommandLine commandLine = parse("--model", "cli-model", "--instructions", "cli instructions",
                "--excludes", "one,two", "--threads", "5", "--projectDir", tempDir.resolve("cli").toString(),
                "first-path", "second-path");

        // Act + Assert
        assertEquals("cli-model", invoke("resolveGenai", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, config));
        assertEquals("cli instructions", invoke("resolveInstructions",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class, Scanner.class }, commandLine, config,
                new Scanner("unused")));
        assertArrayEquals(new String[] { "one", "two" }, (String[]) invoke("resolveExcludes",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, config));
        assertEquals("5", invoke("resolveMultiThread", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, config));
        assertEquals(tempDir.resolve("cli").toFile(), invoke("resolveProjectDir",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, config));
        assertArrayEquals(new String[] { "first-path", "second-path" }, (String[]) invoke("resolvePaths",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine, config));

        CommandLine emptyCommandLine = parse();
        assertEquals("configured-model", invoke("resolveGenai", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                emptyCommandLine, config));
        assertEquals("configured instructions", invoke("resolveInstructions",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class, Scanner.class }, emptyCommandLine, config,
                new Scanner("unused")));
        assertArrayEquals(new String[] { "build", "target" }, (String[]) invoke("resolveExcludes",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, emptyCommandLine, config));
        assertEquals("3", invoke("resolveMultiThread", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                emptyCommandLine, config));
        assertArrayEquals(new String[] { "configured-path" }, (String[]) invoke("resolvePaths",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, emptyCommandLine, config));
    }

    @Test
    void processPaths_returnsSuccessTerminationAndFailureExitCodes() throws Exception {
        // Arrange
        RecordingProcessor successful = new RecordingProcessor(tempDir.toFile(), null);
        RecordingProcessor terminated = new RecordingProcessor(tempDir.toFile(), new ProcessTerminationException(7));
        RecordingProcessor failed = new RecordingProcessor(tempDir.toFile(), new IllegalArgumentException("bad input"));

        // Act
        int success = (Integer) invoke("processPathectories",
                new Class<?>[] { AIFileProcessor.class, String[].class, File.class }, successful, new String[] { "a", "b" },
                tempDir.toFile());
        int termination = (Integer) invoke("processPathectories",
                new Class<?>[] { AIFileProcessor.class, String[].class, File.class }, terminated, new String[] { "a" },
                tempDir.toFile());
        int failure = (Integer) invoke("processPathectories",
                new Class<?>[] { AIFileProcessor.class, String[].class, File.class }, failed, new String[] { "a" }, tempDir.toFile());

        // Assert
        assertEquals(0, success);
        assertEquals(2, successful.scans);
        assertEquals(7, termination);
        assertEquals(1, failure);
    }

    @Test
    void commonSettings_applyOnlyConfiguredValues() throws Exception {
        // Arrange
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "model");
        java.lang.reflect.Constructor<?> constructor = Class
                .forName("org.machanism.machai.gw.processor.Ghostwriter$RuntimeSettings").getDeclaredConstructor();
        constructor.setAccessible(true);
        Object settings = constructor.newInstance();
        setField(settings, "instructions", "instructions");
        setField(settings, "excludes", new String[] { "target" });
        setField(settings, "multiThread", "2");

        // Act
        invoke("applyCommonSettings", new Class<?>[] { AIFileProcessor.class, settings.getClass() }, processor, settings);

        // Assert
        assertEquals("instructions", processor.getInstructions());
        assertArrayEquals(new String[] { "target" }, processor.getExcludes());
        assertFalse(processor.isNonRecursive());
        assertTrue(processor.getModuleThreadTimeoutMinutes() > 0);
    }

    private CommandLine parse(String... arguments) throws Exception {
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        return new DefaultParser().parse(options, arguments);
    }

    private static Object invoke(String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class RecordingProcessor extends AIFileProcessor {
        private final RuntimeException failure;
        private int scans;

        private RecordingProcessor(File rootDir, RuntimeException failure) {
            super(rootDir, new PropertiesConfigurator(), "model");
            this.failure = failure;
        }

        @Override
        public void scanDocuments(File projectDir, String path) {
            scans++;
            if (failure != null) {
                throw failure;
            }
        }
    }
}
