package org.machanism.machai.project.layout;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MavenProjectLayoutTest {

    private MavenProjectLayout layout;
    private File projectDir;

    @BeforeEach
    void setUp() {
        layout = new MavenProjectLayout();
        projectDir = new File("src/test/resources/mockMavenProject");
        layout.projectDir(projectDir);
    }

    @Test
    void testIsMavenProject() {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        boolean result = MavenProjectLayout.isMavenProject(projectDir);

        // Assert
        assertTrue(result);
    }

    @Test
    @Disabled
    void testGetModules() {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNotNull(modules);
        assertTrue(modules.contains("moduleX"));
    }

    @Test
    @Disabled
    void testGetSources() {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        List<String> sources = layout.getSources();

        // Assert
        assertNotNull(sources);
        assertTrue(sources.contains("src/main/java"));
    }

    @Test
    void testGetDocuments() {
        // Act
        List<String> documents = layout.getDocuments();

        // Assert
        assertNotNull(documents);
        assertTrue(documents.contains("src/site"));
    }

    @Test
    @Disabled
    void testGetTests() {
        // Arrange
        layout.projectDir(projectDir);

        // Act
        List<String> tests = layout.getTests();

        // Assert
        assertNotNull(tests);
        assertTrue(tests.contains("src/test/java"));
    }

    @Test
    void testModelInitialization() {
        // Act
        Model model = layout.getModel();

        // Assert
        assertNotNull(model);
        assertEquals("mock-project", model.getArtifactId());
    }
}