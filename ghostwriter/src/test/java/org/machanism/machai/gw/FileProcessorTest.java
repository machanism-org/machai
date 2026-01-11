package org.machanism.machai.gw;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.mockito.Mockito;
import java.io.File;
import java.io.IOException;

class FileProcessorTest {
    private FileProcessor processor;
    private GenAIProvider provider;

    @BeforeEach
    void setUp() {
        provider = Mockito.mock(GenAIProvider.class);
        processor = new FileProcessor(provider);
    }

    /**
     * Tests if setInheritance and isInheritance work correctly.
     */
    @Test
    void testInheritanceFlag() {
        processor.setInheritance(true);
        assertTrue(processor.isInheritance());
        processor.setInheritance(false);
        assertFalse(processor.isInheritance());
    }

    /**
     * Tests if setUseParentsGuidances and isUseParentsGuidances work as expected.
     */
    @Test
    void testParentGuidanceFlag() {
        processor.setUseParentsGuidances(true);
        assertTrue(processor.isUseParentsGuidances());
        processor.setUseParentsGuidances(false);
        assertFalse(processor.isUseParentsGuidances());
    }

    /**
     * Tests getRootDir returns correct directory.
     */
    @Test
    void testGetRootDirReturnsInput() {
        File dir = new File("/input-dir");
        assertEquals(dir, processor.getRootDir(dir));
    }

    /**
     * Tests scanDocuments on non-existent directory does not throw.
     */
    @Test
    @Disabled("Scanning files in a non-existent directory should not throw, but may require environment mocks.")
    void testScanDocumentsDoesNotThrowOnNonExistentDirectory() {
        File dir = new File("non-existent-path");
        assertDoesNotThrow(() -> processor.scanDocuments(dir));
    }

    /**
     * Tests scanFolder logic with simple File directory stub.
     */
    @Test
    @Disabled("Integration test: requires file system stub/mocks.")
    void testScanFolderIntegration() throws IOException {
        File dir = Mockito.mock(File.class);
        Mockito.when(dir.isDirectory()).thenReturn(true);
        // This test is a placeholder for integration with actual file system/mocks.
        assertDoesNotThrow(() -> processor.scanFolder(dir));
    }
}
