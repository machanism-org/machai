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
	void isPackageJsonPresent_shouldReturnTrueOnlyWhenPackageJsonExists() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		
		// Act + Assert
		assertFalse(JScriptProjectLayout.isPackageJsonPresent(projectDir.toFile()));
		Files.write(projectDir.resolve(JScriptProjectLayout.PROJECT_MODEL_FILE_NAME), "{}".getBytes(StandardCharsets.UTF_8));
		assertTrue(JScriptProjectLayout.isPackageJsonPresent(projectDir.toFile()));
	}

	@Test
	void getModules_shouldReturnNullWhenNoWorkspacesKey() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		Files.write(projectDir.resolve("package.json"), "{\"name\":\"repo\"}".getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(projectDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_shouldResolveWorkspaceGlobs_andExcludePathsContainingExcludedDirs() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		Files.write(projectDir.resolve("package.json"), "{\"workspaces\":[\"packages/**\"]}".getBytes(StandardCharsets.UTF_8));

		Path packages = Files.createDirectories(projectDir.resolve("packages"));
		Path pkgA = Files.createDirectories(packages.resolve("a"));
		Files.write(pkgA.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

		Path nestedWithExcluded = Files.createDirectories(packages.resolve("node_modules").resolve("x"));
		Files.write(nestedWithExcluded.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(projectDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(1, modules.size());
		assertEquals("packages/a", modules.get(0).replace('\\', '/'));
	}

	@Test
	void getModules_shouldThrowIllegalArgumentExceptionWhenPackageJsonIsInvalid() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		Files.write(projectDir.resolve("package.json"), "{ invalid json".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(projectDir.toFile());

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, layout::getModules);

		// Assert
		assertNotNull(ex.getCause());
	}
}
