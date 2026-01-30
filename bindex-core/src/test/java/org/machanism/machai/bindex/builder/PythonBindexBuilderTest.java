package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
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
import org.machanism.machai.project.layout.ProjectLayout;

class PythonBindexBuilderTest {

    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("python-bindex-builder-test").toFile();
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
    void projectContext_whenProjectNameMissing_promptsPyprojectAndAdditionalRulesOnly() throws Exception {
        // Arrange
        Files.write(new File(tempDir, "pyproject.toml").toPath(), "[project]\nversion=\"1\"\n".getBytes(StandardCharsets.UTF_8));

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());

        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, times(2)).prompt(contains("version"));
        verify(provider, times(2)).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenProjectNamePresent_promptsAllFilesUnderInferredDir() throws Exception {
        // Arrange
        Files.write(new File(tempDir, "pyproject.toml").toPath(), "[project]\nname=\"my.pkg\"\n".getBytes(StandardCharsets.UTF_8));

        File srcDir = new File(tempDir, "my/pkg");
        assertEquals(true, srcDir.mkdirs());
        File a = new File(srcDir, "a.py");
        File b = new File(srcDir, "readme.txt");
        Files.write(a.toPath(), "print('a')".getBytes(StandardCharsets.UTF_8));
        Files.write(b.toPath(), "hi".getBytes(StandardCharsets.UTF_8));

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).promptFile(any(File.class), anyString());

        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act
        builder.projectContext();

        // Assert
        verify(provider, times(2)).promptFile(any(File.class), anyString());
        verify(provider, times(2)).prompt(anyString());
    }

    @Test
    void projectContext_whenInferredDirDoesNotExist_doesNotThrowAndStillPromptsAdditionalRules() throws Exception {
        // Arrange
        Files.write(new File(tempDir, "pyproject.toml").toPath(), "[project]\nname=\"missing.pkg\"\n".getBytes(StandardCharsets.UTF_8));

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());

        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act + Assert
        assertDoesNotThrow(builder::projectContext);
        verify(provider, times(2)).prompt(anyString());
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void projectContext_whenPyprojectMissing_throwsIOException() {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).prompt(anyString());

        PythonBindexBuilder builder = new PythonBindexBuilder(layout);
        builder.genAIProvider(provider);

        // Act + Assert
        assertThrows(IOException.class, builder::projectContext);
    }
}
