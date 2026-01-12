package org.machanism.machai.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.DefaultProjectLayout;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ProjectProcessorTest {

    static class TestProcessor extends ProjectProcessor {
        final AtomicBoolean folderProcessed = new AtomicBoolean(false);
        @Override
        public void processFolder(ProjectLayout processor) {
            folderProcessed.set(true);
        }
    }

    @Test
    @Disabled
    void scanFolderProcessesModulesAndFolders(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File root = tempDir.toFile();
        File moduleA = new File(root, "moduleA");
        assertTrue(moduleA.mkdir());
        new File(moduleA, "pom.xml").createNewFile();

        TestProcessor processor = new TestProcessor() {
            @Override
            public void processFolder(ProjectLayout processor) {
                super.folderProcessed.set(true);
            }
        };

        // Act
        processor.scanFolder(root);

        // Assert (processFolder was called during scanFolder)
        assertTrue(processor.folderProcessed.get());
    }

    @Test
    void processModuleDelegatesToScanFolder(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File root = tempDir.toFile();
        File moduleB = new File(root, "moduleB");
        assertTrue(moduleB.mkdir());

        TestProcessor processor = new TestProcessor();

        // Act & Assert
        processor.processModule(root, "moduleB");
        // Should not throw
    }

    @Test
    void getProjectLayoutReturnsProperLayout(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File root = tempDir.toFile();
        // No build files, so should return DefaultProjectLayout
        TestProcessor processor = new TestProcessor();

        // Act
        ProjectLayout layout = processor.getProjectLayout(root);

        // Assert
        assertTrue(layout instanceof DefaultProjectLayout);
        assertEquals(root, layout.getProjectDir());
    }
}
