package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class ProjectLayoutManagerTest {

	@Test
	void detectProjectLayout_whenMavenProject_returnsMavenLayoutWithProjectDirSet() throws FileNotFoundException {
		// Arrange
		File dir = new File("src/test/resources/mockMavenProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(MavenProjectLayout.class, layout);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenJScriptProject_returnsJScriptLayoutWithProjectDirSet() throws FileNotFoundException {
		// Arrange
		File dir = new File("src/test/resources/mockJsProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(JScriptProjectLayout.class, layout);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenPythonProject_returnsPythonLayoutWithProjectDirSet() throws FileNotFoundException {
		// Arrange
		File dir = new File("src/test/resources/mockPythonProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(PythonProjectLayout.class, layout);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenExistingButNoSpecificMarker_returnsDefaultLayout() throws FileNotFoundException {
		// Arrange
		File dir = new File("src/test/resources/mockDefaultProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(DefaultProjectLayout.class, layout);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenMissingDirectory_throwsFileNotFoundException() {
		// Arrange
		File dir = new File("src/test/resources/does-not-exist");

		// Act + Assert
		assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(dir));
	}
}
