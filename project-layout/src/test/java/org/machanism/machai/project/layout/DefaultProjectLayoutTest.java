package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

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
	void getModules_shouldReturnOnlyNonExcludedDirectories() throws IOException {
		// Arrange
		Files.createDirectories(tempDir.resolve("moduleA"));
		Files.createDirectories(tempDir.resolve("moduleB"));
		Files.createDirectories(tempDir.resolve("target"));
		Files.createDirectories(tempDir.resolve(".git"));
		Files.createFile(tempDir.resolve("file.txt"));

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		assertTrue(modules.contains("moduleA"));
		assertTrue(modules.contains("moduleB"));
		assertFalse(modules.contains("target"));
		assertFalse(modules.contains(".git"));
	}

	@Test
	void getModules_shouldCacheResultAndNotReflectLaterFilesystemChanges() throws IOException {
		// Arrange
		Files.createDirectories(tempDir.resolve("moduleA"));
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
		List<String> first = layout.getModules();
		Files.createDirectories(tempDir.resolve("moduleB"));

		// Act
		List<String> second = layout.getModules();

		// Assert
		assertSame(first, second);
		assertEquals(1, second.size());
		assertTrue(second.contains("moduleA"));
		assertFalse(second.contains("moduleB"));
	}

	@Test
	void getModules_shouldReturnEmptyListWhenNoSubdirectories() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void notImplementedMethods_shouldReturnNull() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(new File("."));

		// Act & Assert
		assertNull(layout.getSources());
		assertNull(layout.getDocuments());
		assertNull(layout.getTests());
	}
}
