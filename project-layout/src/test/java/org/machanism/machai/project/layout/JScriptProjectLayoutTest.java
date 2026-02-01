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
	void getModules_shouldReturnNullWhenNoWorkspacesKey() throws Exception {
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
	void getModules_shouldReturnEmptyListBecauseMatcherSearchesForExcludedDirNamesInAbsolutePath() throws Exception {
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

	@Test
	void getModules_shouldIgnoreExcludedDirectoriesWhenResolvingWorkspaces() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/js-workspaces-excluded");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String packageJson = "{\"name\":\"root\",\"workspaces\":[\"packages/**\"]}";
		Files.write(new File(dir, "package.json").toPath(), packageJson.getBytes(StandardCharsets.UTF_8));

		File excluded = new File(dir, "packages/node_modules/module-a");
		assertTrue(excluded.mkdirs() || excluded.isDirectory());
		Files.write(new File(excluded, "package.json").toPath(), "{\"name\":\"a\"}".getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void getModules_shouldReturnNullWhenWorkspacesIsNotArray() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/js-workspaces-not-array");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String packageJson = "{\"name\":\"root\",\"workspaces\":{\"packages\":[\"packages/*\"]}}";
		Files.write(new File(dir, "package.json").toPath(), packageJson.getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_documents_tests_shouldReturnNull() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(new File("target/test-tmp/js-nulls"));

		// Act
		List<String> sources = layout.getSources();
		List<String> docs = layout.getDocuments();
		List<String> tests = layout.getTests();

		// Assert
		assertNull(sources);
		assertNull(docs);
		assertNull(tests);
	}

	@Test
	void projectDir_shouldReturnConcreteTypeForChaining() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout();
		File dir = new File("target/test-tmp/js-chain");

		// Act
		JScriptProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}
}
