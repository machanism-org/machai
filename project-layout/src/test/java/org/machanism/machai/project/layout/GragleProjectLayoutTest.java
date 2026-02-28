package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GragleProjectLayoutTest {

	@TempDir
	File tempDir;

	@Test
	void isGradleProject_whenBuildGradleExists_returnsTrue() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "build.gradle").toPath(), "plugins {}".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = GragleProjectLayout.isGradleProject(tempDir);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(result);
	}

	@Test
	void getSources_returnsDefaultSrcMain() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(java.util.Collections.singletonList("src/main"), sources);
	}

	@Test
	void getTests_returnsDefaultSrcTest() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(java.util.Collections.singletonList("src/test"), tests);
	}

	@Test
	void getDocuments_returnsSrcSite() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir);

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(java.util.Collections.singletonList("src/site"), docs);
	}

	@Test
	void getModules_whenGradleProjectHasChildren_returnsChildNames() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "build.gradle").toPath(), "plugins {}".getBytes(StandardCharsets.UTF_8));

		DomainObjectSet<GradleProject> children = StubGradleProjectFactory.domainObjectSet(
				StubGradleProjectFactory.project("child-a", StubGradleProjectFactory.domainObjectSet()),
				StubGradleProjectFactory.project("child-b", StubGradleProjectFactory.domainObjectSet()));
		GradleProject root = StubGradleProjectFactory.project("root", children);

		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir);
		Field projectField = GragleProjectLayout.class.getDeclaredField("project");
		projectField.setAccessible(true);
		projectField.set(layout, root);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		org.junit.jupiter.api.Assertions.assertTrue(modules.contains("child-a"));
		org.junit.jupiter.api.Assertions.assertTrue(modules.contains("child-b"));
	}

	@Test
	void getModules_whenGradleProjectHasNoChildren_returnsNull() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "build.gradle").toPath(), "plugins {}".getBytes(StandardCharsets.UTF_8));

		GradleProject root = StubGradleProjectFactory.project("root", StubGradleProjectFactory.domainObjectSet());
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir);
		Field projectField = GragleProjectLayout.class.getDeclaredField("project");
		projectField.setAccessible(true);
		projectField.set(layout, root);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}
}
