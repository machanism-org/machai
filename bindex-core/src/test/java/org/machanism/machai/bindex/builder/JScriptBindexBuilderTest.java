package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;

class JScriptBindexBuilderTest {
    private ProjectLayout mockLayout;
    private JScriptBindexBuilder builder;
    private GenAIProvider mockGenAI;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockLayout = Mockito.mock(ProjectLayout.class);
        Mockito.when(mockLayout.getProjectDir()).thenReturn(tempDir.toFile());
        builder = Mockito.spy(new JScriptBindexBuilder(mockLayout));
        mockGenAI = Mockito.mock(GenAIProvider.class);
        Mockito.doReturn(mockGenAI).when(builder).getGenAIProvider();
    }

    @Test
    void projectContext_readsProjectModelFile_andPromptsGenAI() throws Exception {
        // Arrange
        File packageFile = new File(tempDir.toFile(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
        try (FileWriter writer = new FileWriter(packageFile)) {
            writer.write("testContent");
        }
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        Path sourceFile = srcDir.resolve("test.js");
        Files.writeString(sourceFile, "console.log('hello');");
        ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

        // Act
        builder.projectContext();

        // Assert
        Mockito.verify(mockGenAI, Mockito.atLeastOnce()).prompt(Mockito.anyString());
        Mockito.verify(mockGenAI, Mockito.atLeastOnce()).promptFile(Mockito.eq(sourceFile.toFile()), Mockito.eq("source_resource_section"));
    }

    @Test
    void projectContext_handlesMissingSrcFolderGracefully() throws Exception {
        // Arrange
        File packageFile = new File(tempDir.toFile(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
        try (FileWriter writer = new FileWriter(packageFile)) {
            writer.write("testContent");
        }
        // No src directory

        // Act/Assert
        assertDoesNotThrow(() -> builder.projectContext());
        Mockito.verify(mockGenAI, Mockito.atLeastOnce()).prompt(Mockito.anyString());
    }

    @Test
    void projectContext_logsWarningOnIOException_whenReadingSourceFile() throws Exception {
        // Arrange
        File packageFile = new File(tempDir.toFile(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
        try (FileWriter writer = new FileWriter(packageFile)) {
            writer.write("testContent");
        }
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        Path unreadableFile = srcDir.resolve("unreadable.js");
        Files.writeString(unreadableFile, "badContent");
        unreadableFile.toFile().setReadable(false);

        // Allows catching warnings via logger if needed (can use appender in extended tests)
        assertDoesNotThrow(() -> builder.projectContext());
        Mockito.verify(mockGenAI, Mockito.atLeastOnce()).prompt(Mockito.anyString());
    }
}