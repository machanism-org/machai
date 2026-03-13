package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CommandFunctionToolsAdditionalTest {

	@TempDir
	File tempDir;

	private static final class NoopConfigurator implements Configurator {
		@Override
		public String get(String key) {
			return null;
		}

		@Override
		public String get(String key, String defaultValue) {
			return defaultValue;
		}

		@Override
		public int getInt(String key) {
			return 0;
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			return defaultValue;
		}

		@Override
		public boolean getBoolean(String key) {
			return false;
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			return defaultValue;
		}

		@Override
		public long getLong(String key) {
			return 0L;
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			return defaultValue;
		}

		@Override
		public File getFile(String key) {
			return null;
		}

		@Override
		public File getFile(String key, File defaultValue) {
			return defaultValue;
		}

		@Override
		public double getDouble(String key) {
			return 0.0;
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			return defaultValue;
		}

		@Override
		public String getName() {
			return "noop";
		}

		@Override
		public void set(String key, String value) {
			// not used
		}
	}

	@Test
	void terminateProcess_whenMessageAndExitCode_throwsProcessTerminationException() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("message", "bye");
		node.put("exitCode", 7);

		// Act
		CommandFunctionTools.ProcessTerminationException ex = assertThrows(
				CommandFunctionTools.ProcessTerminationException.class,
				() -> tools.terminateProcess(new Object[] { node }));

		// Assert
		assertEquals("bye", ex.getMessage());
		assertEquals(7, ex.getExitCode());
		assertNull(ex.getCause());
	}

	@Test
	void terminateProcess_whenCauseProvided_wrapsCause() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("message", "bye");
		node.put("cause", "root");
		node.put("exitCode", 2);

		// Act
		CommandFunctionTools.ProcessTerminationException ex = assertThrows(
				CommandFunctionTools.ProcessTerminationException.class,
				() -> tools.terminateProcess(new Object[] { node }));

		// Assert
		assertEquals("bye", ex.getMessage());
		assertEquals(2, ex.getExitCode());
		assertNotNull(ex.getCause());
		assertEquals("java.lang.Exception: root", ex.getCause().toString());
	}

	@Test
	void executeCommand_whenWorkingDirOutsideProject_returnsErrorMessage() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		tools.setConfigurator(new NoopConfigurator());

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("command", "cmd /c echo hi");
		node.put("dir", "..");

		File projectSubDir = new File(tempDir, "project");
		assertTrue(projectSubDir.mkdirs());

		// Act
		String result = tools.executeCommand(new Object[] { node, projectSubDir });

		// Assert
		assertEquals("Error: Invalid working directory.", result);
	}

	@Test
	void waitAndCollect_whenProcessFinishes_appendsExitCode() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		LimitedStringBuilder out = new LimitedStringBuilder(1024);
		Process process = new ProcessBuilder("cmd", "/c", "exit", "0").start();
		java.util.concurrent.Future<?> done = java.util.concurrent.CompletableFuture.completedFuture(null);

		// Act
		String result = tools.waitAndCollect(process, done, done, out, "id");

		// Assert
		assertTrue(result.contains("Command exited with code: 0"));
	}

	@Test
	void waitAndCollect_whenProcessTimesOut_destroysAndMentionsTimeout() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		LimitedStringBuilder out = new LimitedStringBuilder(2048);
		Process process = new ProcessBuilder("cmd", "/c", "ping", "-n", "6", "127.0.0.1", ">nul").start();
		java.util.concurrent.Future<?> done = java.util.concurrent.CompletableFuture.completedFuture(null);

		Field timeoutField = CommandFunctionTools.class.getDeclaredField("processTimeoutSeconds");
		timeoutField.setAccessible(true);
		timeoutField.setInt(tools, 1);

		// Act
		String result = tools.waitAndCollect(process, done, done, out, "id");

		// Assert
		assertTrue(result.contains("Command timed out"));
		assertTrue(result.contains("Command exited with code:"));

		if (process.isAlive()) {
			process.destroyForcibly();
		}
	}
}
