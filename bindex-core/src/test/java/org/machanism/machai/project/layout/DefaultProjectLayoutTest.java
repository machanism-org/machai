package org.machanism.machai.project.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultProjectLayoutTest {
    @Test
    void getModulesExcludesDefaultProjectLayouts(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File root = tempDir.toFile();
        File moduleDir = new File(root, "moduleA");
        assertTrue(moduleDir.mkdir());
        // mimic as non-default (create a pom.xml for Maven detection)
        new java.io.PrintWriter(new File(moduleDir, "pom.xml")).close();
        DefaultProjectLayout layout = new DefaultProjectLayout();
        layout.projectDir(root);

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNotNull(modules);
        assertTrue(modules.contains("moduleA"));
    }

    @Test
    void getModulesReturnsEmptyIfNoDirs(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        DefaultProjectLayout layout = new DefaultProjectLayout();
        layout.projectDir(tempDir.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertTrue(modules.isEmpty() || modules == null);
    }

    @Test
    void returnsNullForSourcesDocumentsAndTests() {
        DefaultProjectLayout layout = new DefaultProjectLayout();
        assertNull(layout.getSources());
        assertNull(layout.getDocuments());
        assertNull(layout.getTests());
    }
}
