package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.fixtures.FakeGenAIProvider;
import org.machanism.machai.schema.Bindex;

class ApplicationAssemblyTest {

    @Test
    void projectDir_setsDirectoryAndIsFluent() throws Exception {
        // Arrange
        FakeGenAIProvider provider = new FakeGenAIProvider().respondWith("");
        ApplicationAssembly assembly = new ApplicationAssembly(provider);
        File dir = Files.createTempDirectory("assembly-project-dir").toFile();

        // Act
        ApplicationAssembly returned = assembly.projectDir(dir);

        // Assert
        assertEquals(assembly, returned);

        // Act
        assembly.assembly("do something", List.of(new Bindex()));

        // Assert
        assertNotNull(provider.getInputsLogFile());
        assertEquals(new File(dir, ".machai/assembly-inputs.txt").getPath(), provider.getInputsLogFile().getPath());
    }
}
