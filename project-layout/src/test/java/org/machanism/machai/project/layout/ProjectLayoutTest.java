package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Test;

class ProjectLayoutTest {

	@Test
	void getRelatedPath_instanceMethod_trimsLeadingSlashAndNormalizesSeparators() {
		// Arrange
		ProjectLayout layout = new DefaultProjectLayout();
		String currentPath = "C:/repo";
		File file = new File("C:/repo/src/main/java");

		// Act
		String related = layout.getRelatedPath(currentPath, file);

		// Assert
		assertEquals("src/main/java", related);
	}

	@Test
	void getRelatedPath_static_whenSameDir_returnsDot() {
		// Arrange
		File dir = new File("C:/repo");
		File file = new File("C:/repo");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file);

		// Assert
		assertEquals(".", related);
	}

	@Test
	void getRelatedPath_static_whenAddSingleDotAndRelativeNotStartsWithDot_prefixesDotSlash() {
		// Arrange
		File dir = new File("C:/repo");
		File file = new File("C:/repo/moduleA");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, true);

		// Assert
		assertEquals("./moduleA", related);
	}

	@Test
	void getRelatedPath_static_whenFileOutsideDir_returnsNull() {
		// Arrange
		File dir = new File("C:/repo");
		File file = new File("C:/other");

		// Act
		String related = ProjectLayout.getRelatedPath(dir, file, false);

		// Assert
		assertNull(related);
	}
}
