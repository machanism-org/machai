package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

/**
 * Targets the remaining branch-coverage gaps in {@link MavenProjectLayout}.
 */
class MavenProjectLayoutBranchCoverageTest {

	@Test
	void getModules_shouldReturnNullWhenModelIsNullSentinel() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout() {
			@Override
			public org.apache.maven.model.Model getModel() {
				return null;
			}
		}.projectDir(new java.io.File("."));

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_shouldNotApplyDefaultsWhenSourceAndTestSourceDirectoriesArePresent() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-maven-layout-sources-present-");
		Model model = new Model();
		Build build = new Build();
		String customSource = tempDir.resolve("custom-src").toFile().getAbsolutePath();
		String customTestSource = tempDir.resolve("custom-test-src").toFile().getAbsolutePath();
		build.setSourceDirectory(customSource);
		build.setTestSourceDirectory(customTestSource);
		model.setBuild(build);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		Set<String> sources = layout.getSources();

		// Assert
		assertEquals(customSource, build.getSourceDirectory());
		assertEquals(customTestSource, build.getTestSourceDirectory());
		assertTrue(sources.contains("custom-src"));
		assertFalse(sources.contains("src/main/java"));
	}

	@Test
	void getSources_shouldSkipResourcesWhenResourcesCollectionIsNull() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-maven-layout-resources-null-");
		Model model = new Model();
		Build build = new Build();
		build.setSourceDirectory(tempDir.resolve("src/main/java").toFile().getAbsolutePath());
		build.setTestSourceDirectory(tempDir.resolve("src/test/java").toFile().getAbsolutePath());
		build.setResources(null);
		model.setBuild(build);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		Set<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertEquals(1, sources.size());
		assertTrue(sources.contains("src/main/java"));
	}

	@Test
	void getTests_shouldReturnEmptyListWhenBuildHasNoTestDirectoriesNorResources() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-maven-layout-tests-empty-");
		Model model = new Model();
		Build build = new Build();
		build.setTestSourceDirectory(null);
		build.setTestResources(null);
		model.setBuild(build);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertTrue(tests.isEmpty());
	}

	@Test
	void getTests_shouldOnlyIncludeTestSourceWhenTestResourcesIsNull() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-maven-layout-tests-source-only-");
		Model model = new Model();
		Build build = new Build();
		build.setTestSourceDirectory(tempDir.resolve("custom-tests").toFile().getAbsolutePath());
		build.setTestResources(null);
		model.setBuild(build);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(1, tests.size());
		assertEquals("custom-tests", tests.get(0));
	}

	@Test
	void getModel_shouldCacheParsedModelAcrossCalls() throws Exception {
		// Arrange
		Path projectDir = Files.createTempDirectory("machai-maven-layout-model-cache-");
		Path pom = projectDir.resolve("pom.xml");
		Files.write(pom,
				("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
						+ "<modelVersion>4.0.0</modelVersion>"
						+ "<groupId>g</groupId><artifactId>a</artifactId><version>1</version>"
						+ "</project>").getBytes(StandardCharsets.UTF_8));
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir.toFile());

		// Act
		Model first = layout.getModel();
		Model second = layout.getModel();

		// Assert
		assertNotNull(first);
		assertEquals(first, second);
	}
}
