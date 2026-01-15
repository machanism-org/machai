package org.machanism.machai.project.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DefaultProjectLayoutTest {

    private DefaultProjectLayout layout;
    private File projectDir;

    @BeforeEach
    void setUp() {
        layout = new DefaultProjectLayout();
        projectDir = new File("src/test/resources/mockProject");
        layout.projectDir(projectDir);
    }

    @Test
    @Disabled
    void testGetModules() throws IOException {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNotNull(modules);
        assertFalse(modules.isEmpty());
        assertTrue(modules.contains("moduleA"));
    }

    @Test
    void testNullSources() {
        // Act & Assert
        assertNull(layout.getSources());
    }

    @Test
    void testNullDocuments() {
        // Act & Assert
        assertNull(layout.getDocuments());
    }

    @Test
    void testNullTests() {
        // Act & Assert
        assertNull(layout.getTests());
    }
}