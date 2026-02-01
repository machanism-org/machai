package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class ProjectLayoutManagerTest {

	@Test
	void detectProjectLayout_shouldReturnMavenProjectLayoutWhenPomXmlPresent() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/detect-layout-maven");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "pom.xml").toPath(), "<project/>".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertNotNull(layout);
		assertInstanceOf(MavenProjectLayout.class, layout);
		assertEquals(dir.getCanonicalFile(), layout.getProjectDir().getCanonicalFile());
	}

	@Test
	void detectProjectLayout_shouldReturnJScriptProjectLayoutWhenPackageJsonPresentAndNoPomXml() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/detect-layout-js");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "package.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertNotNull(layout);
		assertInstanceOf(JScriptProjectLayout.class, layout);
		assertEquals(dir.getCanonicalFile(), layout.getProjectDir().getCanonicalFile());
	}

	@Test
	void detectProjectLayout_shouldReturnPythonProjectLayoutWhenPyprojectTomlIndicatesPublicProjectAndNoOtherDescriptors()
			throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/detect-layout-python");
		Files.createDirectories(dir.toPath());
		String pyproject = "[project]\n" + "name = \"demo\"\n";
		Files.write(new File(dir, "pyproject.toml").toPath(), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertNotNull(layout);
		assertInstanceOf(PythonProjectLayout.class, layout);
		assertEquals(dir.getCanonicalFile(), layout.getProjectDir().getCanonicalFile());
	}

	@Test
	void detectProjectLayout_shouldReturnDefaultProjectLayoutWhenDirExistsButNoDescriptors() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/detect-layout-default");
		Files.createDirectories(dir.toPath());

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);

		// Assert
		assertNotNull(layout);
		assertInstanceOf(DefaultProjectLayout.class, layout);
		assertEquals(dir.getCanonicalFile(), layout.getProjectDir().getCanonicalFile());
	}

	@Test
	void detectProjectLayout_shouldThrowFileNotFoundExceptionWhenDirDoesNotExist() {
		// Arrange
		File dir = new File("target/test-tmp/does-not-exist-12345");
		if (dir.exists()) {
			assertTrue(dir.delete());
		}
		assertFalse(dir.exists());

		// Act
		FileNotFoundException ex = assertThrows(FileNotFoundException.class,
				() -> ProjectLayoutManager.detectProjectLayout(dir));

		// Assert
		assertTrue(ex.getMessage().contains(dir.getAbsolutePath()));
	}
}
