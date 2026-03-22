package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
	void isPackageJsonPresent_shouldReturnTrueWhenPackageJsonExists() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile());

		// Assert
		assertTrue(present);
	}

	@Test
	void isPackageJsonPresent_shouldReturnFalseWhenPackageJsonMissing() {
		// Arrange
		File dir = tempDir.toFile();

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(dir);

		// Assert
		assertFalse(present);
	}

	@Test
	void getModules_shouldReturnEmptyListWhenNoWorkspacesKey() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"root\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void getModules_shouldResolveWorkspaceGlobsAndReturnRelativeModulePaths() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"),
				("{\"name\":\"root\",\"workspaces\":[\"packages/*\",\"./apps/*\"]}")
						.getBytes(StandardCharsets.UTF_8));

		Path packages = Files.createDirectories(tempDir.resolve("packages"));
		Path apps = Files.createDirectories(tempDir.resolve("apps"));
		Path p1 = Files.createDirectories(packages.resolve("p1"));
		Path p2 = Files.createDirectories(packages.resolve("p2"));
		Path app1 = Files.createDirectories(apps.resolve("app1"));

		Files.write(p1.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));
		Files.write(p2.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));
		Files.write(app1.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

		// Directory matches glob but missing package.json -> should not be included.
		Files.createDirectories(packages.resolve("no-package-json"));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(3, modules.size());
		assertTrue(modules.contains("packages/p1"));
		assertTrue(modules.contains("packages/p2"));
		assertTrue(modules.contains("apps/app1"));
	}

	@Test
	void getProjectId_shouldReturnNameFromPackageJson() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"my-package\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		String id = layout.getProjectId();

		// Assert
		assertEquals("my-package", id);
	}

	@Test
	void getModules_shouldThrowIllegalStateExceptionWhenProjectDirNotSet() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout();

		// Act
		IllegalStateException ex = assertThrows(IllegalStateException.class, layout::getModules);

		// Assert
		assertTrue(ex.getMessage().contains("projectDir"));
	}

	@Test
	void getSources_getDocuments_getTests_shouldReturnEmptyLists() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"root\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act / Assert
		assertNotNull(layout.getSources());
		assertNotNull(layout.getDocuments());
		assertNotNull(layout.getTests());
		assertTrue(layout.getSources().isEmpty());
		assertTrue(layout.getDocuments().isEmpty());
		assertTrue(layout.getTests().isEmpty());
	}
}
