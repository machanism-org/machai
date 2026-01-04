package org.machanism.machai.project.layout;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
    void getModules_shouldReturnDirectoryNamesIgnoringExcluded() throws IOException {
        File module1 = new File(tempDir, "module1");
        File module2 = new File(tempDir, "module2");
        File excluded = new File(tempDir, "node_modules");
        module1.mkdir();
        module2.mkdir();
        excluded.mkdir();
        List<String> modules = layout.getModules();
        assertTrue(modules.contains("module1"));
        assertTrue(modules.contains("module2"));
        assertFalse(modules.contains("node_modules"));
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
