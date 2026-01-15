package org.machanism.machai.project.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class PythonProjectLayoutTest {

    private PythonProjectLayout layout;
    private File projectDir;

    @BeforeEach
    void setUp() {
        layout = new PythonProjectLayout();
        projectDir = new File("src/test/resources/mockPythonProject");
        layout.projectDir(projectDir);
    }

    @Test
    @Disabled
    void testIsPythonProject() {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        boolean result = PythonProjectLayout.isPythonProject(projectDir);

        // Assert
        assertTrue(result);
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