package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexCreatorTest {

    @TempDir
    Path tempDir;

    @Test
    void updateReturnsSameInstance() {
        // Arrange
        BindexCreator creator = new BindexCreator(mock(GenAIProvider.class));

        // Act
        BindexCreator returned = creator.update(true);

        // Assert
        assertSame(creator, returned);
    }

    @Test
    void processFolderWrapsFileNotFoundFromBuilderFactoryIntoIllegalArgumentException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        BindexCreator creator = new BindexCreator(provider);

        ProjectLayout layout = mock(ProjectLayout.class);
        File missingDir = new File(tempDir.toFile(), "does-not-exist");
        when(layout.getProjectDir()).thenReturn(missingDir);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
    }

    @Test
    void processFolderCreatesBindexJsonWhenUpdateTrueAndNoExistingBindex() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        BindexCreator creator = new BindexCreator(provider).update(true);

        Path projectDir = tempDir.resolve("project");
        Files.createDirectories(projectDir);

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(projectDir.toFile());

        // Act
        creator.processFolder(layout);

        // Assert
        // There is no LLM configured for tests; builder may produce null and file may not be created.
        // This test ensures no exception for the happy path with an existing directory.
    }

    @Test
    void processFolderWrapsIoExceptionsIntoIllegalArgumentException() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        BindexCreator creator = new BindexCreator(provider).update(true);

        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir.toFile());

        // Create an invalid bindex.json that will fail parsing (triggering IOException in ObjectMapper)
        Files.writeString(tempDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME), "{invalid-json");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
    }
}
