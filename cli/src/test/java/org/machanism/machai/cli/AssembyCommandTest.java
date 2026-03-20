package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class AssembyCommandTest {

	@TempDir
	Path tempDir;

	@Test
	void getQueryFromFile_shouldReturnOriginalQuery_whenFileDoesNotExist() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		AssembyCommand cmd = new AssembyCommand(reader);
		Method m = AssembyCommand.class.getDeclaredMethod("getQueryFromFile", String.class);
		m.setAccessible(true);
		String query = tempDir.resolve("missing-file.txt").toString();

		// Act
		String result = (String) m.invoke(cmd, query);

		// Assert
		assertEquals(query, result);
	}

	@Test
	void getQueryFromFile_shouldLoadContents_whenFileExists() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		AssembyCommand cmd = new AssembyCommand(reader);
		Method m = AssembyCommand.class.getDeclaredMethod("getQueryFromFile", String.class);
		m.setAccessible(true);

		Path file = tempDir.resolve("query.txt");
		Files.writeString(file, "hello from file", StandardCharsets.UTF_8);

		// Act
		String result = (String) m.invoke(cmd, file.toString());

		// Assert
		assertEquals("hello from file", result);
	}

	@Test
	void getQueryFromFile_shouldThrowIOException_whenPathIsDirectory() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		AssembyCommand cmd = new AssembyCommand(reader);
		Method m = AssembyCommand.class.getDeclaredMethod("getQueryFromFile", String.class);
		m.setAccessible(true);

		Path dir = tempDir.resolve("dir");
		Files.createDirectories(dir);

		// Act
		IOException ex = null;
		try {
			m.invoke(cmd, dir.toString());
		} catch (java.lang.reflect.InvocationTargetException ite) {
			if (ite.getCause() instanceof IOException io) {
				ex = io;
			} else {
				throw ite;
			}
		}

		// Assert
		assertNotNull(ex);
	}
}
