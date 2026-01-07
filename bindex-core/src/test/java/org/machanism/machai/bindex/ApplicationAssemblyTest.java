package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.machanism.machai.schema. Bindex;

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
        List< Bindex> bindexList = new ArrayList<>();
        when(provider.perform()).thenReturn("Response from LLM");
        ResourceBundle.clearCache();
        // BindexBuilder.bindexSchemaPrompt and MessageFormat exercised indirectly
        assembly.assembly(prompt, bindexList);
        // There should be no exceptions
    }

    @Test
    @Disabled("Need to fix.")
    void testAssemblyThrowsOnIOException() throws IOException {
        GenAIProvider faultyProvider = mock(GenAIProvider.class);
        ApplicationAssembly faultyAssembly = new ApplicationAssembly(faultyProvider);
        doThrow(new IOException("fail")).when(faultyProvider).perform();
        List< Bindex> bindexList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> {
            faultyAssembly.assembly("prompt", bindexList);
        });
    }
}
