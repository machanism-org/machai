package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CleanTest {

	private static class TestClean extends Clean {
		void setBasedir(File dir) {
			this.basedir = dir;
		}
	}

	@TempDir
	File tempDir;

	@Test
	void execute_returnsImmediatelyWhenFileDoesNotExist() {
		// Arrange
		TestClean mojo = new TestClean();
		mojo.setBasedir(tempDir);

		// Act + Assert
		// Sonar java:S1612 - replace lambda with method reference.
		assertDoesNotThrow(mojo::execute);
	}

	@Test
	void execute_deletesExistingInputsLogFile() throws Exception {
		// Arrange
		File machaiDir = new File(tempDir, Clean.MACHAI_TEMP_DIR);
		assertTrue(machaiDir.mkdirs());
		File logFile = new File(machaiDir, "bindex-inputs.txt");
		Files.write(logFile.toPath(), "x".getBytes(java.nio.charset.StandardCharsets.UTF_8));
		assertTrue(logFile.exists());

		TestClean mojo = new TestClean();
		mojo.setBasedir(tempDir);

		// Act
		mojo.execute();

		// Assert
		assertFalse(logFile.exists());
	}

	@Test
	void execute_wrapsIOExceptionFromDelete() throws Exception {
		// Arrange
		File machaiDir = new File(tempDir, Clean.MACHAI_TEMP_DIR);
		assertTrue(machaiDir.mkdirs());
		File logFile = new File(machaiDir, "bindex-inputs.txt");
		Files.write(logFile.toPath(), "x".getBytes(java.nio.charset.StandardCharsets.UTF_8));
		assertTrue(logFile.exists());

		// Make the path non-deletable by turning it into a directory.
		// On Windows, deleting a non-empty directory throws DirectoryNotEmptyException (IOException).
		assertTrue(logFile.delete());
		assertTrue(logFile.mkdir());
		Files.write(new File(logFile, "child.txt").toPath(), "x".getBytes(java.nio.charset.StandardCharsets.UTF_8));

		TestClean mojo = new TestClean();
		mojo.setBasedir(tempDir);

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);
		assertTrue(ex.getMessage().startsWith("Failed to delete inputs log file:"));
		assertTrue(ex.getCause() instanceof IOException);
	}
}
