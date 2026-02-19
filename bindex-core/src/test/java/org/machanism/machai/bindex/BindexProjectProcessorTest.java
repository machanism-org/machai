package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexProjectProcessorTest {

    @TempDir
    File tempDir;

    static class TestProcessor extends BindexProjectProcessor {
        @Override
        public void processFolder(ProjectLayout projectLayout) {
            // not needed for these tests
        }
    }

    @Test
    void getBindexFile_returnsBindexJsonInProjectDir() {
        // Arrange
        TestProcessor processor = new TestProcessor();

        // Act
        File file = processor.getBindexFile(tempDir);

        // Assert
        assertEquals(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).getAbsolutePath(), file.getAbsolutePath());
    }

    @Test
    void getBindex_whenFileDoesNotExist_returnsNull() {
        // Arrange
        TestProcessor processor = new TestProcessor();

        // Act
        Bindex bindex = processor.getBindex(tempDir);

        // Assert
        assertNull(bindex);
    }

    @Test
    public void getBindex_whenFileExistsAndValid_parsesAndReturnsBindex() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File file = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.write(file.toPath(), "{\"id\":\"i\",\"name\":\"n\",\"version\":\"1\"}".getBytes(StandardCharsets.UTF_8));

        // Act
        Bindex bindex = processor.getBindex(tempDir);

        // Assert
        assertNotNull(bindex);
        assertEquals("i", bindex.getId());
    }

    @Test
    public void getBindex_whenFileExistsButInvalidJson_throwsIllegalArgumentException() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File file = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.write(file.toPath(), "not-json".getBytes(StandardCharsets.UTF_8));

        // Act / Assert
        try {
            processor.getBindex(tempDir);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // Test passes
        }
    }
}
