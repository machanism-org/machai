package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

	@TempDir
	Path tempDir;

	@Test
	void detectProjectLayout_shouldReturnMavenLayoutWhenPomExists() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("pom.xml"), minimalPom().getBytes(StandardCharsets.UTF_8));
		File projectDir = tempDir.toFile();

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof MavenProjectLayout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_shouldReturnJScriptLayoutWhenPackageJsonExists() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8));
		File projectDir = tempDir.toFile();

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof JScriptProjectLayout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_shouldReturnPythonLayoutWhenPyProjectTomlIndicatesPublicProject() throws Exception {
		// Arrange
		String pyproject = "[project]\n" +
				"name = \"demo\"\n" +
				"classifiers = [\"Development Status :: 4 - Beta\"]\n";
		Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes(StandardCharsets.UTF_8));
		File projectDir = tempDir.toFile();

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof PythonProjectLayout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_shouldReturnDefaultLayoutWhenNoKnownBuildFilesButDirExists() throws Exception {
		// Arrange
		Files.createDirectories(tempDir.resolve("some-dir"));
		File projectDir = tempDir.toFile();

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof DefaultProjectLayout);
		assertEquals(projectDir, layout.getProjectDir());
	}

	@Test
	void detectProjectLayout_shouldThrowFileNotFoundExceptionWhenDirDoesNotExist() {
		// Arrange
		File missingDir = tempDir.resolve("missing").toFile();
		assertFalse(missingDir.exists());

		// Act & Assert
		FileNotFoundException ex = assertThrows(FileNotFoundException.class,
				() -> ProjectLayoutManager.detectProjectLayout(missingDir));
		assertTrue(ex.getMessage().contains("missing"));
	}

	private static String minimalPom() {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1.0</version>\n" +
				"</project>\n";
	}
}
