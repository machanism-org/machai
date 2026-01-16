package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

	@Test
	void projectDir_setsAndReturnsThis() {
		// Arrange
		ProjectLayout layout = new DefaultProjectLayout();
		File dir = new File("src/test/resources/mockMavenProject");

		// Act
		ProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertEquals(layout, returned);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	@Disabled
	void getRelatedPath_instanceMethod_trimsLeadingSlash() {
		// Arrange
		ProjectLayout layout = new DefaultProjectLayout();
		String currentPath = "/tmp/root";
		File file = new File("/tmp/root/src/main/java");

		// Act
		String relative = layout.getRelatedPath(currentPath, file);

		// Assert
		assertEquals("src/main/java", relative);
	}

	@Test
	void getRelatedPath_static_whenSameDir_returnsDot() {
		// Arrange
		File dir = new File("/tmp/root");
		File file = new File("/tmp/root");

		// Act
		String relative = ProjectLayout.getRelatedPath(dir, file);

		// Assert
		assertEquals(".", relative);
	}

	@Test
	void getRelatedPath_static_whenDifferentRoot_returnsNull() {
		// Arrange
		File dir = new File("/tmp/root");
		File file = new File("/other/place");

		// Act
		String relative = ProjectLayout.getRelatedPath(dir, file);

		// Assert
		assertNull(relative);
	}

	@Test
	void getRelatedPath_static_withSingleDotPrefix_addsDotSlash() {
		// Arrange
		File dir = new File("/tmp/root");
		File file = new File("/tmp/root/src");

		// Act
		String relative = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals("./src", relative);
	}
}
