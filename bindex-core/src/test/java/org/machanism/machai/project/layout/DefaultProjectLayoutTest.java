package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultProjectLayoutTest {
    private File tempDir;
    private DefaultProjectLayout layout;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "default_project_layout_test");
        tempDir.mkdir();
        layout = new DefaultProjectLayout();
        layout.projectDir(tempDir);
    }

    @AfterEach
    void tearDown() {
        for (File file : tempDir.listFiles()) file.delete();
        tempDir.delete();
    }

    @Test
    void getModules_shouldReturnEmptyListIfNoDirectories() throws IOException {
        List<String> modules = layout.getModules();
        assertTrue(modules.isEmpty());
    }

    @Test
    void getSources_shouldReturnNull() {
        assertNull(layout.getSources());
    }

    @Test
    void getDocuments_shouldReturnNull() {
        assertNull(layout.getDocuments());
    }

    @Test
    void getTests_shouldReturnNull() {
        assertNull(layout.getTests());
    }
}
