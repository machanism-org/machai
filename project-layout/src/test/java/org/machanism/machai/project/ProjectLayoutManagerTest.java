package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
	void detectProjectLayout_whenDirectoryDoesNotExist_throwsFileNotFoundException() {
		// Arrange
		File projectDir = new File("target/non-existent-dir-" + System.nanoTime());

		// Act + Assert
		assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(projectDir));
	}

	@Test
	void detectProjectLayout_whenMavenProject_detectsMavenProjectLayoutAndSetsProjectDir() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockMavenProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(MavenProjectLayout.class, layout);
		assertNotNull(layout.getProjectDir());
		assertInstanceOf(File.class, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenPackageJsonPresent_detectsJScriptProjectLayout() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockJsProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(JScriptProjectLayout.class, layout);
	}

	@Test
	void detectProjectLayout_whenPythonProject_detectsPythonProjectLayout() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockPythonProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(PythonProjectLayout.class, layout);
	}

	@Test
	void detectProjectLayout_whenNoBuildFilesPresentAndDirectoryExists_detectsDefaultProjectLayout() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockDefaultProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(DefaultProjectLayout.class, layout);
	}
}
