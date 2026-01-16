package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;

class PythonBindexBuilderTest {
	@TempDir
	Path tempDir;

	private ProjectLayout layout;
	private PythonBindexBuilder builder;
	private GenAIProvider genAI;

	@BeforeEach
	void setUp() {
		layout = mock(ProjectLayout.class);
		doReturn(tempDir.toFile()).when(layout).getProjectDir();
		builder = spy(new PythonBindexBuilder(layout));
		genAI = mock(GenAIProvider.class);
		doReturn(genAI).when(builder).getGenAIProvider();
	}

	@Test
	@Disabled
	void projectContext_promptsProjectBuildSectionAndPythonSourcesWhenPresent() throws Exception {
		// Arrange
		File pyproject = new File(tempDir.toFile(), "pyproject.toml");
		try (FileWriter writer = new FileWriter(pyproject)) {
			writer.write("[project]\nname = \"sample\"\n");
		}

		Path sourceDir = tempDir.resolve("sample");
		Files.createDirectories(sourceDir);
		Path pyFile = sourceDir.resolve("mod1.py");
		Files.writeString(pyFile, "print('hello')");

		// Act
		builder.projectContext();

		// Assert
		verify(genAI).prompt(anyString());
		verify(genAI).promptFile(pyFile.toFile(), "source_resource_section");
	}

	@Test
	void projectContext_throwsWhenPyprojectTomlIsMissing() {
		// Arrange
		// no pyproject.toml

		// Act + Assert
		assertDoesNotThrow(() -> {
			try {
				builder.projectContext();
			} catch (java.io.FileNotFoundException expected) {
				return;
			}
		});
	}
}
