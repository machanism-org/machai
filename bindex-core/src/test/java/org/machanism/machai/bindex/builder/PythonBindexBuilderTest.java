package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class PythonBindexBuilderTest {
    @TempDir
    Path tempDir;
    private ProjectLayout mockLayout;
    private PythonBindexBuilder builder;
    private GenAIProvider mockGenAI;

    @BeforeEach
    void setUp() {
        mockLayout = mock(ProjectLayout.class);
        doReturn(tempDir.toFile()).when(mockLayout).getProjectDir();
        builder = spy(new PythonBindexBuilder(mockLayout));
        mockGenAI = mock(GenAIProvider.class);
        doReturn(mockGenAI).when(builder).getGenAIProvider();
    }

    @Test
    @Disabled("Need to fix.")
    void projectContext_readsPyprojectToml_andPromptsGenAI() throws Exception {
        // Arrange
        File pyprojectFile = new File(tempDir.toFile(), "pyproject.toml");
        try (FileWriter writer = new FileWriter(pyprojectFile)) {
            writer.write("[project]\nname = \"sample\"");
        }
        Path srcDir = tempDir.resolve("sample");
        Files.createDirectory(srcDir);
        Path sourceFile = srcDir.resolve("mod1.py");
        Files.writeString(sourceFile, "print('hello')");
        // Act
        assertDoesNotThrow(() -> builder.projectContext());
        // Assert
        verify(mockGenAI, times(1)).prompt(org.mockito.ArgumentMatchers.anyString());
        verify(mockGenAI, times(1)).promptFile(sourceFile.toFile(), "source_resource_section");
    }

    @Test
    @Disabled("Need to fix.")
    void projectContext_handlesMissingPyprojectTomlGracefully() throws Exception {
        // Arrange: No pyproject.toml
        assertDoesNotThrow(() -> builder.projectContext());
        // Should not throw, should handle missing file
    }
    
    @Test
    @Disabled("Need to fix.")
    void projectContext_handlesMissingSourceDirGracefully() throws Exception {
        // Arrange
        File pyprojectFile = new File(tempDir.toFile(), "pyproject.toml");
        try (FileWriter writer = new FileWriter(pyprojectFile)) {
            writer.write("[project]\nname = \"nonesuch\"");
        }
        // Do NOT create nonesuch directory
        assertDoesNotThrow(() -> builder.projectContext());
        verify(mockGenAI, times(1)).prompt(org.mockito.ArgumentMatchers.anyString());
    }
}
