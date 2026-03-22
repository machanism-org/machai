package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.mockito.Mockito;

/**
 * Additional focused tests to cover package-private/private logic in {@link GWCommand}.
 *
 * <p>Uses reflection intentionally to validate behaviors of small resolver helpers
 * and internal value objects without widening visibility in production code.
 */
class GWCommandCoverageTest {

	private final InputStream originalIn = System.in;

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
	}

	@Test
	void resolveInstructions_whenNull_returnsConfigValue() throws Exception {
		// Arrange
		ConfigCommand.config.set(org.machanism.machai.gw.processor.Ghostwriter.GW_INSTRUCTIONS_PROP_NAME, "from-config");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);

		Method m = GWCommand.class.getDeclaredMethod("resolveInstructions", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("from-config", result);
	}

	@Test
	void resolveGuidance_whenNull_returnsConfigValue() throws Exception {
		// Arrange
		ConfigCommand.config.set(org.machanism.machai.gw.processor.Ghostwriter.GW_GUIDANCE_PROP_NAME,
				"guidance-from-config");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveGuidance", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("guidance-from-config", result);
	}

	@Test
	void splitExcludes_whenNull_returnsNull_andWhenCsv_splits() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		String[] resultNull = (String[]) m.invoke(cmd, new Object[] { null });
		String[] resultCsv = (String[]) m.invoke(cmd, "target,.git");

		// Assert
		assertNull(resultNull);
		assertArrayEquals(new String[] { "target", ".git" }, resultCsv);
	}

	@Test
	void resolveScanDirs_whenNullOrEmpty_returnsRootDirAbsolutePath() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);
		File rootDir = new File(".").getAbsoluteFile();

		// Act
		String[] resultNull = (String[]) m.invoke(cmd, null, rootDir);
		String[] resultEmpty = (String[]) m.invoke(cmd, new Object[] { new String[0], rootDir });

		// Assert
		assertArrayEquals(new String[] { rootDir.getAbsolutePath() }, resultNull);
		assertArrayEquals(new String[] { rootDir.getAbsolutePath() }, resultEmpty);
	}

	@Test
	void loadMachaiPropertiesConfig_whenFileMissing_doesNotThrow_andReturnsNonNullConfig() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("loadMachaiPropertiesConfig");
		m.setAccessible(true);

		// Act
		Object cfg = m.invoke(cmd);

		// Assert
		assertNotNull(cfg);
		assertEquals("org.machanism.macha.core.commons.configurator.PropertiesConfigurator", cfg.getClass().getName());
	}

	@Test
	void internalValueObjects_constructorsAssignFields() throws Exception {
		// Arrange
		Class<?> promptCtxClass = Class.forName("org.machanism.machai.cli.GWCommand$PromptContext");
		Constructor<?> promptCtor = promptCtxClass.getDeclaredConstructor(String.class, String.class);
		promptCtor.setAccessible(true);

		Class<?> execCtxClass = Class.forName("org.machanism.machai.cli.GWCommand$ExecutionContext");
		Constructor<?> execCtor = execCtxClass.getDeclaredConstructor(int.class, Boolean.class);
		execCtor.setAccessible(true);

		Class<?> processingCtxClass = Class.forName("org.machanism.machai.cli.GWCommand$ProcessingContext");
		Constructor<?> processingCtor = processingCtxClass.getDeclaredConstructor(File.class, String.class, String.class,
				org.machanism.macha.core.commons.configurator.PropertiesConfigurator.class, String[].class, promptCtxClass,
				execCtxClass);
		processingCtor.setAccessible(true);

		Object promptCtx = promptCtor.newInstance("instr", "guid");
		Object execCtx = execCtor.newInstance(3, Boolean.TRUE);
		Object propsCfg = new org.machanism.macha.core.commons.configurator.PropertiesConfigurator();
		String[] excludes = new String[] { "a", "b" };
		File root = new File(".");
		String scanDir = "src";
		String model = "X:Y";

		// Act
		Object processingCtx = processingCtor.newInstance(root, scanDir, model, propsCfg, excludes, promptCtx, execCtx);

		// Assert (via reflective field reads)
		assertEquals("instr", getField(promptCtx, "instructionsValue"));
		assertEquals("guid", getField(promptCtx, "defaultGuidance"));
		assertEquals(3, getField(execCtx, "threads"));
		assertEquals(Boolean.TRUE, getField(execCtx, "logInputs"));

		assertEquals(root, getField(processingCtx, "projectDir"));
		assertEquals(scanDir, getField(processingCtx, "scanDir"));
		assertEquals(model, getField(processingCtx, "genaiValue"));
		assertSame(propsCfg, getField(processingCtx, "config"));
		assertArrayEquals(excludes, (String[]) getField(processingCtx, "excludesArr"));
		assertSame(promptCtx, getField(processingCtx, "prompts"));
		assertSame(execCtx, getField(processingCtx, "execution"));
	}

	private static Object getField(Object target, String fieldName) throws Exception {
		var f = target.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(target);
	}

	@Test
	void init_isNoOpButCallable() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);

		// Act/Assert
		assertDoesNotThrow(cmd::init);
	}

	@Test
	void gw_whenDownstreamThrowsException_isHandledAndDoesNotThrow() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);

		// Act/Assert
		// Use a clearly invalid scan path to increase the likelihood of downstream IOException.
		assertDoesNotThrow(() -> cmd.gw(1, null, null, null, null, null, null, new String[] { "Z:\\this-drive-should-not-exist" }));
	}

	@Test
	void processTerminationException_isConstructible_andExitCodeReadable() throws Exception {
		// Arrange
		Constructor<ProcessTerminationException> ctor = ProcessTerminationException.class.getDeclaredConstructor(String.class,
				int.class);
		ctor.setAccessible(true);

		// Act
		ProcessTerminationException ex = ctor.newInstance("bye", 7);

		// Assert
		assertEquals(7, ex.getExitCode());
		assertEquals("bye", ex.getMessage());
	}
}
