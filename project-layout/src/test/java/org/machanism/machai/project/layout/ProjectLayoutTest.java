package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void projectDir_shouldSetAndReturnConfiguredProjectDir() {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout();
		File dir = tempDir.toFile();

		// Act
		ProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void getModules_defaultImplementationShouldReturnEmptyList() {
		// Arrange
		ProjectLayout layout = new ProjectLayout() {
			@Override
			public List<String> getSources() {
				return Collections.emptyList();
			}

			@Override
			public List<String> getDocuments() {
				return Collections.emptyList();
			}

			@Override
			public List<String> getTests() {
				return Collections.emptyList();
			}
		};

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void getRelativePath_instanceMethodShouldStripBasePathAndLeadingSlash() throws IOException {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout();
		Path base = Files.createDirectories(tempDir.resolve("base"));
		Path file = Files.createDirectories(base.resolve("nested")).resolve("a.txt");
		Files.write(file, "x".getBytes(StandardCharsets.UTF_8));
		String basePath = base.toFile().getAbsolutePath();

		// Act
		String relative = layout.getRelativePath(basePath, file.toFile());

		// Assert
		assertEquals("nested/a.txt", relative);
		assertFalse(relative.startsWith("/"));
		assertTrue(relative.contains("/"));
	}

	@Test
	void getRelativePath_staticShouldReturnDotWhenDirEqualsFile() {
		// Arrange
		File dir = tempDir.toFile();

		// Act
		String relative = ProjectLayout.getRelativePath(dir, dir);

		// Assert
		assertEquals(".", relative);
	}

	@Test
	void getRelativePath_staticShouldAddSingleDotPrefixWhenRequested() throws IOException {
		// Arrange
		Path child = Files.createDirectories(tempDir.resolve("child"));

		// Act
		String relative = ProjectLayout.getRelativePath(tempDir.toFile(), child.toFile(), true);

		// Assert
		assertEquals("./child", relative);
	}

	@Test
	void getRelativePath_staticShouldReturnNullWhenFileNotInsideDir() {
		// Arrange
		File dir = tempDir.toFile();
		File outside = new File(new File(dir.getParentFile(), "outside"), "file.txt");

		// Act
		String relative = ProjectLayout.getRelativePath(dir, outside, false);

		// Assert
		assertNull(relative);
	}

	@Test
	void findFiles_shouldReturnEmptyListWhenNullOrNotDirectory() {
		// Arrange
		File notDirectory = tempDir.resolve("file.txt").toFile();

		// Act
		List<File> nullDir = ProjectLayout.findFiles(null);
		List<File> notDir = ProjectLayout.findFiles(notDirectory);

		// Assert
		assertNotNull(nullDir);
		assertTrue(nullDir.isEmpty());
		assertNotNull(notDir);
		assertTrue(notDir.isEmpty());
	}

	@Test
	void findDirectories_shouldReturnEmptyListWhenNullOrNotDirectory() {
		// Arrange
		File notDirectory = tempDir.resolve("file.txt").toFile();

		// Act
		List<File> nullDir = ProjectLayout.findDirectories(null);
		List<File> notDir = ProjectLayout.findDirectories(notDirectory);

		// Assert
		assertNotNull(nullDir);
		assertTrue(nullDir.isEmpty());
		assertNotNull(notDir);
		assertTrue(notDir.isEmpty());
	}

	@Test
	void findFiles_shouldRecurseAndExcludeKnownToolingDirectories() throws IOException {
		// Arrange
		Path root = tempDir;
		Path includedDir = Files.createDirectories(root.resolve("src"));
		Path includedFile = includedDir.resolve("a.txt");
		Files.write(includedFile, "a".getBytes(StandardCharsets.UTF_8));

		Path excludedGit = Files.createDirectories(root.resolve(".git"));
		Files.write(excludedGit.resolve("shouldNotBeIncluded.txt"), "x".getBytes(StandardCharsets.UTF_8));

		Path excludedTarget = Files.createDirectories(root.resolve("target"));
		Files.write(excludedTarget.resolve("shouldNotBeIncluded2.txt"), "y".getBytes(StandardCharsets.UTF_8));

		// Act
		List<File> files = ProjectLayout.findFiles(root.toFile());

		// Assert
		assertTrue(files.stream().anyMatch(f -> f.getName().equals("a.txt")));
		assertTrue(files.stream().noneMatch(f -> f.getName().equals("shouldNotBeIncluded.txt")));
		assertTrue(files.stream().noneMatch(f -> f.getName().equals("shouldNotBeIncluded2.txt")));
	}

	@Test
	void findDirectories_shouldRecurseAndExcludeKnownToolingDirectories() throws IOException {
		// Arrange
		Path root = tempDir;
		Path includedDir = Files.createDirectories(root.resolve("src").resolve("main"));
		Files.write(includedDir.resolve("a.txt"), "a".getBytes(StandardCharsets.UTF_8));

		Files.createDirectories(root.resolve(".idea"));
		Files.createDirectories(root.resolve("build"));

		// Act
		List<File> dirs = ProjectLayout.findDirectories(root.toFile());

		// Assert
		assertTrue(dirs.stream().anyMatch(d -> d.getName().equals("src")));
		assertTrue(dirs.stream().anyMatch(d -> d.getName().equals("main")));
		assertTrue(dirs.stream().noneMatch(d -> d.getName().equals(".idea")));
		assertTrue(dirs.stream().noneMatch(d -> d.getName().equals("build")));
	}

	@Test
	void getProjectLayoutType_shouldRemoveProjectLayoutSuffix() {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout();

		// Act
		String type = layout.getProjectLayoutType();

		// Assert
		assertEquals("Maven", type);
	}

	@Test
	void getExcludeDirs_shouldReturnCloneToPreventExternalMutation() {
		// Arrange
		String[] dirs1 = ProjectLayout.getExcludeDirs();
		String originalFirst = dirs1[0];

		// Act
		dirs1[0] = "mutated";
		String[] dirs2 = ProjectLayout.getExcludeDirs();

		// Assert
		assertEquals(originalFirst, dirs2[0]);
		assertNotEquals(dirs1[0], dirs2[0]);
	}
}
