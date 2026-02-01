package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
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
    void projectContext_whenProjectNamePresent_promptsFilesInInferredSourceDirAndAdditionalRules() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, "pyproject.toml").toPath(),
                "[project]\nname=\"my.pkg\"\n", StandardCharsets.UTF_8);

        File sourceDir = new File(tempDir, "my/pkg");
        Files.createDirectories(sourceDir.toPath());
        Files.writeString(new File(sourceDir, "a.py").toPath(), "print('x')", StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(builder::projectContext);

        // Assert
        verify(provider, atLeastOnce()).prompt(anyString());
        verify(provider, atLeastOnce()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenProjectNameMissing_stillPromptsManifestAndAdditionalRules() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, "pyproject.toml").toPath(), "[project]\n", StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(builder::projectContext);

        // Assert
        verify(provider, atLeastOnce()).prompt(anyString());
    }
}
