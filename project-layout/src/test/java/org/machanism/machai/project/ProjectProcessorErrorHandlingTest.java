package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/** Verifies failure propagation at the package-level processor boundary. */
class ProjectProcessorErrorHandlingTest {

    @TempDir
    File projectDirectory;

    @Test
    void scanFolder_shouldPropagateIOExceptionFromTheFirstModuleAndStopProcessingLaterModules() {
        // Arrange
        FailingModuleProcessor processor = new FailingModuleProcessor();
        processor.layout = new ModulesLayout(Arrays.asList("first", "second"));

        // Act
        IOException exception = assertThrows(IOException.class,
                () -> processor.scanFolder(projectDirectory));

        // Assert
        assertEquals("Cannot process first", exception.getMessage());
        assertEquals(Arrays.asList("first"), processor.attemptedModules);
    }

    @Test
    void scanFolder_shouldPropagateFailureRaisedByFolderHandler() {
        // Arrange
        RuntimeException expected = new RuntimeException("folder failure");
        ProjectProcessor processor = new ProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout layout) {
                throw expected;
            }

            @Override
            public ProjectLayout getProjectLayout(File directory) {
                return new ModulesLayout(null);
            }
        };

        // Act
        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> processor.scanFolder(projectDirectory));

        // Assert
        assertEquals(expected, actual);
    }

    private static final class FailingModuleProcessor extends ProjectProcessor {
        private ProjectLayout layout;
        private final java.util.List<String> attemptedModules = new java.util.ArrayList<String>();

        @Override
        public void processFolder(ProjectLayout processor) {
            // The test exercises the module branch only.
        }

        @Override
        public ProjectLayout getProjectLayout(File directory) {
            return layout;
        }

        @Override
        protected void processModule(File directory, String module) throws IOException {
            attemptedModules.add(module);
            throw new IOException("Cannot process " + module);
        }
    }

    private static final class ModulesLayout extends ProjectLayout {
        private final java.util.List<String> modules;

        private ModulesLayout(java.util.List<String> modules) {
            this.modules = modules;
        }

        @Override
        public File getProjectDir() {
            return null;
        }

        @Override
        public java.util.List<String> getModules() {
            return modules;
        }

        @Override
        public java.util.List<String> getSources() {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<String> getDocuments() {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<String> getTests() {
            return java.util.Collections.emptyList();
        }
    }
}
