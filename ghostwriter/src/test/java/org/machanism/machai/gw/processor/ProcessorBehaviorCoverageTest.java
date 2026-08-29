package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.tools.MoveToEpisodeException;

/** Covers episode redirection and scanning configurations not exercised by basic tests. */
class ProcessorBehaviorCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void regularOrder_restartsAtRequestedEpisodeAfterMoveByName() {
        // Arrange
        ActProcessor processor = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());
        Episodes episodes = new Episodes(processor);
        episodes.setEpisodes(Arrays.asList("# First\nfirst", "# Second\nsecond"));
        AtomicInteger firstCalls = new AtomicInteger();

        // Act
        episodes.regularOrder(1, (id, prompt) -> {
            if (id == 1 && firstCalls.getAndIncrement() == 0) {
                throw new MoveToEpisodeException(null, "Second");
            }
            return "done-" + id;
        });

        // Assert
        assertEquals(1, firstCalls.get());
        assertEquals(Arrays.asList("done-2"), processor.getResults());
    }

    @Test
    void scanDocuments_rejectsInvalidArgumentsAndConfiguresAbsoluteProjectScan() throws Exception {
        // Arrange
        RecordingAiProcessor processor = new RecordingAiProcessor(tempDir.toFile());

        // Act + Assert
        assertInvalidScanArguments(processor);
        processor.scanDocuments(tempDir.toFile(), tempDir.toFile().getAbsolutePath());
        assertEquals(1, processor.scans);
        assertEquals(tempDir.toFile(), processor.getPath());
        assertEquals(null, processor.getPathMatcher());
    }

    @Test
    void parsePath_usesRootForDotAndFallsBackWhenPathIsOutsideProject() throws Exception {
        // Arrange
        RecordingAiProcessor processor = new RecordingAiProcessor(tempDir.toFile());
        Path project = Files.createDirectories(tempDir.resolve("project"));

        // Act
        String rootPattern = processor.parsePath(project.toFile(), ".");
        String outsidePattern = processor.parsePath(project.toFile(), tempDir.resolveSibling("outside").toString());

        // Assert
        assertTrue(rootPattern.startsWith("glob:"));
        assertTrue(outsidePattern.startsWith("glob:."));
        assertEquals(tempDir.toFile(), processor.getPath());
    }

    @Test
    void abstractProcessor_helpers_handlePatternsAndPathDepth() {
        // Arrange + Act + Assert
        assertTrue(AbstractFileProcessor.isPathPattern("glob:**/*.java"));
        assertTrue(AbstractFileProcessor.isPathPattern("REGEX:.*"));
        assertFalse(AbstractFileProcessor.isPathPattern("src/main"));
        assertEquals(0, AbstractFileProcessor.pathDepth(" "));
        assertEquals(3, AbstractFileProcessor.pathDepth("one\\two/three"));
    }

    private static final class RecordingAiProcessor extends AIFileProcessor {
        private int scans;

        private RecordingAiProcessor(File root) {
            super(root, new PropertiesConfigurator(), "model");
        }

        @Override
        public void scanFolder(File projectDir) {
            scans++;
        }
    }

    private void assertInvalidScanArguments(RecordingAiProcessor processor) {
        assertThrows(IllegalArgumentException.class, () -> scanWithNullProject(processor));
        assertThrows(IllegalArgumentException.class, () -> scanWithBlankPath(processor));
    }

    private static void scanWithNullProject(RecordingAiProcessor processor) throws IOException {
        processor.scanDocuments(null, ".");
    }

    private void scanWithBlankPath(RecordingAiProcessor processor) throws IOException {
        processor.scanDocuments(tempDir.toFile(), "  ");
    }
}
