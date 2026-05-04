package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;

class CoverageGapFillTest {

    @Test
    void pythonProjectDetectionReturnsFalseForUnreadableToml() throws Exception {
        Path projectDir = Files.createTempDirectory("python-bad");
        Files.createDirectory(projectDir.resolve("pyproject.toml"));

        boolean result = PythonProjectLayout.isPythonProject(projectDir.toFile());

        assertFalse(result);
    }

    @Test
    void pomReaderReplacesPropertiesOnSecondReadUsingCollectedValues() throws Exception {
        PomReader reader = new PomReader();
        Path firstDir = Files.createTempDirectory("first-pom");
        Path secondDir = Files.createTempDirectory("second-pom");
        Path firstPom = writePom(firstDir.resolve("pom.xml"),
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                        + "<modelVersion>4.0.0</modelVersion><groupId>a</groupId><artifactId>first</artifactId><version>1</version>"
                        + "<properties><shared.name>Resolved Name</shared.name></properties></project>");
        Path secondPom = writePom(secondDir.resolve("pom.xml"),
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                        + "<modelVersion>4.0.0</modelVersion><groupId>a</groupId><artifactId>second</artifactId><version>1</version>"
                        + "<name>${shared.name}</name></project>");
        reader.getProjectModel(firstPom.toFile());

        Model model = reader.getProjectModel(secondPom.toFile());

        assertEquals("Resolved Name", model.getName());
    }

    @Test
    void staticRelativePathReturnsNullWhenFileIsOutsideBaseDirectory() {
        Path baseDir = tempDir("base");
        File outsideFile = tempDir("outside-root").resolve("outside.txt").toFile();

        String result = ProjectLayout.getRelativePath(baseDir.toFile(), outsideFile, false);

        assertNull(result);
    }

    @Test
    void findFilesAndDirectoriesIgnoreExcludedPrefixes() throws Exception {
        Path root = Files.createTempDirectory("scan");
        Files.createDirectories(root.resolve("src/main"));
        Files.write(root.resolve("src/main/App.java"), Arrays.asList("class App {}"), StandardCharsets.UTF_8);
        Files.createDirectories(root.resolve("target/generated"));
        Files.write(root.resolve("target/generated/Skip.txt"), Arrays.asList("skip"), StandardCharsets.UTF_8);
        Files.createDirectories(root.resolve("node_modules/pkg"));
        Files.write(root.resolve("node_modules/pkg/index.js"), Arrays.asList("skip"), StandardCharsets.UTF_8);

        List<File> files = ProjectLayout.findFiles(root.toFile());
        List<File> dirs = ProjectLayout.findDirectories(root.toFile());

        assertTrue(files.stream().anyMatch(file -> file.getPath().replace('\\', '/').endsWith("src/main/App.java")));
        assertFalse(files.stream().anyMatch(file -> file.getPath().replace('\\', '/').contains("target/generated/Skip.txt")));
        assertTrue(dirs.stream().anyMatch(file -> file.getPath().replace('\\', '/').endsWith("src/main")));
        assertFalse(dirs.stream().anyMatch(file -> file.getPath().replace('\\', '/').contains("node_modules")));
    }

    @Test
    void jscriptGetProjectIdThrowsWhenNameNodeMissing() throws Exception {
        Path projectDir = Files.createTempDirectory("js-no-name");
        Files.write(projectDir.resolve("package.json"), Arrays.asList("{}"), StandardCharsets.UTF_8);
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(projectDir.toFile());

        assertThrows(NullPointerException.class, layout::getProjectId);
    }

    @Test
    void mavenLayoutModulesReturnNullForNonPomPackagingAndTestsEmptyWhenNoBuild() {
        Model model = new Model();
        model.setArtifactId("artifact");
        model.setPackaging("jar");
        MavenProjectLayout layout = new MavenProjectLayout().model(model);

        List<String> modules = layout.getModules();
        List<String> tests = layout.getTests();

        assertNull(modules);
        assertTrue(tests.isEmpty());
    }

    @Test
    void mavenLayoutUsesDefaultsAndConvertsAbsoluteResourcePaths() {
        Path projectDir = tempDir("maven-project");
        Path absoluteResourceDir = projectDir.resolve("absolute-resource");
        absoluteResourceDir.toFile().mkdirs();
        Model model = new Model();
        model.setArtifactId("artifact");
        model.setName("Project Name");
        Parent parent = new Parent();
        parent.setArtifactId("parent-artifact");
        model.setParent(parent);
        Build build = new Build();
        Resource absoluteResource = new Resource();
        absoluteResource.setDirectory(absoluteResourceDir.toFile().getAbsolutePath());
        Resource relativeResource = new Resource();
        relativeResource.setDirectory("src/main/resources");
        build.setResources(Arrays.asList(absoluteResource, relativeResource));
        model.setBuild(build);
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir.toFile()).model(model);

        Set<String> sources = layout.getSources();

        assertNotNull(model.getBuild().getTestSourceDirectory());
        assertTrue(sources.contains("src/main/java"));
        assertTrue(sources.contains("src/main/resources"));
        assertTrue(sources.contains("absolute-resource"));
        assertEquals("artifact", layout.getProjectId());
        assertEquals("Project Name", layout.getProjectName());
        assertEquals("parent-artifact", layout.getParentId());
    }

    private Path writePom(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private Path tempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
