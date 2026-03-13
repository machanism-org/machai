package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

class ActProcessorTest {

	@TempDir
	Path tempDir;

	@Test
	void tryLoadActFromDirectory_whenNullActDir_thenNull() throws Exception {
		assertNull(ActProcessor.tryLoadActFromDirectory(new HashMap<>(), "x", null));
	}

	@Test
	void tryLoadActFromDirectory_whenFileExists_thenLoadsAndPopulatesProperties() throws Exception {
		File actDir = tempDir.resolve("acts").toFile();
		assertTrue(actDir.mkdirs());
		File actFile = new File(actDir, "a.toml");
		Files.write(actFile.toPath(), java.util.Arrays.asList("instructions='I'", "inputs='P'", "prologue=['x']"),
				StandardCharsets.UTF_8);

		Map<String, Object> props = new HashMap<>();
		TomlParseResult result = ActProcessor.tryLoadActFromDirectory(props, "a", actDir);
		assertNotNull(result);
		assertEquals("I", props.get("instructions"));
		assertEquals("P", props.get("inputs"));
		assertTrue(props.get("prologue") instanceof List);
		@SuppressWarnings("unchecked")
		List<Object> prologue = (List<Object>) props.get("prologue");
		assertEquals("x", prologue.get(0).toString());
	}

	@Test
	void setActData_whenStringInheritance_thenFormats() {
		Map<String, Object> props = new HashMap<>();
		props.put("inputs", "Hello %s");

		TomlParseResult toml = Toml.parse("inputs='World'");
		ActProcessor.setActData(props, toml);
		assertEquals("Hello World", props.get("inputs"));
	}

	@Test
	void applyActData_whenKnownKeys_thenAppliesToProcessorWithoutLoadingActs() throws Exception {
		PropertiesConfigurator config = new PropertiesConfigurator();
		ActProcessor p = new ActProcessor(tempDir.toFile(), config, "Any:Model") {
			@Override
			public void setDefaultPrompt(String defaultPrompt) {
				// bypass ActProcessor#setDefaultPrompt(String act) logic which loads act files
				super.setDefaultPrompt(defaultPrompt);
			}
		};

		// set initial prompt without invoking ActProcessor override
		setAiDefaultPrompt(p, "PROMPT");

		Map<String, Object> props = new HashMap<>();
		props.put("instructions", "INS");
		props.put("inputs", "Run: %s");
		props.put("gw.threads", "2");
		props.put("gw.excludes", "a,b");
		props.put("gw.nonRecursive", "true");
		props.put("custom.key", "VALUE");
		props.put("prologue", java.util.Arrays.asList("p1"));
		props.put("epilogue", java.util.Arrays.asList("e1"));

		p.applyActData(props);

		assertEquals("INS\n", p.getInstructions());
		assertEquals("Run: PROMPT", p.getDefaultPrompt());
		assertEquals(2, getPrivateInt(p, "degreeOfConcurrency"));
		assertArrayEquals(new String[] { "a", "b" }, p.getExcludes());
		assertTrue(p.isNonRecursive());
		assertEquals("VALUE", config.get("custom.key", null));
	}

	@Test
	void setActDir_whenNotDirectory_thenDoesNotChange() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		ActProcessor p = new ActProcessor(tempDir.toFile(), config, "Any:Model");

		File invalid = tempDir.resolve("nope").resolve("file.txt").toFile();
		assertNull(getPrivateFile(p, "actDir"));
		p.setActDir(invalid);
		assertNull(getPrivateFile(p, "actDir"));
	}

	private static void setAiDefaultPrompt(ActProcessor processor, String value) throws Exception {
		java.lang.reflect.Field f = AIFileProcessor.class.getDeclaredField("defaultPrompt");
		f.setAccessible(true);
		f.set(processor, value);
	}

	private static int getPrivateInt(Object target, String fieldName) throws Exception {
		Class<?> c = target.getClass();
		while (c != null) {
			try {
				java.lang.reflect.Field f = c.getDeclaredField(fieldName);
				f.setAccessible(true);
				return ((Number) f.get(target)).intValue();
			} catch (NoSuchFieldException e) {
				c = c.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}

	private static File getPrivateFile(Object target, String fieldName) {
		try {
			java.lang.reflect.Field f = ActProcessor.class.getDeclaredField(fieldName);
			f.setAccessible(true);
			return (File) f.get(target);
		} catch (Exception e) {
			return null;
		}
	}
}
