package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFunctionToolsTest {

	@TempDir
	File tempDir;

	@Test
	void resolveWorkingDir_whenProjectDirNull_returnsNull() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		File resolved = tools.resolveWorkingDir(null, ".");

		// Assert
		assertNull(resolved);
	}

	@Test
	void resolveWorkingDir_whenDirNull_returnsNull() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		File resolved = tools.resolveWorkingDir(tempDir, null);

		// Assert
		assertNull(resolved);
	}

	@Test
	void resolveWorkingDir_whenDot_returnsCanonicalProjectDir() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		File resolved = tools.resolveWorkingDir(tempDir, ".");

		// Assert
		assertNotNull(resolved);
		assertEquals(tempDir.getCanonicalFile(), resolved);
	}

	@Test
	void resolveWorkingDir_whenRelativeChild_returnsCanonicalChildWithinProject() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		File child = new File(tempDir, "a");
		assertTrue(child.mkdirs());

		// Act
		File resolved = tools.resolveWorkingDir(tempDir, "a");

		// Assert
		assertNotNull(resolved);
		assertEquals(child.getCanonicalFile(), resolved);
	}

	@Test
	void resolveWorkingDir_whenAttemptsTraversalOutside_returnsNull() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		File resolved = tools.resolveWorkingDir(tempDir, "..\\..");

		// Assert
		assertNull(resolved);
	}

	@Test
	void resolveWorkingDir_whenAbsolutePath_returnsNull() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		String absolutePath = tempDir.getAbsolutePath();

		// Act
		File resolved = tools.resolveWorkingDir(tempDir, absolutePath);

		// Assert
		assertNull(resolved);
	}

	@Test
	void parseEnv_whenNull_returnsEmptyMap() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		java.util.Map<String, String> result = tools.parseEnv(null);

		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void parseEnv_whenEmpty_returnsEmptyMap() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		java.util.Map<String, String> result = tools.parseEnv("");

		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void parseEnv_ignoresCommentsAndBlankLines_andParsesValidPairs() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		String env = "\n# comment\nFOO=bar\n";

		// Act
		java.util.Map<String, String> result = tools.parseEnv(env);

		// Assert
		assertEquals(1, result.size());
		assertEquals("bar", result.get("FOO"));
	}

	@Test
	void parseEnv_rejectsInvalidKey_andRejectsMissingValue() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		String env = "1BAD=x\nNOVAL=\nGOOD_1=ok\n";

		// Act
		java.util.Map<String, String> result = tools.parseEnv(env);

		// Assert
		assertEquals(1, result.size());
		assertEquals("ok", result.get("GOOD_1"));
		assertFalse(result.containsKey("1BAD"));
		assertFalse(result.containsKey("NOVAL"));
	}

	@Test
	void parseEnv_trimsWhitespaceAroundKeyAndValue() {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		String env = "  KEY  =  value with spaces  \n";

		// Act
		java.util.Map<String, String> result = tools.parseEnv(env);

		// Assert
		assertEquals("value with spaces", result.get("KEY"));
	}
}
