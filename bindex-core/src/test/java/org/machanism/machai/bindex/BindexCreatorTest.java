package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexCreatorTest {

	@TempDir
	Path tempDir;

	@Test
	void update_returnsSameInstance() {
		// Arrange
		BindexCreator creator = new BindexCreator(mock(GenAIProvider.class));

		// Act
		BindexCreator returned = creator.update(true);

		// Assert
		assertSame(creator, returned);
	}

	@Test
	void processFolder_throwsIllegalArgumentExceptionWhenProjectDirDoesNotExist() {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		BindexCreator creator = new BindexCreator(provider);

		ProjectLayout layout = mock(ProjectLayout.class);
		File missingDir = tempDir.resolve("does-not-exist").toFile();
		when(layout.getProjectDir()).thenReturn(missingDir);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
	}

	@Test
	void processFolder_throwsIllegalArgumentExceptionWhenExistingBindexJsonIsInvalid() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		BindexCreator creator = new BindexCreator(provider).update(false);

		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);
		Files.write(projectDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME), "{invalid-json".getBytes(StandardCharsets.UTF_8));

		ProjectLayout layout = mock(ProjectLayout.class);
		when(layout.getProjectDir()).thenReturn(projectDir.toFile());

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
	}
}
