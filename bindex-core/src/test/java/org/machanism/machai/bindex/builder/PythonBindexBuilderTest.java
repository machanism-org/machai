package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class PythonBindexBuilderTest {

    @TempDir
    File tempDir;

    @Test
    void projectContext_whenPyprojectTomlMissing_throwsIOException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act / Assert
        assertThrows(IOException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                builder.projectContext();
            }
        });
    }

    @Test
    void projectContext_whenProjectNamePresent_promptsFilesInInferredSourceDirAndAdditionalRules() throws Exception {
        // Arrange
        Files.write(new File(tempDir, "pyproject.toml").toPath(), "[project]\nname=\"my.pkg\"\n".getBytes(StandardCharsets.UTF_8));

        File sourceDir = new File(tempDir, "my\\pkg");
        Files.createDirectories(sourceDir.toPath());
        Files.write(new File(sourceDir, "a.py").toPath(), "print('x')".getBytes(StandardCharsets.UTF_8));

        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
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
    void projectContext_whenProjectNameMissing_stillPromptsManifestAndAdditionalRulesOnly() throws Exception {
        // Arrange
        Files.write(new File(tempDir, "pyproject.toml").toPath(), "[project]\n".getBytes(StandardCharsets.UTF_8));

        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
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
    void projectContext_whenPromptFileThrows_propagatesException() throws Exception {
        // Arrange
        Files.write(new File(tempDir, "pyproject.toml").toPath(), "[project]\nname=\"my.pkg\"\n".getBytes(StandardCharsets.UTF_8));

        File sourceDir = new File(tempDir, "my\\pkg");
        Files.createDirectories(sourceDir.toPath());
        Files.write(new File(sourceDir, "a.py").toPath(), "print('x')".getBytes(StandardCharsets.UTF_8));

        GenAIProvider provider = mock(GenAIProvider.class);
        org.mockito.Mockito.doThrow(new IOException("fail")).when(provider).promptFile(any(File.class), anyString());

        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act / Assert
        assertThrows(IOException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                builder.projectContext();
            }
        });
    }
}
