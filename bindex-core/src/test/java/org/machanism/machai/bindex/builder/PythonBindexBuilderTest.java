package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class PythonBindexBuilderTest {

    @TempDir
    Path tempDir;

    private ProjectLayout layout;
    private PythonBindexBuilder builder;
    private GenAIProvider genAI;

    @BeforeEach
    void setUp() {
        // Arrange
        layout = mock(ProjectLayout.class);
        doReturn(tempDir.toFile()).when(layout).getProjectDir();

        builder = spy(new PythonBindexBuilder(layout));
        genAI = mock(GenAIProvider.class);
        doReturn(genAI).when(builder).getGenAIProvider();
    }

    @Test
    void projectContext_promptsManifestAndSourceFilesWhenProjectNamePresent() throws Exception {
        // Arrange
        File pyproject = new File(tempDir.toFile(), "pyproject.toml");
        Files.write(pyproject.toPath(), "[project]\nname = \"sample\"\n".getBytes(StandardCharsets.UTF_8));

        Path sourceDir = tempDir.resolve("sample");
        Files.createDirectories(sourceDir);
        Path pyFile = sourceDir.resolve("mod1.py");
        Files.write(pyFile, "print('hello')".getBytes(StandardCharsets.UTF_8));

        // Act
        builder.projectContext();

        // Assert
        verify(genAI, org.mockito.Mockito.atLeastOnce()).prompt(anyString());
        verify(genAI).promptFile(pyFile.toFile(), "source_resource_section");
    }

    @Test
    void projectContext_throwsFileNotFoundExceptionWhenManifestMissing() throws Exception {
        // Arrange
        // no pyproject.toml

        // Act + Assert
        assertThrows(java.io.FileNotFoundException.class, () -> builder.projectContext());

        verify(genAI, org.mockito.Mockito.never()).promptFile(any(File.class), anyString());
        verify(genAI, org.mockito.Mockito.never()).prompt(anyString());
    }
}
