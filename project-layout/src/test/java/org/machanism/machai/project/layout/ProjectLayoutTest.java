package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

	@Test
	void getRelativePath_static_shouldReturnDotWhenSameDirectory() {
		// Arrange
		File dir = new File("C:\\work");
		File file = new File("C:\\work");

		// Act
		String relativePath = ProjectLayout.getRelativePath(dir, file);

		// Assert
		assertEquals(".", relativePath);
	}

	@Test
	void getRelativePath_static_shouldPrependDotSlashWhenRequestedAndNotAlreadyDotted() {
		// Arrange
		File dir = new File("C:\\work");
		File file = new File("C:\\work\\module");

		// Act
		String relativePath = ProjectLayout.getRelativePath(dir, file, true);

		// Assert
		assertEquals("./module", relativePath);
	}

	@Test
	void getRelativePath_static_shouldReturnNullWhenFileIsNotUnderDir() {
		// Arrange
		File dir = new File("C:\\work");
		File file = new File("D:\\other\\file.txt");

		// Act
		String relativePath = ProjectLayout.getRelativePath(dir, file, false);

		// Assert
		assertNull(relativePath);
	}

	@Test
	void defaultImplementations_shouldReturnNullExceptExcludedDirsConstant() {
		// Arrange
		ProjectLayout layout = new ProjectLayout() {
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
		};

		// Act & Assert
		assertNull(layout.getModules());
		assertNull(layout.getProjectId());
		assertNull(layout.getProjectName());
		assertNotNull(ProjectLayout.EXCLUDE_DIRS);
		assertTrue(ProjectLayout.EXCLUDE_DIRS.length > 0);
	}
}
