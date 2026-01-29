package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexProjectProcessorTest {

    private static final class TestProcessor extends BindexProjectProcessor {
        @Override
        public void processFolder(ProjectLayout processor) {
            // no-op for tests
        }
    }

    @Test
    void getBindexFile_returnsBindexJsonInProjectDirectory() {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File projectDir = new File("build/tmp/test-project");

        // Act
        File file = processor.getBindexFile(projectDir);

        // Assert
        assertEquals(new File(projectDir, BindexProjectProcessor.BINDEX_FILE_NAME).getPath(), file.getPath());
    }

    @Test
    void getBindex_returnsNullWhenBindexFileDoesNotExist() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File projectDir = Files.createTempDirectory("bindex-processor-no-file").toFile();

        // Act
        Bindex bindex = processor.getBindex(projectDir);

        // Assert
        assertNull(bindex);
    }

    @Test
    void getBindex_throwsIllegalArgumentExceptionWhenJsonIsInvalid() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File projectDir = Files.createTempDirectory("bindex-processor-invalid-json").toFile();
        File bindexFile = new File(projectDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.write(bindexFile.toPath(), "not-json".getBytes(StandardCharsets.UTF_8));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> processor.getBindex(projectDir));
    }

    @Test
    void getBindex_returnsParsedBindexWhenJsonIsValidMinimal() throws Exception {
        // Arrange
        TestProcessor processor = new TestProcessor();
        File projectDir = Files.createTempDirectory("bindex-processor-valid-json").toFile();
        File bindexFile = new File(projectDir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Files.write(bindexFile.toPath(), "{}".getBytes(StandardCharsets.UTF_8));

        // Act
        Bindex bindex = processor.getBindex(projectDir);

        // Assert
        assertNotNull(bindex);
    }
}
