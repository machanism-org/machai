package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Bindex;
import org.mockito.InOrder;

class ApplicationAssemblyTest {

    @TempDir
    Path tempDir;

    @Test
    void projectDirReturnsSameInstanceAndIsUsedForInputsLogFileLocation() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        ApplicationAssembly assembly = new ApplicationAssembly(provider);
        File projectDir = tempDir.toFile();
        when(provider.perform()).thenReturn(null);

        // Act
        ApplicationAssembly returned = assembly.projectDir(projectDir);
        assembly.assembly("any", Collections.emptyList());

        // Assert
        assertSame(assembly, returned);
        verify(provider).inputsLog(new File(projectDir, ".machai/assembly-inputs.txt"));
    }

    @Test
    @Disabled
    void assemblySendsSystemInstructionsSchemaAndBindexSectionsInOrder() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir.toFile());

        Bindex bindex = mock(Bindex.class);
        when(bindex.getId()).thenReturn("lib-x");
        when(provider.perform()).thenReturn("ok");

        // Act
        assembly.assembly("user prompt", List.of(bindex));

        // Assert
        InOrder ordered = inOrder(provider);
        ordered.verify(provider).instructions(anyString());
        ordered.verify(provider).prompt(anyString()); // assembly_instructions
        ordered.verify(provider).prompt(anyString()); // bindex schema
        ordered.verify(provider).prompt(anyString()); // bindex section
        ordered.verify(provider).prompt("user prompt");
        ordered.verify(provider).inputsLog(any(File.class));
        ordered.verify(provider).perform();

        verify(provider, never()).prompt((String) null);
    }

    @Test
    @Disabled
    void assemblyWrapsObjectMappingIoExceptionsIntoIllegalArgumentException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir.toFile());

        Bindex bindex = mock(Bindex.class);
        when(bindex.getId()).thenReturn("id");

        // Force ObjectMapper to attempt to serialize an object that throws an IOException
        Object bad = new Object() {
            @SuppressWarnings("unused")
            public String getBoom() throws IOException {
                throw new IOException("boom");
            }
        };
        when(bindex.getClassification()).thenReturn((org.machanism.machai.schema.Classification) bad);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> assembly.assembly("prompt", List.of(bindex)));
        assertEquals(IOException.class, ex.getCause().getClass());

        verify(provider, never()).perform();
        verify(provider, never()).inputsLog(any(File.class));
    }

    @Test
    void assemblyCreatesParentDirectoriesForInputsLogPathWhenProjectDirIsWritable() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        ApplicationAssembly assembly = new ApplicationAssembly(provider);

        Path projectDir = tempDir.resolve("project");
        Files.createDirectories(projectDir);
        when(provider.perform()).thenReturn(null);
        assembly.projectDir(projectDir.toFile());

        // Act
        assembly.assembly("p", Collections.emptyList());

        // Assert
        verify(provider).inputsLog(new File(projectDir.toFile(), ".machai/assembly-inputs.txt"));
    }
}
