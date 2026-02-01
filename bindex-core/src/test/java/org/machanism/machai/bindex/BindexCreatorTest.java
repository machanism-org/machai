package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
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
        assertNotNull(returned);
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
        String json = Files.readString(bindexFile.toPath(), StandardCharsets.UTF_8);
        assertNotNull(json);
    }

    @Test
    void processFolder_whenUpdateTrue_overwritesExistingFile() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"2\"}");

        File bindexFile = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.writeString(bindexFile.toPath(), "{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}",
                StandardCharsets.UTF_8);

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider).update(true);

        // Act
        creator.processFolder(layout);

        // Assert
        Bindex loaded = creator.getBindex(tempDir);
        assertNotNull(loaded);
    }

    @Test
    void processFolder_whenProviderThrowsRuntimeException_propagatesRuntimeException() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new RuntimeException("boom")).when(provider).instructions(anyString());

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexCreator creator = new BindexCreator(provider).update(true);

        // Act / Assert
        assertThrows(RuntimeException.class, () -> creator.processFolder(layout));
    }
}
