package org.machanism.machai.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class ProjectProcessorTest {

    private static class TestProcessor extends ProjectProcessor {
        private final List<String> processedModules = new ArrayList<>();
        private boolean folderProcessed = false;
        private ProjectLayout lastLayout = null;

        @Override
        public void processFolder(ProjectLayout processor) {
            folderProcessed = true;
            lastLayout = processor;
        }

        public boolean isFolderProcessed() {
            return folderProcessed;
        }

        public List<String> getProcessedModules() {
            return processedModules;
        }

        @Override
        protected void processModule(File projectDir, String module) throws IOException {
            processedModules.add(module);
        }
    }

    private TestProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TestProcessor();
    }

    @Test
    void scanFolderProcessesModules(@TempDir File tempDir) throws Exception {
        // Arrange
        File moduleA = new File(tempDir, "moduleA");
        File moduleB = new File(tempDir, "moduleB");
        assertTrue(moduleA.mkdir());
        assertTrue(moduleB.mkdir());
        ProjectLayout layout = new ProjectLayout() {
            @Override
            public List<String> getModules() { return List.of("moduleA", "moduleB"); }
            @Override
            public List<String> getSources() { return List.of(); }
            @Override
            public List<String> getDocuments() { return List.of(); }
            @Override
            public List<String> getTests() { return List.of(); }
        };
        // Inject layout override
        processor = new TestProcessor() {
            @Override
            protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
                return layout;
            }
        };

        // Act
        processor.scanFolder(tempDir);

        // Assert
        List<String> processed = processor.getProcessedModules();
        assertTrue(processed.contains("moduleA"));
        assertTrue(processed.contains("moduleB"));
        assertFalse(processor.isFolderProcessed());
    }

    @Test
    void scanFolderProcessesFolderIfNoModules(@TempDir File tempDir) throws Exception {
        // Arrange
        ProjectLayout layout = new ProjectLayout() {
            @Override
            public List<String> getModules() { return null; }
            @Override
            public List<String> getSources() { return List.of(); }
            @Override
            public List<String> getDocuments() { return List.of(); }
            @Override
            public List<String> getTests() { return List.of(); }
        };
        processor = new TestProcessor() {
            @Override
            protected ProjectLayout getProjectLayout(File dir) throws FileNotFoundException {
                return layout;
            }
        };

        // Act
        processor.scanFolder(tempDir);

        // Assert
        assertTrue(processor.isFolderProcessed());
        assertEquals(layout, processor.lastLayout);
    }

    @Test
    void getProjectLayoutThrowsIfFileNotFound() {
        File notFoundDir = new File("/does/not/exist/" + System.currentTimeMillis());
        assertThrows(FileNotFoundException.class, () -> processor.getProjectLayout(notFoundDir));
    }
}
