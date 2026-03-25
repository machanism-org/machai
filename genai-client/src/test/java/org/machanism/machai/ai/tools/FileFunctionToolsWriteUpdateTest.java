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
}
