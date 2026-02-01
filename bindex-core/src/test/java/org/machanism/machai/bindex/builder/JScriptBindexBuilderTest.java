package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class JScriptBindexBuilderTest {

    @TempDir
    File tempDir;

    @Test
    void projectContext_promptsPackageJsonPromptsSourceFilesAndAdditionalRules_evenIfSomeFilesFail() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, "package.json").toPath(), "{}", StandardCharsets.UTF_8);
        File srcDir = new File(tempDir, "src");
        Files.createDirectories(srcDir.toPath());
        File jsFile = new File(srcDir, "a.js");
        Files.writeString(jsFile.toPath(), "console.log(1)", StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new java.io.IOException("fail")).when(provider).promptFile(any(File.class), anyString());

        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(builder::projectContext);

        // Assert
        verify(provider, atLeastOnce()).prompt(anyString());
        verify(provider, atLeastOnce()).promptFile(any(File.class), anyString());
    }
}
