package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.layout.ProjectLayout;

/** Exercises processor workflows without making an external AI request. */
class ProcessorUncoveredWorkflowTest {

    @TempDir
    Path tempDir;

    @Test
    void guidanceProcessFile_usesReviewerGuidanceAndDefaultPromptOnlyWhenNeeded() throws Exception {
        RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor(tempDir.toFile());
        processor.register("txt", new Reviewer() {
            @Override
            public String perform(File project, File file) {
                return "extracted guidance";
            }

            @Override
            public String[] getSupportedFileExtensions() {
                return new String[] { "txt" };
            }
        });
        ProjectLayout layout = layout();
        File tagged = Files.write(tempDir.resolve("tagged.txt"), Collections.singletonList("text"), StandardCharsets.UTF_8)
                .toFile();
        File unsupported = Files.write(tempDir.resolve("plain.bin"), Collections.singletonList("text"),
                StandardCharsets.UTF_8).toFile();

        processor.processFile(layout, tagged);
        assertEquals("extracted guidance", processor.guidance);
        assertEquals(tagged, processor.processedFile);

        processor.setDefault("fallback guidance");
        processor.setPathMatcher(path -> true);
        processor.processFile(layout, unsupported);
        assertEquals("fallback guidance", processor.guidance);

        processor.setDefault(null);
        processor.guidance = null;
        processor.processFile(layout, unsupported);
        assertEquals(null, processor.guidance);
    }

    @Test
    void guidanceMatch_appliesDefaultPromptRulesWhenNoMatcherConfigured() {
        RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor(tempDir.toFile());
        File child = tempDir.resolve("child.txt").toFile();

        assertTrue(processor.matches(tempDir.toFile(), tempDir.toFile()));
        assertTrue(processor.matches(child, tempDir.toFile()));
        processor.setDefault("default");
        assertTrue(processor.matches(tempDir.toFile(), tempDir.toFile()));
        assertFalse(processor.matches(child, tempDir.toFile()));
    }

    @Test
    void actProcessor_privateMergeAndAutoToolHelpers_handleEdgeCases() throws Exception {
        Method merge = ActProcessor.class.getDeclaredMethod("mergeStringWithListValue", List.class, String.class,
                String.class);
        merge.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> merged = (List<String>) merge.invoke(null, Arrays.asList("before ${super.value}", ""), "child",
                ActProcessor.INPUTS_PROPERTY_NAME);
        assertEquals(Arrays.asList("before child", ""), merged);

        ActProcessor processor = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());
        Method auto = ActProcessor.class.getDeclaredMethod("isAutoToolSelection", String.class);
        auto.setAccessible(true);
        Method query = ActProcessor.class.getDeclaredMethod("getAutoToolSelectionQuery", String.class);
        query.setAccessible(true);
        assertTrue((Boolean) auto.invoke(processor, "auto"));
        assertTrue((Boolean) auto.invoke(processor, "{auto=local only}"));
        assertFalse((Boolean) auto.invoke(processor, "read_file"));
        assertEquals("", query.invoke(processor, "auto"));
        assertEquals("local only", query.invoke(processor, "{auto=local only}"));
    }

    @Test
    void actProcessFile_addsOverriddenProcessingResult() throws Exception {
        RecordingActProcessor processor = new RecordingActProcessor(tempDir.toFile());
        processor.setPrompt("review");
        File file = Files.write(tempDir.resolve("sample.txt"), Collections.singletonList("x")).toFile();

        processor.processFile(layout(), file);

        assertEquals(Collections.singletonList("done:review"), processor.getResults());
    }

    private ProjectLayout layout() {
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir.toFile());
        return layout;
    }

    private static class RecordingGuidanceProcessor extends GuidanceProcessor {
        String guidance;
        File processedFile;
        private final Map<String, Reviewer> reviewers = new HashMap<>();

        RecordingGuidanceProcessor(File root) {
            super(root, "model", new PropertiesConfigurator());
        }

        @Override
        void loadReviewers() {
            // Sonar java:S1186: tests inject reviewers explicitly instead of loading services.
        }

        void register(String extension, Reviewer reviewer) {
            reviewers.put(extension, reviewer);
        }

        @Override
        Reviewer getReviewerForExtension(String extension) {
            return reviewers.get(normalizeExtensionKey(extension));
        }

        @Override
        protected String process(ProjectLayout layout, File file, String instructions, String... prompts) {
            processedFile = file;
            guidance = prompts[prompts.length - 1];
            return "recorded";
        }

        @Override
        public String process(ProjectLayout layout, File file, String value) {
            processedFile = file;
            guidance = value;
            return "recorded";
        }

        void setDefault(String prompt) {
            setDefaultPrompt(prompt);
        }

        boolean matches(File file, File project) {
            return match(file, project);
        }
    }

    private static final class RecordingActProcessor extends ActProcessor {
        RecordingActProcessor(File root) {
            super(root, "model", new PropertiesConfigurator());
        }

        void setPrompt(String prompt) {
            setDefaultPrompt(prompt);
        }

        @Override
        public String process(ProjectLayout layout, File file, String prompt) {
            return "done:" + prompt;
        }
    }
}
