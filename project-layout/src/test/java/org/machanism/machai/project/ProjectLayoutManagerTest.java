package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.GragleProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class ProjectLayoutManagerTest {

	@TempDir
	File tempDir;

	@Test
	void detectProjectLayout_whenDirectoryDoesNotExist_throwsFileNotFoundException() {
		// Arrange
		File missing = new File(tempDir, "missing");

		// Act
		FileNotFoundException ex = assertThrows(FileNotFoundException.class,
				() -> ProjectLayoutManager.detectProjectLayout(missing));

		// Assert
		assertEquals(missing.getAbsolutePath(), ex.getMessage());
	}

	@Test
	void detectProjectLayout_whenMavenPomPresent_returnsMavenLayoutAndSetsProjectDir() throws Exception {
		// Arrange
		File dir = new File(tempDir, "maven");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "pom.xml").toPath(), minimalPom("jar").getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(MavenProjectLayout.class, layout);
		assertEquals(dir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
	}

	@Test
	void detectProjectLayout_whenGradleBuildPresent_returnsGradleLayoutAndSetsProjectDir() throws Exception {
		// Arrange
		File dir = new File(tempDir, "gradle");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "build.gradle").toPath(), "plugins {}".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(GragleProjectLayout.class, layout);
		assertEquals(dir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
	}

	@Test
	void detectProjectLayout_whenPackageJsonPresent_returnsJScriptLayoutAndSetsProjectDir() throws IOException {
		// Arrange
		File dir = new File(tempDir, "js");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(JScriptProjectLayout.class, layout);
		assertEquals(dir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
	}

	@Test
	void detectProjectLayout_whenPyProjectTomlPresentAndNonPrivate_returnsPythonLayoutAndSetsProjectDir() throws IOException {
		// Arrange
		File dir = new File(tempDir, "py");
		Files.createDirectories(dir.toPath());
		String toml = "[project]\nname = \"demo\"\n";
		Files.write(new File(dir, "pyproject.toml").toPath(), toml.getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertInstanceOf(PythonProjectLayout.class, layout);
		assertEquals(dir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
	}

	@Test
	void detectProjectLayout_whenNoMarkersButExists_returnsDefaultLayoutAndSetsProjectDir() throws Exception {
		// Arrange
		File dir = new File(tempDir, "default");
		Files.createDirectories(dir.toPath());

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertNotNull(layout);
		assertInstanceOf(DefaultProjectLayout.class, layout);
		assertEquals(dir.getAbsolutePath(), layout.getProjectDir().getAbsolutePath());
	}

	private static String minimalPom(String packaging) {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <groupId>org.example</groupId>\n"
				+ "  <artifactId>demo</artifactId>\n"
				+ "  <version>1.0.0</version>\n"
				+ "  <packaging>" + packaging + "</packaging>\n"
				+ "</project>\n";
	}
}
