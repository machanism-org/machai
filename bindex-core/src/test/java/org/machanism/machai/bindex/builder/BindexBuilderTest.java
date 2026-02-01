package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexBuilderTest {

    @TempDir
    File tempDir;

    private ProjectLayout layout() {
        return org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
    }

    @Test
    void genAIProvider_setsSystemInstructionsAndReturnsSameInstance() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        BindexBuilder builder = new BindexBuilder(layout());

        // Act
        BindexBuilder returned = builder.genAIProvider(provider);

        // Assert
        assertNotNull(returned);
        verify(provider).instructions(anyString());
    }

    @Test
    void build_whenProviderReturnsNull_returnsNull() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn(null);

        BindexBuilder builder = new BindexBuilder(layout()).genAIProvider(provider);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNull(result);
    }

    @Test
    void build_whenOriginProvided_addsUpdatePrompt() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}");

        Bindex origin = new Bindex();
        origin.setId("x");
        origin.setName("n");
        origin.setVersion("0");

        BindexBuilder builder = new BindexBuilder(layout()).genAIProvider(provider).origin(origin);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNotNull(result);
        verify(provider).prompt(contains("update"));
        verify(provider).inputsLog(any(File.class));
        verify(provider).perform();
    }

    @Test
    void build_whenProviderReturnsInvalidJson_throwsException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("not-json");

        BindexBuilder builder = new BindexBuilder(layout()).genAIProvider(provider);

        // Act / Assert
        assertThrows(Exception.class, builder::build);
    }
}
