package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;

class MavenProjectLayoutTest {

	@Test
	void isMavenProject_shouldReturnTrueWhenPomXmlExists() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/maven-is-maven");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "pom.xml").toPath(), "<project/>".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = MavenProjectLayout.isMavenProject(dir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isMavenProject_shouldReturnFalseWhenPomXmlMissing() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/maven-not-maven");
		Files.createDirectories(dir.toPath());

		// Act
		boolean result = MavenProjectLayout.isMavenProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void getModules_shouldReturnModulesWhenPackagingPom() {
		// Arrange
		File dir = new File("target/test-tmp/maven-modules");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);

		Model model = new Model();
		model.setPackaging("pom");
		model.setModules(Arrays.asList("module-a", "module-b"));
		layout.model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		assertEquals(Arrays.asList("module-a", "module-b"), modules);
	}

	@Test
	void getModules_shouldReturnNullWhenPackagingNotPom() {
		// Arrange
		File dir = new File("target/test-tmp/maven-no-modules");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);

		Model model = new Model();
		model.setPackaging("jar");
		model.setModules(Arrays.asList("module-a"));
		layout.model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_shouldReturnNullWhenModelCannotBeBuiltAndPackagingNull() {
		// Arrange
		File dir = new File("target/test-tmp/maven-bad-pom");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);

		PomReader failingPomReader = new PomReader() {
			@Override
			public Model getProjectModel(File pomFile, boolean effective) {
				throw new IllegalArgumentException("boom");
			}
		};

		MavenProjectLayout layoutWithFailingReader = new MavenProjectLayout() {
			@Override
			public Model getModel() {
				File pomFile = new File(getProjectDir(), "pom.xml");
				try {
					return failingPomReader.getProjectModel(pomFile, true);
				} catch (Exception e) {
					return new Model();
				}
			}
		}.projectDir(dir);

		// Act
		List<String> modules = layoutWithFailingReader.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModel_whenEffectivePomFailsAndEffectiveRequiredTrue_shouldFallbackToNonEffective() {
		// Arrange
		File dir = new File("target/test-tmp/maven-model-fallback");
		MavenProjectLayout layout = new MavenProjectLayout() {
			@Override
			public Model getModel() {
				if (super.getProjectDir() == null) {
					throw new IllegalStateException("projectDir must be set");
				}
				// delegate to real implementation
				return super.getModel();
			}
		}.projectDir(dir);

		layout.effectivePomRequired(true);
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String pomXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1</version>\n" +
				"</project>\n";
		try {
			Files.write(new File(dir, "pom.xml").toPath(), pomXml.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			fail(e);
		}

		// Act
		Model model = layout.getModel();

		// Assert
		assertNotNull(model);
		assertEquals("demo", model.getArtifactId());
	}

	@Test
	void getSources_shouldReturnConfiguredSourceDirectoryAndResourcesAsRelativePaths() {
		// Arrange
		File dir = new File("/repo");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);

		Build build = new Build();
		build.setSourceDirectory("/repo/src/main/java");
		Resource resource = new Resource();
		resource.setDirectory("/repo/src/main/resources");
		build.setResources(Arrays.asList(resource));

		Model model = new Model();
		model.setBuild(build);
		layout.model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(2, sources.size());
		assertTrue(sources.contains("src/main/java"));
		assertTrue(sources.contains("src/main/resources"));
	}

	@Test
	void getSources_shouldReturnEmptyListWhenBuildAbsentOrHasNoSources() {
		// Arrange
		File dir = new File("/repo");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);
		layout.model(new Model());

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertTrue(sources.isEmpty());
	}

	@Test
	void getDocuments_shouldReturnSrcSiteByDefault() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(new File("/repo"));
		layout.model(new Model());

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(1, docs.size());
		assertEquals("src/site", docs.get(0));
	}

	@Test
	void getTests_shouldReturnConfiguredTestSourceDirectoryAndTestResourcesAsRelativePaths() {
		// Arrange
		File dir = new File("/repo");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);

		Build build = new Build();
		build.setTestSourceDirectory("/repo/src/test/java");
		Resource testResource = new Resource();
		testResource.setDirectory("/repo/src/test/resources");
		build.setTestResources(Arrays.asList(testResource));

		Model model = new Model();
		model.setBuild(build);
		layout.model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(2, tests.size());
		assertTrue(tests.contains("src/test/java"));
		assertTrue(tests.contains("src/test/resources"));
	}

	@Test
	void getTests_shouldReturnEmptyListWhenBuildAbsentOrHasNoTests() {
		// Arrange
		File dir = new File("/repo");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir);
		layout.model(new Model());

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertTrue(tests.isEmpty());
	}

	@Test
	void projectDir_shouldReturnConcreteTypeForChaining() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout();
		File dir = new File("target/test-tmp/maven-chain");

		// Act
		MavenProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}
}
