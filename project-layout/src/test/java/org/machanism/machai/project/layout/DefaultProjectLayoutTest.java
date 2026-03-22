package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void getModules_shouldReturnImmediateSubdirectoriesExcludingKnownDirectories() throws IOException {
		// Arrange
		Files.createDirectories(tempDir.resolve("module-a"));
		Files.createDirectories(tempDir.resolve("module-b"));
		Files.createDirectories(tempDir.resolve("target"));
		Files.createDirectories(tempDir.resolve(".git"));
		Files.createFile(tempDir.resolve("file.txt"));

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(2, modules.size());
		assertTrue(modules.contains("module-a"));
		assertTrue(modules.contains("module-b"));
		assertFalse(modules.contains("target"));
		assertFalse(modules.contains(".git"));
	}

	@Test
	void getModules_shouldReturnCachedListAndNotRescanAfterDirectoryChanges() throws IOException {
		// Arrange
		Files.createDirectories(tempDir.resolve("module-a"));
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
		List<String> first = layout.getModules();
		assertTrue(first.contains("module-a"));

		Files.createDirectories(tempDir.resolve("module-added-later"));

		// Act
		List<String> second = layout.getModules();

		// Assert
		assertSame(first, second);
		assertFalse(second.contains("module-added-later"));
	}

	@Test
	void getModules_shouldReturnEmptyListWhenProjectDirNotSet() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void projectDir_shouldReturnDefaultProjectLayoutForFluentChaining() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();
		File dir = tempDir.toFile();

		// Act
		DefaultProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertEquals(dir, returned.getProjectDir());
	}

	@Test
	void getSources_getDocuments_getTests_shouldReturnEmptyLists() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

		// Act
		assertNotNull(layout.getSources());
		assertNotNull(layout.getDocuments());
		assertNotNull(layout.getTests());

		// Assert
		assertTrue(layout.getSources().isEmpty());
		assertTrue(layout.getDocuments().isEmpty());
		assertTrue(layout.getTests().isEmpty());
	}
}
