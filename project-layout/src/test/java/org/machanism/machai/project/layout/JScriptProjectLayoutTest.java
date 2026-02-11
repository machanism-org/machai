package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JScriptProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void isPackageJsonPresent_shouldReturnTrueWhenPresent() throws IOException {
		// Arrange
		Files.write(tempDir.resolve(JScriptProjectLayout.PROJECT_MODEL_FILE_NAME), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile());

		// Assert
		assertTrue(present);
	}

	@Test
	void isPackageJsonPresent_shouldReturnFalseWhenAbsent() {
		// Arrange
		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile());

		// Assert
		assertFalse(present);
	}

	@Test
	void getModules_shouldReturnNullWhenWorkspacesNotDefined() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_shouldResolveWorkspaceGlobsAndExcludeDirectories() throws IOException {
		// Arrange
		Files.createDirectories(tempDir.resolve("packages\\a"));
		Files.write(tempDir.resolve("packages\\a\\package.json"), "{\"name\":\"a\"}".getBytes(StandardCharsets.UTF_8));
		Files.createDirectories(tempDir.resolve("packages\\b"));
		Files.write(tempDir.resolve("packages\\b\\package.json"), "{\"name\":\"b\"}".getBytes(StandardCharsets.UTF_8));

		Files.createDirectories(tempDir.resolve("packages\\target\\c"));
		Files.write(tempDir.resolve("packages\\target\\c\\package.json"), "{\"name\":\"c\"}".getBytes(StandardCharsets.UTF_8));

		String rootPackageJson = "{\"workspaces\":[\"packages/**\"]}";
		Files.write(tempDir.resolve("package.json"), rootPackageJson.getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		assertTrue(modules.contains("packages/a"));
		assertTrue(modules.contains("packages/b"));
		assertFalse(modules.contains("packages/target/c"));
	}

	@Test
	void getModules_shouldThrowIllegalArgumentExceptionWhenPackageJsonInvalid() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{ invalid json".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act & Assert
		assertThrows(IllegalArgumentException.class, layout::getModules);
	}

	@Test
	void notImplementedMethods_shouldReturnNull() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act & Assert
		assertNull(layout.getSources());
		assertNull(layout.getDocuments());
		assertNull(layout.getTests());
	}
}
