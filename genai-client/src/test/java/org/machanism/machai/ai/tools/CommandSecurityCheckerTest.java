package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class CommandSecurityCheckerTest {

	private static final class MapConfigurator implements Configurator {
		private final java.util.Map<String, String> map;
		private final String name;

		private MapConfigurator(String name, java.util.Map<String, String> map) {
			this.name = name;
			this.map = map;
		}

		@Override
		public String get(String key) {
			return map.get(key);
		}

		@Override
		public String get(String key, String defaultValue) {
			String val = map.get(key);
			return val == null ? defaultValue : val;
		}

		@Override
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Integer.valueOf(val);
		}

		@Override
		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(get(key));
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Boolean.valueOf(val);
		}

		@Override
		public long getLong(String key) {
			return Long.parseLong(get(key));
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Long.valueOf(val);
		}

		@Override
		public File getFile(String key) {
			String val = get(key);
			return val == null ? null : new File(val);
		}

		@Override
		public File getFile(String key, File defaultValue) {
			File val = getFile(key);
			return val == null ? defaultValue : val;
		}

		@Override
		public double getDouble(String key) {
			return Double.parseDouble(get(key));
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Double.valueOf(val);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void set(String key, String value) {
			map.put(key, value);
		}
	}

	@Test
	void denyCheck_whenCustomRulesDenyRegex_throwsDenyException() throws Exception {
		// Arrange
		Configurator conf = new MapConfigurator("test", new HashMap<>());
		CommandSecurityChecker checker = new CommandSecurityChecker(conf);

		Method loadRules = CommandSecurityChecker.class.getDeclaredMethod("loadRules", String.class);
		loadRules.setAccessible(true);
		loadRules.invoke(checker, "REGEX:rm\\s+-rf\\s+/\n");

		// Act
		DenyException ex = assertThrows(DenyException.class, () -> checker.denyCheck("rm -rf /"));

		// Assert
		assertTrue(ex.getMessage().contains("Pattern:"));
	}

	@Test
	void denyCheck_whenCustomRulesDenyKeyword_caseInsensitive_throwsDenyException() throws Exception {
		// Arrange
		Configurator conf = new MapConfigurator("test", new HashMap<>());
		CommandSecurityChecker checker = new CommandSecurityChecker(conf);

		Method loadRules = CommandSecurityChecker.class.getDeclaredMethod("loadRules", String.class);
		loadRules.setAccessible(true);
		loadRules.invoke(checker, "KEYWORD:PoWeRsHeLl\n");

		// Act
		DenyException ex = assertThrows(DenyException.class, () -> checker.denyCheck("powershell -c echo hi"));

		// Assert
		assertTrue(ex.getMessage().contains("Keyword:"));
	}

	@Test
	void denyCheck_whenNoRuleMatches_doesNotThrow() throws Exception {
		// Arrange
		Configurator conf = new MapConfigurator("test", new HashMap<>());
		CommandSecurityChecker checker = new CommandSecurityChecker(conf);

		Method loadRules = CommandSecurityChecker.class.getDeclaredMethod("loadRules", String.class);
		loadRules.setAccessible(true);
		loadRules.invoke(checker, "#comment\n\nKEYWORD:bad\nREGEX:evil\n");

		// Act + Assert
		assertDoesNotThrow(() -> checker.denyCheck("echo hello"));
	}

	@Test
	void constructor_whenConfiguratorOverridesRuleString_formatIsApplied() throws Exception {
		// Arrange
		String injected = "KEYWORD:INJECTED\n%s\n";
		java.util.Map<String, String> map = new HashMap<>();
		map.put("ft.command.denylist", injected);
		Configurator conf = new MapConfigurator("test", map);

		// Act
		CommandSecurityChecker checker = new CommandSecurityChecker(conf);

		// Assert
		DenyException ex = assertThrows(DenyException.class, () -> checker.denyCheck("injected"));
		assertTrue(ex.getMessage().contains("Keyword:"));
	}

	@Test
	void denyException_constructor_setsMessage() {
		// Arrange
		String msg = "reason";

		// Act
		DenyException ex = new DenyException(msg);

		// Assert
		assertEquals(msg, ex.getMessage());
	}

	@Test
	void denyCheck_whenDenylistResourceLoaded_doesNotBreakOnBenignCommand() throws Exception {
		// Arrange
		Configurator conf = new MapConfigurator("test", new HashMap<>());
		CommandSecurityChecker checker = new CommandSecurityChecker(conf);

		// Act + Assert
		assertDoesNotThrow(() -> checker.denyCheck(
				"echo " + new String("ok".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)));
	}
}
