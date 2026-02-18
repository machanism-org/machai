package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JScriptProjectLayoutTest {

	@TempDir
	File tempDir;

	@Test
	void isPackageJsonPresent_whenPackageJsonExists_returnsTrue() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = JScriptProjectLayout.isPackageJsonPresent(tempDir);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(result);
	}

	@Test
	void getModules_whenNoWorkspaces_returnsNull() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(), "{\"name\":\"root\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_whenWorkspacesArray_returnsListOrEmptyButNotNull() throws Exception {
		// Arrange
		Files.createDirectories(new File(tempDir, "packages\\a").toPath());
		Files.createDirectories(new File(tempDir, "packages\\b").toPath());

		String packageJson = "{\n" + "  \"name\": \"root\",\n" + "  \"workspaces\": [\"packages/*\"]\n" + "}";
		Files.write(new File(tempDir, "package.json").toPath(), packageJson.getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		org.junit.jupiter.api.Assertions.assertNotNull(modules);
	}

	@Test
	void getProjectId_readsNameFromPackageJson() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(), "{\"name\":\"my-app\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act
		String id = layout.getProjectId();

		// Assert
		assertEquals("my-app", id);
	}

	@Test
	void getModules_whenPackageJsonInvalid_throwsIllegalArgumentException() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(), "{invalid".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, layout::getModules);
	}
}
