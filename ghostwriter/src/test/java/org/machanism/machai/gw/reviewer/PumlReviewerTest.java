package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PumlReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsPuml() {
		PumlReviewer reviewer = new PumlReviewer();
		assertArrayEquals(new String[] { "puml" }, reviewer.getSupportedFileExtensions());
	}

	@Test
	void perform_throwsWhenArgumentsNull() {
		PumlReviewer reviewer = new PumlReviewer();
		assertThrows(NullPointerException.class, () -> reviewer.perform(null, null));
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		PumlReviewer reviewer = new PumlReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("diagram.puml");
		Files.write(file, "@startuml\nAlice -> Bob\n@enduml\n".getBytes(StandardCharsets.UTF_8));

		assertNull(reviewer.perform(project.toFile(), file.toFile()));
	}

	@Test
	void perform_formatsWhenGuidanceTagPresent() throws IOException {
		PumlReviewer reviewer = new PumlReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("docs").resolve("diagram.puml");
		Files.createDirectories(file.getParent());
		String content = "' @guidance: include\n@startuml\nAlice -> Bob\n@enduml\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
	}
}
