package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;

class ApplicationAssemblyTest {

	@TempDir
	Path tempDir;

	@Test
	void projectDir_returnsSameInstanceAndIsUsedForInputsLogFileLocation() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		ApplicationAssembly assembly = new ApplicationAssembly(provider);
		File projectDir = tempDir.toFile();
		when(provider.perform()).thenReturn(null);

		// Act
		ApplicationAssembly returned = assembly.projectDir(projectDir);
		assembly.assembly("any", Collections.emptyList());

		// Assert
		assertSame(assembly, returned);
		verify(provider).inputsLog(new File(projectDir, ".machai/assembly-inputs.txt"));
	}

	@Test
	void assembly_usesProvidedProjectDirForInputsLogPath() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		ApplicationAssembly assembly = new ApplicationAssembly(provider);

		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);
		when(provider.perform()).thenReturn(null);
		assembly.projectDir(projectDir.toFile());

		// Act
		assembly.assembly("p", Collections.emptyList());

		// Assert
		verify(provider).inputsLog(new File(projectDir.toFile(), ".machai/assembly-inputs.txt"));
	}
}
