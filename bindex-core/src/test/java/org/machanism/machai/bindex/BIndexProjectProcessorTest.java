package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link BindexProjectProcessor} abstract class.
 *
 * <p>
 * This test verifies file location logic and reading behavior of the BindexProjectProcessor,
 * especially its ability to return null, correct Bindex instance, or accurate file object.
 * </p>
 *
 * @author Viktor Tovstyi
 */
class BindexProjectProcessorTest {
    /**
     * Minimal implementation for testing purposes.
     */
    static class TestProcessor extends BindexProjectProcessor {
        @Override
        public void processFolder(ProjectLayout processor) {
            // No-op for test
        }
    }

    /**
     * Test that getBindex returns null if the bindex file does not exist.
     */
    @Test
    void getBindexReturnsNullWhenFileDoesNotExist() {
        TestProcessor processor = new TestProcessor();
        File tempDir = new File("/tmp/nonexistent");
        Bindex bindex = processor.getBindex(tempDir);
        assertNull(bindex);
    }

    /**
     * Test that getBindex returns a Bindex if the file exists and can be deserialized.
     */
    @Test
    void getBindexReturnsBindexWhenFileExists() throws Exception {
        TestProcessor processor = new TestProcessor();
        File dir = new File(System.getProperty("java.io.tmpdir"));
        File bindexFile = new File(dir, BindexProjectProcessor.BINDEX_FILE_NAME);
        Bindex testBindex = mock(Bindex.class);
        new ObjectMapper().writeValue(bindexFile, testBindex);
        Bindex result = processor.getBindex(dir);
        assertNotNull(result);
        bindexFile.delete();
    }

    /**
     * Test that getBindexFile returns the correct file path for a given directory.
     */
    @Test
    void getBindexFileReturnsCorrectFile() {
        TestProcessor processor = new TestProcessor();
        File dir = new File("/tmp/testdir");
        File file = processor.getBindexFile(dir);
        assertEquals(new File(dir, BindexProjectProcessor.BINDEX_FILE_NAME), file);
    }
}
