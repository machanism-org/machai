package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.mockito.MockedStatic;

class AssembyCommandBehaviorTest {

	@TempDir
	File tempDir;

	@Test
	void pick_whenQueryIsNull_shouldPromptAndAlwaysLogUsage_evenIfPickerFailsFast() {
		// Arrange
		LineReader lineReader = mock(LineReader.class);
		when(lineReader.readLine("Prompt: ")).thenReturn("my query");
		AssembyCommand cmd = new AssembyCommand(lineReader);

		try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class)) {
			// Act: we don't have a reliable way to isolate Picker construction here (final/JDK21),
			// so we only assert the finally-block behavior.
			assertThrows(Exception.class, () -> cmd.pick(null, null, null, null));

			// Assert
			providerManager.verify(GenAIProviderManager::logUsage);
		}
	}

	@Test
	void assembly_whenQueryNullAndNoPreviousPick_shouldThrowIllegalArgumentExceptionAndLogUsage() {
		// Arrange
		LineReader lineReader = mock(LineReader.class);
		when(lineReader.readLine("Project assembly prompt: ")).thenReturn(null);
		AssembyCommand cmd = new AssembyCommand(lineReader);

		try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class)) {
			// Act
			assertThrows(IllegalArgumentException.class, () -> cmd.assembly(null, tempDir, null, null, null));

			// Assert
			providerManager.verify(GenAIProviderManager::logUsage);
		}
	}
}
