package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexBuilderTest {

    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = java.nio.file.Files.createTempDirectory("bindex-builder-test").toFile();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && tempDir.exists()) {
            java.nio.file.Files.walk(tempDir.toPath())
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(p -> {
                    try {
                        java.nio.file.Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // ignore
                    }
                });
        }
    }

    @Test
    void build_whenProviderReturnsNull_returnsNullAndStillLogsInputs() throws Exception {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).instructions(anyString());
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).inputsLog(any(File.class));
        when(provider.perform()).thenReturn(null);

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNull(result);
        verify(provider, times(1)).inputsLog(new File(tempDir, BindexBuilder.BINDEX_TEMP_DIR));
        verify(provider, times(1)).perform();
    }

    @Test
    void build_whenProviderReturnsInvalidJson_throwsIOException() throws Exception {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).instructions(anyString());
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).inputsLog(any(File.class));
        when(provider.perform()).thenReturn("not-json");

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act + Assert
        assertThrows(IOException.class, builder::build);
    }

    @Test
    void build_whenOriginProvided_promptsUpdateContainingSerializedOrigin() throws Exception {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        List<String> prompts = new ArrayList<>();
        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).instructions(anyString());
        doNothing().when(provider).inputsLog(any(File.class));
        doAnswer(inv -> {
            prompts.add(inv.getArgument(0, String.class));
            return null;
        }).when(provider).prompt(anyString());
        when(provider.perform()).thenReturn("{}");

        Bindex origin = new Bindex();

        BindexBuilder builder = new BindexBuilder(layout)
            .genAIProvider(provider)
            .origin(origin);

        // Act
        Bindex result = builder.build();

        // Assert
        assertNotNull(result);
        assertEquals(true, prompts.stream().anyMatch(p -> p.contains("existing bindex")));
        assertEquals(true, prompts.stream().anyMatch(p -> p.contains("\"constructors\"")));
    }

    @Test
    void origin_getOriginAndFluentReturn_behavesAsExpected() {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        BindexBuilder builder = new BindexBuilder(layout);
        Bindex origin = new Bindex();

        // Act
        BindexBuilder returned = builder.origin(origin);

        // Assert
        assertSame(builder, returned);
        assertSame(origin, builder.getOrigin());
    }

    @Test
    void genAIProvider_setsProviderAndAppliesSystemInstructions() {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        GenAIProvider provider = mock(GenAIProvider.class);

        // Act
        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Assert
        assertSame(provider, builder.getGenAIProvider());
        verify(provider, times(1)).instructions(anyString());
    }

    @Test
    void getProjectLayout_returnsCtorArgument() {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);

        // Act
        BindexBuilder builder = new BindexBuilder(layout);

        // Assert
        assertSame(layout, builder.getProjectLayout());
    }

    @Test
    void bindexSchemaPrompt_promptsProviderWithSchemaContents() throws Exception {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);
        final StringBuilder captured = new StringBuilder();
        doAnswer(inv -> {
            captured.append(inv.getArgument(0, String.class));
            return null;
        }).when(provider).prompt(anyString());

        // Act
        BindexBuilder.bindexSchemaPrompt(provider);

        // Assert
        assertNotNull(captured.toString());
        assertEquals(true, captured.toString().contains("$schema") || captured.toString().contains("title"));
        verify(provider, times(1)).prompt(anyString());
    }

    @Test
    void build_whenProjectContextThrows_propagatesIOExceptionAndDoesNotPerform() throws Exception {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).instructions(anyString());

        BindexBuilder builder = new BindexBuilder(layout) {
            @Override
            protected void projectContext() throws IOException {
                throw new IOException("boom");
            }
        }.genAIProvider(provider);

        // Act + Assert
        assertThrows(IOException.class, builder::build);
        verify(provider, never()).perform();
        verify(provider, never()).inputsLog(any(File.class));
    }

    @Test
    void build_callsInputsLogWithExpectedRelativePath() throws Exception {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).instructions(anyString());
        doNothing().when(provider).prompt(anyString());
        doNothing().when(provider).inputsLog(any(File.class));
        when(provider.perform()).thenReturn("{}");

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act
        builder.build();

        // Assert
        verify(provider, times(1)).inputsLog(new File(tempDir, BindexBuilder.BINDEX_TEMP_DIR));
    }

    @Test
    void build_whenInputsLogFails_propagatesIOExceptionFromProvider() throws Exception {
        // Arrange
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir);

        GenAIProvider provider = mock(GenAIProvider.class);
        doNothing().when(provider).instructions(anyString());
        doNothing().when(provider).prompt(anyString());
        doAnswer(inv -> {
            throw new IOException("no-dir");
        }).when(provider).inputsLog(any(File.class));

        BindexBuilder builder = new BindexBuilder(layout).genAIProvider(provider);

        // Act + Assert
        assertThrows(IOException.class, builder::build);
        verify(provider, never()).perform();
    }
}
