package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;

class MavenProjectLayoutTest {

	@Test
	void isMavenProject_whenPomExists_returnsTrue() {
		// Arrange
		File projectDir = new File("src/test/resources/mockMavenProject");

		// Act
		boolean maven = MavenProjectLayout.isMavenProject(projectDir);

		// Assert
		assertTrue(maven);
	}

	@Test
	void getModules_whenPackagingIsPom_returnsModules() {
		// Arrange
		Model model = new Model();
		model.setPackaging("pom");
		model.addModule("module-a");
		model.addModule("module-b");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(new File("/repo")).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(2, modules.size());
		assertEquals("module-a", modules.get(0));
	}

	@Test
	void getModules_whenPackagingNotPom_returnsNull() {
		// Arrange
		Model model = new Model();
		model.setPackaging("jar");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(new File("/repo")).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_whenBuildHasSourceDirectoryAndResources_returnsRelatedPaths() {
		// Arrange
		File projectDir = new File("C:/repo");
		Model model = new Model();
		Build build = new Build();
		build.setSourceDirectory("C:/repo/src/main/java");
		Resource r1 = new Resource();
		r1.setDirectory("C:/repo/src/main/resources");
		build.setResources(java.util.Arrays.asList(r1));
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertEquals(2, sources.size());
		assertEquals("src/main/java", sources.get(0));
		assertEquals("src/main/resources", sources.get(1));
	}

	@Test
	void getSources_whenBuildIsNull_returnsEmptyList() {
		// Arrange
		Model model = new Model();
		model.setBuild(null);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(new File("/repo")).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertEquals(0, sources.size());
	}

	@Test
	void getTests_whenBuildHasTestDirsAndResources_returnsRelatedPaths() {
		// Arrange
		File projectDir = new File("C:/repo");
		Model model = new Model();
		Build build = new Build();
		build.setTestSourceDirectory("C:/repo/src/test/java");
		Resource r1 = new Resource();
		r1.setDirectory("C:/repo/src/test/resources");
		build.setTestResources(java.util.Arrays.asList(r1));
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertEquals(2, tests.size());
		assertEquals("src/test/java", tests.get(0));
		assertEquals("src/test/resources", tests.get(1));
	}

	@Test
	void getDocuments_returnsDefaultSrcSite() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(new File("/repo")).model(new Model());

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(1, docs.size());
		assertEquals("src/site", docs.get(0));
	}
}
