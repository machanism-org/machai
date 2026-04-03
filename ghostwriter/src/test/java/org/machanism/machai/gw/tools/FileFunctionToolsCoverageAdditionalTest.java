package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class FileFunctionToolsCoverageAdditionalTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@TempDir
	File tempDir;

	@Test
	void readFile_whenCharsetProvidedAsNullNode_thenUsesDefaultCharset() throws Exception {
		File file = new File(tempDir, "sample.txt");
		Files.write(file.toPath(), Collections.singletonList("héllo"), StandardCharsets.UTF_8);
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "sample.txt");
		props.putNull("charsetName");
		Method method = FileFunctionTools.class.getDeclaredMethod("readFile", Object[].class);
		method.setAccessible(true);

		Object result = method.invoke(new FileFunctionTools(), new Object[] { new Object[] { props, tempDir } });

		assertEquals("héllo" + System.lineSeparator(), result);
	}

	@Test
	void listFiles_whenDirPathBlank_thenUsesWorkingDirectory() throws Exception {
		File child = new File(tempDir, "child.txt");
		assertTrue(child.createNewFile());
		ObjectNode props = objectMapper.createObjectNode();
		props.put("dir_path", "   ");
		Method method = FileFunctionTools.class.getDeclaredMethod("listFiles", Object[].class);
		method.setAccessible(true);

		Object result = method.invoke(new FileFunctionTools(), new Object[] { new Object[] { props, tempDir } });

		assertEquals("./child.txt", result);
	}
}
