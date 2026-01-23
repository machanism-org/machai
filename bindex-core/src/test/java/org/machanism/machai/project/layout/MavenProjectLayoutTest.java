package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;

class MavenProjectLayoutTest {

    @Test
    void isMavenProject_whenPomXmlExists_thenReturnsTrue() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-maven-layout-detect");
        Files.write(root.resolve("pom.xml"), "<project/>".getBytes());

        // Act
        boolean result = MavenProjectLayout.isMavenProject(root.toFile());

        // Assert
        assertEquals(true, result);
    }

    @Test
    void getModules_whenPackagingIsPom_thenReturnsModules() {
        // Arrange
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setPackaging("pom");
        model.setModules(List.of("m1", "m2"));

        MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(new java.io.File("."));

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(List.of("m1", "m2"), modules);
    }

    @Test
    void getModules_whenPackagingIsNotPom_thenReturnsNull() {
        // Arrange
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setPackaging("jar");

        MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(new java.io.File("."));

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNull(modules);
    }

    @Test
    void getSources_whenBuildDefinesSourceAndResources_thenReturnsRelatedPaths() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-maven-layout-sources");
        Files.createDirectories(root.resolve("src/main/java"));
        Files.createDirectories(root.resolve("src/main/resources"));

        Build build = new Build();
        build.setSourceDirectory(root.resolve("src/main/java").toString());
        Resource resource = new Resource();
        resource.setDirectory(root.resolve("src/main/resources").toString());
        build.setResources(List.of(resource));

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setBuild(build);

        MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(root.toFile());

        // Act
        List<String> sources = layout.getSources();

        // Assert
        assertEquals(List.of("src/main/java", "src/main/resources"), sources);
    }

    @Test
    void getTests_whenBuildDefinesTestSourcesAndResources_thenReturnsRelatedPaths() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-maven-layout-tests");
        Files.createDirectories(root.resolve("src/test/java"));
        Files.createDirectories(root.resolve("src/test/resources"));

        Build build = new Build();
        build.setTestSourceDirectory(root.resolve("src/test/java").toString());
        Resource tr = new Resource();
        tr.setDirectory(root.resolve("src/test/resources").toString());
        build.setTestResources(List.of(tr));

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setBuild(build);

        MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(root.toFile());

        // Act
        List<String> tests = layout.getTests();

        // Assert
        assertEquals(List.of("src/test/java", "src/test/resources"), tests);
    }

    @Test
    void getDocuments_whenCalled_thenReturnsSrcSite() {
        // Arrange
        MavenProjectLayout layout = new MavenProjectLayout().model(new Model()).projectDir(new java.io.File("."));

        // Act
        List<String> docs = layout.getDocuments();

        // Assert
        assertNotNull(docs);
        assertEquals(List.of("src/site"), docs);
    }
}
