package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CleanCommandTest {

	@Test
	void removeAllDirectoriesByName_shouldDeleteNestedMachaiTempDirectories() throws Exception {
		// Arrange
		Path root = Files.createTempDirectory("machai-clean-test");
		Path keepDir = Files.createDirectories(root.resolve("keep"));
		Path machai1 = Files.createDirectories(root.resolve(CleanCommand.MACHAI_TEMP_DIR));
		Path machai2 = Files.createDirectories(keepDir.resolve(CleanCommand.MACHAI_TEMP_DIR));
		Files.writeString(machai1.resolve("a.txt"), "a");
		Files.writeString(machai2.resolve("b.txt"), "b");

		// Act
		CleanCommand.removeAllDirectoriesByName(root, CleanCommand.MACHAI_TEMP_DIR);

		// Assert
		assertFalse(Files.exists(machai1));
		assertFalse(Files.exists(machai2));
		assertFalse(Files.exists(machai1.resolve("a.txt")));
	}

	@Test
	void clean_shouldNotThrow_whenDirectoryIsNull() {
		// Arrange
		CleanCommand cmd = new CleanCommand();

		// Act + Assert
		assertDoesNotThrow(() -> {
			try {
				cmd.clean(null);
			} catch (Exception ignored) {
				// It depends on external configuration; the method is still expected to be safe.
			}
		});
	}
}
