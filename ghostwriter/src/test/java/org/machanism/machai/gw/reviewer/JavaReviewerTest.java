package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
		JavaReviewer reviewer = new JavaReviewer();
		assertArrayEquals(new String[] { "java" }, reviewer.getSupportedFileExtensions());
	}

	@Test
	void extractPackageName_returnsPackageWhenPresent() {
		String content = "/* some header */\npackage org.example.test;\n// tail";
		assertEquals("org.example.test", JavaReviewer.extractPackageName(content));
	}

	@Test
	void extractPackageName_returnsDefaultWhenMissing() {
		String content = "// no package here\npublic class X {}";
		assertEquals("<default package>", JavaReviewer.extractPackageName(content));
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("A.java");
		Files.write(file, "public class A {}".getBytes(StandardCharsets.UTF_8));

		assertNull(reviewer.perform(project.toFile(), file.toFile()));
	}

	@Test
	void perform_formatsJavaFileWhenGuidancePresentInLineComment() throws IOException {
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("src").resolve("main").resolve("java").resolve("A.java");
		Files.createDirectories(file.getParent());
		Files.write(file, ("// " + GuidanceProcessor.GUIDANCE_TAG_NAME + " keep\npublic class A {}\n")
				.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
		assertEquals(true, result.contains("Path: `src/main/java/A.java`"));
	}

	@Test
	void perform_formatsPackageInfoWhenGuidancePresent() throws IOException {
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path pkg = project.resolve("src").resolve("main").resolve("java").resolve("org").resolve("example");
		Files.createDirectories(pkg);
		Path file = pkg.resolve("package-info.java");
		Files.write(file, ("/* " + GuidanceProcessor.GUIDANCE_TAG_NAME + " package docs */\npackage org.example;\n")
				.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
		assertEquals(true, result.contains("Path: `src/main/java/org/example/package-info.java`"));
	}

	@Test
	void perform_formatsJavaFileWhenGuidancePresentInBlockComment() throws IOException {
		JavaReviewer reviewer = new JavaReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("A.java");
		Files.write(file, ("/*\n * header\n * " + GuidanceProcessor.GUIDANCE_TAG_NAME + " keep\n */\npublic class A {}\n")
				.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
		assertEquals(true, result.contains("Path: `A.java`"));
	}

	@Test
	void perform_throwsIOExceptionForMissingFile() {
		JavaReviewer reviewer = new JavaReviewer();
		File project = tempDir.toFile();
		File missing = new File(project, "missing.java");
		try {
			reviewer.perform(project, missing);
		} catch (IOException e) {
			assertNotNull(e);
			return;
		}
		throw new AssertionError("Expected IOException");
	}
}
