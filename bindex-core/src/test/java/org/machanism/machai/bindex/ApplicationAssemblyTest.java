package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Bindex;

class ApplicationAssemblyTest {

    @TempDir
    File tempDir;

    @Test
    void assembly_whenProviderReturnsBlankResponse_stillCallsPerformAndDoesNotThrow() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("\n\t  ");

        Bindex b1 = new Bindex();
        b1.setId("lib-1");
        b1.setName("Lib 1");
        b1.setVersion("1.0.0");

        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

        // Act / Assert
        assertDoesNotThrow(() -> assembly.assembly("do something", Collections.singletonList(b1)));

        // Assert
        verify(provider).instructions(anyString());
        verify(provider).inputsLog(eq(new File(tempDir, ".machai/assembly-inputs.txt")));
        verify(provider).perform();
    }

    @Test
    void assembly_whenInputsLogThrowsRuntimeException_propagatesRuntimeException() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new RuntimeException("fail")).when(provider).inputsLog(eq(new File(tempDir, ".machai/assembly-inputs.txt")));

        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

        // Act / Assert
        assertThrows(RuntimeException.class,
                () -> assembly.assembly("do something", Collections.emptyList()));
    }

    @Test
    void assembly_whenPromptThrowsRuntimeException_propagatesRuntimeException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new RuntimeException("boom")).when(provider).prompt(anyString());

        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> assembly.assembly("do something", Collections.emptyList()));

        // Assert
        assertNotNull(ex);
        verify(provider, never()).perform();
    }

    @Test
    void projectDir_setsDirectoryAndReturnsSameInstance() {
        // Arrange
        ApplicationAssembly assembly = new ApplicationAssembly(mock(GenAIProvider.class));

        // Act
        ApplicationAssembly returned = assembly.projectDir(tempDir);

        // Assert
        assertSame(assembly, returned);
    }

    @Test
    void assembly_withMultipleBindexes_promptsEachRecommendedLibrarySection() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("ok");

        Bindex b1 = new Bindex();
        b1.setId("id-1");
        b1.setName("n1");
        b1.setVersion("1");

        Bindex b2 = new Bindex();
        b2.setId("id-2");
        b2.setName("n2");
        b2.setVersion("2");

        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

        // Act
        assembly.assembly("assemble", Arrays.asList(b1, b2));

        // Assert
        verify(provider).prompt(contains("id-1"));
        verify(provider).prompt(contains("id-2"));
        verify(provider).perform();
    }

    @Test
    public void assembly_whenBindexListIsEmpty_stillPerforms() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("ok");

        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

        // Act
        assembly.assembly("assemble", Collections.emptyList());

        // Assert
        verify(provider).perform();
    }
}
