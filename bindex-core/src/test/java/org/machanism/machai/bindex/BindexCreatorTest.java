package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexCreatorTest {

    @TempDir
    File tempDir;

    @Test
    void update_setsFlagAndReturnsSameInstance() {
        // Arrange
        BindexCreator creator = new BindexCreator(mock(GenAIProvider.class));

        // Act
        BindexCreator returned = creator.update(true);

        // Assert
        assertSame(creator, returned);
    }

    @Test
    void processFolder_whenNoExistingBindexAndUpdateFalse_createsFile() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}");

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider);

        // Act
        assertDoesNotThrow(() -> creator.processFolder(layout));

        // Assert
        File bindexFile = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        assertNotNull(bindexFile);
        assertEquals(true, bindexFile.exists());

        String json = Files.readString(bindexFile.toPath(), StandardCharsets.UTF_8);
        assertNotNull(json);
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"version\""));
    }

    @Test
    void processFolder_whenUpdateFalseAndExistingBindex_present_doesNotOverwrite() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).toPath(),
                "{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}", StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"2\"}");

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider).update(false);

        // Act
        creator.processFolder(layout);

        // Assert
        Bindex loaded = creator.getBindex(tempDir);
        assertNotNull(loaded);
        assertEquals("1", loaded.getVersion());
    }

    @Test
    void processFolder_whenUpdateTrue_overwritesExistingFile() throws Exception {
        // Arrange
        File bindexFile = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.writeString(bindexFile.toPath(), "{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}",
                StandardCharsets.UTF_8);

        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"2\"}");

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider).update(true);

        // Act
        creator.processFolder(layout);

        // Assert
        Bindex loaded = creator.getBindex(tempDir);
        assertNotNull(loaded);
        assertEquals("2", loaded.getVersion());
    }

    @Test
    void processFolder_whenProviderReturnsNull_doesNotCreateFile() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn(null);

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider);

        // Act
        creator.processFolder(layout);

        // Assert
        File bindexFile = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        org.junit.jupiter.api.Assertions.assertFalse(bindexFile.exists());
    }

    @Test
    void processFolder_whenBuilderCreationFails_wrapsIntoIllegalArgumentException() {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(new File(tempDir, "does-not-exist"));
        BindexCreator creator = new BindexCreator(mock(GenAIProvider.class));

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
    }

    @Test
    void processFolder_whenObjectMapperWriteFails_wrapsIntoIllegalArgumentException() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}");

        File bindexFile = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.createDirectories(bindexFile.toPath());

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider);

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
    }
}
