package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

	private static final class TestLayout extends ProjectLayout {
		@Override
		public java.util.List<String> getSources() {
			return null;
		}

		@Override
		public java.util.List<String> getDocuments() {
			return null;
		}

		@Override
		public java.util.List<String> getTests() {
			return null;
		}
	}

	@Test
	void projectDir_shouldSetAndReturnSameInstance() {
		// Arrange
		TestLayout layout = new TestLayout();
		File dir = new File("build\\tmp\\project");

		// Act
		ProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}

	@Test
	void getRelatedPath_instanceMethod_shouldReturnRelativePathAndTrimLeadingSlash() {
		// Arrange
		TestLayout layout = new TestLayout();
		String base = "C:/repo";
		File file = new File("C:/repo/sub/module");

		// Act
		String related = layout.getRelatedPath(base, file);

		// Assert
		assertEquals("sub/module", related);
	}

	@Test
	void getRelatedPath_static_shouldReturnDotWhenFileEqualsDir() {
		// Arrange
		File dir = new File("C:/repo");
		File file = new File("C:/repo");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file);

		// Assert
		assertEquals(".", related);
	}

	@Test
	void getRelatedPath_static_shouldReturnNullWhenFileStringEqualsResult() {
		// Arrange
		File dir = new File("C:/repo");
		File file = new File("C:/other");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, false);

		// Assert
		assertNull(related);
	}

	@Test
	void getRelatedPath_static_shouldOptionallyAddSingleDotPrefix() {
		// Arrange
		File dir = new File("C:/repo");
		File file = new File("C:/repo/a/b");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals("./a/b", related);
	}
}
