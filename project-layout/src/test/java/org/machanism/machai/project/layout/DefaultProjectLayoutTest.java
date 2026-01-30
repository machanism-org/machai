package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class DefaultProjectLayoutTest {

	@Test
	void getModules_whenContainsSubprojects_detectsNonDefaultLayoutsAndExcludesStandardDirs() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("default-layout-");
		File projectDir = tempDir.toFile();

		Files.createDirectories(tempDir.resolve("target"));
		Files.createDirectories(tempDir.resolve("moduleMaven"));
		Files.createDirectories(tempDir.resolve("moduleJs"));
		Files.createDirectories(tempDir.resolve("plainDir"));

		writeFile(tempDir.resolve("moduleMaven").resolve("pom.xml"),
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><modelVersion>4.0.0</modelVersion>"
						+ "<groupId>a</groupId><artifactId>b</artifactId><version>1</version></project>");
		writeFile(tempDir.resolve("moduleJs").resolve("package.json"), "{\"name\":\"a\"}");

		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(projectDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(2, modules.size());
		org.junit.jupiter.api.Assertions.assertTrue(modules.contains("moduleMaven"));
		org.junit.jupiter.api.Assertions.assertTrue(modules.contains("moduleJs"));
	}

	private static void writeFile(Path path, String content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
			fos.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}
}
