package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexRegisterTest {

	@TempDir
	Path tempDir;

	@Test
	void update_returnsSameInstance() {
		// Arrange
		BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost:27017");

		// Act
		BindexRegister returned = register.update(true);

		// Assert
		assertSame(register, returned);
	}

	@Test
	void processFolder_throwsIllegalArgumentExceptionWhenBindexJsonIsInvalid() throws Exception {
		// Arrange
		BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost:27017");

		ProjectLayout layout = mock(ProjectLayout.class);
		when(layout.getProjectDir()).thenReturn(tempDir.toFile());
		Files.write(tempDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME), "{invalid".getBytes("UTF-8"));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> register.processFolder(layout));
	}

	@Test
	void processFolder_doesNothingWhenNoBindexFilePresent() {
		// Arrange
		BindexRegister register = new BindexRegister(mock(GenAIProvider.class), "mongodb://localhost:27017");

		ProjectLayout layout = mock(ProjectLayout.class);
		File projectDir = tempDir.toFile();
		when(layout.getProjectDir()).thenReturn(projectDir);

		// Act
		register.processFolder(layout);

		// Assert
		// No exception indicates correct handling of missing bindex.json.
	}

	@Test
	void close_delegatesToPickerClose() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		BindexRegister register = new BindexRegister(provider, "mongodb://localhost:27017");

		Picker picker = mock(Picker.class);
		Field f = BindexRegister.class.getDeclaredField("picker");
		f.setAccessible(true);
		f.set(register, picker);

		// Act
		register.close();

		// Assert
		verify(picker).close();
	}
}
