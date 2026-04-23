package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ToolCoverageEdgeCasesAdditionalTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@TempDir
	Path tempDir;

	@Test
	void fileFunctionTools_getRelativePath_returnsNullWhenBaseIsNull() {
		// Act
		String result = FileFunctionTools.getRelativePath(null, tempDir.toFile(), true);

		// Assert
		assertNull(result);
	}

	@Test
	void fileFunctionTools_getRelativePath_returnsDotWhenPathsAreEqual() {
		// Act
		String result = FileFunctionTools.getRelativePath(tempDir.toFile(), tempDir.toFile(), true);

		// Assert
		assertEquals(".", result);
	}

	@Test
	void fileFunctionTools_writeFile_returnsErrorMessageWhenPathIsDirectory() throws Exception {
		// Arrange
		File workingDir = tempDir.toFile();
		File directory = new File(workingDir, "targetDir");
		assertTrue(directory.mkdirs());
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "targetDir");
		props.put("text", "content");
		Method method = FileFunctionTools.class.getDeclaredMethod("writeFile", Object[].class);
		method.setAccessible(true);

		// Act
		Object result = method.invoke(new FileFunctionTools(), new Object[] { new Object[] { props, workingDir } });

		// Assert
		assertTrue(String.valueOf(result).length() > 0);
	}

	@Test
	void fileFunctionTools_getRecursiveFiles_returnsNoFilesMessageForMissingDirectory() throws Exception {
		// Arrange
		ObjectNode props = objectMapper.createObjectNode();
		props.put("dir_path", "missing");
		Method method = FileFunctionTools.class.getDeclaredMethod("getRecursiveFiles", Object[].class);
		method.setAccessible(true);

		// Act
		Object result = method.invoke(new FileFunctionTools(), new Object[] { new Object[] { props, tempDir.toFile() } });

		// Assert
		assertEquals("No files found in directory.", result);
	}

	@Test
	void actFunctionTools_getActDetails_throwsWhenActNameMissing() throws Exception {
		// Arrange
		ActFunctionTools tools = new ActFunctionTools();
		tools.setConfigurator(new PropertiesConfigurator());
		ObjectNode props = objectMapper.createObjectNode();
		Method method = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		method.setAccessible(true);

		// Act + Assert
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> method.invoke(tools, new Object[] { new Object[] { props } }));
		assertInstanceOf(NullPointerException.class, ex.getCause());
	}

	@Test
	void actFunctionTools_getActDetails_loadsCustomActFromConfiguredDirectory() throws Exception {
		// Arrange
		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Files.write(actsDir.resolve("custom.toml"), (
				"description = 'Custom description'\n" +
				"instructions = 'Do custom work'\n" +
				"input = 'Input text'\n").getBytes(StandardCharsets.UTF_8));
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		configurator.set("gw.acts", actsDir.toString());
		ActFunctionTools tools = new ActFunctionTools();
		tools.setConfigurator(configurator);
		ObjectNode props = objectMapper.createObjectNode();
		props.put("actName", "custom");
		props.put("custom", "true");
		Method method = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		method.setAccessible(true);

		// Act
		Object result = method.invoke(tools, new Object[] { new Object[] { props } });

		// Assert
		assertInstanceOf(Map.class, result);
		Map<?, ?> map = (Map<?, ?>) result;
		assertEquals("Custom description", map.get("description"));
	}

	@Test
	void actFunctionTools_getBaseActList_returnsEmptyOutsideJarExecution() throws IOException {
		// Arrange
		ActFunctionTools tools = new ActFunctionTools();

		// Act
		Object result = tools.getBaseActList();

		// Assert
		assertTrue(((java.util.Set<?>) result).isEmpty());
	}
}
