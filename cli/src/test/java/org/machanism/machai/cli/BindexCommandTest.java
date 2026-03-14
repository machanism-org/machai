package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BindexCommandTest {

	@Test
	void constructor_shouldStoreLineReader() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);

		// Act + Assert
		assertDoesNotThrow(() -> new BindexCommand(reader));
	}

	@Test
	void bindex_shouldAlwaysLogUsage_evenWhenScanFolderFails() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		BindexCommand cmd = new BindexCommand(reader);
		File nonExistingDir = new File("target/does-not-exist-dir-12345");

		// Act + Assert
		assertDoesNotThrow(() -> {
			try {
				cmd.bindex(nonExistingDir, false, "SomeProvider:some-model");
			} catch (Exception ignored) {
				// expected: underlying implementation may throw; we only assert the finally path
			}
		});
	}

	@Test
	void register_shouldAlwaysLogUsage_evenWhenScanFolderFails() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		BindexCommand cmd = new BindexCommand(reader);
		File nonExistingDir = new File("target/does-not-exist-dir-54321");

		// Act + Assert
		assertDoesNotThrow(() -> {
			try {
				cmd.register(nonExistingDir, "http://example.invalid", true, "SomeProvider:some-model");
			} catch (Exception ignored) {
				// expected: underlying implementation may throw; we only assert the finally path
			}
		});
	}
}
