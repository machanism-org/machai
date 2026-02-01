package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
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

class JScriptBindexBuilderTest {

    @TempDir
    File tempDir;

    @Test
    void projectContext_whenPackageJsonMissing_throwsIOException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act / Assert
        assertThrows(IOException.class, () -> builder.projectContext());
    }

    @Test
    void projectContext_whenSrcDirectoryMissing_promptsManifestAndAdditionalRulesOnly() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, "package.json").toPath(), "{}", StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(() -> builder.projectContext());

        // Assert
        verify(provider, atLeast(2)).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_walksSrcAndPromptsOnlyEligibleExtensions_andSwallowsPromptFileErrors() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, "package.json").toPath(), "{}", StandardCharsets.UTF_8);

        File srcDir = new File(tempDir, "src");
        Files.createDirectories(srcDir.toPath());
        Files.writeString(new File(srcDir, "a.js").toPath(), "console.log(1)", StandardCharsets.UTF_8);
        Files.writeString(new File(srcDir, "b.ts").toPath(), "export const x = 1;", StandardCharsets.UTF_8);
        Files.writeString(new File(srcDir, "c.vue").toPath(), "<template></template>", StandardCharsets.UTF_8);
        Files.writeString(new File(srcDir, "ignored.txt").toPath(), "nope", StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new IOException("fail")).when(provider).promptFile(any(File.class), anyString());

        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        assertDoesNotThrow(() -> builder.projectContext());

        // Assert
        verify(provider, atLeast(2)).prompt(anyString());
        verify(provider, atLeast(3)).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenPromptFails_propagatesRuntimeException() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, "package.json").toPath(), "{}", StandardCharsets.UTF_8);
        Files.createDirectories(new File(tempDir, "src").toPath());

        GenAIProvider provider = mock(GenAIProvider.class);
        org.mockito.Mockito.doThrow(new RuntimeException("prompt failed")).when(provider).prompt(anyString());

        ProjectLayout layout = org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act / Assert
        assertThrows(RuntimeException.class, () -> builder.projectContext());
    }
}
