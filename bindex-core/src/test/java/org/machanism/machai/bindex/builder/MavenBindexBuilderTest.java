package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.MavenProjectLayout;

class MavenBindexBuilderTest {

    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("maven-bindex-builder-test").toFile();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && tempDir.exists()) {
            Files.walk(tempDir.toPath())
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // ignore
                    }
                });
        }
    }

    @Test
    void projectContext_whenBuildIsNull_returnsWithoutPrompting() throws Exception {
        // Arrange
        Model model = new Model();
        model.setBuild(null);

        MavenProjectLayout layout = mock(MavenProjectLayout.class);
        when(layout.getModel()).thenReturn(model);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).promptFile(any(File.class), anyString());

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, never()).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenSourceAndResourceDirsPresent_promptsFilesAndPomAndAdditionalRules() throws Exception {
        // Arrange
        File mainSrc = new File(tempDir, "src/main/java");
        File mainRes = new File(tempDir, "src/main/resources");
        File testSrc = new File(tempDir, "src/test/java");
        assertNotNull(mainSrc.mkdirs());
        assertNotNull(mainRes.mkdirs());
        assertNotNull(testSrc.mkdirs());

        File f1 = new File(mainSrc, "A.java");
        File f2 = new File(mainRes, "a.properties");
        File f3 = new File(testSrc, "ATest.java");
        Files.write(f1.toPath(), "class A {}".getBytes(StandardCharsets.UTF_8));
        Files.write(f2.toPath(), "k=v".getBytes(StandardCharsets.UTF_8));
        Files.write(f3.toPath(), "class ATest {}".getBytes(StandardCharsets.UTF_8));

        Build build = new Build();
        build.setSourceDirectory(mainSrc.getAbsolutePath());
        build.setTestSourceDirectory(testSrc.getAbsolutePath());

        Resource r = new Resource();
        r.setDirectory(mainRes.getAbsolutePath());
        build.addResource(r);

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setBuild(build);

        MavenProjectLayout layout = mock(MavenProjectLayout.class);
        when(layout.getModel()).thenReturn(model);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).promptFile(any(File.class), anyString());

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, times(3)).promptFile(any(File.class), anyString());
        verify(provider, times(2)).prompt(anyString());
    }

    @Test
    void projectContext_whenPromptFileThrowsForOneFile_doesNotPropagate() throws Exception {
        // Arrange
        File mainSrc = new File(tempDir, "src/main/java");
        assertNotNull(mainSrc.mkdirs());
        File f1 = new File(mainSrc, "A.java");
        File f2 = new File(mainSrc, "B.java");
        Files.write(f1.toPath(), "class A {}".getBytes(StandardCharsets.UTF_8));
        Files.write(f2.toPath(), "class B {}".getBytes(StandardCharsets.UTF_8));

        Build build = new Build();
        build.setSourceDirectory(mainSrc.getAbsolutePath());

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setBuild(build);

        MavenProjectLayout layout = mock(MavenProjectLayout.class);
        when(layout.getModel()).thenReturn(model);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());
        doThrow(new IOException("fail")).when(provider).promptFile(f1, "source_resource_section");
        doNothing().when(provider).promptFile(f2, "source_resource_section");

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act + Assert
        assertDoesNotThrow(builder::projectContext);
        verify(provider, times(1)).promptFile(f2, "source_resource_section");
    }

    @Test
    void removeNotImportantData_clearsKeySections() {
        // Arrange
        Model model = new Model();
        model.setBuild(new Build());
        model.setProperties(new java.util.Properties());
        model.setDependencyManagement(new org.apache.maven.model.DependencyManagement());
        model.setReporting(new org.apache.maven.model.Reporting());
        model.setScm(new org.apache.maven.model.Scm());
        model.setPluginRepositories(java.util.List.of(new org.apache.maven.model.Repository()));
        model.setDistributionManagement(new org.apache.maven.model.DistributionManagement());

        MavenProjectLayout layout = mock(MavenProjectLayout.class);
        when(layout.getModel()).thenReturn(model);

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);

        // Act
        builder.removeNotImportantData(model);

        // Assert
        assertTrue(model.getBuild() == null || model.getBuild().getPlugins().isEmpty());
        assertTrue(model.getProperties() == null || model.getProperties().isEmpty());
        assertNull(model.getDependencyManagement());
        assertNull(model.getReporting());
        assertNull(model.getScm());
        assertTrue(model.getPluginRepositories() == null || model.getPluginRepositories().isEmpty());
        assertNull(model.getDistributionManagement());
    }

    @Test
    void projectContext_whenInvalidResourceDirectoryPath_ignoresAndStillPromptsPomAndRules() throws Exception {
        // Arrange
        Build build = new Build();
        build.setSourceDirectory(new File(tempDir, "does-not-exist").getAbsolutePath());

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        model.setBuild(build);

        MavenProjectLayout layout = mock(MavenProjectLayout.class);
        when(layout.getModel()).thenReturn(model);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, times(2)).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }
}
