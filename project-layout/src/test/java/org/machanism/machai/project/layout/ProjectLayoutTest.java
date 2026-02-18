package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;

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
	void findFiles_whenProjectDirNull_returnsEmptyList() {
		// Arrange
		// Act
		List<File> files = ProjectLayout.findFiles(null);

		// Assert
		assertNotNull(files);
		assertEquals(0, files.size());
	}

	@Test
	void findFiles_whenProjectDirIsNotDirectory_returnsEmptyList() {
		// Arrange
		File file = new File(tempDir, "not-a-dir.txt");

		// Act
		List<File> files = ProjectLayout.findFiles(file);

		// Assert
		assertNotNull(files);
		assertEquals(0, files.size());
	}

	@Test
	void findFiles_whenDirectoryRecurses_includesNestedFilesAndDirs() throws Exception {
		// Arrange
		File dir = new File(tempDir, "root");
		File nestedDir = new File(dir, "a\\b");
		nestedDir.mkdirs();
		File nestedFile = new File(nestedDir, "c.txt");
		nestedFile.createNewFile();

		// Act
		List<File> files = ProjectLayout.findFiles(dir);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(files.contains(nestedDir));
		org.junit.jupiter.api.Assertions.assertTrue(files.contains(nestedFile));
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
