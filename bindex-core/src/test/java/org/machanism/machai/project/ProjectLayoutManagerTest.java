package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

/**
 * Unit tests for {@link ProjectLayoutManager}.
 * <p>
 * Tests detecting various project layouts (Maven, JavaScript, Python, Default) and exception handling.
 * <p>
 * Usage Example:
 * <pre>{@code
 *     ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(new File("src/test/resources/sample-maven-project"));
 *     assertTrue(layout instanceof MavenProjectLayout);
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
@DisplayName("ProjectLayoutManager Tests")
public class ProjectLayoutManagerTest {

    /**
     * Verifies detection of Maven project layout.
     * @throws Exception if IO error occurs
     */
    @Test
    @DisplayName("Detect Maven Project Layout")
    @Disabled("Need to fix.")
    void testDetectMavenProjectLayout() throws Exception {
        File mavenDir = new File("src/test/resources/sample-maven-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(mavenDir);
        assertTrue(layout instanceof MavenProjectLayout);
    }

    /**
     * Verifies detection of JavaScript (Node.js/package.json) project layout.
     * @throws Exception if IO error occurs
     */
    @Test
    @DisplayName("Detect JavaScript Project Layout")
    @Disabled("Need to fix.")
    void testDetectJScriptProjectLayout() throws Exception {
        File jsDir = new File("src/test/resources/sample-js-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(jsDir);
        assertTrue(layout instanceof JScriptProjectLayout);
    }

    /**
     * Verifies detection of Python project layout.
     * @throws Exception if IO error occurs
     */
    @Test
    @DisplayName("Detect Python Project Layout")
    @Disabled("Need to fix.")
    void testDetectPythonProjectLayout() throws Exception {
        File pyDir = new File("src/test/resources/sample-python-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(pyDir);
        assertTrue(layout instanceof PythonProjectLayout);
    }

    /**
     * Verifies detection of the default project layout.
     * @throws Exception if IO error occurs
     */
    @Test
    @DisplayName("Detect Default Project Layout")
    @Disabled("Need to fix.")
    void testDetectDefaultProjectLayout() throws Exception {
        File defaultDir = new File("src/test/resources/sample-default-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(defaultDir);
        assertTrue(layout instanceof DefaultProjectLayout);
    }

    /**
     * Verifies FileNotFoundException is thrown when the directory is missing.
     */
    @Test
    @DisplayName("Throws FileNotFoundException for missing directory")
    void testDetectFileNotFound() {
        File missingDir = new File("src/test/resources/missing-project");
        assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(missingDir));
    }
}
