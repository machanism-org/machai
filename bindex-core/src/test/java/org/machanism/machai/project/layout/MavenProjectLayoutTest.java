package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MavenProjectLayoutTest {
    private File tempDir;
    private MavenProjectLayout layout;
    private File pomFile;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("maven-project-layout-test").toFile();
        layout = new MavenProjectLayout();
        layout.projectDir(tempDir);
        pomFile = new File(tempDir, "pom.xml");
    }

    @AfterEach
    void tearDown() {
        for (File file : tempDir.listFiles()) file.delete();
        tempDir.delete();
    }

    @Test
    void isMavenProject_shouldReturnTrueIfPomPresent() throws Exception {
        assertFalse(MavenProjectLayout.isMavenProject(tempDir));
        Files.write(pomFile.toPath(), "<project></project>".getBytes());
        assertTrue(MavenProjectLayout.isMavenProject(tempDir));
    }

    @Test
    void getModules_shouldReturnNullIfPomNotMultiModule() throws Exception {
        String pomContent = "<project><modelVersion>4.0.0</modelVersion><packaging>jar</packaging></project>";
        Files.write(pomFile.toPath(), pomContent.getBytes());
        layout.model(null); // ensure fresh
        List<String> modules = layout.getModules();
        assertNull(modules);
    }

    @Test
    void getModules_shouldReturnModulesIfMultiModulePom() throws Exception {
        String pomContent = "<project><modelVersion>4.0.0</modelVersion><packaging>pom</packaging><modules><module>mod1</module><module>mod2</module></modules></project>";
        Files.write(pomFile.toPath(), pomContent.getBytes());
        layout.model(null); // ensure fresh
        List<String> modules = layout.getModules();
        assertNotNull(modules);
        assertTrue(modules.contains("mod1"));
        assertTrue(modules.contains("mod2"));
    }

    @Test
    @Disabled("Need to fix.")
    void getSources_shouldReturnMavenSourceDirectories() throws Exception {
        String pomContent = "<project><modelVersion>4.0.0</modelVersion><build><sourceDirectory>src/main/java</sourceDirectory></build></project>";
        Files.write(pomFile.toPath(), pomContent.getBytes());
        layout.model(null);
        List<String> sources = layout.getSources();
        assertTrue(sources.contains("src/main/java"));
    }

    @Test
    void getDocuments_shouldReturnMavenDocsDirectory() throws Exception {
        String pomContent = "<project><modelVersion>4.0.0</modelVersion></project>";
        Files.write(pomFile.toPath(), pomContent.getBytes());
        List<String> docs = layout.getDocuments();
        assertTrue(docs.contains("src/site"));
    }

    @Test
    @Disabled("Need to fix.")
    void getTests_shouldReturnMavenTestDirectories() throws Exception {
        String pomContent = "<project><modelVersion>4.0.0</modelVersion><build><testSourceDirectory>src/test/java</testSourceDirectory></build></project>";
        Files.write(pomFile.toPath(), pomContent.getBytes());
        layout.model(null);
        List<String> tests = layout.getTests();
        assertTrue(tests.contains("src/test/java"));
    }
}
