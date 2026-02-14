package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

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
}
