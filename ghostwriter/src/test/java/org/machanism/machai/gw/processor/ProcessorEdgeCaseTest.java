package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/** Boundary tests for public and package-private processor behavior. */
class ProcessorEdgeCaseTest {

    @TempDir
    Path tempDir;

    private static AbstractFileProcessor fileProcessor(File root) {
        return new AbstractFileProcessor(root, new PropertiesConfigurator()) {
            @Override
            protected void processParentFiles(ProjectLayout layout) {
                // Traversal tests do not need parent processing.
            }
        };
    }

    @Test
    void abstractProcessor_addMatchingFile_appliesMatcherAndExclusionRules() throws IOException {
        // Arrange
        File file = Files.createFile(tempDir.resolve("readme.md")).toFile();
        AbstractFileProcessor processor = fileProcessor(tempDir.toFile());
        List<File> matches = new ArrayList<>();

        // Act
        processor.addMatchingFile(matches, null, tempDir.toFile(), file);
        processor.setExcludes(new String[] { "readme.md" });
        processor.addMatchingFile(matches, null, tempDir.toFile(), file);

        // Assert
        assertEquals(1, matches.size());
        assertEquals(file, matches.get(0));
    }

    @Test
    void abstractProcessor_processFolder_wrapsFileProcessingFailure() throws IOException {
        // Arrange
        File file = Files.createFile(tempDir.resolve("input.txt")).toFile();
        AbstractFileProcessor processor = new AbstractFileProcessor(tempDir.toFile(), new PropertiesConfigurator()) {
            @Override
            protected void processFile(ProjectLayout layout, File candidate) throws IOException {
                if (candidate.equals(file)) {
                    throw new IOException("read failure");
                }
            }
        };
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> processor.processFolder(layout));

        // Assert
        assertTrue(failure.getCause() instanceof IOException);
        assertEquals("read failure", failure.getCause().getMessage());
    }

    @Test
    void abstractProcessor_settersPreserveNullAndValidValues() {
        // Arrange
        AbstractFileProcessor processor = fileProcessor(tempDir.toFile());

        // Act
        processor.setExcludes(null);
        processor.setPath(null);
        processor.setPathMatcher(null);
        processor.setNonRecursive(false);
        processor.setThreads(1);

        // Assert
        assertEquals(null, processor.getExcludes());
        assertEquals(null, processor.getPath());
        assertEquals(null, processor.getPathMatcher());
        assertFalse(processor.isNonRecursive());
    }

    @Test
    void aiProcessor_parseLines_substitutesPublicValuesOnOrdinaryLines() {
        // Arrange
        PropertiesConfigurator configurator = new PropertiesConfigurator();
        configurator.set("public.value", "resolved");
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configurator, "model");

        // Act
        String parsed = processor.parseLines("before ${public.value}\nafter", tempDir.toFile(), configurator);

        // Assert
        assertEquals("before resolved\nafter", parsed);
    }

    @Test
    void aiProcessor_getDirInfoLine_ignoresNullAndMissingEntries() {
        // Arrange
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "model");

        // Act
        com.fasterxml.jackson.databind.node.ArrayNode result = processor.getDirInfoLine(
                java.util.Arrays.asList(null, "missing"), tempDir.toFile());

        // Assert
        assertEquals(null, result);
    }

    @Test
    void episodes_emptyInformationContainsEmptyEpisodeCollection() {
        // Arrange
        Episodes episodes = new Episodes(new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator()));

        // Act
        java.util.Map<String, Object> information = episodes.getActInformation(0);

        // Assert
        assertNotNull(information);
        assertEquals(Collections.emptyList(), information.get("EPISODES"));
        assertEquals(0, information.get("CURRENT_EPISODE_ID"));
    }
}
