package org.machanism.machai.project.layout;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutTest {

    static class DummyProjectLayout extends ProjectLayout {
        @Override
        public List<String> getSources() { return Collections.singletonList("src"); }
        @Override
        public List<String> getDocuments() { return Collections.singletonList("docs"); }
        @Override
        public List<String> getTests() { return Collections.singletonList("tests"); }
        @Override
        public List<String> getModules() { return Collections.singletonList("mod"); }
    }

    @Test
    @DisplayName("Sets and gets project dir")
    void projectDirSetsAndGetsCorrectly() {
        File dir = new File("/tmp/foo");
        ProjectLayout layout = new DummyProjectLayout().projectDir(dir);
        assertEquals(dir, layout.getProjectDir());
    }

    @Test
    @DisplayName("getSources/getDocuments/getTests returns expected values")
    void sourcesDocumentsTestsReturn() {
        ProjectLayout layout = new DummyProjectLayout();
        assertEquals(Collections.singletonList("src"), layout.getSources());
        assertEquals(Collections.singletonList("docs"), layout.getDocuments());
        assertEquals(Collections.singletonList("tests"), layout.getTests());
    }

    @Test
    @DisplayName("getModules returns expected value")
    void getModulesReturnsCorrectly() throws IOException {
        ProjectLayout layout = new DummyProjectLayout();
        assertEquals(Collections.singletonList("mod"), layout.getModules());
    }

    @Test
    @DisplayName("getRelatedPath (instance method) produces correct relative path")
    @Disabled("Need to fix.")
    void instanceRelatedPathCorrect() {
        File dir = new File("/some/test/dir");
        File file = new File("/some/test/dir/src/foo.java");
        ProjectLayout layout = new DummyProjectLayout().projectDir(dir);
        String rel = layout.getRelatedPath(dir.getAbsolutePath(), file);
        assertEquals("src/foo.java", rel);
    }

    @Test
    @DisplayName("getRelatedPath (static method) produces correct relative path")
    void staticRelatedPathCorrect() {
        File dir = new File("/some/base");
        File file = new File("/some/base/a/b.txt");
        String rel = ProjectLayout.getRelatedPath(dir, file);
        assertEquals("a/b.txt", rel);
    }

    @Test
    @DisplayName("getRelatedPath (static) with dot prepending")
    void staticRelatedPathDotPrepended() {
        File dir = new File("/dir");
        File file = new File("/dir/file.java");
        String rel = ProjectLayout.getRelatedPath(dir, file, true);
        assertEquals("./file.java", rel);
    }

    @Test
    @DisplayName("getRelatedPath for same path")
    void relatedPathSameFile() {
        File file = new File("/fpath");
        assertEquals(".", ProjectLayout.getRelatedPath(file, file));
    }

    @Test
    @DisplayName("EXCLUDE_DIRS constant contains expected entries")
    void excludeDirsConstantIntegrity() {
        String[] expect = { "node_modules", ".git", ".nx", ".svn", ".machai", "target", "build", ".venv", "__", ".pytest_cache", ".idea", ".egg-info", ".classpath", ".settings", "logs", ".settings", ".project", ".m2" };
        assertArrayEquals(expect, ProjectLayout.EXCLUDE_DIRS);
    }
}
