package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

	private static final class TestLayout extends ProjectLayout {
		@Override
		public List<String> getSources() {
			return Arrays.asList("src");
		}

		@Override
		public List<String> getDocuments() {
			return Arrays.asList("docs");
		}

		@Override
		public List<String> getTests() {
			return Arrays.asList("test");
		}
	}

	@Test
	void projectDir_shouldSetAndReturnSameInstance() {
		// Arrange
		ProjectLayout layout = new TestLayout();
		File dir = new File("target/test-tmp/repo");

		// Act
		ProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}

	@Test
	void getModules_defaultImplementation_shouldReturnNull() throws Exception {
		// Arrange
		ProjectLayout layout = new TestLayout();

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getRelatedPath_instance_shouldStripCurrentPath_andLeadingSlash() {
		// Arrange
		ProjectLayout layout = new TestLayout();
		String currentPath = "C:/repo";
		File file = new File("C:/repo/src/main/java");

		// Act
		String related = layout.getRelatedPath(currentPath, file);

		// Assert
		assertEquals("src/main/java", related);
	}

	@Test
	void getRelatedPath_instance_shouldReturnEmptyStringWhenFileEqualsCurrentPath() {
		// Arrange
		ProjectLayout layout = new TestLayout();
		String currentPath = "C:/repo";
		File file = new File("C:/repo");

		// Act
		String related = layout.getRelatedPath(currentPath, file);

		// Assert
		assertEquals("", related);
	}

	@Test
	void getRelatedPath_static_whenFileEqualsDir_shouldReturnDot() {
		// Arrange
		File dir = new File("/repo");
		File file = new File("/repo");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file);

		// Assert
		assertEquals(".", related);
	}

	@Test
	void getRelatedPath_static_whenAddSingleDot_shouldPrefixWhenNotStartingWithDot() {
		// Arrange
		File dir = new File("/repo");
		File file = new File("/repo/module");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals("./module", related);
	}

	@Test
	void getRelatedPath_static_whenRelativePathEmpty_shouldReturnDotEvenIfAddSingleDotTrue() {
		// Arrange
		File dir = new File("/repo");
		File file = new File("/repo");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals(".", related);
	}

	@Test
	void getRelatedPath_static_whenFileNotUnderDir_shouldReturnNull() {
		// Arrange
		File dir = new File("/repo");
		File file = new File("/other/place");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, false);

		// Assert
		assertNull(related);
	}
}
