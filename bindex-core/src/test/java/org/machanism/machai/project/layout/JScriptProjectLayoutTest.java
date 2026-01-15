package org.machanism.machai.project.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JScriptProjectLayoutTest {

    private JScriptProjectLayout layout;
    private File projectDir;

    @BeforeEach
    void setUp() {
        layout = new JScriptProjectLayout();
        projectDir = new File("src/test/resources/mockJsProject");
        layout.projectDir(projectDir);
    }

    @Test
    void testPackageJsonPresence() {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        boolean result = JScriptProjectLayout.isPackageJsonPresent(projectDir);

        // Assert
        assertTrue(result);
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
        assertTrue(modules.contains("workspaceA"));
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