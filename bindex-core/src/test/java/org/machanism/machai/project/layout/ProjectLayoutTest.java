package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectLayoutTest {

	private static final class DummyProjectLayout extends ProjectLayout {
		@Override
		public List<String> getSources() {
			return Collections.singletonList("src");
		}

		@Override
		public List<String> getDocuments() {
			return Collections.singletonList("docs");
		}

		@Override
		public List<String> getTests() {
			return Collections.singletonList("tests");
		}
	}

	@Test
	void projectDir_whenSet_returnsSameDir(@TempDir Path tempDir) {
		// Arrange
		File dir = tempDir.toFile();
		ProjectLayout layout = new DummyProjectLayout();

		// Act
		layout.projectDir(dir);

		// Assert
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void getModules_defaultImplementation_returnsNull() throws Exception {
		// Arrange
		ProjectLayout layout = new DummyProjectLayout();

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getRelatedPath_instanceMethod_removesLeadingSlashAndNormalizesSeparators(@TempDir Path tempDir) {
		// Arrange
		File baseDir = tempDir.toFile();
		File nested = tempDir.resolve("a").resolve("b.txt").toFile();
		ProjectLayout layout = new DummyProjectLayout().projectDir(baseDir);
		String currentPath = baseDir.getAbsolutePath().replace("\\", "/");

		// Act
		String related = layout.getRelatedPath(currentPath, nested);

		// Assert
		assertEquals("a/b.txt", related);
	}

	@Test
	void getRelatedPath_static_whenFileEqualsDir_returnsDot(@TempDir Path tempDir) {
		// Arrange
		File dir = tempDir.toFile();

		// Act
		String related = ProjectLayout.getRelatedPath(dir, dir);

		// Assert
		assertEquals(".", related);
	}

	@Test
	void getRelatedPath_static_whenAddSingleDotTrue_prefixesDotSlash(@TempDir Path tempDir) {
		// Arrange
		File dir = tempDir.toFile();
		File file = tempDir.resolve("file.java").toFile();

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals("./file.java", related);
	}

	@Test
	void getRelatedPath_static_whenFileOutsideDir_returnsNull(@TempDir Path tempDir) {
		// Arrange
		File dir = tempDir.toFile();
		File outside = new File(dir.getParentFile(), "outside.txt");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, outside, false);

		// Assert
		assertNull(related);
	}
}
