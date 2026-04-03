package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoverageGapAdditionalTest {

    @TempDir
    Path tempDir;

    @Test
    void pythonProject_shouldReturnFalseWhenPyprojectCannotBeRead() throws Exception {
        Path projectDir = tempDir.resolve("python-io-failure");
        Files.createDirectories(projectDir);
        Files.createDirectory(projectDir.resolve("pyproject.toml"));

        boolean result = PythonProjectLayout.isPythonProject(projectDir.toFile());

        assertFalse(result);
    }

    @Test
    void gradleProject_getProjectIdAndName_shouldReturnEmptyWhenProjectDirectoryIsMissing() {
        GragleProjectLayout layout = new GragleProjectLayout();

        assertEquals("", layout.getProjectId());
        assertEquals("", layout.getProjectName());
    }

    @Test
    void jscriptModules_shouldIgnoreWorkspaceDirectoryWithoutPackageJson() throws Exception {
        Path projectDir = tempDir.resolve("js-workspaces");
        Files.createDirectories(projectDir);
        Files.write(projectDir.resolve("package.json"),
                ("{\"name\":\"root\",\"workspaces\":[\"packages/*\"]}").getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(projectDir.resolve("packages/app"));

        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(projectDir.toFile());

        List<String> modules = layout.getModules();

        assertTrue(modules.isEmpty());
    }

    @Test
    void pomReader_shouldWrapReadFailureWhenPomFileIsDirectory() throws Exception {
        Path pomDir = tempDir.resolve("pom-as-directory");
        Files.createDirectories(pomDir);

        PomReader reader = new PomReader();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> invokeGetProjectModel(reader, pomDir)); // Sonar fix java:S5778

        assertTrue(exception.getMessage().contains("POM file:"));
        assertNotNull(exception.getCause());
    }

    private void invokeGetProjectModel(PomReader reader, Path pomDir) {
        reader.getProjectModel(pomDir.toFile());
    }

    @Test
    void projectLayout_relativePathShouldPrefixOutsidePathWhenDotPrefixRequested() throws Exception {
        Path baseDir = tempDir.resolve("base");
        Path outsideDir = tempDir.resolve("outside");
        Files.createDirectories(baseDir);
        Files.createDirectories(outsideDir);
        Path outsideFile = outsideDir.resolve("file.txt");
        Files.write(outsideFile, new byte[0]);

        String relative = ProjectLayout.getRelativePath(baseDir.toFile(), outsideFile.toFile(), true);

        assertTrue(relative.startsWith("./"));
        assertTrue(relative.replace("\\", "/").endsWith("/outside/file.txt"));
    }

    @Test
    void mavenProjectLayout_getSourcesShouldKeepRelativeResourceDirectoryUnchanged() throws IOException {
        Path projectDir = tempDir.resolve("maven-relative-resource");
        Files.createDirectories(projectDir);
        Files.createDirectories(projectDir.resolve("src/main/java"));

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setArtifactId("artifact");
        model.setVersion("1");

        Build build = new Build();
        build.setSourceDirectory(projectDir.resolve("src/main/java").toString());
        Resource resource = new Resource();
        resource.setDirectory("src/main/resources");
        build.addResource(resource);
        model.setBuild(build);

        MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir.toFile()).model(model);

        Set<String> sources = layout.getSources();

        assertTrue(sources.contains("src/main/java"));
        assertTrue(sources.contains("src/main/resources"));
    }
}
