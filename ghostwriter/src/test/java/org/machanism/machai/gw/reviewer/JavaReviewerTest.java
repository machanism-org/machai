package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.machanism.machai.gw.processor.GuidanceProcessor;

class JavaReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsJava() {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		// Act + Assert
		assertArrayEquals(new String[] { "java" }, reviewer.getSupportedFileExtensions());
	}

	@ParameterizedTest
	@CsvSource({
			"'/* some header */\npackage org.example.test;\n// tail', org.example.test",
			"'// no package here\npublic class X {}', <default package>",
			"'package org.exa_mple.v2;', org.exa_mple.v2"
	})
	void extractPackageName_variousCases(String content, String expected) {
		// Act
		String result = JavaReviewer.extractPackageName(content);

		// Assert
		assertEquals(expected, result);
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("A.java");
		Files.write(file, "public class A {}".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_returnsNullWhenGuidanceTagPresentButNoCommentMatches() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("A.java");
		Files.write(file, ("public class A { String s = \"" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\"; }")
				.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	// Sonar java:S5976 - merge similar tests into a single parameterized test.
	@ParameterizedTest
	@CsvSource({
			"'src/main/java/A.java', '// " + GuidanceProcessor.GUIDANCE_TAG_NAME + " keep\npublic class A {}\n'",
			"'A.java', '/*\n * header\n * " + GuidanceProcessor.GUIDANCE_TAG_NAME + " keep\n */\npublic class A {}\n'",
			"'src/main/java/org/example/package-info.java', '/* " + GuidanceProcessor.GUIDANCE_TAG_NAME
					+ " package docs */\npackage org.example;\n'"
	})
	void perform_returnsFormattedOutputWhenGuidancePresent(String relativePath, String fileContent) throws IOException {
		assertGuidanceIsExtracted(relativePath, fileContent);
	}

	private void assertGuidanceIsExtracted(String relativePath, String fileContent) throws IOException {
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.write(file, fileContent.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
	}

	@Test
	void perform_throwsIOExceptionForMissingFile() {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();
		File project = tempDir.toFile();
		File missing = new File(project, "missing.java");

		// Act + Assert
		assertThrows(IOException.class, () -> reviewer.perform(project, missing));
	}
}
