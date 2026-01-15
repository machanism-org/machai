package org.machanism.machai.project.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutTest {

    private static final String MOCK_PROJECT_DIR = "src/test/resources/mockProjectDir";
    private ProjectLayout layout;
    private File projectDir;

    @BeforeEach
    void setUp() {
        layout = new DefaultProjectLayout();
        projectDir = new File(MOCK_PROJECT_DIR);
        layout.projectDir(projectDir);
    }

    @Test
    void testProjectDirSetter() {
        // Act
        layout.projectDir(new File("newPath"));

        // Assert
        assertEquals("newPath", layout.getProjectDir().getPath());
    }

    @Test
    void testGetRelatedPath() {
        // Arrange
        File file = new File(MOCK_PROJECT_DIR + "/src/main/java");

        // Act
        String relativePath = ProjectLayout.getRelatedPath(projectDir, file);

        // Assert
        assertNotNull(relativePath);
        assertEquals("src/main/java", relativePath);
    }
}