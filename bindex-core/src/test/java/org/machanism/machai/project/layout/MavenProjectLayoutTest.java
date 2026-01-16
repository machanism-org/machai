package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Disabled;
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
	void getModel_readsPom() {
		// Arrange
		File projectDir = new File("src/test/resources/mockMavenProject");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);

		// Act
		Model model = layout.getModel();

		// Assert
		assertNotNull(model);
		assertEquals("mock-project", model.getArtifactId());
	}

	@Test
	void getModules_whenPackagingNotPom_returnsNull() {
		// Arrange
		File projectDir = new File("src/test/resources/mockMavenProject");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(null, modules);
	}

	@Test
	@Disabled
	void getSources_returnsSourceAndResourceDirectoriesAsRelativePaths() {
		// Arrange
		File projectDir = new File("src/test/resources/mockMavenProject");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertEquals(2, sources.size());
		assertTrue(sources.contains("src/main/java"));
		assertTrue(sources.contains("src/main/resources"));
	}

	@Test
	@Disabled
	void getTests_returnsTestSourceAndTestResourceDirectoriesAsRelativePaths() {
		// Arrange
		File projectDir = new File("src/test/resources/mockMavenProject");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertEquals(2, tests.size());
		assertTrue(tests.contains("src/test/java"));
		assertTrue(tests.contains("src/test/resources"));
	}

	@Test
	void getDocuments_returnsSrcSite() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout();

		// Act
		List<String> documents = layout.getDocuments();

		// Assert
		assertNotNull(documents);
		assertEquals(1, documents.size());
		assertEquals("src/site", documents.get(0));
	}

	@Test
	void fluentSetters_returnSameInstance() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout();

		// Act
		MavenProjectLayout afterModel = layout.model(new Model());
		MavenProjectLayout afterEffective = layout.effectivePomRequired(true);

		// Assert
		assertEquals(layout, afterModel);
		assertEquals(layout, afterEffective);
	}
}
