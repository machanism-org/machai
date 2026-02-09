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
	void getModules_shouldReturnDirectoriesExcludingKnownBuildAndVcsDirs_andCacheResult() throws IOException {
		// Arrange
		Path moduleA = Files.createDirectories(tempDir.resolve("module-a"));
		Files.createDirectories(tempDir.resolve("target"));
		Files.createDirectories(tempDir.resolve(".git"));
		Files.createDirectories(tempDir.resolve("build"));

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> first = layout.getModules();
		Files.createDirectories(tempDir.resolve("module-b"));
		List<String> second = layout.getModules();

		// Assert
		assertNotNull(first);
		assertEquals(1, first.size());
		assertTrue(first.contains(moduleA.getFileName().toString()));

		assertSame(first, second, "Expected cached list instance to be returned on subsequent calls");
		assertEquals(1, second.size(), "Expected cached result not to change after filesystem updates");
	}

	@Test
	void getSources_getDocuments_getTests_shouldReturnNullAsNotImplemented() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(new File("."));

		// Act
		List<String> sources = layout.getSources();
		List<String> docs = layout.getDocuments();
		List<String> tests = layout.getTests();

		// Assert
		assertNull(sources);
		assertNull(docs);
		assertNull(tests);
	}
}
