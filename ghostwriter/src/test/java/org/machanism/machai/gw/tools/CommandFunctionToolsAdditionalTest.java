package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFunctionToolsAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void resolveWorkingDirShouldHandleDotTraversalAbsoluteAndNullValues() throws Exception {
		CommandFunctionTools tools = new CommandFunctionTools();
		File projectDir = tempDir.toFile();

		File dotResult = tools.resolveWorkingDir(projectDir, ".");
		File childResult = tools.resolveWorkingDir(projectDir, "child");
		File traversalResult = tools.resolveWorkingDir(projectDir, "..");
		File absoluteResult = tools.resolveWorkingDir(projectDir, projectDir.getAbsolutePath());
		File nullResult = tools.resolveWorkingDir(null, ".");

		assertEquals(projectDir.getCanonicalFile(), dotResult);
		assertEquals(new File(projectDir, "child").getCanonicalFile(), childResult);
		assertEquals(null, traversalResult);
		assertEquals(null, absoluteResult);
		assertEquals(null, nullResult);
	}

	@Test
	void commandTerminationExceptionShouldExposeExitCode() {
		ProcessTerminationException ex = new ProcessTerminationException(
				"stop", 7);
		assertEquals(7, ex.getExitCode());
	}

	@Test
	void executorServiceAutoCloseableShouldShutdownExecutor() throws Exception {
		Class<?> clazz = Class
				.forName("org.machanism.machai.gw.tools.CommandFunctionTools$ExecutorServiceAutoCloseable");
		Constructor<?> constructor = clazz.getDeclaredConstructor(java.util.concurrent.ExecutorService.class);
		constructor.setAccessible(true);
		java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
		Object wrapper = constructor.newInstance(executor);

		clazz.getDeclaredMethod("close").invoke(wrapper);

		assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
	}
}
