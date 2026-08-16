package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/** Focused tests for package-level processor state and episode control flow. */
class ProcessorCoreLogicTest {

    @TempDir
    Path tempDir;

    @Test
    void episodes_regularOrder_recordsResultsAndRepeatsAnEpisode() {
        // Arrange
        ActProcessor act = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());
        Episodes episodes = new Episodes(act);
        episodes.setEpisodes(Arrays.asList("# One\nfirst", "# Two\nsecond"));
        AtomicInteger calls = new AtomicInteger();

        // Act
        episodes.regularOrder(1, (id, prompt) -> {
            if (id == 1 && calls.getAndIncrement() == 0) {
                throw new RepeatEpisodeException();
            }
            return "result-" + id;
        });

        // Assert
        assertEquals(2, calls.get());
        assertEquals(Arrays.asList("result-1", "result-2"), act.getResults());
    }

    @Test
    void episodes_requestedOrder_executesOnlySelectedEpisodes() {
        // Arrange
        ActProcessor act = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());
        Episodes episodes = new Episodes(act);
        episodes.setEpisodes(Arrays.asList("# One\nfirst", "# Two\nsecond", "third"));
        episodes.setSelectedEpisodes(Collections.singletonList(2));

        // Act
        int lastId = episodes.requestedOrder((id, prompt) -> "selected");

        // Assert
        assertEquals(2, lastId);
        assertEquals(Collections.singletonList("selected"), act.getResults());
        assertFalse(episodes.isRegularOrder());
    }

    @Test
    void episodes_selectionRejectsIdsOutsideConfiguredRange() {
        // Arrange
        Episodes episodes = new Episodes(new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator()));
        episodes.setEpisodes(Collections.singletonList("one"));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> episodes.setSelectedEpisodes(Arrays.asList(0)));
        assertThrows(IllegalArgumentException.class, () -> episodes.setSelectedEpisodes(Arrays.asList(2)));
    }

    @Test
    void episodes_resolvesMoveByIdAndHeadingAndReportsUnknownHeading() {
        // Arrange
        Episodes episodes = new Episodes(new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator()));
        episodes.setEpisodes(Arrays.asList("---\n---\n# Intro\ntext", "# Review\ntext"));

        // Act + Assert
        assertEquals(2, episodes.getEpisodeId(1, new MoveToEpisodeException(2, null)));
        assertEquals(2, episodes.getEpisodeId(1, new MoveToEpisodeException(null, "Review")));
        assertThrows(EpisodeNotFoundException.class,
                () -> episodes.getEpisodeId(1, new MoveToEpisodeException(null, "Missing")));
    }

    @Test
    void episodes_exposesNamesAndActInformation() {
        // Arrange
        Episodes episodes = new Episodes(new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator()));
        episodes.setName("demo");
        episodes.setEpisodes(Arrays.asList("# Intro\ntext", "plain"));

        // Act
        Map<String, Object> information = episodes.getActInformation(1);

        // Assert
        assertEquals("demo", episodes.getName());
        assertEquals(2, episodes.size());
        assertEquals(2, ((List<?>) information.get("EPISODES")).size());
        assertEquals(1, information.get("CURRENT_EPISODE_ID"));
        assertNotNull(information.get("ACT_INFORMATION"));
    }

    @Test
    void aiProcessor_parsesSubstitutionAndDirectoryInfoAndProcessMetadata() throws Exception {
        // Arrange
        PropertiesConfigurator config = new PropertiesConfigurator();
        config.set("public.name", "Ada");
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), config, "model");
        Files.createDirectories(tempDir.resolve("src"));
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act
        String parsed = processor.parseLines("Hello ${public.name}\nplain", tempDir.toFile(), config);
        String info = processor.getProcessInfo(layout, tempDir.resolve("src/File.java").toFile());

        // Assert
        assertEquals("Hello Ada\nplain", parsed);
        assertTrue(info.contains("PROCESSED_FILE_REL_PATH"));
        assertTrue(info.contains("NOT-INTERACTIVE"));
        assertEquals(1, processor.getDirInfoLine(Collections.singleton("src"), tempDir.toFile()).size());
        assertEquals(null, processor.getDirInfoLine(Collections.singleton("missing"), tempDir.toFile()));
    }

    @Test
    void aiProcessor_stateAccessorsRoundTripValues() {
        // Arrange
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "old");

        // Act
        processor.setModel("new");
        processor.setInstructions("instructions");
        processor.setDefaultPrompt("prompt");
        processor.setInteractive(true);

        // Assert
        assertEquals("new", processor.getModel());
        assertEquals("instructions", processor.getInstructions());
        assertEquals("prompt", processor.getDefaultPrompt());
        assertTrue(processor.isInteractive());
    }

    @Test
    void projectContextKey_returnsDeclaredWireValues() {
        // Arrange + Act + Assert
        assertEquals(ProjectContextKey.values().length, 12);
        assertEquals("OPERATING_SYSTEM", ProjectContextKey.OPERATING_SYSTEM.getKey());
        assertEquals("MODULES", ProjectContextKey.MODULES.getKey());
    }
}
