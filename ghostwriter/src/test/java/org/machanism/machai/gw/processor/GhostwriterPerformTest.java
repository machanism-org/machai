package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;

class GhostwriterPerformTest {

	private AIFileProcessor processor;
	private Ghostwriter ghostwriter;

	@BeforeEach
	void setUp() {
		processor = mock(AIFileProcessor.class);
		when(processor.getProjectDir()).thenReturn(new File("."));
		ghostwriter = new Ghostwriter("Any:Model", processor);
	}

	@Test
	void perform_whenAllScanDirsProcessedSuccessfully_returnsZeroAndScansEach() throws Exception {
		// Arrange
		String[] scanDirs = { "dir1", "dir2" };

		// Act
		int exitCode = ghostwriter.perform(scanDirs);

		// Assert
		assertEquals(0, exitCode);
		// Sonar java:S6068 - avoid useless eq(...) when passing exact values.
		verify(processor).scanDocuments(new File("."), "dir1");
		// Sonar java:S6068 - avoid useless eq(...) when passing exact values.
		verify(processor).scanDocuments(new File("."), "dir2");
	}

	@Test
	void perform_whenProcessTerminationException_returnsProvidedExitCode() throws Exception {
		// Arrange
		String[] scanDirs = { "dir" };
		// Sonar java:S6068 - only remove eq(...) when not required; keep it when mixing with matchers.
		doThrow(new ProcessTerminationException("stop", 42)).when(processor).scanDocuments(any(File.class), eq("dir"));

		// Act
		int exitCode = ghostwriter.perform(scanDirs);

		// Assert
		assertEquals(42, exitCode);
	}

	@Test
	void perform_whenIllegalArgumentException_returnsOne() throws Exception {
		// Arrange
		String[] scanDirs = { "dir" };
		// Sonar java:S6068 - only remove eq(...) when not required; keep it when mixing with matchers.
		doThrow(new IllegalArgumentException("bad")).when(processor).scanDocuments(any(File.class), eq("dir"));

		// Act
		int exitCode = ghostwriter.perform(scanDirs);

		// Assert
		assertEquals(1, exitCode);
	}

	@Test
	void perform_whenUnexpectedException_returnsOne() throws Exception {
		// Arrange
		String[] scanDirs = { "dir" };
		// Sonar java:S6068 - only remove eq(...) when not required; keep it when mixing with matchers.
		doThrow(new IOException("io")).when(processor).scanDocuments(any(File.class), eq("dir"));

		// Act
		int exitCode = ghostwriter.perform(scanDirs);

		// Assert
		assertEquals(1, exitCode);
	}
}
