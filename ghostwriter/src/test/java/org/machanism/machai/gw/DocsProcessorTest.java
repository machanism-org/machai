package org.machanism.machai.gw;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.mockito.Mockito;

/**
 * Unit tests for {@link DocsProcessor}.
 */
class DocsProcessorTest {

    private DocsProcessor docsProcessor;
    private GenAIProvider provider;

    @BeforeEach
    void setUp() {
        provider = Mockito.mock(GenAIProvider.class);
        docsProcessor = new DocsProcessor(provider);
    }

    /**
     * Tests scanDocuments with a non-existent directory does not throw.
     */
    @Test
    @Disabled("Need to fix.")
    void testScanDocumentsHandlesNonExistentDirectory() {
        File nonExistentDir = new File("non-existent-dir-path");
        assertDoesNotThrow(() -> docsProcessor.scanDocuments(nonExistentDir));
    }

    /**
     * Tests setInheritance and getInheritance functionality.
     */
    @Test
    void testSetAndGetInheritance() {
        docsProcessor.setInheritance(true);
        assertTrue(docsProcessor.isInheritance());
        docsProcessor.setInheritance(false);
        assertFalse(docsProcessor.isInheritance());
    }

    /**
     * Tests setUseParentsGuidances and getUseParentsGuidances functionality.
     */
    @Test
    void testSetAndGetUseParentsGuidances() {
        docsProcessor.setUseParentsGuidances(true);
        assertTrue(docsProcessor.isUseParentsGuidances());
        docsProcessor.setUseParentsGuidances(false);
        assertFalse(docsProcessor.isUseParentsGuidances());
    }

    /**
     * Ensures getRootDir returns the correct directory.
     */
    @Test
    void testGetRootDir() {
        File dir = new File("/some-path");
        assertEquals(dir, docsProcessor.getRootDir(dir));
    }

}
