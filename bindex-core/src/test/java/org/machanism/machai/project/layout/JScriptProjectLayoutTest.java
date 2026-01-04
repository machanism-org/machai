package org.machanism.machai.project.layout;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

class JScriptProjectLayoutTest {
    private File tempDir;
    private JScriptProjectLayout layout;
    private File packageJson;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("jscript-project-layout-test").toFile();
        layout = new JScriptProjectLayout();
        layout.projectDir(tempDir);
        packageJson = new File(tempDir, "package.json");
    }

    @AfterEach
    void tearDown() {
        for (File file : tempDir.listFiles()) file.delete();
        tempDir.delete();
    }

    @Test
    void isPackageJsonPresent_shouldReturnTrueIfPresent() throws IOException {
        assertFalse(JScriptProjectLayout.isPackageJsonPresent(tempDir));
        Files.write(packageJson.toPath(), "{}".getBytes());
        assertTrue(JScriptProjectLayout.isPackageJsonPresent(tempDir));
    }

    @Test
    @Disabled("Need to fix.")
    void getModules_shouldReturnWorkspacesModules() throws IOException {
        String json = "{\"workspaces\": [\"apps/*\", \"libs/*\"]}";
        Files.write(packageJson.toPath(), json.getBytes());
        File appsDir = new File(tempDir, "apps");
        File libsDir = new File(tempDir, "libs");
        appsDir.mkdir();
        libsDir.mkdir();
        new File(appsDir, "fakemodule/package.json").getParentFile().mkdirs();
        Files.write(new File(appsDir, "fakemodule/package.json").toPath(), "{}".getBytes());
        new File(libsDir, "libmodule/package.json").getParentFile().mkdirs();
        Files.write(new File(libsDir, "libmodule/package.json").toPath(), "{}".getBytes());
        List<String> modules = layout.getModules();
        assertTrue(modules.contains("apps/fakemodule"));
        assertTrue(modules.contains("libs/libmodule"));
    }

    @Test
    void getModules_shouldReturnNullIfNoWorkspaces() throws IOException {
        Files.write(packageJson.toPath(), "{}".getBytes());
        List<String> modules = layout.getModules();
        assertNull(modules);
    }

    @Test
    void getSources_shouldReturnNull() {
        assertNull(layout.getSources());
    }

    @Test
    void getDocuments_shouldReturnNull() {
        assertNull(layout.getDocuments());
    }

    @Test
    void getTests_shouldReturnNull() {
        assertNull(layout.getTests());
    }
}
