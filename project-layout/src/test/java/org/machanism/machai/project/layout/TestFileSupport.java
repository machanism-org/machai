package org.machanism.machai.project.layout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

final class TestFileSupport {
	private TestFileSupport() {
	}

	static void deleteRecursivelyBestEffort(Path dir) throws IOException {
		if (dir == null || !Files.exists(dir)) {
			return;
		}

		Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(p -> {
			try {
				Files.deleteIfExists(p);
			} catch (IOException e) {
				// Best-effort cleanup; Windows file locks can prevent deletion in CI.
			}
		});
	}
}
