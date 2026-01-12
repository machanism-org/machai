package org.machanism.machai.project.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JScriptProjectLayoutTest {
    @Test
    void isPackageJsonPresentReturnsTrueIfFileExists(@TempDir java.nio.file.Path tempDir) {
        File dir = tempDir.toFile();
        File pkgJson = new File(dir, "package.json");
        assertDoesNotThrow(() -> new PrintWriter(pkgJson).close());
        assertTrue(JScriptProjectLayout.isPackageJsonPresent(dir));
    }

    @Test
    void isPackageJsonPresentReturnsFalseIfFileNotExists(@TempDir java.nio.file.Path tempDir) {
        File dir = tempDir.toFile();
        assertFalse(JScriptProjectLayout.isPackageJsonPresent(dir));
    }

    @Test
    void getSourcesDocumentsAndTestsReturnNull() {
        JScriptProjectLayout layout = new JScriptProjectLayout();
        assertNull(layout.getSources());
        assertNull(layout.getDocuments());
        assertNull(layout.getTests());
    }
}
