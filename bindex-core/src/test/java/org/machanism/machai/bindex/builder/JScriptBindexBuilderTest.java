package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;

class JScriptBindexBuilderTest {
	@TempDir
	Path tempDir;

	private ProjectLayout layout;
	private JScriptBindexBuilder builder;
	private GenAIProvider genAI;

	@BeforeEach
	void setUp() {
		layout = Mockito.mock(ProjectLayout.class);
		Mockito.when(layout.getProjectDir()).thenReturn(tempDir.toFile());

		builder = Mockito.spy(new JScriptBindexBuilder(layout));
		genAI = Mockito.mock(GenAIProvider.class);
		Mockito.doReturn(genAI).when(builder).getGenAIProvider();
	}

	@Test
	void projectContext_readsPackageJsonAndPromptsSourceFiles() throws Exception {
		// Arrange
		File packageFile = new File(tempDir.toFile(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
		try (FileWriter writer = new FileWriter(packageFile, StandardCharsets.UTF_8)) {
			writer.write("{\"name\":\"x\"}");
		}

		Path srcDir = tempDir.resolve("src");
		Files.createDirectories(srcDir);
		Path sourceFile = srcDir.resolve("test.js");
		Files.write(sourceFile, "console.log('hello');".getBytes(StandardCharsets.UTF_8));

		// Act
		builder.projectContext();

		// Assert
		Mockito.verify(genAI, Mockito.atLeastOnce()).prompt(Mockito.anyString());
		Mockito.verify(genAI).promptFile(sourceFile.toFile(), "source_resource_section");
	}

	@Test
	void projectContext_handlesMissingSrcFolderGracefully() throws Exception {
		// Arrange
		File packageFile = new File(tempDir.toFile(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
		try (FileWriter writer = new FileWriter(packageFile, StandardCharsets.UTF_8)) {
			writer.write("{\"name\":\"x\"}");
		}
		// no src

		// Act + Assert
		assertDoesNotThrow(() -> builder.projectContext());
		Mockito.verify(genAI, Mockito.atLeastOnce()).prompt(Mockito.anyString());
	}
}
