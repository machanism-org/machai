package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

class JScriptProjectLayoutTest {

	@Test
	void isPackageJsonPresent_shouldReturnTrueWhenPackageJsonExists() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/js-project");
		assertTrue(dir.mkdirs() || dir.isDirectory());
		File pkg = new File(dir, "package.json");
		Files.write(pkg.toPath(), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = JScriptProjectLayout.isPackageJsonPresent(dir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isPackageJsonPresent_shouldReturnFalseWhenPackageJsonMissing() {
		// Arrange
		File dir = new File("target/test-tmp/not-js-project");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		// Act
		boolean result = JScriptProjectLayout.isPackageJsonPresent(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void getModules_shouldReturnNullWhenNoWorkspaces() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/js-no-workspaces");
		assertTrue(dir.mkdirs() || dir.isDirectory());
		File pkg = new File(dir, "package.json");
		Files.write(pkg.toPath(), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_shouldReturnEmptyListWhenWorkspaceScanDoesNotMatchDueToExcludeLogic() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/js-workspaces");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String packageJson = "{\"name\":\"root\",\"workspaces\":[\"packages/**\"]}";
		Files.write(new File(dir, "package.json").toPath(), packageJson.getBytes(StandardCharsets.UTF_8));

		File moduleA = new File(dir, "packages/module-a");
		assertTrue(moduleA.mkdirs() || moduleA.isDirectory());
		Files.write(new File(moduleA, "package.json").toPath(), "{\"name\":\"a\"}".getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}
}
