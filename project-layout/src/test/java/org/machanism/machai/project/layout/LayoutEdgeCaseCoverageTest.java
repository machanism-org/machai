package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional boundary and error-path tests for the layout implementations. */
class LayoutEdgeCaseCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void relativePathShouldReturnNullForFileOutsideProjectAndRemoveTrailingSeparator() throws Exception {
        // Arrange
        Path childDirectory = Files.createDirectories(tempDir.resolve("child"));
        File directory = tempDir.toFile();
        File child = childDirectory.toFile();
        File outside = Files.createTempFile("outside-layout-", ".txt").toFile();
        outside.deleteOnExit();

        // Act
        String childPath = ProjectLayout.getRelativePath(directory, child);
        String outsidePath = ProjectLayout.getRelativePath(directory, outside);

        // Assert
        assertEquals("child", childPath);
        assertNull(outsidePath);
    }

    @Test
    void listDirectoriesShouldSkipExcludedDirectoriesButTraverseRegularDirectories() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve(".git/hidden"));
        Files.createDirectories(tempDir.resolve("visible/nested"));
        Files.createDirectories(tempDir.resolve("build/output"));

        // Act
        List<File> directories = ProjectLayout.listDirectories(tempDir.toFile());

        // Assert
        assertTrue(directories.stream().anyMatch(file -> file.getName().equals("visible")));
        assertTrue(directories.stream().anyMatch(file -> file.getName().equals("nested")));
        assertFalse(directories.stream().anyMatch(file -> file.getName().equals(".git")));
        assertFalse(ProjectLayout.isExcludedPath("visible"));
        assertTrue(ProjectLayout.isExcludedPath(".git"));
        String[] exclusions = ProjectLayout.getExcludeDirs();
        exclusions[0] = "changed";
        assertTrue(ProjectLayout.isExcludedPath("node_modules"));
    }

    @Test
    void gradleLayoutShouldExposeConventionsAndDetectionResults() throws Exception {
        // Arrange
        GradleProjectLayout layout = new GradleProjectLayout().projectDir(tempDir.toFile());

        // Act
        boolean absent = GradleProjectLayout.isGradleProject(tempDir.toFile());
        Files.write(tempDir.resolve("build.gradle"), "plugins {}".getBytes(StandardCharsets.UTF_8));
        boolean present = GradleProjectLayout.isGradleProject(tempDir.toFile());

        // Assert
        assertFalse(absent);
        assertTrue(present);
        assertEquals(Collections.singletonList("src/main"), layout.getSources());
        assertEquals(Collections.singletonList("src/site"), layout.getDocuments());
        assertEquals(Collections.singletonList("src/test"), layout.getTests());
        assertEquals(Collections.emptyList(), layout.getModules());
        assertEquals("", layout.getProjectId());
        assertEquals("", layout.getProjectName());
    }

    @Test
    void javascriptLayoutShouldFindWorkspacePackagesAndRejectMalformedJson() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("packages/one"));
        Files.write(tempDir.resolve("packages/one/package.json"), "{\"name\":\"one\"}".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("package.json"),
                "{\"name\":\"root\",\"workspaces\":[\"./packages/*\",\"packages/one\"]}"
                        .getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> modules = layout.getModules();
        String projectId = layout.getProjectId();
        Files.write(tempDir.resolve("package.json"), "not-json".getBytes(StandardCharsets.UTF_8));

        // Assert
        assertEquals(Collections.singletonList("packages/one"), modules);
        assertEquals("root", projectId);
        assertThrows(IllegalArgumentException.class, layout::getModules);
    }

    @Test
    void mavenLayoutShouldApplyDefaultsAndRetainRelativeResources() {
        // Arrange
        Model model = new Model();
        model.setArtifactId("defaults");
        Build build = new Build();
        Resource resources = new Resource();
        resources.setDirectory("src/main/resources");
        build.setResources(Collections.singletonList(resources));
        model.setBuild(build);
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        java.util.Set<String> sources = layout.getSources();
        List<String> tests = layout.getTests();

        // Assert
        assertTrue(sources.contains("src/main/java"));
        assertTrue(sources.contains("src/main/resources"));
        assertEquals(Collections.singletonList("src/test/java"), tests);
        assertNotNull(model.getBuild().getTestSourceDirectory());
    }

    @Test
    void pomReaderShouldWrapMissingPomAndPreserveNullPropertyInput() throws Exception {
        // Arrange
        PomReader reader = new PomReader();
        File missing = tempDir.resolve("missing-pom.xml").toFile();

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> reader.getProjectModel(missing));
        assertTrue(reader.getPomProperties().isEmpty());
    }
}
