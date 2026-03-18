package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Additional coverage tests for {@link AssembyCommand}.
 *
 * Note: This class intentionally targets hard-to-reach branches without invoking
 * external services.
 */
class AssembyCommandCoverageTest {

	@TempDir
	File tempDir;

	@Test
	void getQueryFromFile_whenPathExists_returnsFileContent() throws Exception {
		// Arrange
		File queryFile = new File(tempDir, "q.txt");
		Files.writeString(queryFile.toPath(), "hello from file", StandardCharsets.UTF_8);

		AssembyCommand cmd = new AssembyCommand();
		Method m = AssembyCommand.class.getDeclaredMethod("getQueryFromFile", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, queryFile.getAbsolutePath());

		// Assert
		assertEquals("hello from file", result);
	}

	@Test
	void getQueryFromFile_whenPathDoesNotExist_returnsOriginalString() throws Exception {
		// Arrange
		AssembyCommand cmd = new AssembyCommand();
		Method m = AssembyCommand.class.getDeclaredMethod("getQueryFromFile", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, "not-a-real-file-path-123");

		// Assert
		assertEquals("not-a-real-file-path-123", result);
	}

	@Test
	void assembly_whenQueryIsNullAndNoPreviousPick_shouldThrowIllegalArgumentException() {
		// Arrange
		AssembyCommand cmd = new AssembyCommand();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> cmd.assembly(null, null, null, null, null));

		// Assert
		assertEquals("The query is empty.", ex.getMessage());
	}
}
