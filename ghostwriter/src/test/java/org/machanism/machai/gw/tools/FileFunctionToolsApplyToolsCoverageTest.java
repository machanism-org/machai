package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class FileFunctionToolsApplyToolsCoverageTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@TempDir
	File tempDir;

	@Test
	void applyTools_registersAllExpectedTools() {
		// Arrange
		CapturingGenai genai = new CapturingGenai();
		FileFunctionTools tools = new FileFunctionTools();

		// Act
		tools.applyTools(genai);

		// Assert
		assertEquals(4, genai.toolNames.size());
		assertTrue(genai.toolNames.contains("read_file_from_file_system"));
		assertTrue(genai.toolNames.contains("write_file_to_file_system"));
		assertTrue(genai.toolNames.contains("list_files_in_directory"));
		assertTrue(genai.toolNames.contains("get_recursive_file_list"));
	}

	@Test
	void getRecursiveFiles_whenDirectoryDoesNotExist_returnsNoFilesMessage() throws Exception {
		// Arrange
		FileFunctionTools tools = new FileFunctionTools();
		Method method = FileFunctionTools.class.getDeclaredMethod("getRecursiveFiles", Object[].class);
		method.setAccessible(true);
		ObjectNode props = objectMapper.createObjectNode();
		props.put("dir_path", "missing");

		// Act
		Object result = method.invoke(tools, new Object[] { new Object[] { props, tempDir } });

		// Assert
		assertEquals("No files found in directory.", result);
	}

	@Test
	void writeFile_whenFileExists_updatesContentSuccessfully() throws Exception {
		// Arrange
		File target = new File(tempDir, "existing.txt");
		Files.write(target.toPath(), "old".getBytes(StandardCharsets.UTF_8));
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "existing.txt");
		props.put("text", "new text");
		Method method = FileFunctionTools.class.getDeclaredMethod("writeFile", Object[].class);
		method.setAccessible(true);

		// Act
		Object result = method.invoke(new FileFunctionTools(), new Object[] { new Object[] { props, tempDir } });

		// Assert
		assertEquals("File updated successfully: existing.txt", result);
		assertEquals("new text", new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	void listFiles_whenPathPointsToAFile_returnsNoFilesMessage() throws Exception {
		// Arrange
		File file = new File(tempDir, "single.txt");
		Files.write(file.toPath(), "x".getBytes(StandardCharsets.UTF_8));
		ObjectNode props = objectMapper.createObjectNode();
		props.put("dir_path", "single.txt");
		Method method = FileFunctionTools.class.getDeclaredMethod("listFiles", Object[].class);
		method.setAccessible(true);

		// Act
		Object result = method.invoke(new FileFunctionTools(), new Object[] { new Object[] { props, tempDir } });

		// Assert
		assertEquals("No files found in directory.", result);
	}

	private static final class CapturingGenai implements Genai {
		private final List<String> toolNames = new ArrayList<>();

		@Override
		public void addTool(String name, String description, ToolFunction function, String... parameters) {
			toolNames.add(name);
		}

		@Override
		public void init(org.machanism.macha.core.commons.configurator.Configurator configurator) {
			// Sonar java:S1186 - intentionally empty test double method because initialization is not exercised here.
		}

		@Override
		public void prompt(String prompt) {
			// Sonar java:S1186 - intentionally empty test double method because prompting is not exercised here.
		}

		@Override
		public List<Double> embedding(String input, long timeoutMillis) {
			return java.util.Collections.emptyList();
		}

		@Override
		public void clear() {
			// Sonar java:S1186 - intentionally empty test double method because state clearing is not exercised here.
		}

		@Override
		public void instructions(String instructions) {
			// Sonar java:S1186 - intentionally empty test double method because instructions handling is not exercised here.
		}

		@Override
		public String perform() {
			return null;
		}

		@Override
		public void inputsLog(File file) {
			// Sonar java:S1186 - intentionally empty test double method because input logging is not exercised here.
		}

		@Override
		public void setWorkingDir(File workingDir) {
			// Sonar java:S1186 - intentionally empty test double method because working directory handling is not exercised here.
		}

		@Override
		public Usage usage() {
			return null;
		}
	}
}
