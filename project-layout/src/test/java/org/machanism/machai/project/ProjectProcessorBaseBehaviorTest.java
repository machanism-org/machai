package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/** Tests the non-overridden behavior implemented by {@link ProjectProcessor}. */
class ProjectProcessorBaseBehaviorTest {

    @TempDir
    Path tempDir;

    @Test
    void processModule_shouldScanExistingChildAndProcessItsLayout() throws Exception {
        // Arrange
        File moduleDirectory = tempDir.resolve("module").toFile();
        if (!moduleDirectory.mkdir()) {
            throw new IOException("Could not create module test directory");
        }
        RecordingProcessor processor = new RecordingProcessor();

        // Act
        processor.invokeProcessModule(tempDir.toFile(), "module");

        // Assert
        assertEquals(1, processor.processedLayouts.size());
        assertEquals(moduleDirectory, processor.processedLayouts.get(0).getProjectDir());
    }

    @Test
    void processModule_shouldPropagateMissingChildFailure() {
        // Arrange
        RecordingProcessor processor = new RecordingProcessor();
        String missingModule = "missing-module";

        // Act
        FileNotFoundException exception = assertThrows(FileNotFoundException.class,
                () -> processor.invokeProcessModule(tempDir.toFile(), missingModule));

        // Assert
        assertEquals(tempDir.resolve(missingModule).toFile().getAbsolutePath(), exception.getMessage());
    }

    private static final class RecordingProcessor extends ProjectProcessor {
        private final List<ProjectLayout> processedLayouts = new ArrayList<ProjectLayout>();

        @Override
        public ProjectLayout getProjectLayout(File projectDirectory) throws FileNotFoundException {
            if ("module".equals(projectDirectory.getName())) {
                return new TestLayout(projectDirectory);
            }
            return super.getProjectLayout(projectDirectory);
        }

        @Override
        public void processFolder(ProjectLayout processor) {
            processedLayouts.add(processor);
        }

        private void invokeProcessModule(File projectDirectory, String module) throws IOException {
            processModule(projectDirectory, module);
        }
    }

    private static final class TestLayout extends ProjectLayout {
        private final File directory;

        private TestLayout(File directory) {
            this.directory = directory;
        }

        @Override
        public File getProjectDir() {
            return directory;
        }

        @Override
        public List<String> getModules() {
            return null;
        }

        @Override
        public List<String> getSources() {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<String> getDocuments() {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<String> getTests() {
            return java.util.Collections.emptyList();
        }
    }
}
