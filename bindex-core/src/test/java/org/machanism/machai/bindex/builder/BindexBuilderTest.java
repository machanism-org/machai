package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.TestLayouts;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexBuilderTest {

    @TempDir
    File tempDir;

    @Test
    void origin_setsAndGetOrigin_returnsSameInstance() {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexBuilder builder = new BindexBuilder(layout);

        Bindex origin = new Bindex();
        origin.setId("i");
        origin.setName("n");
        origin.setVersion("1");

        // Act
        BindexBuilder returned = builder.origin(origin);

        // Assert
        assertSame(builder, returned);
        assertSame(origin, builder.getOrigin());
    }

    @Test
    void genAIProvider_setsProviderAndReturnsSameInstance_andCallsInstructions() {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexBuilder builder = new BindexBuilder(layout);
        GenAIProvider provider = mock(GenAIProvider.class);

        // Act
        BindexBuilder returned = builder.genAIProvider(provider);

        // Assert
        assertSame(builder, returned);
        assertSame(provider, builder.getGenAIProvider());
        verify(provider, atLeastOnce()).instructions(anyString());
    }

    @Test
    void build_whenProviderReturnsNull_returnsNull() throws Exception {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn(null);

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNull(result);
    }

    @Test
    void build_whenPerformReturnsInvalidJson_throwsIOException() throws Exception {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("not-json");

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act / Assert
        assertThrows(IOException.class, builder::build);
    }

    @Test
    void build_whenInputsLogThrowsRuntimeException_propagatesRuntimeException() {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new RuntimeException("fail")).when(provider).inputsLog(any(File.class));

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act / Assert
        assertThrows(RuntimeException.class, builder::build);
    }

    @Test
    void build_whenSuccessful_returnsParsedBindex() throws Exception {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"i\",\"name\":\"n\",\"version\":\"1\"}");

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNotNull(result);
        assertEquals("i", result.getId());
        verify(provider, atLeastOnce()).prompt(anyString());
        verify(provider).inputsLog(any(File.class));
    }

    @Test
    void build_whenOriginProvided_promptsUpdateSection() throws Exception {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        GenAIProvider provider = mock(GenAIProvider.class);
        when(provider.perform()).thenReturn("{\"id\":\"i\",\"name\":\"n\",\"version\":\"1\"}");

        Bindex origin = new Bindex();
        origin.setId("i");
        origin.setName("n");
        origin.setVersion("0");

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider).origin(origin);

        // Act
        builder.build();

        // Assert
        verify(provider, atLeastOnce()).prompt(org.mockito.ArgumentMatchers.contains("update"));
    }

    @Test
    void getProjectLayout_returnsSameLayout() {
        // Arrange
        ProjectLayout layout = TestLayouts.projectLayout(tempDir);
        BindexBuilder builder = new BindexBuilder(layout);

        // Act
        ProjectLayout returned = builder.getProjectLayout();

        // Assert
        assertSame(layout, returned);
    }

    @Test
    void bindexSchemaPrompt_whenProviderPromptThrows_propagatesRuntimeException() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        doThrow(new RuntimeException("prompt failed")).when(provider).prompt(anyString());

        // Act / Assert
        assertThrows(RuntimeException.class, () -> BindexBuilder.bindexSchemaPrompt(provider));
    }
}
