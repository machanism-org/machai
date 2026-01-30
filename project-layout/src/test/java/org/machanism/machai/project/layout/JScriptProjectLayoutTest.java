package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class JScriptProjectLayoutTest {

	@Test
	void isPackageJsonPresent_whenFileExists_returnsTrue() {
		// Arrange
		File projectDir = new File("src/test/resources/mockJsProject");

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(projectDir);

		// Assert
		assertTrue(present);
	}

	@Test
	void isPackageJsonPresent_whenFileMissing_returnsFalse() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("js-layout-");

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile());

		// Assert
		assertFalse(present);
	}

	@Test
	void getModules_whenWorkspacesPresent_resolvesWorkspaceModulesByFindingNestedPackageJson() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("js-layout-workspaces-");
		writeFile(tempDir.resolve("package.json"), "{\"workspaces\":[\"workspaceA\",\"workspaceB\"]}");
		Files.createDirectories(tempDir.resolve("workspaceA"));
		Files.createDirectories(tempDir.resolve("workspaceB"));
		writeFile(tempDir.resolve("workspaceA").resolve("package.json"), "{\"name\":\"a\"}");
		writeFile(tempDir.resolve("workspaceB").resolve("package.json"), "{\"name\":\"b\"}");

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		assertTrue(modules.contains("workspaceA"));
		assertTrue(modules.contains("workspaceB"));
	}

	@Test
	void getModules_whenWorkspacesMissing_returnsNull() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("js-layout-no-workspaces-");
		writeFile(tempDir.resolve("package.json"), "{\"name\":\"root\"}");
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_whenWorkspacesIsNotArray_returnsNull() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("js-layout-workspaces-object-");
		writeFile(tempDir.resolve("package.json"), "{\"workspaces\":{\"packages\":[\"a/*\"]}}");
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_whenWorkspacesPatternHasNoMatches_returnsEmptyList() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("js-layout-empty-");
		writeFile(tempDir.resolve("package.json"), "{\"workspaces\":[\"packages/**\"]}");
		Files.createDirectories(tempDir.resolve("packages"));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(0, modules.size());
	}

	private static void writeFile(Path path, String content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
			fos.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}
}
