package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link FileFunctionTools}.
 * <p>
 * Covers construction, working directory setting, and read/write/list functionality.
 *
 * @author Viktor Tovstyi
 * @guidance
 */
class FileFunctionToolsTest {
    private FileFunctionTools fileFunctionTools;
    private File tempDir;
    private static final Logger logger = LoggerFactory.getLogger(FileFunctionToolsTest.class);

    /**
     * Set up a FileFunctionTools instance for each test using a unique temp directory.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("testFileFuncTools").toFile();
        fileFunctionTools = new FileFunctionTools(tempDir);
    }

    /**
     * Verify that getWorkingDir throws when working directory is not set.
     */
    @Test
    void getWorkingDir_withNullWorkingDir_throwsException() {
        FileFunctionTools tools = new FileFunctionTools(null);
        Exception ex = assertThrows(IllegalArgumentException.class, tools::getWorkingDir);
        assertEquals("The function tool working dir is not defined.", ex.getMessage());
    }

    /**
     * Validate that setWorkingDir correctly updates the directory.
     */
    @Test
    void setWorkingDir_setsDirSuccessfully() {
        File newDir = new File(tempDir, "otherDir");
        fileFunctionTools.setWorkingDir(newDir);
        assertSame(newDir, fileFunctionTools.getWorkingDir());
    }

    /**
     * Verify that applyTools adds the correct file tools to provider.
     */
    @Test
    void applyTools_invokesAddTool() {
        GenAIProvider mockProvider = Mockito.mock(GenAIProvider.class);
        fileFunctionTools.applyTools(mockProvider);
        Mockito.verify(mockProvider, Mockito.atLeastOnce()).addTool(
                Mockito.eq("read_file_from_file_system"),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any());
    }

    /**
     * Test writing to and reading from file system functions.
     */
    @Test
    @Disabled("Need to fix.")
    void writeFile_and_readFile_workCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String testPath = "sample.txt";
        String testText = "This is sample content.";
        JsonNode writeParams = mapper.readTree("{\"file_path\": \"" + testPath + "\", \"text\": \"" + testText + "\"}");
        JsonNode readParams = mapper.readTree("{\"file_path\": \"" + testPath + "\"}");
        boolean writeResult = (boolean) fileFunctionTools.getClass()
                .getDeclaredMethod("writeFile", JsonNode.class)
                .invoke(fileFunctionTools, writeParams);
        assertTrue(writeResult);
        String content = fileFunctionTools.getClass()
                .getDeclaredMethod("readFile", JsonNode.class)
                .invoke(fileFunctionTools, readParams).toString();
        assertEquals(testText, content);
    }

    /**
     * Test recursive file listing functionality.
     */
    @Test
    @Disabled("Need to fix.")
    void getRecursiveFiles_listsFilesRecursively() throws Exception {
        File subDir = new File(tempDir, "subdir");
        subDir.mkdirs();
        File file1 = new File(tempDir, "file1.txt");
        File file2 = new File(subDir, "file2.txt");
        Files.write(file1.toPath(), "file1".getBytes());
        Files.write(file2.toPath(), "file2".getBytes());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode params = mapper.readTree("{\"dir_path\": \".\"}");
        String result = fileFunctionTools.getClass()
                .getDeclaredMethod("getRecursiveFiles", JsonNode.class)
                .invoke(fileFunctionTools, params).toString();
        assertTrue(result.contains("file1.txt"));
        assertTrue(result.contains("file2.txt"));
    }
}
