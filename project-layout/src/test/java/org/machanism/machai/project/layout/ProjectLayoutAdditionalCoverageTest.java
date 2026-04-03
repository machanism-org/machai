package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectLayoutAdditionalCoverageTest {

	@TempDir
	File tempDir;

	@Test
	void getRelativePath_instanceMethodShouldKeepPathWithoutLeadingSlashWhenBaseNotAtBeginning() {
		// Arrange
		ProjectLayout layout = new DefaultProjectLayout();
		File file = new File(tempDir, "alpha.txt");
		String unrelatedBasePath = new File(tempDir.getParentFile(), "unrelated").getAbsolutePath();

		// Act
		String relative = layout.getRelativePath(unrelatedBasePath, file);

		// Assert
		assertEquals(file.getAbsolutePath().replace("\\", "/"), relative);
	}

	@Test
	void getRelativePath_staticShouldReturnOriginalRelativeValueWhenAlreadyDotPrefixed() {
		// Arrange
		File relativeFile = new File(".");

		// Act
		String relative = ProjectLayout.getRelativePath(relativeFile, relativeFile, true);

		// Assert
		assertEquals(".", relative);
	}
}
