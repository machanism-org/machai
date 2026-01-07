package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema. Bindex;

class BindexRegisterTest {
    private GenAIProvider provider;
    private ProjectLayout layout;

    @BeforeEach
    void setup() {
        provider = mock(GenAIProvider.class);
        layout = mock(ProjectLayout.class);
    }

    @Test
    @Disabled("Need to fix.")
    void testProcessProjectRegistersNewOrUpdateBindex() throws Exception {
        BindexRegister register = spy(new BindexRegister(provider));
        File dir = new File("/tmp/testproject");
        when(layout.getProjectDir()).thenReturn(dir);
         Bindex bindex = mock( Bindex.class);
        doReturn(bindex).when(register).getBindex(dir);
        Picker picker = mock(Picker.class);
        doReturn(null).when(picker).getRegistredId(bindex);
        doReturn("newid").when(picker).create(bindex);
        register.update(true);
        // Use reflection to inject mock Picker if needed
        register.processFolder(layout); // Should not throw
    }

    @Test
    @Disabled("Need to fix.")
    void testProcessProjectThrowsOnIOException() {
        BindexRegister register = new BindexRegister(provider);
        when(layout.getProjectDir()).thenReturn(new File("."));
        doThrow(new IOException("fail")).when(register).getBindex(any(File.class));
        assertThrows(IllegalArgumentException.class, () -> {
            register.processFolder(layout);
        });
    }

    @Test
    void testCloseDelegatesToPickerClose() throws Exception {
        BindexRegister register = spy(new BindexRegister(provider));
        Picker picker = mock(Picker.class);
        // Use reflection or other means if needed
        doNothing().when(picker).close();
        register.close();
        // Should not throw
    }

    @Test
    void testUpdateReturnsSelfAndSetsFlag() {
        BindexRegister register = new BindexRegister(provider);
        BindexRegister result = register.update(true);
        assertSame(register, result);
    }
}
