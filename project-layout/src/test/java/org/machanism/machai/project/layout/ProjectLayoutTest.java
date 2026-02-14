package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectLayoutTest {

	@TempDir
	File tempDir;

	@Test
	void getRelativePath_instanceMethod_stripsBasePathAndLeadingSlash() throws Exception {
		// Arrange
		File base = new File(tempDir, "base");
		File file = new File(base, "a\\b.txt");
		file.getParentFile().mkdirs();
		file.createNewFile();
		ProjectLayout layout = new DefaultProjectLayout().projectDir(base);

		// Act
		String relative = layout.getRelativePath(base.getAbsolutePath(), file);

		// Assert
		assertEquals("a/b.txt", relative);
	}

	@Test
	void getRelativePath_static_whenFileEqualsDir_returnsDot() {
		// Arrange
		File dir = tempDir;

		// Act
		String relative = ProjectLayout.getRelativePath(dir, dir);

		// Assert
		assertEquals(".", relative);
	}

	@Test
	void getRelativePath_static_whenAddSingleDotTrue_prefixesForNonDotPath() {
		// Arrange
		File dir = tempDir;
		File file = new File(dir, "child");

		// Act
		String relative = ProjectLayout.getRelativePath(dir, file, true);

		// Assert
		assertEquals("./child", relative);
	}

	@Test
	void getRelativePath_static_whenFileOutsideDir_returnsNull() {
		// Arrange
		File dir = new File(tempDir, "p1");
		File file = new File(tempDir, "p2\\x.txt");

		// Act
		String relative = ProjectLayout.getRelativePath(dir, file);

		// Assert
		assertNull(relative);
	}

	@Test
	void getProjectLayoutType_stripsProjectLayoutSuffix() {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout();

		// Act
		String type = layout.getProjectLayoutType();

		// Assert
		assertEquals("Maven", type);
	}
}
