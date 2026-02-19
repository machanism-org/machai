package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.MavenProjectLayout;

class MavenBindexBuilderTest {

    @TempDir
    File tempDir;

    @Test
    void removeNotImportantData_clearsConfiguredSections() {
        // Arrange
        Model model = new Model();
        model.setDistributionManagement(new org.apache.maven.model.DistributionManagement());
        model.setBuild(new org.apache.maven.model.Build());
        model.setProperties(new java.util.Properties());
        model.setDependencyManagement(new org.apache.maven.model.DependencyManagement());
        model.setReporting(new org.apache.maven.model.Reporting());
        model.setScm(new org.apache.maven.model.Scm());
        model.setPluginRepositories(new java.util.ArrayList<org.apache.maven.model.Repository>());

        MavenBindexBuilder builder = new MavenBindexBuilder(mock(MavenProjectLayout.class));

        // Act
        builder.removeNotImportantData(model);

        // Assert
        assertNull(model.getDistributionManagement());
        assertNull(model.getBuild());
        assertNull(model.getDependencyManagement());
        assertNull(model.getReporting());
        assertNull(model.getScm());

        if (model.getProperties() != null) {
            org.junit.jupiter.api.Assertions.assertTrue(model.getProperties().isEmpty());
        }
        if (model.getPluginRepositories() != null) {
            org.junit.jupiter.api.Assertions.assertTrue(model.getPluginRepositories().isEmpty());
        }
    }

    @Test
    void projectContext_whenBuildNull_returnsEarlyAndDoesNotPrompt() throws Exception {
        // Arrange
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir);

        Model model = new Model();
        model.setBuild(null);
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        layout.model(model);

        GenAIProvider provider = mock(GenAIProvider.class);

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                builder.projectContext();
            }
        });

        // Assert
        verify(provider, never()).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenDirectoriesBlankOrMissing_doesNotWalkAndStillPromptsPomAndAdditionalRules() throws Exception {
        // Arrange
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir);

        Build build = new Build();
        build.setSourceDirectory(" ");
        build.setTestSourceDirectory(null);
        build.setResources(null);
        build.setTestResources(null);

        Model model = new Model();
        model.setBuild(build);
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        layout.model(model);

        GenAIProvider provider = mock(GenAIProvider.class);

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                builder.projectContext();
            }
        });

        // Assert
        verify(provider, atLeastOnce()).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenPromptFileThrows_isSwallowedAndContextContinues() throws Exception {
        // Arrange
        File srcMainJava = new File(tempDir, "src\\main\\java");
        Files.createDirectories(srcMainJava.toPath());
        File javaFile = new File(srcMainJava, "A.java");
        Files.write(javaFile.toPath(), "class A {}".getBytes(StandardCharsets.UTF_8));

        File resourcesDir = new File(tempDir, "src\\main\\resources");
        Files.createDirectories(resourcesDir.toPath());
        Files.write(new File(resourcesDir, "a.txt").toPath(), "x".getBytes(StandardCharsets.UTF_8));

        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir);

        Build build = new Build();
        build.setSourceDirectory(srcMainJava.getAbsolutePath());
        Resource r = new Resource();
        r.setDirectory(resourcesDir.getAbsolutePath());
        build.setResources(Collections.singletonList(r));

        Model model = new Model();
        model.setBuild(build);
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        layout.model(model);

        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new IOException("fail")).when(provider).promptFile(any(File.class), anyString());

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                builder.projectContext();
            }
        });

        // Assert
        verify(provider, atLeastOnce()).prompt(anyString());
        verify(provider, atLeastOnce()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenPromptForPomFails_propagatesRuntimeException() throws Exception {
        // Arrange
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir);

        Build build = new Build();
        build.setSourceDirectory(new File(tempDir, "missing").getAbsolutePath());

        Model model = new Model();
        model.setBuild(build);
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");
        layout.model(model);

        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new RuntimeException("prompt failed")).when(provider).prompt(anyString());

        MavenBindexBuilder builder = new MavenBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act / Assert
        assertThrows(RuntimeException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                builder.projectContext();
            }
        });
    }
}
