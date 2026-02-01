package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

class DefaultProjectLayoutTest {

	@Test
	void getModules_shouldReturnEmptyListWhenProjectDirDoesNotExist() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/default-layout-empty");
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files) {
					f.delete();
				}
			}
			dir.delete();
		}
		assertFalse(dir.exists());
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void getModules_shouldReturnOnlyDirectoriesAndExcludeStandardExcludedDirs() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/default-layout-modules");
		Files.createDirectories(dir.toPath());

		Files.createDirectories(new File(dir, "module-a").toPath());
		Files.createDirectories(new File(dir, "module-b").toPath());
		Files.createDirectories(new File(dir, "target").toPath());
		Files.write(new File(dir, "not-a-dir.txt").toPath(), "x".getBytes());

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		assertTrue(modules.contains("module-a"));
		assertTrue(modules.contains("module-b"));
		assertFalse(modules.contains("target"));
	}

	@Test
	void getModules_shouldReflectFilesystemChangesAcrossCallsBecauseItCachesMutableListReference() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/default-layout-cache");
		Files.createDirectories(dir.toPath());
		Files.createDirectories(new File(dir, "module-a").toPath());

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(dir);
		List<String> first = layout.getModules();
		Files.createDirectories(new File(dir, "module-b").toPath());

		// Act
		List<String> second = layout.getModules();

		// Assert
		assertSame(first, second);
		assertEquals(2, second.size());
		assertTrue(second.contains("module-a"));
		assertTrue(second.contains("module-b"));
	}

	@Test
	void getSources_documents_tests_shouldReturnNull() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(new File("target/test-tmp/default-layout-nulls"));

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
		DefaultProjectLayout layout = new DefaultProjectLayout();
		File dir = new File("target/test-tmp/default-chain");

		// Act
		DefaultProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}
}
