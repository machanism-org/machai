package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

class BindexProjectProcessorTest {

    @TempDir
    Path tempDir;

    static class TestProcessor extends BindexProjectProcessor {
        @Override
        public void processFolder(ProjectLayout processor) {
            // no-op
        }
    }

    @Test
    void getBindexReturnsNullWhenBindexJsonDoesNotExist() {
        // Arrange
        TestProcessor processor = new TestProcessor();

        // Act
        Bindex bindex = processor.getBindex(tempDir.toFile());

        // Assert
        assertNull(bindex);
    }

    @Test
    void getBindexReadsFileWhenBindexJsonExists() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();

        Path bindexFile = tempDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME);
        Bindex expected = new Bindex();
        expected.setId("id");
        expected.setName("name");
        expected.setVersion("1.0");
        new ObjectMapper().writeValue(bindexFile.toFile(), expected);

        // Act
        Bindex actual = processor.getBindex(tempDir.toFile());

        // Assert
        assertNotNull(actual);
        assertEquals("id", actual.getId());
        assertEquals("name", actual.getName());
        assertEquals("1.0", actual.getVersion());
    }

    @Test
    void getBindexThrowsIllegalArgumentExceptionWhenJsonInvalid() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();

        Files.writeString(tempDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME), "{invalid");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> processor.getBindex(tempDir.toFile()));
    }

    @Test
    void getBindexFileUsesBindexJsonNameInDirectory() {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File projectDir = new File("/some/dir");

        // Act
        File actual = processor.getBindexFile(projectDir);

        // Assert
        assertEquals(new File(projectDir, BindexProjectProcessor.BINDEX_FILE_NAME), actual);
    }
}
