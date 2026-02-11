package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

	@Test
	void projectDir_shouldStoreAndReturnSameInstance() {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout();
		File dir = new File(".");

		// Act
		ProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}

	@Test
	void getRelatedPath_instanceMethod_shouldReturnRelativePathWithNormalizedSeparatorsAndNoLeadingSlash() {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout();
		String basePath = "C:\\work";
		File file = new File("C:\\work\\sub\\file.txt");

		// Act
		String relatedPath = layout.getRelatedPath(basePath, file);

		// Assert
		assertEquals("sub/file.txt", relatedPath);
	}

	@Test
	void getRelatedPath_static_shouldReturnDotWhenSameDirectory() {
		// Arrange
		File dir = new File("C:\\work");
		File file = new File("C:\\work");

		// Act
		String relatedPath = ProjectLayout.getRelatedPath(dir, file);

		// Assert
		assertEquals(".", relatedPath);
	}

	@Test
	void getRelatedPath_static_shouldPrependDotSlashWhenRequestedAndNotAlreadyDotted() {
		// Arrange
		File dir = new File("C:\\work");
		File file = new File("C:\\work\\module");

		// Act
		String relatedPath = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals("./module", relatedPath);
	}

	@Test
	void getRelatedPath_static_shouldReturnNullWhenFileIsNotUnderDir() {
		// Arrange
		File dir = new File("C:\\work");
		File file = new File("D:\\other\\file.txt");

		// Act
		String relatedPath = ProjectLayout.getRelatedPath(dir, file, false);

		// Assert
		assertNull(relatedPath);
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
