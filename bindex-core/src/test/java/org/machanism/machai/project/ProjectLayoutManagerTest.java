package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class ProjectLayoutManagerTest {

	@Test
	void detectProjectLayout_whenPomXmlExists_returnsMavenProjectLayoutAndSetsProjectDir(@TempDir Path tempDir)
			throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		Files.write(tempDir.resolve("pom.xml"), "<project/>".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(MavenProjectLayout.class, layout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenPackageJsonExists_returnsJScriptProjectLayoutAndSetsProjectDir(@TempDir Path tempDir)
			throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		Files.write(tempDir.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(JScriptProjectLayout.class, layout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenPyProjectTomlIsPublicPythonProject_returnsPythonProjectLayoutAndSetsProjectDir(
			@TempDir Path tempDir) throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		String pyproject = "[project]\nname = \"test\"\n";
		Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(PythonProjectLayout.class, layout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenNoKnownMarkersExist_returnsDefaultProjectLayoutAndSetsProjectDir(@TempDir Path tempDir)
			throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertInstanceOf(DefaultProjectLayout.class, layout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_whenProjectDirDoesNotExist_throwsFileNotFoundException(@TempDir Path tempDir)
			throws IOException {
		// Arrange
		Path missingDir = tempDir.resolve("missing");
		File missingDirFile = missingDir.toFile();

		// Act & Assert
		assertThrows(FileNotFoundException.class, () -> ProjectLayoutManager.detectProjectLayout(missingDirFile));
	}

}
