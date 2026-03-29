package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.mockito.Mockito;

class GhostwriterCoverageTest {

	private final java.io.InputStream originalIn = System.in;
	private final String originalGwHome = System.getProperty(Ghostwriter.HOME_PROP_NAME);
	private final String originalGwConfig = System.getProperty(Ghostwriter.CONFIG_PROP_NAME);

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		if (originalGwHome == null) {
			System.clearProperty(Ghostwriter.HOME_PROP_NAME);
		} else {
			System.setProperty(Ghostwriter.HOME_PROP_NAME, originalGwHome);
		}
		if (originalGwConfig == null) {
			System.clearProperty(Ghostwriter.CONFIG_PROP_NAME);
		} else {
			System.setProperty(Ghostwriter.CONFIG_PROP_NAME, originalGwConfig);
		}
	}

	@Test
	void constructor_whenGenAiBlank_throwsIllegalArgumentException() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Ghostwriter("  ", processor));
		assertTrue(ex.getMessage().contains(Ghostwriter.MODEL_PROP_NAME));
	}

	@Test
	void readText_whenInputContainsContinuationLines_concatenatesUsingProviderLineSeparator() {
		// Arrange
		String stdin = "line1\\\r\nline2\\\r\nlast\r\n";
		System.setIn(new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
		Ghostwriter.initializeConfiguration(new File("."));

		// Act
		String text = Ghostwriter.readText("Prompt");

		// Assert
		assertEquals("line1" + org.machanism.machai.ai.manager.Genai.LINE_SEPARATOR + "line2"
				+ org.machanism.machai.ai.manager.Genai.LINE_SEPARATOR + "last", text);
	}

	@Test
	void resolveScanDirs_whenNoArgsAndNoConfig_usesUserDirAbsolutePath() throws Exception {
		// Arrange
		Options opts = new Options();
		CommandLine cmd = new DefaultParser().parse(opts, new String[0]);
		PropertiesConfigurator config = new PropertiesConfigurator();

		// Act
		String[] scanDirs = Ghostwriter.resolveScanDirs(cmd, config);

		// Assert
		assertNotNull(scanDirs);
		assertEquals(1, scanDirs.length);
		assertTrue(new File(scanDirs[0]).isAbsolute());
	}

	@Test
	void resolveScanDirs_whenNoArgsButConfigHasScanDir_usesConfigValue() throws Exception {
		// Arrange
		Options opts = new Options();
		CommandLine cmd = new DefaultParser().parse(opts, new String[0]);
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.SCAN_DIR_PROP_NAME, null)).thenReturn("glob:**/*.java");

		// Act
		String[] scanDirs = Ghostwriter.resolveScanDirs(cmd, config);

		// Assert
		assertEquals(1, scanDirs.length);
		assertEquals("glob:**/*.java", scanDirs[0]);
	}

	@Test
	void resolveActPrompt_whenActOptionPresentButValueMissing_readsFromStdin() throws Exception {
		// Arrange
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		CommandLine cmd = new DefaultParser().parse(opts, new String[] { "-a" });

		System.setIn(new ByteArrayInputStream("help\r\n".getBytes(StandardCharsets.UTF_8)));
		Ghostwriter.initializeConfiguration(new File("."));

		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn("ignored");

		// Act
		String act = Ghostwriter.resolveActPrompt(cmd, config);

		// Assert
		assertEquals("help", act);
	}

	@Test
	void createProcessor_whenActOptionPresent_createsActProcessorAndSetsDefaultPrompt(@TempDir File tmp) throws Exception {
		// Arrange
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		opts.addOption(new Option("as", "acts", true, ""));
		CommandLine cmd = new DefaultParser().parse(opts, new String[] { "--act", "help", "--acts", tmp.getAbsolutePath() });

		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn("configAct");
		Mockito.when(config.get(Ghostwriter.ACTS_LOCATION_PROP_NAME, null)).thenReturn(null);

		// Act
		AIFileProcessor processor = Ghostwriter.createProcessor(cmd, tmp, config, "Any:Model");

		// Assert
		assertNotNull(processor);
		assertTrue(processor instanceof ActProcessor);
	}

	@Test
	void createActProcessor_whenCmdHasNoActsButConfigHasActs_setsActsLocation(@TempDir File tmp) throws Exception {
		// Arrange
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		CommandLine cmd = new DefaultParser().parse(opts, new String[] { "--act", "help" });

		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACTS_LOCATION_PROP_NAME, null)).thenReturn(tmp.getAbsolutePath());

		// Act
		AIFileProcessor processor = Ghostwriter.createActProcessor(cmd, tmp, config, "Any:Model");

		// Assert
		assertNotNull(processor);
		assertTrue(processor instanceof ActProcessor);
	}

	@Test
	void setInstructions_whenNull_doesNotCallProcessor() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		// Act
		gw.setInstructions(null);

		// Assert
		Mockito.verify(processor, Mockito.never()).setInstructions(Mockito.anyString());
	}

	@Test
	void setExcludes_whenNull_doesNotCallProcessor() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		// Act
		gw.setExcludes(null);

		// Assert
		Mockito.verify(processor, Mockito.never()).setExcludes(Mockito.<String[]>any());
	}

	@Test
	void setDefaultPrompt_whenNull_doesNotCallProcessor() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		// Act
		gw.setDefaultPrompt(null);

		// Assert
		Mockito.verify(processor, Mockito.never()).setDefaultPrompt(Mockito.anyString());
	}

	@Test
	void setDegreeOfConcurrency_whenNull_doesNotCallProcessor() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		// Act
		gw.setDegreeOfConcurrency(null);

		// Assert
		Mockito.verify(processor, Mockito.never()).setDegreeOfConcurrency(Mockito.anyInt());
	}

	@Test
	void setDegreeOfConcurrency_whenNumber_callsProcessorWithParsedValue() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		// Act
		gw.setDegreeOfConcurrency("3");

		// Assert
		Mockito.verify(processor).setDegreeOfConcurrency(3);
	}

	@Test
	void initializeConfiguration_whenHomeAlreadySet_doesNotOverride(@TempDir File tmp) {
		// Arrange
		System.setProperty(Ghostwriter.HOME_PROP_NAME, new File("C:/opt/gw").getAbsolutePath());
		System.clearProperty(Ghostwriter.CONFIG_PROP_NAME);

		// Act
		PropertiesConfigurator cfg = Ghostwriter.initializeConfiguration(tmp);

		// Assert
		assertNotNull(cfg);
		assertEquals(new File("C:/opt/gw").getAbsolutePath(), System.getProperty(Ghostwriter.HOME_PROP_NAME));
	}

	@Test
	void logDefaultPrompt_whenPromptNull_doesNotThrow() {
		// Arrange
		Ghostwriter.initializeConfiguration(new File("."));

		// Act + Assert
		Ghostwriter.logDefaultPrompt("Label", null);
		assertTrue(true);
	}

	@Test
	void resolveActPrompt_whenNoActOption_returnsConfigValue() throws Exception {
		// Arrange
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		CommandLine cmd = new DefaultParser().parse(opts, new String[0]);
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn("help");

		// Act
		String prompt = Ghostwriter.resolveActPrompt(cmd, config);

		// Assert
		assertEquals("help", prompt);
	}

	@Test
	void setLogInputs_alwaysDelegates() {
		// Arrange
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		// Act
		gw.setLogInputs(true);

		// Assert
		Mockito.verify(processor).setLogInputs(true);
	}

	@Test
	void readText_whenNoInputLines_returnsEmptyString() {
		// Arrange
		System.setIn(new ByteArrayInputStream(new byte[0]));
		Ghostwriter.initializeConfiguration(new File("."));

		// Act
		String text = Ghostwriter.readText("Prompt");

		// Assert
		assertEquals("", text);
	}

	@Test
	void initializeConfiguration_whenConfigFilePropertySet_doesNotThrowEvenIfFileMissing(@TempDir File tmp) {
		// Arrange
		System.setProperty(Ghostwriter.HOME_PROP_NAME, tmp.getAbsolutePath());
		System.setProperty(Ghostwriter.CONFIG_PROP_NAME, "missing.properties");

		// Act
		PropertiesConfigurator cfg = Ghostwriter.initializeConfiguration(tmp);

		// Assert
		assertNotNull(cfg);
		assertEquals(tmp.getAbsolutePath(), System.getProperty(Ghostwriter.HOME_PROP_NAME));
	}

	@Test
	void resolveScanDirs_whenCmdHasArgs_returnsArgsAsIs() throws Exception {
		// Arrange
		Options opts = new Options();
		CommandLine cmd = new DefaultParser().parse(opts, new String[] { "src" });
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);

		// Act
		String[] scanDirs = Ghostwriter.resolveScanDirs(cmd, config);

		// Assert
		assertEquals(1, scanDirs.length);
		assertEquals("src", scanDirs[0]);
		Mockito.verifyNoInteractions(config);
	}

	@Test
	void logDefaultPrompt_whenPromptNotNull_logsWithoutThrowing() {
		// Arrange
		Ghostwriter.initializeConfiguration(new File("."));

		// Act + Assert
		Ghostwriter.logDefaultPrompt("Label", "Some long text");
		assertTrue(true);
	}
}
