package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

class DefaultProjectLayoutTest {

	@Test
	void getModules_shouldReturnEmptyListWhenProjectDirDoesNotExistOrIsEmpty() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/default-layout-empty");
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				f.delete();
			}
			dir.delete();
		}
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(dir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void getModules_shouldReturnEmptyListWhenProjectDirIsNotADirectory() throws Exception {
		// Arrange
		File file = new File("target/test-tmp/default-layout-file");
		assertTrue(file.getParentFile().mkdirs() || file.getParentFile().isDirectory());
		FilesTestUtil.writeUtf8(file, "x");
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(file);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
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

	private static final class FilesTestUtil {
		private static void writeUtf8(File file, String content) throws Exception {
			assertTrue(file.getParentFile().mkdirs() || file.getParentFile().isDirectory());
			java.nio.file.Files.write(file.toPath(), content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		}
	}
}
