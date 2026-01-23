package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class PythonBindexBuilderTest {
	@TempDir
	Path tempDir;

	private ProjectLayout layout;
	private PythonBindexBuilder builder;

	@BeforeEach
	void setUp() {
		layout = mock(ProjectLayout.class);
		doReturn(tempDir.toFile()).when(layout).getProjectDir();
		builder = spy(new PythonBindexBuilder(layout));
		GenAIProvider genAI = mock(GenAIProvider.class);
		doReturn(genAI).when(builder).getGenAIProvider();
	}

	@Test
	void projectContext_throwsFileNotFoundExceptionWhenPyprojectTomlIsMissing() {
		// Arrange
		File pyproject = new File(tempDir.toFile(), "pyproject.toml");
		org.junit.jupiter.api.Assertions.assertFalse(pyproject.exists());

		// Act + Assert
		assertThrows(java.io.FileNotFoundException.class, () -> builder.projectContext());
	}
}
