package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexRegisterTest {

    @TempDir
    File tempDir;

    @Test
    void update_setsFlagAndReturnsSameInstance() {
        // Arrange
        BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost");

        // Act
        BindexRegister returned = register.update(true);

        // Assert
        assertSame(register, returned);
    }

    @Test
    void processFolder_whenNoBindexFile_doesNotCallPicker() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        BindexRegister register = new BindexRegister(provider, "mongodb://localhost");

        Picker picker = mock(Picker.class);
        setPicker(register, picker);

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);

        // Act
        register.processFolder(layout);

        // Assert
        verify(picker, never()).getRegistredId(any(Bindex.class));
        verify(picker, never()).create(any(Bindex.class));
    }

    @Test
    void processFolder_whenRegisteredIdExistsAndUpdateFalse_doesNotCreate() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).toPath(),
                "{\"id\":\"i\",\"name\":\"n\",\"version\":\"1\",\"classification\":{\"languages\":[],\"domains\":[],\"layers\":[],\"integrations\":[]},\"dependencies\":[]}",
                StandardCharsets.UTF_8);

        BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost");

        Picker picker = mock(Picker.class);
        when(picker.getRegistredId(any(Bindex.class))).thenReturn("existing");
        setPicker(register, picker);

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);

        // Act
        register.update(false).processFolder(layout);

        // Assert
        verify(picker).getRegistredId(any(Bindex.class));
        verify(picker, never()).create(any(Bindex.class));
    }

    @Test
    void processFolder_whenRegisteredIdMissing_creates() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).toPath(),
                "{\"id\":\"i\",\"name\":\"n\",\"version\":\"1\",\"classification\":{\"languages\":[],\"domains\":[],\"layers\":[],\"integrations\":[]},\"dependencies\":[]}",
                StandardCharsets.UTF_8);

        BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost");

        Picker picker = mock(Picker.class);
        when(picker.getRegistredId(any(Bindex.class))).thenReturn(null);
        when(picker.create(any(Bindex.class))).thenReturn("newId");
        setPicker(register, picker);

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);

        // Act
        register.processFolder(layout);

        // Assert
        verify(picker).getRegistredId(any(Bindex.class));
        verify(picker).create(any(Bindex.class));
    }

    @Test
    void processFolder_whenUpdateTrue_alwaysCreates() throws Exception {
        // Arrange
        Files.writeString(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).toPath(),
                "{\"id\":\"i\",\"name\":\"n\",\"version\":\"1\",\"classification\":{\"languages\":[],\"domains\":[],\"layers\":[],\"integrations\":[]},\"dependencies\":[]}",
                StandardCharsets.UTF_8);

        BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost");

        Picker picker = mock(Picker.class);
        when(picker.getRegistredId(any(Bindex.class))).thenReturn("existing");
        when(picker.create(any(Bindex.class))).thenReturn("newId");
        setPicker(register, picker);

        ProjectLayout layout = TestLayouts.projectLayout(tempDir);

        // Act
        register.update(true).processFolder(layout);

        // Assert
        verify(picker).getRegistredId(any(Bindex.class));
        verify(picker).create(any(Bindex.class));
    }

    @Test
    void close_delegatesToPickerClose() throws Exception {
        // Arrange
        BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost");
        Picker picker = mock(Picker.class);
        setPicker(register, picker);

        // Act
        register.close();

        // Assert
        verify(picker).close();
    }

    @Test
    void close_whenPickerCloseThrows_propagatesIOException() throws Exception {
        // Arrange
        BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost");
        Picker picker = mock(Picker.class);
        doThrow(new IOException("fail")).when(picker).close();
        setPicker(register, picker);

        // Act / Assert
        assertThrows(IOException.class, register::close);
    }

    private static void setPicker(BindexRegister register, Picker picker) throws Exception {
        Field field = BindexRegister.class.getDeclaredField("picker");
        field.setAccessible(true);
        field.set(register, picker);
    }
}
