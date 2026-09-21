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
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Focused tests for shared utilities and metadata-driven layout branches. */
class LayoutComprehensiveAdditionalTest {

    @TempDir
    Path tempDir;

    @Test
    void relativePathUtilities_shouldHandleRootChildOutsideAndOptionalDot() throws Exception {
        // Arrange
        File root = tempDir.toFile();
        File child = Files.createDirectories(tempDir.resolve("src/main")).toFile();
        File outside = Files.createTempDirectory("layout-outside").toFile();

        // Act
        String childPath = ProjectLayout.getRelativePath(root, child);
        String dottedPath = ProjectLayout.getRelativePath(root, child, true);
        String rootPath = ProjectLayout.getRelativePath(root, root);
        String outsidePath = ProjectLayout.getRelativePath(root, outside);

        // Assert
        assertEquals("src/main", childPath);
        assertEquals("./src/main", dottedPath);
        assertEquals(".", rootPath);
        assertNull(outsidePath);
        outside.delete();
    }

    @Test
    void exclusionMatching_shouldSupportFileNameGlobInvalidPatternAndNull() {
        // Arrange
        DefaultProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("*.tmp", "[invalid"));

        // Act
        boolean globMatch = layout.isExcludedPath(new File("notes.tmp"));
        boolean invalidExactMatch = layout.isExcludedPath(new File("[invalid"));
        boolean ordinary = layout.isExcludedPath(new File("notes.txt"));
        boolean nullFile = layout.isExcludedPath(null);

        // Assert
        assertTrue(globMatch);
        assertTrue(invalidExactMatch);
        assertFalse(ordinary);
        assertFalse(nullFile);
    }

    @Test
    void defaultLayout_shouldDiscoverEligibleImmediateModulesAndCacheResult() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("module"));
        Files.createDirectories(tempDir.resolve("build"));
        DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
        layout.getExcludeDirs().add("build");

        // Act
        List<String> first = layout.getModules();
        List<String> second = layout.getModules();

        // Assert
        assertEquals(Collections.singletonList("module"), first);
        assertSame(first, second);
    }

    @Test
    void mavenLayout_shouldExposeConfiguredBuildSourcesTestsAndIdentity() {
        // Arrange
        Model model = new Model();
        model.setArtifactId("artifact");
        model.setName("Readable name");
        Parent parent = new Parent();
        parent.setArtifactId("parent-artifact");
        model.setParent(parent);
        Build build = new Build();
        build.setSourceDirectory(tempDir.resolve("custom-src").toString());
        build.setTestSourceDirectory(tempDir.resolve("custom-test").toString());
        Resource resource = new Resource();
        resource.setDirectory("resources");
        build.setResources(Collections.singletonList(resource));
        Resource testResource = new Resource();
        testResource.setDirectory(tempDir.resolve("test-resources").toString());
        build.setTestResources(Collections.singletonList(testResource));
        model.setBuild(build);
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        Set<String> sources = layout.getSources();
        List<String> tests = layout.getTests();

        // Assert
        assertEquals("artifact", layout.getProjectId());
        assertEquals("Readable name", layout.getProjectName());
        assertEquals("parent-artifact", layout.getParentId());
        assertTrue(sources.contains("custom-src"));
        assertTrue(sources.contains("resources"));
        assertEquals(Arrays.asList("custom-test", "test-resources"), tests);
        assertEquals(Collections.singletonList("src/site"), layout.getDocuments());
    }

    @Test
    void mavenLayout_shouldReturnNoModulesForNonParentAndModulesForPomPackaging() {
        // Arrange
        Model child = new Model();
        child.setPackaging("jar");
        MavenProjectLayout childLayout = new MavenProjectLayout().model(child);
        Model parent = new Model();
        parent.setPackaging("pom");
        parent.setModules(Arrays.asList("one", "two"));
        MavenProjectLayout parentLayout = new MavenProjectLayout().model(parent);

        // Act
        List<String> childModules = childLayout.getModules();
        List<String> parentModules = parentLayout.getModules();

        // Assert
        assertNull(childModules);
        assertEquals(Arrays.asList("one", "two"), parentModules);
    }

    @Test
    void javascriptLayout_shouldDetectPackageAndRejectUnsetRoot() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("package.json"), "{\"name\":\"demo\",\"workspaces\":\"packages/*\"}".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act
        String projectId = layout.getProjectId();
        List<String> modules = layout.getModules();

        // Assert
        assertEquals("demo", projectId);
        assertNull(modules);
        assertThrows(IllegalStateException.class, () -> new JScriptProjectLayout().getModules());
    }

    @Test
    void pythonDetection_shouldRecognizePublicProjectsAndRejectPrivateMissingAndMalformedDescriptors() throws Exception {
        // Arrange
        Path descriptor = tempDir.resolve("pyproject.toml");
        Files.write(descriptor, "[project]\nname = \"demo\"\n".getBytes(StandardCharsets.UTF_8));
        assertTrue(PythonProjectLayout.isPythonProject(tempDir.toFile()));
        Files.write(descriptor, "[project]\nname = \"demo\"\nclassifiers = [\"Private :: Internal\"]\n".getBytes(StandardCharsets.UTF_8));

        // Act
        boolean privateProject = PythonProjectLayout.isPythonProject(tempDir.toFile());
        boolean missingProject = PythonProjectLayout.isPythonProject(tempDir.resolve("missing").toFile());
        Files.write(descriptor, "not valid = [".getBytes(StandardCharsets.UTF_8));
        boolean malformedProject = PythonProjectLayout.isPythonProject(tempDir.toFile());

        // Assert
        assertFalse(privateProject);
        assertFalse(missingProject);
        assertFalse(malformedProject);
        assertEquals(Collections.emptyList(), new PythonProjectLayout().getSources());
        assertNotNull(new PythonProjectLayout().getDocuments());
    }
}
