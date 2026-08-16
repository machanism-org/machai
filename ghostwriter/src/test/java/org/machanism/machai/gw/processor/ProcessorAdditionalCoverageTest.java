package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/** Additional boundary and filesystem tests for the processor package. */
class ProcessorAdditionalCoverageTest {

    @TempDir
    Path tempDir;

    private AbstractFileProcessorTest.TestProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AbstractFileProcessorTest.TestProcessor(tempDir.toFile(), new PropertiesConfigurator());
    }

    @Test
    void accessorsAndValidation_preserveConfiguredProcessorState() {
        // Arrange
        Path scanPath = tempDir.resolve("src");
        processor.setThreads(3);
        processor.setNonRecursive(true);
        processor.setExcludes(new String[] { "glob:**/*.tmp" });
        processor.setPath(scanPath.toFile());

        // Act
        processor.setPathMatcher(AbstractFileProcessor.getPatternPath("glob:**/*.java"));

        // Assert
        assertEquals(3, getThreads(processor));
        assertTrue(processor.isNonRecursive());
        assertEquals(scanPath.toFile(), processor.getPath());
        assertEquals("glob:**/*.tmp", processor.getExcludes()[0]);
        assertNotNull(processor.getPathMatcher());
        assertEquals(60, processor.getModuleThreadTimeoutMinutes());
        assertThrows(IllegalArgumentException.class, () -> processor.setThreads(0));
    }

    @Test
    void listFiles_recursesExcludesConfiguredEntriesAndSortsDeepestFirst() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("src/main"));
        Files.write(tempDir.resolve("src/main/App.java"), "class App {}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("src/main/skip.tmp"), "temporary".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.createDirectories(tempDir.resolve("target"));
        Files.write(tempDir.resolve("target/generated.java"), "generated".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        processor.setExcludes(new String[] { "skip.tmp" });

        // Act
        List<File> files = processor.listFiles(tempDir.toFile());

        // Assert
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("App.java")));
        assertFalse(files.stream().anyMatch(f -> f.getName().equals("skip.tmp")));
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("target")));
        assertTrue(files.get(0).getPath().length() >= files.get(files.size() - 1).getPath().length());
    }

    @Test
    void listFiles_withDirectoryAndGlobPatterns_returnsOnlyEligibleMatches() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("src"));
        Files.write(tempDir.resolve("src/A.java"), "a".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("src/B.txt"), "b".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act
        List<File> directoryMatches = processor.listFiles(tempDir.toFile(), "src");
        List<File> globMatches = processor.listFiles(tempDir.toFile(), "glob:**/*.java");

        // Assert
        assertTrue(directoryMatches.stream().anyMatch(f -> f.getName().equals("A.java")));
        assertTrue(directoryMatches.stream().anyMatch(f -> f.getName().equals("src")));
        assertEquals(1, globMatches.stream().filter(f -> f.getName().equals("A.java")).count());
        assertFalse(globMatches.stream().anyMatch(f -> f.getName().equals("B.txt")));
    }

    @Test
    void match_handlesNullMatcherExactPathAndOutOfScopeScanPath() {
        // Arrange
        File projectDir = tempDir.toFile();
        File file = tempDir.resolve("a.txt").toFile();
        processor.setPath(null);
        processor.setPathMatcher(null);

        // Act + Assert
        assertFalse(processor.match(null, projectDir));
        assertFalse(processor.match(file, projectDir));
        processor.setPath(file);
        assertTrue(processor.match(file, projectDir));
        processor.setPathMatcher(AbstractFileProcessor.getPatternPath("glob:**/*.txt"));
        processor.setPath(tempDir.resolve("other").toFile());
        assertFalse(processor.match(file, projectDir));
    }

    @Test
    void addMatchingFile_andExcludeHelpers_applyMatcherAndExactRules() throws Exception {
        // Arrange
        File javaFile = tempDir.resolve("A.java").toFile();
        File textFile = tempDir.resolve("A.txt").toFile();
        Files.write(javaFile.toPath(), "java".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.write(textFile.toPath(), "text".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        List<File> result = new java.util.ArrayList<>();
        processor.setExcludes(new String[] { "A.txt" });

        // Act
        processor.addMatchingFile(result, null, tempDir.toFile(), javaFile);
        processor.addMatchingFile(result, null, tempDir.toFile(), textFile);

        // Assert
        assertEquals(Collections.singletonList(javaFile), result);
        assertTrue(processor.shouldExcludePath(textFile.toPath().getFileName()));
        assertFalse(processor.shouldExcludePath(null));
    }

    @Test
    void processFolder_wrapsIoFailuresAsIllegalArgumentException() {
        // Arrange
        AbstractFileProcessor failing = new AbstractFileProcessorTest.TestProcessor(tempDir.toFile(), new PropertiesConfigurator()) {
            @Override
            List<File> listFiles(File directory) throws IOException {
                throw new IOException("cannot read");
            }
        };
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act + Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> failing.processFolder(layout));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void aiProcessor_fileReferencesSupportNestedIncludesAndSubstitution() throws Exception {
        // Arrange
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set("public.value", "resolved");
        Files.write(tempDir.resolve("inner.txt"), "inner ${public.value}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("outer.txt"), ">>> file://inner.txt\nouter".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        AIFileProcessor ai = new AIFileProcessor(tempDir.toFile(), config, "model");

        // Act
        String result = ai.parseLines(">>> file://outer.txt", tempDir.toFile(), config);

        // Assert
        assertEquals("inner resolved\nouter", result);
    }

    @Test
    void guidanceProcessor_usesDefaultInstructionsAndCaseInsensitiveReviewerLookup() {
        // Arrange
        GuidanceProcessor guidance = new GuidanceProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());

        // Act
        String instructions = guidance.getInstructions();
        String normalized = GuidanceProcessor.normalizeExtensionKey(" .JaVa ");

        // Assert
        assertNotNull(instructions);
        assertEquals("java", normalized);
        assertNull(guidance.getReviewerForExtension("definitely-unknown"));
    }

    @Test
    void shutdownExecutor_acceptsCompletedExecutorAndPreservesConfiguredTimeout() {
        // Arrange
        java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
        processor.setModuleThreadTimeoutMinutes(1);

        // Act
        processor.shutdownExecutor(executor);

        // Assert
        assertTrue(executor.isTerminated() || executor.isShutdown());
        assertEquals(1, processor.getModuleThreadTimeoutMinutes());
    }

    private static int getThreads(AbstractFileProcessor value) {
        try {
            java.lang.reflect.Field field = AbstractFileProcessor.class.getDeclaredField("threads");
            field.setAccessible(true);
            return field.getInt(value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}

