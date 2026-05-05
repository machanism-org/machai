package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class FileFunctionToolsAdditionalTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void writeFileShouldCreateDirectoriesAndReadBackWithRequestedCharset() {
        FileFunctionTools tools = new FileFunctionTools();
        ObjectNode writeProps = MAPPER.createObjectNode();
        writeProps.put("file_path", "nested/data.txt");
        writeProps.put("text", "žlutý");
        writeProps.put("charsetName", "UTF-8");

        ObjectNode readProps = MAPPER.createObjectNode();
        readProps.put("file_path", "nested/data.txt");
        readProps.put("charsetName", "UTF-8");

        Object writeResult = tools.writeFile(writeProps, tempDir.toFile());
        Object readResult = tools.readFile(readProps, tempDir.toFile());

        assertEquals("File written successfully: nested/data.txt", writeResult);
        assertEquals("žlutý", readResult);
        assertTrue(Files.exists(tempDir.resolve("nested").resolve("data.txt")));
    }

    @Test
    void listFilesShouldReturnCommaSeparatedRelativePathsForBlankDirPath() throws Exception {
        FileFunctionTools tools = new FileFunctionTools();
        Files.write(tempDir.resolve("a.txt"), "a".getBytes(StandardCharsets.UTF_8));
        Files.createDirectory(tempDir.resolve("folder"));

        ObjectNode props = MAPPER.createObjectNode();
        props.put("dir_path", "   ");

        String result = (String) tools.listFiles(props, tempDir.toFile());

        assertTrue(result.contains("./a.txt"));
        assertTrue(result.contains("./folder"));
    }

    @Test
    void getRecursiveFilesShouldIgnoreExcludedDirectoriesAndReturnNoFilesForMissingDir() throws Exception {
        FileFunctionTools tools = new FileFunctionTools();
        Files.write(tempDir.resolve("root.txt"), "x".getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(tempDir.resolve("target"));
        Files.write(tempDir.resolve("target").resolve("ignored.txt"), "y".getBytes(StandardCharsets.UTF_8));

        ObjectNode existingDirProps = MAPPER.createObjectNode();
        ObjectNode missingDirProps = MAPPER.createObjectNode();
        missingDirProps.put("dir_path", "missing");

        Object recursiveResult = tools.getRecursiveFiles(existingDirProps, tempDir.toFile());
        Object missingDirResult = tools.getRecursiveFiles(missingDirProps, tempDir.toFile());

        assertTrue(recursiveResult.toString().contains("./root.txt"));
        assertTrue(!recursiveResult.toString().contains("ignored.txt"));
        assertEquals("No files found in directory.", missingDirResult);
    }

    @Test
    void getRelativePathShouldHandleNullSameDirectoryAndOutsidePath() throws Exception {
        File baseDir = tempDir.toFile();
        File sameDir = tempDir.toFile();
        Path otherRoot = Files.createTempDirectory("other-root");
        File outsideFile = otherRoot.resolve("x.txt").toFile();

        String nullResult = FileFunctionTools.getRelativePath(null, sameDir, true);
        String sameDirResult = FileFunctionTools.getRelativePath(baseDir, sameDir, true);
        String outsideResult = FileFunctionTools.getRelativePath(baseDir, outsideFile, true);

        assertNull(nullResult);
        assertEquals(".", sameDirResult);
        assertTrue(outsideResult.contains("../"));
    }
}
