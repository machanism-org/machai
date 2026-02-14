package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultProjectLayoutTest {

	@TempDir
	File tempDir;

	@Test
	void getModules_filtersExcludedDirectoriesAndCachesResult() throws Exception {
		// Arrange
		Files.createDirectories(new File(tempDir, "moduleA").toPath());
		Files.createDirectories(new File(tempDir, "moduleB").toPath());
		Files.createDirectories(new File(tempDir, "target").toPath());
		Files.createDirectories(new File(tempDir, ".git").toPath());

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir);

		// Act
		List<String> first = layout.getModules();
		Files.createDirectories(new File(tempDir, "moduleC").toPath());
		List<String> second = layout.getModules();

		// Assert
		assertEquals(2, first.size());
		org.junit.jupiter.api.Assertions.assertTrue(first.contains("moduleA"));
		org.junit.jupiter.api.Assertions.assertTrue(first.contains("moduleB"));
		assertSame(first, second);
		org.junit.jupiter.api.Assertions.assertFalse(second.contains("moduleC"));
	}

	@Test
	void projectDir_returnsSameTypeForChaining() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();

		// Act
		DefaultProjectLayout chained = layout.projectDir(tempDir);

		// Assert
		assertSame(layout, chained);
		assertEquals(tempDir.getAbsolutePath(), chained.getProjectDir().getAbsolutePath());
	}
}
