package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class JScriptBindexBuilderTest {

    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("js-bindex-builder-test").toFile();
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
    void projectContext_whenSrcMissing_stillPromptsPackageJsonAndAdditionalRules() throws Exception {
        // Arrange
        Files.write(new File(tempDir, JScriptProjectLayout.PROJECT_MODEL_FILE_NAME).toPath(), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, times(1)).prompt(contains("name"));
        verify(provider, times(2)).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenSrcHasSupportedAndUnsupportedFiles_promptsOnlySupported() throws Exception {
        // Arrange
        Files.write(new File(tempDir, JScriptProjectLayout.PROJECT_MODEL_FILE_NAME).toPath(), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));
        File src = new File(tempDir, "src");
        assertTrue(src.mkdirs());

        File a = new File(src, "a.js");
        File b = new File(src, "b.ts");
        File c = new File(src, "c.vue");
        File d = new File(src, "d.txt");
        Files.write(a.toPath(), "// js".getBytes(StandardCharsets.UTF_8));
        Files.write(b.toPath(), "// ts".getBytes(StandardCharsets.UTF_8));
        Files.write(c.toPath(), "<!-- vue -->".getBytes(StandardCharsets.UTF_8));
        Files.write(d.toPath(), "no".getBytes(StandardCharsets.UTF_8));

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).promptFile(any(File.class), anyString());

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, times(3)).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenPromptFileThrowsForOneFile_doesNotPropagate() throws Exception {
        // Arrange
        Files.write(new File(tempDir, JScriptProjectLayout.PROJECT_MODEL_FILE_NAME).toPath(), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));
        File src = new File(tempDir, "src");
        assertTrue(src.mkdirs());

        File a = new File(src, "a.js");
        File b = new File(src, "b.ts");
        Files.write(a.toPath(), "// js".getBytes(StandardCharsets.UTF_8));
        Files.write(b.toPath(), "// ts".getBytes(StandardCharsets.UTF_8));

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());
        doThrow(new IOException("fail")).when(provider).promptFile(a, "source_resource_section");
        doNothing().when(provider).promptFile(b, "source_resource_section");

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act + Assert
        assertDoesNotThrow(builder::projectContext);
        verify(provider, times(1)).promptFile(b, "source_resource_section");
    }

    @Test
    void projectContext_whenPackageJsonMissing_throwsIOException() {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());

        JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act + Assert
        assertThrows(IOException.class, builder::projectContext);
    }
}
