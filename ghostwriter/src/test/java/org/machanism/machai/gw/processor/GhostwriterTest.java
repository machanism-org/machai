package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class GhostwriterTest {

	@TempDir
	Path tempDir;

	@Test
	void constructor_whenBlankGenai_thenThrows() {
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		assertThrows(IllegalArgumentException.class, () -> new Ghostwriter(" ", processor));
	}

	@Test
	void resolveScanDirs_whenNoneProvidedAndNoneInConfig_thenDefaultsToUserDir() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		CommandLine cmd = parse(new String[] {}, new Options());
		String[] dirs = Ghostwriter.resolveScanDirs(cmd, config);
		assertNotNull(dirs);
		assertEquals(1, dirs.length);
		assertNotNull(dirs[0]);
	}

	@Test
	void resolveScanDirs_whenConfigProvides_thenUsesConfig() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		config.set(Ghostwriter.SCAN_DIR_PROP_NAME, "src");
		CommandLine cmd = parse(new String[] {}, new Options());
		String[] dirs = Ghostwriter.resolveScanDirs(cmd, config);
		assertArrayEquals(new String[] { "src" }, dirs);
	}

	@Test
	void logDefaultPrompt_whenNull_thenNoThrow() {
		assertDoesNotThrow(() -> Ghostwriter.logDefaultPrompt("X", null));
	}

	@Test
	void createProcessor_whenActOption_thenActProcessor() throws Exception {
		Options options = new Options();
		options.addOption("a", "act", true, "act");
		CommandLine cmd = new DefaultParser().parse(options, new String[] { "--act", "help" });
		PropertiesConfigurator config = new PropertiesConfigurator();
		Path actsDir = Files.createDirectory(tempDir.resolve("acts"));
		config.set(Ghostwriter.ACTS_LOCATION_PROP_NAME, actsDir.toString());

		Ghostwriter.initializeConfiguration(tempDir.toFile());

		AIFileProcessor p = Ghostwriter.createProcessor(cmd, tempDir.toFile(), config, "Any:Model");
		assertInstanceOf(ActProcessor.class, p);
	}

	@Test
	void createProcessor_whenNoActOption_thenGuidanceProcessor() throws Exception {
		Options options = new Options();
		CommandLine cmd = new DefaultParser().parse(options, new String[] {});
		PropertiesConfigurator config = new PropertiesConfigurator();

		Ghostwriter.initializeConfiguration(tempDir.toFile());

		AIFileProcessor p = Ghostwriter.createProcessor(cmd, tempDir.toFile(), config, "Any:Model");
		assertInstanceOf(GuidanceProcessor.class, p);
	}

	private static CommandLine parse(String[] args, Options options) {
		try {
			return new DefaultParser().parse(options, args);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
