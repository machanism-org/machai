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

	@Test
	void extractPackageName_returnsPackageWhenPresent() {
		// Arrange
		String content = "/* some header */\npackage org.example.test;\n// tail";

		// Act
		String result = JavaReviewer.extractPackageName(content);

		// Assert
		assertEquals("org.example.test", result);
	}

	@Test
	void extractPackageName_returnsDefaultWhenMissing() {
		// Arrange
		String content = "// no package here\npublic class X {}";

		// Act
		String result = JavaReviewer.extractPackageName(content);

		// Assert
		assertEquals("<default package>", result);
	}

	@Test
	void extractPackageName_supportsUnderscoreAndDigits() {
		// Arrange
		String content = "package org.exa_mple.v2;";

		// Act
		String result = JavaReviewer.extractPackageName(content);

		// Assert
		assertEquals("org.exa_mple.v2", result);
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

	@Test
	void perform_returnsFormattedOutputWhenGuidancePresentInLineComment() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("src").resolve("main").resolve("java").resolve("A.java");
		Files.createDirectories(file.getParent());
		Files.write(file, ("// " + GuidanceProcessor.GUIDANCE_TAG_NAME + " keep\npublic class A {}\n")
				.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_returnsFormattedOutputWhenGuidancePresentInBlockComment() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("A.java");
		Files.write(file, ("/*\n * header\n * " + GuidanceProcessor.GUIDANCE_TAG_NAME + " keep\n */\npublic class A {}\n")
				.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_formatsPackageInfoWhenGuidancePresent() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path pkg = project.resolve("src").resolve("main").resolve("java").resolve("org").resolve("example");
		Files.createDirectories(pkg);
		Path file = pkg.resolve("package-info.java");
		Files.write(file, ("/* " + GuidanceProcessor.GUIDANCE_TAG_NAME + " package docs */\npackage org.example;\n")
				.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
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
