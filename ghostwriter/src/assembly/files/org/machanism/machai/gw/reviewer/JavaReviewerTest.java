package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaReviewerTest {

	@TempDir
	File tempDir;

	@Test
	void getSupportedFileExtensions_returnsJava() {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();

		// Act
		String[] extensions = reviewer.getSupportedFileExtensions();

		// Assert
		assertEquals(1, extensions.length);
		assertEquals("java", extensions[0]);
	}

	@Test
	void perform_returnsNull_whenFileContainsNoGuidanceTag() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();
		File javaFile = new File(tempDir, "Example.java");
		Files.writeString(javaFile.toPath(), "package a;\npublic class Example {}\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, javaFile);

		// Assert
		assertNull(result);
	}

	@Test
	void perform_returnsFormattedPrompt_whenLineCommentGuidancePresent() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();
		File javaFile = new File(tempDir, "Example.java");
		String content = "package a;\n// @guidance: please document this\npublic class Example {}\n";
		Files.writeString(javaFile.toPath(), content, StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, javaFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("Example.java"));
		assertEquals(true, result.contains("please document this"));
		assertEquals(true, result.contains(content));
	}

	@Test
	void perform_returnsFormattedPrompt_whenBlockCommentGuidancePresent() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();
		File javaFile = new File(tempDir, "Example.java");
		Files.writeString(javaFile.toPath(), "/* @guidance: block */\npublic class Example {}\n",
				StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, javaFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("Example.java"));
		assertEquals(true, result.contains("block"));
	}

	@Test
	void perform_returnsPackageInfoPrompt_whenGuidanceInPackageInfo() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();
		File pkg = new File(tempDir, "package-info.java");
		Files.writeString(pkg.toPath(), "/** @guidance: pkg */\npackage a;\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, pkg);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("package-info.java"));
	}

	@Test
	void perform_throwsIllegalArgumentException_whenMalformedInput() throws IOException {
		// Arrange
		JavaReviewer reviewer = new JavaReviewer();
		File javaFile = new File(tempDir, "Example.java");
		Files.write(javaFile.toPath(), new byte[] { (byte) 0xC3, (byte) 0x28 });

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> reviewer.perform(tempDir, javaFile));
	}

	@Test
	void extractPackageName_findsPackageName() {
		// Arrange
		String src = "package org.machanism.example;";

		// Act
		String pkgName = JavaReviewer.extractPackageName(src);

		// Assert
		assertEquals("org.machanism.example", pkgName);
	}

	@Test
	void extractPackageName_returnsDefaultPackage_whenNotFound() {
		// Arrange
		String src = "public class Example {}";

		// Act
		String pkgName = JavaReviewer.extractPackageName(src);

		// Assert
		assertEquals("<default package>", pkgName);
	}
}
