package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GragleProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void isGradleProject_shouldReturnTrueWhenBuildGradleExists() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("build.gradle"), "// gradle".getBytes());

		// Act
		boolean result = GragleProjectLayout.isGradleProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isGradleProject_shouldReturnFalseWhenBuildGradleMissing() {
		// Arrange
		// Act
		boolean result = GragleProjectLayout.isGradleProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void getModules_shouldThrowNullPointerExceptionWhenNoGradleProjectModelAvailable() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act & Assert
		assertThrows(NullPointerException.class, layout::getModules);
	}

	@Test
	void getProjectId_shouldThrowNullPointerExceptionWhenNoGradleProjectModelAvailable() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act & Assert
		assertThrows(NullPointerException.class, layout::getProjectId);
	}

	@Test
	void getProjectName_shouldThrowNullPointerExceptionWhenNoGradleProjectModelAvailable() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act & Assert
		assertThrows(NullPointerException.class, layout::getProjectName);
	}

	@Test
	void getSources_shouldReturnDefaultGradleSourceRoot() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(1, sources.size());
		assertEquals("src/main", sources.get(0));
	}

	@Test
	void getDocuments_shouldReturnDefaultSiteDirectory() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(1, docs.size());
		assertEquals("src/site", docs.get(0));
	}

	@Test
	void getTests_shouldReturnDefaultGradleTestRoot() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(1, tests.size());
		assertEquals("src/test", tests.get(0));
	}

	@Test
	void projectDir_shouldReturnSameConcreteType() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout();
		File dir = tempDir.toFile();

		// Act
		GragleProjectLayout result = layout.projectDir(dir);

		// Assert
		assertSame(layout, result);
		assertSame(dir, result.getProjectDir());
	}

	@Test
	void getModules_shouldReturnNullWhenNoChildren() throws Exception {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		GradleProject root = StubGradleProjectFactory.project("root", StubGradleProjectFactory.domainObjectSet());
		setPrivateProjectField(layout, root);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getModules_shouldReturnChildrenNamesWhenChildrenPresent() throws Exception {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		GradleProject childA = StubGradleProjectFactory.project("a", StubGradleProjectFactory.domainObjectSet());
		GradleProject childB = StubGradleProjectFactory.project("b", StubGradleProjectFactory.domainObjectSet());
		DomainObjectSet<GradleProject> children = StubGradleProjectFactory.domainObjectSet(childA, childB);
		GradleProject root = StubGradleProjectFactory.project("root", children);
		setPrivateProjectField(layout, root);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(2, modules.size());
		assertTrue(modules.contains("a"));
		assertTrue(modules.contains("b"));
	}

	@Test
	void getProjectIdAndName_shouldReturnGradleProjectName() throws Exception {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());
		GradleProject root = StubGradleProjectFactory.project("demo", StubGradleProjectFactory.domainObjectSet());
		setPrivateProjectField(layout, root);

		// Act
		String id = layout.getProjectId();
		String name = layout.getProjectName();

		// Assert
		assertEquals("demo", id);
		assertEquals("demo", name);
	}

	private static void setPrivateProjectField(GragleProjectLayout layout, GradleProject project) throws Exception {
		Field field = GragleProjectLayout.class.getDeclaredField("project");
		field.setAccessible(true);
		field.set(layout, project);
	}
}
