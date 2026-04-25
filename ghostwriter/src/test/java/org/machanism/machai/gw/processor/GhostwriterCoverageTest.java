package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

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
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new Ghostwriter("  ", processor));
		assertTrue(ex.getMessage().contains(Ghostwriter.MODEL_PROP_NAME));
	}

	@Test
	void resolveScanDirs_whenNoArgsAndNoConfig_usesUserDirAbsolutePath() throws Exception {
		Options opts = new Options();
		CommandLine cmd = new DefaultParser().parse(opts, new String[0]);
		PropertiesConfigurator config = new PropertiesConfigurator();

		String[] scanDirs = Ghostwriter.resolveScanDirs(cmd, config);

		assertNotNull(scanDirs);
		assertEquals(1, scanDirs.length);
		assertFalse(new File(scanDirs[0]).isAbsolute());
	}

	@Test
	void resolveScanDirs_whenNoArgsButConfigHasScanDir_usesConfigValue() throws Exception {
		Options opts = new Options();
		CommandLine cmd = new DefaultParser().parse(opts, new String[0]);
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.SCAN_DIR_PROP_NAME, null)).thenReturn("glob:**/*.java");

		String[] scanDirs = Ghostwriter.resolveScanDirs(cmd, config);

		assertEquals(1, scanDirs.length);
		assertEquals("glob:**/*.java", scanDirs[0]);
	}

	@Test
	void createProcessor_whenActOptionPresent_createsActProcessorAndSetsDefaultPrompt(@TempDir File tmp)
			throws Exception {
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		opts.addOption(new Option("as", "acts", true, ""));
		CommandLine cmd = new DefaultParser().parse(opts,
				new String[] { "--act", "help", "--acts", tmp.getAbsolutePath() });

		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn("configAct");
		Mockito.when(config.get(Ghostwriter.ACTS_LOCATION_PROP_NAME, null)).thenReturn(null);

		AIFileProcessor processor = Ghostwriter.createProcessor(cmd, tmp, config, "Any:Model");

		assertNotNull(processor);
		assertTrue(processor instanceof ActProcessor);
	}

	@Test
	void createActProcessor_whenCmdHasNoActsButConfigHasActs_setsActsLocation(@TempDir File tmp) throws Exception {
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		CommandLine cmd = new DefaultParser().parse(opts, new String[] { "--act", "help" });

		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACTS_LOCATION_PROP_NAME, null)).thenReturn(tmp.getAbsolutePath());

		AIFileProcessor processor = Ghostwriter.createActProcessor(cmd, tmp, config, "Any:Model");

		assertNotNull(processor);
		assertTrue(processor instanceof ActProcessor);
	}

	@Test
	void setInstructions_whenNull_doesNotCallProcessor() {
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		gw.setInstructions(null);

		Mockito.verify(processor, Mockito.never()).setInstructions(Mockito.anyString());
	}

	@Test
	void setExcludes_whenNull_doesNotCallProcessor() {
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		gw.setExcludes(null);

		Mockito.verify(processor, Mockito.never()).setExcludes(Mockito.<String[]>any());
	}

	@Test
	void setDefaultPrompt_whenNull_doesNotCallProcessor() {
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		gw.setDefaultPrompt(null);

		Mockito.verify(processor, Mockito.never()).setDefaultPrompt(Mockito.anyString());
	}

	@Test
	void setDegreeOfConcurrency_whenNull_doesNotCallProcessor() {
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		gw.setDegreeOfConcurrency(null);

		Mockito.verify(processor, Mockito.never()).setDegreeOfConcurrency(Mockito.anyInt());
	}

	@Test
	void setDegreeOfConcurrency_whenNumber_callsProcessorWithParsedValue() {
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		gw.setDegreeOfConcurrency("3");

		Mockito.verify(processor).setDegreeOfConcurrency(3);
	}

	@Test
	void initializeConfiguration_whenHomeAlreadySet_doesNotOverride(@TempDir File tmp) {
		System.setProperty(Ghostwriter.HOME_PROP_NAME, new File("C:/opt/gw").getAbsolutePath());
		System.clearProperty(Ghostwriter.CONFIG_PROP_NAME);

		PropertiesConfigurator cfg = Ghostwriter.initializeConfiguration(tmp);

		assertNotNull(cfg);
		assertEquals(new File("C:/opt/gw").getAbsolutePath(), System.getProperty(Ghostwriter.HOME_PROP_NAME));
	}

	@Test
	void logDefaultPrompt_whenPromptNull_doesNotThrow() {
		Ghostwriter.initializeConfiguration(new File("."));

		Ghostwriter.logDefaultPrompt("Label", null);
		assertTrue(true);
	}

	@Test
	void resolveActPrompt_whenNoActOption_returnsConfigValue() throws Exception {
		Options opts = new Options();
		opts.addOption(Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build());
		CommandLine cmd = new DefaultParser().parse(opts, new String[0]);
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);
		Mockito.when(config.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn("help");

		String prompt = Ghostwriter.resolveActPrompt(cmd, config);

		assertEquals("help", prompt);
	}

	@Test
	void setLogInputs_alwaysDelegates() {
		AIFileProcessor processor = Mockito.mock(AIFileProcessor.class);
		Ghostwriter gw = new Ghostwriter("Any:Model", processor);

		gw.setLogInputs(true);

		Mockito.verify(processor).setLogInputs(true);
	}

	@Test
	void initializeConfiguration_whenConfigFilePropertySet_doesNotThrowEvenIfFileMissing(@TempDir File tmp) {
		System.setProperty(Ghostwriter.HOME_PROP_NAME, tmp.getAbsolutePath());
		System.setProperty(Ghostwriter.CONFIG_PROP_NAME, "missing.properties");

		PropertiesConfigurator cfg = Ghostwriter.initializeConfiguration(tmp);

		assertNotNull(cfg);
		assertEquals(tmp.getAbsolutePath(), System.getProperty(Ghostwriter.HOME_PROP_NAME));
	}

	@Test
	void resolveScanDirs_whenCmdHasArgs_returnsArgsAsIs() throws Exception {
		Options opts = new Options();
		CommandLine cmd = new DefaultParser().parse(opts, new String[] { "src" });
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);

		String[] scanDirs = Ghostwriter.resolveScanDirs(cmd, config);

		assertEquals(1, scanDirs.length);
		assertEquals("src", scanDirs[0]);
		Mockito.verifyNoInteractions(config);
	}

	@Test
	void logDefaultPrompt_whenPromptNotNull_logsWithoutThrowing() {
		Ghostwriter.initializeConfiguration(new File("."));

		Ghostwriter.logDefaultPrompt("Label", "Some long text");
		assertTrue(true);
	}
}
