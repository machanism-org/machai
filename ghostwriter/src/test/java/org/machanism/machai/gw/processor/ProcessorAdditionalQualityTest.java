package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.tomlj.Toml;

/** Additional boundary and integration tests for the processor package only. */
class ProcessorAdditionalQualityTest {

    @TempDir
    Path tempDir;

    private static AbstractFileProcessor processor(File root) {
        return new AbstractFileProcessor(root, new PropertiesConfigurator()) {
            @Override
            protected void processParentFiles(ProjectLayout layout) {
                // Deliberately empty: traversal behavior is the unit under test.
            }
        };
    }

    @Test
    void abstractProcessor_accessorsAndValidation_areConsistent() {
        // Arrange
        AbstractFileProcessor subject = processor(tempDir.toFile());
        String[] excludes = {"target", "glob:**/*.generated"};

        // Act
        subject.setThreads(3);
        subject.setNonRecursive(true);
        subject.setExcludes(excludes);
        subject.setPath(tempDir.toFile());
        subject.setPathMatcher(path -> true);
        subject.setModuleThreadTimeoutMinutes(2);

        // Assert
        assertEquals(3, getThreads(subject));
        assertTrue(subject.isNonRecursive());
        assertSame(excludes, subject.getExcludes());
        assertEquals(tempDir.toFile(), subject.getPath());
        assertNotNull(subject.getPathMatcher());
        assertEquals(2, subject.getModuleThreadTimeoutMinutes());
        assertNotNull(subject.getConfigurator());
        assertThrows(IllegalArgumentException.class, () -> subject.setThreads(0));
    }

    @Test
    void abstractProcessor_match_handlesNullExactPathAndMatcherBoundaries() {
        // Arrange
        AbstractFileProcessor subject = processor(tempDir.toFile());
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
        File file = tempDir.resolve("sample.txt").toFile();
        File outside = tempDir.getParent().resolve("outside.txt").toFile();

        // Act + Assert
        assertFalse(subject.match(null, layout));
        assertFalse(subject.match(file, layout));
        subject.setPath(file);
        assertTrue(subject.match(file, layout));
        subject.setPathMatcher(path -> path.toString().endsWith("sample.txt"));
        assertTrue(subject.match(file, layout));
        assertFalse(subject.match(outside, layout));
    }

    @Test
    void abstractProcessor_listFiles_excludesConfiguredEntriesAndSortsDeepestFirst() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("src/main"));
        Files.write(tempDir.resolve("src/main/App.java"), Collections.singletonList("class App {}"));
        Files.write(tempDir.resolve("src/skip.txt"), Collections.singletonList("skip"));
        AbstractFileProcessor subject = processor(tempDir.toFile());
        subject.setExcludes(new String[] {"skip.txt"});

        // Act
        List<File> files = subject.listFiles(tempDir.toFile());

        // Assert
        assertTrue(files.stream().anyMatch(file -> file.getName().equals("App.java")));
        assertFalse(files.stream().anyMatch(file -> file.getName().equals("skip.txt")));
        assertTrue(files.indexOf(tempDir.resolve("src/main/App.java").toFile())
                < files.indexOf(tempDir.resolve("src").toFile()));
    }

    @Test
    void aiProcessor_parseLines_resolvesNestedLocalIncludesAndMissingData() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("docs"));
        Files.write(tempDir.resolve("docs/inner.txt"), Collections.singletonList("inner ${public.name}"));
        Files.write(tempDir.resolve("docs/outer.txt"), Collections.singletonList(">>> file://docs/inner.txt"));
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set("public.name", "Ada");
        AIFileProcessor subject = new AIFileProcessor(tempDir.toFile(), config, "model");
        String input = ">>> file://docs/outer.txt\nend";

        // Act
        String parsed = subject.parseLines(input, tempDir.toFile(), config);

        // Assert
        assertEquals("inner Ada\nend", parsed);
        assertEquals("", subject.parseLines(null, tempDir.toFile(), config));
        assertThrows(IllegalArgumentException.class,
                () -> subject.parseLines(">>> file://docs/missing.txt", tempDir.toFile(), config));
    }

    @Test
    void aiProcessor_parsePath_andDirectoryInfo_rejectOutsidePathsAndHandlesEmptyCases() throws Exception {
        // Arrange
        AIFileProcessor subject = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "model");
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
        subject.setDefaultPrompt(null);

        // Act
        String relativePattern = subject.parsePath(tempDir.toFile(), ".");

        // Assert
        assertTrue(relativePattern.startsWith("glob:"));
        assertTrue(relativePattern.endsWith("{,/**}"));
        assertThrows(IllegalArgumentException.class,
                () -> subject.parsePath(tempDir.toFile(), tempDir.getParent().toString()));
        assertNull(subject.getDirInfoLine(Collections.emptyList(), layout.getProjectDir()));
        assertEquals(tempDir.toFile(), subject.getPath());
    }

    @Test
    void guidanceProcessor_normalizesExtensionsAndUsesDefaultInstructions() {
        // Arrange
        GuidanceProcessor subject = new GuidanceProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());

        // Act + Assert
        assertEquals("java", GuidanceProcessor.normalizeExtensionKey(" .JAVA "));
        assertEquals("md", GuidanceProcessor.normalizeExtensionKey(".md"));
        assertNull(GuidanceProcessor.normalizeExtensionKey("  "));
        assertNull(subject.getReviewerForExtension(null));
        assertNotNull(subject.getInstructions());
        assertEquals("java", GuidanceProcessor.normalizeExtensionKey("Java"));
    }

    @Test
    void guidanceProcessor_parseFile_returnsNullForDirectoriesAndUnsupportedTypes() throws Exception {
        // Arrange
        GuidanceProcessor subject = new GuidanceProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());
        File directory = Files.createDirectory(tempDir.resolve("folder")).toFile();
        File unsupported = Files.createFile(tempDir.resolve("file.unknown")).toFile();

        // Act + Assert
        assertNull(subject.parseFile(tempDir.toFile(), directory));
        assertNull(subject.parseFile(tempDir.toFile(), unsupported));
        assertTrue(subject.getReport().isEmpty());
    }

    @Test
    void actProcessor_setActData_mergesInheritedStringsArraysAndScalarValues() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();
        properties.put("instructions", "before ${super.value}");
        properties.put("inputs", new ArrayList<>(Collections.singletonList("old ${super.value}")));
        String toml = "instructions = 'after'\ninputs = ['new']\nactive = true\ncount = 2\nratio = 1.5";

        // Act
        ActProcessor.setActData(properties, Toml.parse(toml));

        // Assert
        assertEquals("before after", properties.get("instructions"));
        assertEquals(Collections.singletonList("old new"), properties.get("inputs"));
        assertEquals("true", properties.get("active"));
        // TOML represents an integer outside the supported scalar branches as a
        // long value; the processor intentionally leaves that value unchanged.
        assertNull(properties.get("count"));
        assertEquals("1.5", properties.get("ratio"));
    }

    @Test
    void actProcessor_externalActLocation_validatesPathsAndPreservesNull() throws Exception {
        // Arrange
        PropertiesConfigurator config = new PropertiesConfigurator();
        ActProcessor subject = new ActProcessor(tempDir.toFile(), "model", config);

        // Act
        subject.setActsLocation(null);
        subject.setActsLocation(tempDir.toString());

        // Assert
        assertNotNull(subject.getConfigurator());
        assertThrows(IllegalArgumentException.class, () -> subject.setActsLocation("missing-acts"));
    }

    @Test
    void episodes_emptyAndRepeatedConfiguration_exposeStableState() {
        // Arrange
        Episodes subject = new Episodes(new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator()));
        List<String> prompts = Arrays.asList("one", "two");

        // Act
        subject.setEpisodes(prompts);
        subject.setSelectedEpisodes(Collections.emptyList());

        // Assert
        assertSame(prompts, subject.getEpisodes());
        assertEquals(2, subject.size());
        assertTrue(subject.isRegularOrder());
        assertEquals(0, subject.getActInformation(0).get("CURRENT_EPISODE_ID"));
    }

    private static int getThreads(AbstractFileProcessor subject) {
        try {
            java.lang.reflect.Field field = AbstractFileProcessor.class.getDeclaredField("threads");
            field.setAccessible(true);
            return field.getInt(subject);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
