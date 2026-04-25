package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class FileFunctionToolsToolingTest {

	@TempDir
	File tempDir;

	@Test
	void readFile_whenMissing_returnsFileNotFoundMessage() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("file_path", "missing.txt");

		Method readFile = FileFunctionTools.class.getDeclaredMethod("readFile", Object[].class);
		readFile.setAccessible(true);

		// Act
		Object result = readFile.invoke(tools, new Object[] { new Object[] { node, tempDir } });

		// Assert
		assertEquals("File not found.", result);
	}

	@Test
	void writeFile_thenReadFile_roundTripWithCharset() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode writeNode = mapper.createObjectNode();
		writeNode.put("file_path", "a/b/test.txt");
		writeNode.put("text", "hello");
		writeNode.put("charsetName", StandardCharsets.UTF_8.name());

		Method writeFile = FileFunctionTools.class.getDeclaredMethod("writeFile", Object[].class);
		writeFile.setAccessible(true);

		ObjectNode readNode = mapper.createObjectNode();
		readNode.put("file_path", "a/b/test.txt");
		readNode.put("charsetName", StandardCharsets.UTF_8.name());

		Method readFile = FileFunctionTools.class.getDeclaredMethod("readFile", Object[].class);
		readFile.setAccessible(true);

		// Act
		Object writeResult = writeFile.invoke(tools, new Object[] { new Object[] { writeNode, tempDir } });
		Object readResult = readFile.invoke(tools, new Object[] { new Object[] { readNode, tempDir } });

		// Assert
		assertEquals("File written successfully: a/b/test.txt", writeResult);
		assertEquals("hello", readResult);
	}

	@Test
	void listFiles_whenDirectoryEmpty_returnsEmptyString() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("dir_path", "empty");
		assertTrue(new File(tempDir, "empty").mkdirs());

		Method listFiles = FileFunctionTools.class.getDeclaredMethod("listFiles", Object[].class);
		listFiles.setAccessible(true);

		// Act
		Object result = listFiles.invoke(tools, new Object[] { new Object[] { node, tempDir } });

		// Assert
		assertEquals("", result);
	}

	@Test
	void listFiles_whenHasChildren_returnsCommaSeparatedRelativePaths() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		File dir = new File(tempDir, "dir");
		assertTrue(dir.mkdirs());
		Files.write(new File(dir, "a.txt").toPath(), "x".getBytes(StandardCharsets.UTF_8));
		assertTrue(new File(dir, "sub").mkdirs());

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("dir_path", "dir");

		Method listFiles = FileFunctionTools.class.getDeclaredMethod("listFiles", Object[].class);
		listFiles.setAccessible(true);

		// Act
		String result = (String) listFiles.invoke(tools, new Object[] { new Object[] { node, tempDir } });

		// Assert
		assertTrue(result.contains("./dir/a.txt"));
		assertTrue(result.contains("./dir/sub"));
		assertTrue(result.contains(","));
	}

	@Test
	void getRecursiveFiles_whenNoFiles_returnsNoFilesMessage() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("dir_path", "empty");
		assertTrue(new File(tempDir, "empty").mkdirs());

		Method getRecursiveFiles = FileFunctionTools.class.getDeclaredMethod("getRecursiveFiles", Object[].class);
		getRecursiveFiles.setAccessible(true);

		// Act
		Object result = getRecursiveFiles.invoke(tools, new Object[] { new Object[] { node, tempDir } });

		// Assert
		assertEquals("No files found in directory.", result);
	}

}
