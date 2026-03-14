package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GWCommandTest {

	private final java.io.InputStream originalIn = System.in;

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
	}

	@Test
	void resolveScanDirs_shouldReturnRootDirAbsolutePath_whenNullOrEmpty() throws Exception {
		// Arrange
		GWCommand cmd = new GWCommand();
		File rootDir = new File(".").getAbsoluteFile();
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);

		// Act
		String[] resultWhenNull = (String[]) m.invoke(cmd, new Object[] { null, rootDir });
		String[] resultWhenEmpty = (String[]) m.invoke(cmd, new Object[] { new String[] {}, rootDir });

		// Assert
		assertArrayEquals(new String[] { rootDir.getAbsolutePath() }, resultWhenNull);
		assertArrayEquals(new String[] { rootDir.getAbsolutePath() }, resultWhenEmpty);
	}

	@Test
	void resolveScanDirs_shouldReturnProvidedScanDirs_whenNotEmpty() throws Exception {
		// Arrange
		GWCommand cmd = new GWCommand();
		File rootDir = new File(".").getAbsoluteFile();
		String[] scanDirs = new String[] { "a", "b" };
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(cmd, new Object[] { scanDirs, rootDir });

		// Assert
		assertArrayEquals(scanDirs, result);
	}

	@Test
	void splitExcludes_shouldReturnNull_whenExcludesIsNull() throws Exception {
		// Arrange
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		Object result = m.invoke(cmd, new Object[] { null });

		// Assert
		assertNull(result);
	}

	@Test
	void splitExcludes_shouldSplitByComma_whenExcludesIsProvided() throws Exception {
		// Arrange
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(cmd, "target,.git");

		// Assert
		assertArrayEquals(new String[] { "target", ".git" }, result);
	}

	@Test
	void readText_shouldReturnNull_whenNoInputProvided() throws Exception {
		// Arrange
		System.setIn(new ByteArrayInputStream(new byte[0]));
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("readText", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, "prompt");

		// Assert
		assertNull(result);
	}

	@Test
	void readText_shouldReturnTextWithoutTrailingNewline_whenInputProvided() throws Exception {
		// Arrange
		String input = "line1\r\nline2\r\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("readText", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, "prompt");

		// Assert
		assertEquals("line1\nline2", result);
	}
}
