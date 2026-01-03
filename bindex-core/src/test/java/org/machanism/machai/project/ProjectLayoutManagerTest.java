package org.machanism.machai.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutManagerTest {

    @Test
    @Disabled("Need to fix.")
    void detectsMavenProjectLayout() throws Exception {
        File mavenDir = new File("src/test/resources/sample-maven-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(mavenDir);
        assertNotNull(layout);
        assertTrue(layout instanceof MavenProjectLayout);
    }

    @Test
    @Disabled("Need to fix.")
    void detectsJScriptProjectLayout() throws Exception {
        File jsDir = new File("src/test/resources/sample-js-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(jsDir);
        assertNotNull(layout);
        assertTrue(layout instanceof JScriptProjectLayout);
    }

    @Test
    @Disabled("Need to fix.")
    void detectsPythonProjectLayout() throws Exception {
        File pythonDir = new File("src/test/resources/sample-python-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(pythonDir);
        assertNotNull(layout);
        assertTrue(layout instanceof PythonProjectLayout);
    }

    @Test
    @Disabled("Need to fix.")
    void detectsDefaultProjectLayout() throws Exception {
        File defaultDir = new File("src/test/resources/sample-default-project");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(defaultDir);
        assertNotNull(layout);
        assertTrue(layout instanceof DefaultProjectLayout);
    }

    @Test
    void throwsFileNotFoundExceptionForMissingDirectory() {
        File missingDir = new File("src/test/resources/does_not_exist");
        assertThrows(FileNotFoundException.class, () -> {
            ProjectLayoutManager.detectProjectLayout(missingDir);
        });
    }
}
