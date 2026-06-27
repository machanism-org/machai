package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
	void setAct_whenEpisodeSelectionAndPromptProvided_appliesRuntimeState() throws Exception {
		// Arrange
		Path actsDir = Files.createDirectories(tempDir.resolve("acts"));
		Files.write(actsDir.resolve("sample.toml"), Arrays.asList(
				"prompt = \"fallback\"",
				"instructions = \"custom instructions\"",
				"inputs = [\"# First\\nPrompt %s\", \"# Second\\nSecond %s\"]",
				"gw.threads = 2",
				"gw.excludes = \"a,b\"",
				"gw.nonRecursive = true",
				"gw.interactive = true",
				"gw.model = \"Configured:Model\"",
				"custom.value = \"custom-setting\""), StandardCharsets.UTF_8);
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		ActProcessor processor = new ActProcessor(tempDir.toFile(), "Initial:Model", configurator);
		processor.setActsLocation(actsDir.toString());

		// Act
		processor.setAct("sample#2! user prompt");

		// Assert
		assertEquals(
				"You are a highly skilled software engineer and developer, with expertise in all major programming languages, frameworks, and platforms.",
				processor.getInstructions());
		assertEquals("# First\nPrompt user prompt", processor.getDefaultPrompt());
		assertEquals(2, getEpisodes(processor).getEpisodes().size());
		assertFalse(getEpisodes(processor).isRegularOrder());
		assertTrue(getDisableNormalOrder(processor));
		assertEquals(0, getDegreeOfConcurrency(processor));
		assertTrue(processor.isNonRecursive());
		assertTrue(processor.isInteractive());
		assertArrayEquals(new String[] { "a", "b" }, processor.getExcludes());
		assertEquals("Configured:Model", processor.getModel());
		assertEquals(processor.getConfigurator().get(GWConstants.MODEL_PROP_NAME), "Initial:Model");
		assertEquals("custom-setting", processor.getConfigurator().get("custom.value", null));
	}

	@Test
	void applyActData_whenListInputsProvided_setsDefaultPromptAndPreservesExplicitState() throws Exception {
		// Arrange
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		configurator.set(GWConstants.INPUTS_PROPERTY_NAME, "inherited");
		ActProcessor processor = new ActProcessor(tempDir.toFile(), "GenAI:Model", configurator);
		processor.setInstructions("existing instructions");
		processor.setModel("Already:Set");

		Map<String, Object> properties = new HashMap<>();
		properties.put(GWConstants.INSTRUCTIONS_PROP_NAME, "new instructions");
		properties.put(GWConstants.INPUTS_PROPERTY_NAME, Arrays.asList("episode-%s", "tail"));
		properties.put(GWConstants.MODEL_PROP_NAME, "ignored-model");

		// Act
		processor.applyActData(properties);

		// Assert
		assertEquals("existing instructions\n", processor.getInstructions());
		assertEquals("episode-%s", processor.getDefaultPrompt());
		assertEquals(Arrays.asList("episode-inherited", "tail"), getEpisodes(processor).getEpisodes());
		assertEquals("Already:Set", processor.getModel());
	}

	@Test
	void setActsLocation_whenDirectoryAndHttpConfigured_updatesConfigurator_andMissingDirectoryFails() {
		// Arrange
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		ActProcessor processor = new ActProcessor(tempDir.toFile(), "Any:Model", configurator);
		File validDir = tempDir.resolve("acts-dir").toFile();
		assertTrue(validDir.mkdirs());

		// Act
		processor.setActsLocation(validDir.getPath());
		processor.setActsLocation("https://example.org/acts");

		// Assert
		assertEquals("https://example.org/acts",
				processor.getConfigurator().get(GWConstants.ACTS_LOCATION_PROP_NAME, null));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> processor.setActsLocation(tempDir.resolve("missing-dir").toString()));
		assertNotNull(exception);
	}

	@Test
	void loadAct_whenInheritanceUsed_mergesDirectoryAndClasspathLookups() throws Exception {
		// Arrange
		Path actsDir = Files.createDirectories(tempDir.resolve("inheritance-acts"));
		Files.write(actsDir.resolve("parent.toml"), Arrays.asList(
				"gw.instructions = \"base\"",
				"inputs = [\"base-%s\", \"second\"]",
				"gw.threads = 4"), StandardCharsets.UTF_8);
		Files.write(actsDir.resolve("child.toml"), Arrays.asList(
				"basedOn = \"parent\"",
				"inputs = [\"child\", \"override-%s\"]",
				"gw.nonRecursive = true"), StandardCharsets.UTF_8);
		Map<String, Object> inherited = new HashMap<>();

		// Act
		ActProcessor.loadAct("child", inherited, actsDir.toString());
		TomlParseResult missingClasspath = ActProcessor.tryLoadActFromClasspath(new HashMap<>(),
				"definitely-missing-act");
		TomlParseResult directoryToml = ActProcessor.tryLoadActFromDirectory(new HashMap<>(), "child",
				actsDir.toString());

		// Assert
		assertEquals("base", inherited.get(GWConstants.INSTRUCTIONS_PROP_NAME));
		assertEquals(Arrays.asList("child", "override-second"), inherited.get(GWConstants.INPUTS_PROPERTY_NAME));
		assertNull(inherited.get(GWConstants.THREADS_PROP_NAME));
		assertEquals("true", inherited.get(GWConstants.NONRECURSIVE_PROP_NAME));
		assertNull(missingClasspath);
		assertNotNull(directoryToml);
		assertThrows(ActNotFound.class, () -> ActProcessor.loadAct("missing", new HashMap<>(), actsDir.toString()));
	}

	@Test
	void setActData_whenTomlContainsSupportedTypes_mergesExistingValues() {
		// Arrange
		Map<String, Object> properties = new HashMap<>();
		properties.put("greeting", "Hello %s");
		properties.put("inputs", Collections.singletonList("%s world"));
		TomlParseResult toml = Toml.parse(String.join("\n",
				"greeting = \"there\"",
				"flag = true",
				"count = 3",
				"score = 2.5",
				"inputs = [\"big\", \"tail\"]"));

		// Act
		ActProcessor.setActData(properties, toml);

		// Assert
		assertEquals("Hello there", properties.get("greeting"));
		assertEquals("true", properties.get("flag"));
		assertNull(properties.get("count"));
		assertEquals("2.5", properties.get("score"));
		assertEquals(Arrays.asList("big world", "tail"), properties.get("inputs"));
	}

	private static Episodes getEpisodes(ActProcessor processor) throws Exception {
		Field field = ActProcessor.class.getDeclaredField("episodes");
		field.setAccessible(true);
		return (Episodes) field.get(processor);
	}

	private static boolean getDisableNormalOrder(ActProcessor processor) throws Exception {
		Field field = ActProcessor.class.getDeclaredField("disableNormalOrder");
		field.setAccessible(true);
		return field.getBoolean(processor);
	}

	private static int getDegreeOfConcurrency(ActProcessor processor) throws Exception {
		Field field = AbstractFileProcessor.class.getDeclaredField("degreeOfConcurrency");
		field.setAccessible(true);
		return field.getInt(processor);
	}
}
