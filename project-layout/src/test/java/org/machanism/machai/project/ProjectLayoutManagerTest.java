package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
	Path tempDir;

	@Test
	void constructor_shouldThrowIllegalStateException() throws Exception {
		// Arrange
		java.lang.reflect.Constructor<ProjectLayoutManager> ctor = ProjectLayoutManager.class.getDeclaredConstructor();
		ctor.setAccessible(true);

		// Act
		Exception ex = assertThrows(Exception.class, ctor::newInstance);

		// Assert
		Throwable cause = ex.getCause();
		assertNotNull(cause);
		assertInstanceOf(IllegalStateException.class, cause);
		assertEquals("Utility class", cause.getMessage());
	}

	@Test
	void detectProjectLayout_shouldThrowFileNotFoundExceptionWhenDirDoesNotExist() {
		// Arrange
		File missing = tempDir.resolve("missing").toFile();

		// Act
		FileNotFoundException ex = assertThrows(FileNotFoundException.class,
				() -> ProjectLayoutManager.detectProjectLayout(missing));

		// Assert
		assertTrue(ex.getMessage().endsWith("missing"));
	}

	@Test
	void detectProjectLayout_shouldReturnMavenLayoutWhenPomXmlPresent() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("pom.xml"), "<project/>".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir.toFile());

		// Assert
		assertInstanceOf(MavenProjectLayout.class, layout);
		assertEquals(tempDir.toFile(), layout.getProjectDir());
		assertTrue(MavenProjectLayout.isMavenProject(tempDir.toFile()));
	}

	@Test
	void detectProjectLayout_shouldReturnGradleLayoutWhenBuildGradlePresentAndNoPom() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("build.gradle"), "".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir.toFile());

		// Assert
		assertInstanceOf(GragleProjectLayout.class, layout);
		assertEquals(tempDir.toFile(), layout.getProjectDir());
		assertTrue(GragleProjectLayout.isGradleProject(tempDir.toFile()));
		assertFalse(MavenProjectLayout.isMavenProject(tempDir.toFile()));
	}

	@Test
	void detectProjectLayout_shouldReturnJScriptLayoutWhenPackageJsonPresentAndNoPomOrGradle() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir.toFile());

		// Assert
		assertInstanceOf(JScriptProjectLayout.class, layout);
		assertEquals(tempDir.toFile(), layout.getProjectDir());
		assertTrue(JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile()));
		assertFalse(GragleProjectLayout.isGradleProject(tempDir.toFile()));
		assertFalse(MavenProjectLayout.isMavenProject(tempDir.toFile()));
	}

	@Test
	void detectProjectLayout_shouldReturnPythonLayoutWhenPyprojectTomlPresentAndPublic() throws Exception {
		// Arrange
		String pyproject = "[project]\nname = 'demo'\nclassifiers = ['Development Status :: 4 - Beta']\n";
		Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir.toFile());

		// Assert
		assertInstanceOf(PythonProjectLayout.class, layout);
		assertEquals(tempDir.toFile(), layout.getProjectDir());
		assertTrue(PythonProjectLayout.isPythonProject(tempDir.toFile()));
		assertFalse(JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile()));
		assertFalse(GragleProjectLayout.isGradleProject(tempDir.toFile()));
		assertFalse(MavenProjectLayout.isMavenProject(tempDir.toFile()));
	}

	@Test
	void detectProjectLayout_shouldReturnDefaultLayoutWhenDirExistsAndNoMarkers() throws Exception {
		// Arrange
		// tempDir exists but has no marker files

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(tempDir.toFile());

		// Assert
		assertInstanceOf(DefaultProjectLayout.class, layout);
		assertEquals(tempDir.toFile(), layout.getProjectDir());
		assertFalse(MavenProjectLayout.isMavenProject(tempDir.toFile()));
		assertFalse(GragleProjectLayout.isGradleProject(tempDir.toFile()));
		assertFalse(JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile()));
		assertFalse(PythonProjectLayout.isPythonProject(tempDir.toFile()));
	}

	@Test
	void detectProjectLayout_shouldPreferMavenOverGradleWhenBothPresent() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pom.xml"), "<project/>".getBytes(StandardCharsets.UTF_8));
		Files.write(tempDir.resolve("build.gradle"), "".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = assertDoesNotThrow(() -> ProjectLayoutManager.detectProjectLayout(tempDir.toFile()));

		// Assert
		assertInstanceOf(MavenProjectLayout.class, layout);
	}
}
