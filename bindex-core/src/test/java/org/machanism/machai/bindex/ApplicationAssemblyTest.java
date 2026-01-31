package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Bindex;

class ApplicationAssemblyTest {

	@TempDir
	File tempDir;

	@Test
	void projectDir_setsFieldAndReturnsSameInstance() {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		ApplicationAssembly assembly = new ApplicationAssembly(provider);

		// Act
		ApplicationAssembly returned = assembly.projectDir(tempDir);

		// Assert
		assertSame(assembly, returned);
	}

	@Test
	void assembly_whenProviderReturnsBlank_stillPerformsAndLogsInputs() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		doNothing().when(provider).instructions(anyString());
		doNothing().when(provider).prompt(anyString());
		doNothing().when(provider).inputsLog(any(File.class));
		when(provider.perform()).thenReturn("  ");

		Bindex b1 = new Bindex();
		b1.setId("id-1");
		b1.setName("n");
		b1.setVersion("1");

		ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

		// Act
		assembly.assembly("do something", List.of(b1));

		// Assert
		verify(provider, times(1)).perform();
		verify(provider, times(1)).inputsLog(new File(tempDir, ".machai/assembly-inputs.txt"));
		verify(provider, times(1)).instructions(anyString());
		verify(provider, times(4)).prompt(anyString());
	}

	@Test
	void assembly_whenProviderPerformThrowsRuntime_exceptionPropagates() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		doNothing().when(provider).instructions(anyString());
		doNothing().when(provider).prompt(anyString());
		doNothing().when(provider).inputsLog(any(File.class));
		when(provider.perform()).thenThrow(new RuntimeException("boom"));

		ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

		// Act + Assert
		assertThrows(RuntimeException.class, () -> assembly.assembly("do something", List.of()));
	}

	@Test
	void assembly_whenInputsLogThrowsRuntime_exceptionPropagates() throws Exception {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		doNothing().when(provider).instructions(anyString());
		doNothing().when(provider).prompt(anyString());
		doThrow(new RuntimeException("io")).when(provider).inputsLog(any(File.class));

		Bindex b1 = new Bindex();
		b1.setId("id-1");
		b1.setName("n");
		b1.setVersion("1");

		ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(tempDir);

		// Act + Assert
		assertThrows(RuntimeException.class, () -> assembly.assembly("do something", List.of(b1)));
		verify(provider, never()).perform();
	}

	@Test
	void assembly_writesInputsLogToConfiguredProjectDir() throws Exception {
		// Arrange
		File otherDir = new File(tempDir, "other");
		Files.createDirectories(otherDir.toPath());

		GenAIProvider provider = mock(GenAIProvider.class);
		doNothing().when(provider).instructions(anyString());
		doNothing().when(provider).prompt(anyString());
		doNothing().when(provider).inputsLog(any(File.class));
		when(provider.perform()).thenReturn("ok");

		ApplicationAssembly assembly = new ApplicationAssembly(provider).projectDir(otherDir);

		Bindex b1 = new Bindex();
		b1.setId("id-1");
		b1.setName("n");
		b1.setVersion("1");

		// Act
		assembly.assembly("do something", List.of(b1));

		// Assert
		verify(provider).inputsLog(new File(otherDir, ".machai/assembly-inputs.txt"));
		verify(provider, times(1)).perform();
	}
}
