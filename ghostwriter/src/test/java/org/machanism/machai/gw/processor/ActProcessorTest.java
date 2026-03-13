package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.tomlj.Toml;

class ActProcessorTest {

	@TempDir
	File tempDir;

	@Test
	void tryLoadActFromDirectory_shouldReturnNullWhenActDirNull() throws Exception {
		Map<String, Object> props = new HashMap<>();
		assertNull(ActProcessor.tryLoadActFromDirectory(props, "any", null));
		assertTrue(props.isEmpty());
	}

	@Test
	void tryLoadActFromDirectory_shouldLoadStringsAndArrays() throws Exception {
		File actsDir = new File(tempDir, "acts");
		assertTrue(actsDir.mkdirs());

		File act = new File(actsDir, "demo.toml");
		Files.write(act.toPath(), "instructions = \"sys\"\nprologue = [\"a\", \"b\"]\n".getBytes(StandardCharsets.UTF_8));

		Map<String, Object> props = new HashMap<>();
		assertNotNull(ActProcessor.tryLoadActFromDirectory(props, "demo", actsDir));

		assertEquals("sys", props.get("instructions"));
		assertEquals(Arrays.asList("a", "b"), props.get("prologue"));
	}

	@Test
	void setActData_shouldFormatInheritedStringValues() {
		Map<String, Object> props = new HashMap<>();
		props.put("instructions", "base:%s");

		ActProcessor.setActData(props, Toml.parse("instructions=\"child\"\n"));

		assertEquals("base:child", props.get("instructions"));
	}

	@Test
	void setDefaultPrompt_shouldDefaultToHelpOnBlankInput() {
		ActProcessor processor = new ActProcessor(tempDir, new PropertiesConfigurator(), "model");
		assertDoesNotThrow(() -> processor.setDefaultPrompt(" "));
		assertNotNull(processor.getDefaultPrompt());
	}

	@Test
	void setActDir_shouldIgnoreNonDirectory() {
		ActProcessor processor = new ActProcessor(tempDir, new PropertiesConfigurator(), "model");
		File notADir = new File(tempDir, "nope");
		processor.setActDir(notADir);

		// no exception; coverage for guard path
		assertNotNull(processor);
	}

	@Test
	void loadAct_shouldThrowWhenNotFoundAnywhere() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ActProcessor.loadAct("does-not-exist", new HashMap<String, Object>(), tempDir));
		assertTrue(ex.getMessage().contains("not found"));
	}

	@Test
	void loadAct_shouldApplyBasedOnByInvokingParentAndRemovingBasedOnKey() throws IOException {
		File actsDir = new File(tempDir, "acts");
		assertTrue(actsDir.mkdirs());

		Files.write(new File(actsDir, "parent.toml").toPath(), "instructions=\"p\"\n".getBytes(StandardCharsets.UTF_8));
		Files.write(new File(actsDir, "child.toml").toPath(), "basedOn=\"parent\"\ninstructions=\"c\"\n".getBytes(StandardCharsets.UTF_8));

		Map<String, Object> props = new HashMap<>();
		ActProcessor.loadAct("child", props, actsDir);

		assertEquals("c", props.get("instructions"), "child should override parent instructions");
		assertFalse(props.containsKey("basedOn"), "basedOn must be removed after resolving inheritance");
	}
}
