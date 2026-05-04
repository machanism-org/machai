package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class ActProcessorAdditionalCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void tryLoadActFromDirectory_whenActsLocationIsHttp_andNetworkFails_propagatesIOException() {
		Map<String, Object> props = new HashMap<>();
		assertThrows(Exception.class,
				() -> ActProcessor.tryLoadActFromDirectory(props, "missing", "http://nonexistent.invalid"));
	}

	@Test
	void tryLoadActFromDirectory_whenActsLocationIsFileAndExists_populatesProperties() throws Exception {
		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Files.write(actsDir.resolve("x.toml"),
				("instructions='i'\n" + "inputs='p %s'\n").getBytes(StandardCharsets.UTF_8));

		Map<String, Object> props = new HashMap<>();

		assertNotNull(ActProcessor.tryLoadActFromDirectory(props, "x", actsDir.toString()));
		assertEquals("i", props.get("instructions"));
		assertEquals("p %s", props.get("inputs"));
	}

	@Test
	void setAct_whenEpisodeSelectionUsesStopSymbol_disablesNormalOrderAndSelectsEpisodes() throws Exception {
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		ActProcessor processor = new ActProcessor(tempDir.toFile(), configurator, "Any:Model");
		processor.setPrompts("episode-1", "episode-2", "episode-3");

		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Files.write(actsDir.resolve("custom.toml"),
				("instructions='i'\n" + "inputs=['first %s','second %s','third %s']\n")
						.getBytes(StandardCharsets.UTF_8));
			processor.setActsLocation(actsDir.toString());

		processor.setAct("custom#2!");

		assertArrayEquals(new String[] { "first episode-1", "second episode-1", "third episode-1" },
				processor.getPrompts());
		assertEquals("second episode-1", processor.getDefaultPrompt());
	}
}
