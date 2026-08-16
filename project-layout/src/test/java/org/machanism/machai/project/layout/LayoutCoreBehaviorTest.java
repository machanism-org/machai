package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Focused unit tests for core, non-integration layout behavior. */
class LayoutCoreBehaviorTest {

    @TempDir
    Path tempDir;

    @Test
    void defaultLayoutShouldDiscoverOnlyImmediateNonExcludedDirectoriesAndCacheResult() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("module-a/nested"));
        Files.createDirectories(tempDir.resolve("build/ignored"));
        Files.write(tempDir.resolve("not-a-module.txt"), new byte[] { 1 });
        DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> first = layout.getModules();
        Files.createDirectories(tempDir.resolve("module-b"));
        List<String> second = layout.getModules();

        // Assert
        assertEquals(Collections.singletonList("module-a"), first);
        assertSame(first, second);
        assertTrue(layout.getSources().isEmpty());
        assertTrue(layout.getDocuments().isEmpty());
        assertTrue(layout.getTests().isEmpty());
    }

    @Test
    void projectLayoutShouldComputeRelativePathsAndExposeDefaults() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
        File child = tempDir.resolve("src/Main.java").toFile();

        // Act
        String plain = ProjectLayout.getRelativePath(tempDir.toFile(), child);
        String dotted = ProjectLayout.getRelativePath(tempDir.toFile(), child, true);
        String same = ProjectLayout.getRelativePath(tempDir.toFile(), tempDir.toFile());

        // Assert
        assertEquals("src/Main.java", plain);
        assertEquals("./src/Main.java", dotted);
        assertEquals(".", same);
        assertEquals(tempDir.toFile(), layout.getProjectDir());
        assertNull(new ProjectLayout() {
            @Override
            public java.util.Collection<String> getSources() { return Collections.emptyList(); }
            @Override
            public java.util.Collection<String> getDocuments() { return Collections.emptyList(); }
            @Override
            public java.util.Collection<String> getTests() { return Collections.emptyList(); }
        }.getModules());
        assertNull(layout.getProjectName());
        assertNull(layout.getProjectId());
        assertNull(layout.getParentId());
        assertEquals("Default", layout.getProjectLayoutType());
    }

    @Test
    void projectLayoutShouldListFilesRecursivelyAndHandleInvalidDirectories() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("a/b"));
        Files.write(tempDir.resolve("a/one.txt"), "1".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("a/b/two.txt"), "2".getBytes(StandardCharsets.UTF_8));

        // Act
        List<File> files = ProjectLayout.listFiles(tempDir.toFile());

        // Assert
        assertEquals(2, files.size());
        assertTrue(files.stream().anyMatch(file -> file.getName().equals("one.txt")));
        assertTrue(files.stream().anyMatch(file -> file.getName().equals("two.txt")));
        assertTrue(ProjectLayout.listFiles(null).isEmpty());
        assertTrue(ProjectLayout.listDirectories(null).isEmpty());
    }

    @Test
    void javaScriptLayoutShouldHandleMissingWorkspaceAndUnsetProjectDirectory() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("package.json"), "{\"name\":\"app\"}".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout configured = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> modules = configured.getModules();
        List<String> sources = configured.getSources();
        JScriptProjectLayout unset = new JScriptProjectLayout();

        // Assert
        assertTrue(modules.isEmpty());
        assertTrue(sources.isEmpty());
        assertTrue(configured.getDocuments().isEmpty());
        assertTrue(configured.getTests().isEmpty());
        assertThrows(IllegalStateException.class, unset::getModules);
    }

    @Test
    void javaScriptProjectDetectionShouldReflectPackageJsonPresence() throws Exception {
        // Arrange
        File directory = tempDir.toFile();

        // Act
        boolean absent = JScriptProjectLayout.isPackageJsonPresent(directory);
        Files.createFile(tempDir.resolve("package.json"));
        boolean present = JScriptProjectLayout.isPackageJsonPresent(directory);

        // Assert
        assertFalse(absent);
        assertTrue(present);
    }

    @Test
    void pythonProjectDetectionShouldAcceptNamedPublicProjectAndRejectPrivateOrIncompleteProject() throws Exception {
        // Arrange
        Path pyproject = tempDir.resolve("pyproject.toml");
        Files.write(pyproject, "[project]\nname = 'sample'\nclassifiers = ['Topic :: Utilities']\n".getBytes(StandardCharsets.UTF_8));

        // Act
        boolean publicProject = PythonProjectLayout.isPythonProject(tempDir.toFile());
        Files.write(pyproject, "[project]\nname = 'sample'\nclassifiers = ['Private :: Internal']\n".getBytes(StandardCharsets.UTF_8));
        boolean privateProject = PythonProjectLayout.isPythonProject(tempDir.toFile());
        Files.write(pyproject, "[project]\nclassifiers = []\n".getBytes(StandardCharsets.UTF_8));
        boolean unnamedProject = PythonProjectLayout.isPythonProject(tempDir.toFile());

        // Assert
        assertTrue(publicProject);
        assertFalse(privateProject);
        assertFalse(unnamedProject);
        PythonProjectLayout layout = new PythonProjectLayout();
        assertTrue(layout.getSources().isEmpty());
        assertTrue(layout.getDocuments().isEmpty());
        assertTrue(layout.getTests().isEmpty());
    }

    @Test
    void pythonProjectDetectionShouldReturnFalseWhenDescriptorIsAbsent() {
        // Arrange
        File directory = tempDir.toFile();

        // Act
        boolean detected = PythonProjectLayout.isPythonProject(directory);

        // Assert
        assertFalse(detected);
    }

    @Test
    void mavenLayoutShouldExposeModelMetadataAndConfiguredDirectories() {
        // Arrange
        Model model = new Model();
        model.setArtifactId("child");
        model.setName("Child project");
        model.setPackaging("pom");
        Parent parent = new Parent();
        parent.setArtifactId("parent");
        model.setParent(parent);
        model.setModules(Arrays.asList("one", "two"));
        Build build = new Build();
        build.setSourceDirectory(tempDir.resolve("custom/main").toString());
        build.setTestSourceDirectory(tempDir.resolve("custom/test").toString());
        Resource resource = new Resource();
        resource.setDirectory(tempDir.resolve("resources").toString());
        build.addResource(resource);
        Resource testResource = new Resource();
        testResource.setDirectory(tempDir.resolve("test-resources").toString());
        build.addTestResource(testResource);
        model.setBuild(build);
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(Arrays.asList("one", "two"), modules);
        assertEquals("child", layout.getProjectId());
        assertEquals("Child project", layout.getProjectName());
        assertEquals("parent", layout.getParentId());
        assertTrue(layout.getSources().contains("custom/main"));
        assertTrue(layout.getTests().contains("custom/test"));
        assertEquals(Collections.singletonList("src/site"), layout.getDocuments());
    }

    @Test
    void mavenLayoutShouldReturnNullModulesForNonParentModelAndDetectPom() throws Exception {
        // Arrange
        Model model = new Model();
        model.setPackaging("jar");
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        List<String> modules = layout.getModules();
        boolean absent = MavenProjectLayout.isMavenProject(tempDir.toFile());
        Files.createFile(tempDir.resolve("pom.xml"));
        boolean present = MavenProjectLayout.isMavenProject(tempDir.toFile());

        // Assert
        assertNull(modules);
        assertFalse(absent);
        assertTrue(present);
        assertNull(layout.getParentId());
    }

    @Test
    void pomReaderShouldReplacePropertiesInSubsequentPomAndReuseLicense() throws Exception {
        // Arrange
        Path pomDir = Files.createTempDirectory("pom-reader-test-");
        pomDir.toFile().deleteOnExit();
        Path first = pomDir.resolve("first.xml");
        first.toFile().deleteOnExit();
        Files.write(first, ("<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId>"
                + "<artifactId>a</artifactId><version>1.0</version><properties><shared>value</shared></properties>"
                + "<licenses><license><name>Apache</name></license></licenses></project>").getBytes(StandardCharsets.UTF_8));
        Path second = pomDir.resolve("second.xml");
        second.toFile().deleteOnExit();
        Files.write(second, ("<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId>"
                + "<artifactId>b</artifactId><version>${shared}</version></project>").getBytes(StandardCharsets.UTF_8));
        PomReader reader = new PomReader();

        // Act
        Model firstModel = reader.getProjectModel(first.toFile());
        Model secondModel = reader.getProjectModel(second.toFile());
        Map<String, String> properties = reader.getPomProperties();

        // Assert
        assertEquals("1.0", firstModel.getVersion());
        assertEquals("value", secondModel.getVersion());
        assertEquals("value", properties.get("shared"));
        assertEquals(1, secondModel.getLicenses().size());
        assertTrue(PomReader.printModel(secondModel).contains("<artifactId>b</artifactId>"));
    }
}
