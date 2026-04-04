package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;

class CommandFunctionToolsApplyToolsTest {

	private static final class RecordingGenai implements Genai {
		private final Map<String, ToolFunction> tools = new LinkedHashMap<>();

		@Override
		public void init(Configurator conf) {
			// not used
		}

		@Override
		public void prompt(String prompt) {
			// not used
		}

		@Override
		public java.util.List<Double> embedding(String text, long timeout) {
			return null;
		}

		@Override
		public void clear() {
			// not used
		}

		@Override
		public void addTool(String name, String description, ToolFunction function, String... parameters) {
			tools.put(name, function);
		}

		@Override
		public void instructions(String instructions) {
			// not used
		}

		@Override
		public String perform() {
			return null;
		}

		@Override
		public void inputsLog(java.io.File file) {
			// not used
		}

		@Override
		public void setWorkingDir(java.io.File dir) {
			// not used
		}

		@Override
		public org.machanism.machai.ai.manager.Usage usage() {
			return null;
		}
	}

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
			return 0;
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			return defaultValue;
		}

		@Override
		public java.io.File getFile(String key) {
			return null;
		}

		@Override
		public java.io.File getFile(String key, java.io.File defaultValue) {
			return defaultValue;
		}

		@Override
		public double getDouble(String key) {
			return 0;
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
	void applyTools_registersExpectedToolNames() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		tools.setConfigurator(new NoopConfigurator());
		RecordingGenai provider = new RecordingGenai();

		// Act
		tools.applyTools(provider);

		// Assert
		assertEquals(2, provider.tools.size());
		assertNotNull(provider.tools.get("run_command_line_tool"));
		assertNotNull(provider.tools.get("terminate_process"));
	}
}
