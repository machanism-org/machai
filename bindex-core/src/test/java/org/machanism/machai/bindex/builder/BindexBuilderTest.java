package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.fixtures.FakeGenAIProvider;
import org.machanism.machai.bindex.fixtures.ProjectLayouts;

class BindexBuilderTest {

    @Test
    void genAIProvider_setsSystemInstructions() {
        // Arrange
        FakeGenAIProvider provider = new FakeGenAIProvider();
        BindexBuilder builder = new BindexBuilder(ProjectLayouts.layoutWithDir(new File(".")));

        // Act
        builder.genAIProvider(provider);

        // Assert
        assertEquals(1, provider.getInstructions().size());
    }

    @Test
    void build_returnsNullWhenProviderReturnsNull() throws Exception {
        // Arrange
        File projectDir = Files.createTempDirectory("bindex-builder-null").toFile();
        FakeGenAIProvider provider = new FakeGenAIProvider().respondWith(null);
        BindexBuilder builder = new BindexBuilder(ProjectLayouts.layoutWithDir(projectDir)).genAIProvider(provider);

        // Act
        Object result = builder.build();

        // Assert
        assertNull(result);
    }

    @Test
    void build_parsesProviderJsonOutput() throws Exception {
        // Arrange
        File projectDir = Files.createTempDirectory("bindex-builder-json").toFile();
        FakeGenAIProvider provider = new FakeGenAIProvider().respondWith("{}");
        BindexBuilder builder = new BindexBuilder(ProjectLayouts.layoutWithDir(projectDir)).genAIProvider(provider);

        // Act
        Object result = builder.build();

        // Assert
        assertNotNull(result);
        // Also asserts temp log location set.
        assertNotNull(provider.getInputsLogFile());
        assertEquals(new File(projectDir, BindexBuilder.BINDEX_TEMP_DIR).getPath(), provider.getInputsLogFile().getPath());
    }
}
