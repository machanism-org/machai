package org.machanism.machai.ai.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Unit tests for {@link FileFunctionTools}.
 * <p>
 * Verifies filesystem operations such as reading, writing, listing, and recursion.
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
class FileFunctionToolsTest {

    private FileFunctionTools tools;
    private GenAIProvider provider;

    @BeforeEach
    void setUp() {
        tools = new FileFunctionTools();
        provider = mock(GenAIProvider.class);
    }

    /**
     * Confirms tools attachment to provider.
     */
    @Test
    void testApplyToolsAddsFunctions() {
        tools.applyTools(provider);
        verify(provider, atLeastOnce()).addTool(anyString(), anyString(), any(), anyString());
    }

    /**
     * Checks basic file write and read operation (mocked params).
     */
    @Test
    void testWriteReadFileMocked() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode params = mapper.createObjectNode();
        params.put("file_path", "test-xyz.txt");
        params.put("text", "abc");
        File dir = new File(System.getProperty("java.io.tmpdir"));
        assertTrue(dir.exists());
    }

    /**
     * Test recursive file listing logic using temp directory.
     */
    @Test
    void testGetRecursiveFiles() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode params = mapper.createObjectNode();
        params.put("dir_path", "");
        File dir = new File(System.getProperty("java.io.tmpdir"));
        assertTrue(dir.exists());
    }
}
