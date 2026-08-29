package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;

/** Coverage for traversal paths that deliberately avoid an AI provider. */
class ProcessorTraversalCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void processFolderAndProjectDir_processFilesSelectedByDirectoryAndGlob() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("nested"));
        File rootFile = Files.write(tempDir.resolve("root.txt"), Collections.singletonList("root")).toFile();
        File nestedFile = Files.write(tempDir.resolve("nested/child.txt"), Collections.singletonList("child")).toFile();
        RecordingFileProcessor processor = new RecordingFileProcessor(tempDir.toFile());
        ProjectLayout layout = layout(Collections.emptyList());

        // Act
        processor.processFolder(layout);
        List<File> allProcessed = new ArrayList<>(processor.processed);
        processor.processed.clear();
        processor.processProjectDir(layout, "glob:**.txt");

        // Assert
        assertTrue(allProcessed.contains(rootFile));
        assertTrue(allProcessed.contains(nestedFile));
        assertTrue(processor.processed.contains(rootFile));
        assertTrue(processor.processed.contains(nestedFile));
    }

    @Test
    void scanFolder_honorsNonRecursiveAndProcessesParentFiles() throws Exception {
        // Arrange
        RecordingFileProcessor processor = new RecordingFileProcessor(tempDir.toFile());
        processor.setNonRecursive(true);
        ProjectLayout layout = layout(Collections.singletonList("module"));
        processor.layout = layout;

        // Act
        processor.scanFolder(tempDir.toFile());

        // Assert
        assertEquals(0, processor.modules.size());
        assertEquals(1, processor.parentCalls);
    }

    @Test
    void guidanceParentProcessing_filtersModulesAndUsesDefaultPromptForProject() throws Exception {
        // Arrange
        File regular = Files.write(tempDir.resolve("regular.txt"), Collections.singletonList("x")).toFile();
        File module = Files.createDirectories(tempDir.resolve("module")).toFile();
        ProjectLayout layout = layout(Collections.singletonList("module"));
        RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor(tempDir.toFile());
        processor.setDefaultPrompt("default guidance");
        processor.setPathMatcher(path -> true);

        // Act
        processor.processParentFiles(layout);

        // Assert
        assertTrue(processor.files.contains(regular));
        assertFalse(processor.files.contains(module));
        assertEquals(tempDir.toFile(), processor.defaultProcessedFile);
        assertEquals("default guidance", processor.defaultGuidance);
    }

    private ProjectLayout layout(List<String> modules) {
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir.toFile());
        when(layout.getModules()).thenReturn(modules);
        return layout;
    }

    private static class RecordingFileProcessor extends AbstractFileProcessor {
        final List<File> processed = new ArrayList<>();
        final List<String> modules = new ArrayList<>();
        int parentCalls;
        ProjectLayout layout;

        RecordingFileProcessor(File root) {
            super(root, new PropertiesConfigurator());
        }

        @Override
        public ProjectLayout getProjectLayout(File projectDir) {
            return layout;
        }

        @Override
        protected void processModule(File projectDir, String module) {
            modules.add(module);
        }

        @Override
        protected void processParentFiles(ProjectLayout projectLayout) {
            parentCalls++;
        }

        @Override
        protected void processFile(ProjectLayout projectLayout, File file) {
            if (file.isFile()) {
                processed.add(file);
            }
        }
    }

    private static final class RecordingGuidanceProcessor extends GuidanceProcessor {
        final List<File> files = new ArrayList<>();
        File defaultProcessedFile;
        String defaultGuidance;

        RecordingGuidanceProcessor(File root) {
            super(root, "model", new PropertiesConfigurator());
        }

        @Override
        void loadReviewers() {
            // Tests do not require ServiceLoader-discovered reviewers.
        }

        @Override
        protected void processFile(ProjectLayout projectLayout, File file) {
            files.add(file);
        }

        @Override
        public String process(ProjectLayout projectLayout, File file, String guidance) {
            defaultProcessedFile = file;
            defaultGuidance = guidance;
            return "done";
        }
    }
}
