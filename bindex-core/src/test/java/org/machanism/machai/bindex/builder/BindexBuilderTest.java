package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.mockito.InOrder;

class BindexBuilderTest {

    @TempDir
    File tempDir;

    private ProjectLayout layout() {
        return org.machanism.machai.bindex.TestLayouts.projectLayout(tempDir);
    }

    @Test
    void origin_whenSet_isReturnedAndBuilderIsFluent() {
        // Arrange
        BindexBuilder builder = new BindexBuilder(layout());
        Bindex origin = new Bindex();
        origin.setId("id-1");

        // Act
        BindexBuilder returned = builder.origin(origin);

        // Assert
        assertSame(builder, returned);
        assertSame(origin, builder.getOrigin());
    }

    @Test
    void genAIProvider_whenSet_isReturnedAndSystemInstructionsAreApplied() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        BindexBuilder builder = new BindexBuilder(layout());

        // Act
        BindexBuilder returned = builder.genAIProvider(provider);

        // Assert
        assertSame(builder, returned);
        assertSame(provider, builder.getGenAIProvider());
        verify(provider).instructions(anyString());
    }

    @Test
    void build_whenProviderNotConfigured_throwsNullPointerException() {
        // Arrange
        BindexBuilder builder = new BindexBuilder(layout());

        // Act / Assert
        assertThrows(NullPointerException.class, builder::build);
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
        verify(provider).inputsLog(any(File.class));
        verify(provider).perform();
    }

    @Test
    void build_whenProviderReturnsInvalidJson_throwsIOException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("not-json");
        BindexBuilder builder = new BindexBuilder(layout()).genAIProvider(provider);

        // Act / Assert
        assertThrows(IOException.class, builder::build);
    }

    @Test
    void build_whenOriginProvided_includesUpdatePromptAndBuildsBindex() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}");

        Bindex origin = new Bindex();
        origin.setId("orig-id");
        origin.setName("orig-name");
        origin.setVersion("0");

        BindexBuilder builder = new BindexBuilder(layout()).genAIProvider(provider).origin(origin);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNotNull(result);
        assertEquals("x", result.getId());
        assertEquals("n", result.getName());
        assertEquals("1", result.getVersion());

        verify(provider).inputsLog(any(File.class));
        verify(provider).perform();
        verify(provider, never()).promptFile(any(File.class), anyString());
    }

    @Test
    void build_callsInputsLogBeforePerform() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"x\",\"name\":\"n\",\"version\":\"1\"}");

        BindexBuilder builder = new BindexBuilder(layout()).genAIProvider(provider);

        // Act
        builder.build();

        // Assert
        InOrder inOrder = inOrder(provider);
        inOrder.verify(provider).inputsLog(any(File.class));
        inOrder.verify(provider).perform();
    }
}
