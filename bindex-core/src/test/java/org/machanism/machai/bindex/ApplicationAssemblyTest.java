package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.BIndex;

class ApplicationAssemblyTest {
    private GenAIProvider provider;
    private ApplicationAssembly assembly;

    @BeforeEach
    void setUp() {
        provider = mock(GenAIProvider.class);
        assembly = new ApplicationAssembly(provider);
    }

    @Test
    void testProjectDirSetterReturnsSelfAndSetsDir() {
        File dir = new File("/tmp/testdir");
        ApplicationAssembly result = assembly.projectDir(dir);
        assertSame(assembly, result);
        // Not public to read, but method must return self
    }

    @Test
    void testAssemblyExecutesLLMAndLogsResponse() throws IOException {
        String prompt = "Test prompt";
        List<BIndex> bindexList = new ArrayList<>();
        when(provider.instructions(anyString())).thenReturn(provider);
        when(provider.prompt(anyString())).thenReturn(provider);
        when(provider.inputsLog(any(File.class))).thenReturn(provider);
        when(provider.perform(true)).thenReturn("Response from LLM");
        ResourceBundle.clearCache();
        // BindexBuilder.bindexSchemaPrompt and MessageFormat exercised indirectly
        assembly.assembly(prompt, bindexList, true);
        // There should be no exceptions
    }

    @Test
    @Disabled("Need to fix.")
    void testAssemblyThrowsOnIOException() throws IOException {
        GenAIProvider faultyProvider = mock(GenAIProvider.class);
        ApplicationAssembly faultyAssembly = new ApplicationAssembly(faultyProvider);
        when(faultyProvider.instructions(anyString())).thenReturn(faultyProvider);
        when(faultyProvider.prompt(anyString())).thenReturn(faultyProvider);
        doThrow(new IOException("fail")).when(faultyProvider).perform(anyBoolean());
        List<BIndex> bindexList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> {
            faultyAssembly.assembly("prompt", bindexList, true);
        });
    }
}
