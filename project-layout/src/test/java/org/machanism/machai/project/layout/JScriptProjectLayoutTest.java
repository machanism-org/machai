package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
	void isPackageJsonPresent_whenPresent_returnsTrue() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(tempDir);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(present);
	}

	@Test
	void getModules_whenWorkspacesWithGlob_discoversWorkspacePackageJsonFiles() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(),
				("{\"workspaces\":[\"packages/**\"]}").getBytes(StandardCharsets.UTF_8));

		File pkg1 = new File(tempDir, "packages\\p1");
		File pkg2 = new File(tempDir, "packages\\p2");
		Files.createDirectories(pkg1.toPath());
		Files.createDirectories(pkg2.toPath());
		Files.write(new File(pkg1, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));
		Files.write(new File(pkg2, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		org.junit.jupiter.api.Assertions.assertTrue(modules.contains("packages/p1"));
		org.junit.jupiter.api.Assertions.assertTrue(modules.contains("packages/p2"));
	}

	@Test
	void getModules_ignoresExcludedFoldersLikeNodeModules() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(),
				("{\"workspaces\":[\"packages/**\"]}").getBytes(StandardCharsets.UTF_8));

		File good = new File(tempDir, "packages\\good");
		Files.createDirectories(good.toPath());
		Files.write(new File(good, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		File ignored = new File(tempDir, "packages\\node_modules\\bad");
		Files.createDirectories(ignored.toPath());
		Files.write(new File(ignored, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(1, modules.size());
		assertEquals("packages/good", modules.get(0));
	}

	@Test
	void getModules_whenPackageJsonInvalid_throwsIllegalArgumentException() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "package.json").toPath(), "{not json".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, layout::getModules);
	}
}
