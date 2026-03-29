package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GWCommandInternalLogicTest {

	@Test
	void resolveScanDirs_shouldDefaultToProjectDirAbsolutePath_whenNull() throws Exception {
		// Arrange
		GWCommand command = new GWCommand(Mockito.mock(LineReader.class));
		File projectDir = new File(".");
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(command, new Object[] { null, projectDir });

		// Assert
		assertArrayEquals(new String[] { projectDir.getAbsolutePath() }, result);
	}

	@Test
	void resolveScanDirs_shouldDefaultToProjectDirAbsolutePath_whenEmptyArray() throws Exception {
		// Arrange
		GWCommand command = new GWCommand(Mockito.mock(LineReader.class));
		File projectDir = new File(".");
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(command, new Object[] { new String[] {}, projectDir });

		// Assert
		assertArrayEquals(new String[] { projectDir.getAbsolutePath() }, result);
	}

	@Test
	void resolveScanDirs_shouldReturnProvidedScanDirs_whenNonEmpty() throws Exception {
		// Arrange
		GWCommand command = new GWCommand(Mockito.mock(LineReader.class));
		File projectDir = new File(".");
		String[] scanDirs = new String[] { "src/main/java", "src/test/java" };
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(command, new Object[] { scanDirs, projectDir });

		// Assert
		assertArrayEquals(scanDirs, result);
	}

	@Test
	void splitExcludes_shouldReturnNull_whenNull() throws Exception {
		// Arrange
		GWCommand command = new GWCommand(Mockito.mock(LineReader.class));
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		Object result = m.invoke(command, new Object[] { null });

		// Assert
		assertEquals(null, result);
	}

	@Test
	void splitExcludes_shouldSplitByComma_whenProvided() throws Exception {
		// Arrange
		GWCommand command = new GWCommand(Mockito.mock(LineReader.class));
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(command, "target,.git,.idea");

		// Assert
		assertArrayEquals(new String[] { "target", ".git", ".idea" }, result);
	}

	@Test
	void gwOptionsBuilderMethods_shouldFluentlySetFields() throws Exception {
		// Arrange
		Class<?> optionsClass = Class.forName("org.machanism.machai.cli.GWCommand$GwOptions");
		Constructor<?> ctor = optionsClass.getDeclaredConstructor();
		ctor.setAccessible(true);
		Object options = ctor.newInstance();

		Method threads = optionsClass.getDeclaredMethod("threads", int.class);
		Method model = optionsClass.getDeclaredMethod("model", String.class);
		Method instructions = optionsClass.getDeclaredMethod("instructions", String.class);
		Method excludes = optionsClass.getDeclaredMethod("excludes", String.class);
		Method logInputs = optionsClass.getDeclaredMethod("logInputs", Boolean.class);
		Method projectDir = optionsClass.getDeclaredMethod("projectDir", File.class);
		Method scanDirs = optionsClass.getDeclaredMethod("scanDirs", String[].class);
		threads.setAccessible(true);
		model.setAccessible(true);
		instructions.setAccessible(true);
		excludes.setAccessible(true);
		logInputs.setAccessible(true);
		projectDir.setAccessible(true);
		scanDirs.setAccessible(true);

		// Act
		Object r1 = threads.invoke(options, 3);
		Object r2 = model.invoke(options, "SomeProvider:some-model");
		Object r3 = instructions.invoke(options, "inst");
		Object r4 = excludes.invoke(options, "target");
		Object r5 = logInputs.invoke(options, Boolean.TRUE);
		Object r6 = projectDir.invoke(options, new File("."));
		Object r7 = scanDirs.invoke(options, new Object[] { new String[] { "." } });

		// Assert
		assertEquals(options, r1);
		assertEquals(options, r2);
		assertEquals(options, r3);
		assertEquals(options, r4);
		assertEquals(options, r5);
		assertEquals(options, r6);
		assertEquals(options, r7);
	}

	@Test
	void processingContextAndExecutionContext_constructors_shouldAssignFields() throws Exception {
		// Arrange
		Class<?> executionClass = Class.forName("org.machanism.machai.cli.GWCommand$ExecutionContext");
		Constructor<?> executionCtor = executionClass.getDeclaredConstructor(int.class, Boolean.class);
		executionCtor.setAccessible(true);
		Object execution = executionCtor.newInstance(2, Boolean.TRUE);

		Class<?> pcClass = Class.forName("org.machanism.machai.cli.GWCommand$ProcessingContext");
		Constructor<?> pcCtor = pcClass.getDeclaredConstructors()[0];
		pcCtor.setAccessible(true);

		Object ctx = pcCtor.newInstance(new File("."), "src", "Model", null, new String[] { "a" }, "inst", execution);

		// Act
		Object projectDir = getField(ctx, "projectDir");
		Object scanDir = getField(ctx, "scanDir");
		Object genaiValue = getField(ctx, "genaiValue");
		Object excludesArr = getField(ctx, "excludesArr");
		Object instructionsValue = getField(ctx, "instructionsValue");
		Object executionValue = getField(ctx, "execution");
		Object threads = getField(execution, "threads");
		Object logInputs = getField(execution, "logInputs");

		// Assert
		assertNotNull(projectDir);
		assertEquals("src", scanDir);
		assertEquals("Model", genaiValue);
		assertArrayEquals(new String[] { "a" }, (String[]) excludesArr);
		assertEquals("inst", instructionsValue);
		assertEquals(execution, executionValue);
		assertEquals(2, threads);
		assertEquals(Boolean.TRUE, logInputs);
	}

	private static Object getField(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
		java.lang.reflect.Field f = target.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(target);
	}
}
