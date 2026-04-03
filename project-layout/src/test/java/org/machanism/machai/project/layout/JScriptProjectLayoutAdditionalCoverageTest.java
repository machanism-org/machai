package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JScriptProjectLayoutAdditionalCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void getModules_shouldIgnoreNonArrayWorkspacesAndReturnEmptyList() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"),
				"{\"name\":\"root\",\"workspaces\":{\"packages\":[\"a\"]}}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertTrue(modules.isEmpty());
	}

	@Test
	void getModules_shouldSkipWorkspacePathWhenRelativePathCannotBeResolved() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"root\",\"workspaces\":[\"*\"]}"
				.getBytes(StandardCharsets.UTF_8));
		Files.createDirectories(tempDir.resolve("child"));
		Files.write(tempDir.resolve("child").resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));
		Files.write(tempDir.resolve("package.json.bak"), "x".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(1, modules.size());
		assertEquals("child", modules.get(0));
	}

	@Test
	void getProjectId_shouldThrowWhenPackageJsonCannotBeParsed() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, layout::getProjectId);
		Throwable cause = ex.getCause();

		// Assert
		assertNotNull(cause); // Sonar fix java:S5785
	}
}
