package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assumptions;

class FileFunctionToolsWriteUpdateTest {

	@TempDir
	File tempDir;

	@Test
	void writeFile_whenExistingAndValidRange_replacesSubstringAndReturnsUpdatedMessage() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		File existing = new File(tempDir, "file.txt");
		Files.write(existing.toPath(), "0123456789".getBytes(StandardCharsets.UTF_8));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("file_path", "file.txt");
		node.put("text", "ABC");
		node.put("start_position", 3);
		node.put("end_position", 6);
		node.put("charsetName", StandardCharsets.UTF_8.name());

		Method writeFile = FileFunctionTools.class.getDeclaredMethod("writeFile", Object[].class);
		writeFile.setAccessible(true);

		// Act
		Object result = writeFile.invoke(tools, new Object[] { new Object[] { node, tempDir } });
		String updated = new String(Files.readAllBytes(existing.toPath()), StandardCharsets.UTF_8);

		// Assert
		assertEquals("File updated successfully: file.txt", result);
		assertEquals("012ABC6789" + org.machanism.machai.ai.manager.GenAIProvider.LINE_SEPARATOR, updated);
	}

	@Test
	void writeFile_whenExistingAndInvalidRange_returnsErrorAndDoesNotModifyFile() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		File existing = new File(tempDir, "file.txt");
		Files.write(existing.toPath(), "hello".getBytes(StandardCharsets.UTF_8));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("file_path", "file.txt");
		node.put("text", "X");
		// invalid because start >= end
		node.put("start_position", 2);
		node.put("end_position", 2);
		node.put("charsetName", StandardCharsets.UTF_8.name());

		Method writeFile = FileFunctionTools.class.getDeclaredMethod("writeFile", Object[].class);
		writeFile.setAccessible(true);

		// Act
		Object result = writeFile.invoke(tools, new Object[] { new Object[] { node, tempDir } });
		String after = new String(Files.readAllBytes(existing.toPath()), StandardCharsets.UTF_8);

		// Assert
		assertEquals("Invalid start or end position for text replacement.", result);
		assertEquals("hello", after);
	}

	@Test
	void writeFile_whenExistingAndStartProvidedButEndMissing_throwsNullPointerExceptionDueToImplementationBug() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		File existing = new File(tempDir, "file.txt");
		Files.write(existing.toPath(), "hello".getBytes(StandardCharsets.UTF_8));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("file_path", "file.txt");
		node.put("text", "X");
		node.put("start_position", 1);
		// end_position intentionally omitted
		node.put("charsetName", StandardCharsets.UTF_8.name());

		Method writeFile = FileFunctionTools.class.getDeclaredMethod("writeFile", Object[].class);
		writeFile.setAccessible(true);

		// Act
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> writeFile.invoke(tools, new Object[] { new Object[] { node, tempDir } }));

		// Assert
		assertInstanceOf(NullPointerException.class, ex.getCause());
	}

    @Test
void getRelativePath_whenPathsAreEquivalent_shouldReturnDot() {
    // TestMate-9eacaed05d90f12c7d15799a4e13439f
    // Arrange
    File baseDir = new File("target");
    File equivalentDir = new File("target/./");
    // Act
    String result = FileFunctionTools.getRelativePath(baseDir, equivalentDir, true);
    // Assert
    assertEquals(".", result);
}

    @Test
    void getRelativePath_whenFileIsDescendant_shouldReturnNormalizedPathWithForwardSlashes() {
        // TestMate-faf16ef5cd83eb95ec63d7ddf3de224a
        // Arrange
        File baseDir = new File("target/base");
        File descendantFile = new File("target/base/src/main/java/App.java");
        // Act
        String resultWithDot = FileFunctionTools.getRelativePath(baseDir, descendantFile, true);
        String resultWithoutDot = FileFunctionTools.getRelativePath(baseDir, descendantFile, false);
        // Assert
        assertEquals("./src/main/java/App.java", resultWithDot);
        assertEquals("src/main/java/App.java", resultWithoutDot);
    }

    @Test
    void getRelativePath_whenPathsAreUnrelated_shouldReturnNull() {
        // TestMate-87315bb1ffd8789071e5f3821f66edc2
        // Arrange
        // This scenario specifically targets the IllegalArgumentException thrown by Path.relativize 
        // when paths have different roots. This is most reliably reproducible on Windows with different drive letters.
        Assumptions.assumeTrue(SystemUtils.IS_OS_WINDOWS);
        File baseDir = new File("C:/project/app");
        File unrelatedFile = new File("D:/other/data.txt");
        // Act
        String result = FileFunctionTools.getRelativePath(baseDir, unrelatedFile, true);
        // Assert
        assertNull(result);
    }

    @Test
void getRelativePath_whenFileIsOutsideDir_shouldReturnParentTraversalWithoutRedundantDot() {
    // TestMate-70ca4c83433165d93b193654ce1008d0
    // Arrange
    File baseDir = new File("target/app/src");
    File targetFile = new File("target/app/readme.md");
    File parentDir = new File("target/app");
    // Act
    String resultToFile = FileFunctionTools.getRelativePath(baseDir, targetFile, true);
    String resultToParent = FileFunctionTools.getRelativePath(baseDir, parentDir, true);
    // Assert
    assertEquals("../readme.md", resultToFile);
    assertEquals("..", resultToParent);
}
}
