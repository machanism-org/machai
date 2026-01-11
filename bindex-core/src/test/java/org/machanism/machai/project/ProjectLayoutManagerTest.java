package org.machanism.machai.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutManagerTest {

    @Test
    void detectsMavenProject(@TempDir File tempDir) throws Exception {
        File pom = new File(tempDir, "pom.xml");
        Files.writeString(pom.toPath(), "<project><modelVersion>4.0.0</modelVersion><packaging>jar</packaging></project>");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);
        assertTrue(layout instanceof MavenProjectLayout);
    }

    @Test
    void detectsNodeProject(@TempDir File tempDir) throws Exception {
        File pkg = new File(tempDir, "package.json");
        Files.writeString(pkg.toPath(), "{\"name\":\"sample\"}");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);
        assertTrue(layout instanceof JScriptProjectLayout);
    }

    @Test
    void detectsPythonProject(@TempDir File tempDir) throws Exception {
        File pyproject = new File(tempDir, "pyproject.toml");
        Files.writeString(pyproject.toPath(), "project.name='DemoProject'\n");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);
        assertTrue(layout instanceof PythonProjectLayout);
    }

    @Test
    void defaultLayoutForUnknownProject(@TempDir File tempDir) throws Exception {
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);
        assertTrue(layout instanceof DefaultProjectLayout);
    }

    @Test
    void throwsIfDirectoryDoesNotExist() {
        File notExist = new File("/this/path/does/not/exist");
        assertThrows(IOException.class, () -> ProjectLayoutManager.detectProjectLayout(notExist));
    }

    @Test
    void setsProjectDirOnDetectedLayout(@TempDir File tempDir) throws Exception {
        File pom = new File(tempDir, "pom.xml");
        Files.writeString(pom.toPath(), "<project/>\n");
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir);
        assertEquals(tempDir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
    }
}
