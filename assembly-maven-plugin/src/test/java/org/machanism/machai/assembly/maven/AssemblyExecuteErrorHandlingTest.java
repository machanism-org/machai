package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;

/**
 * Tests for {@link Assembly#execute()} error handling and remaining branches.
 */
class AssemblyExecuteErrorHandlingTest {

	@TempDir
	Path tempDir;

	@Test
	void execute_whenPromptFileReadFails_wrapsInMojoExecutionException() throws IOException {
		// Sonar java:S1130 - do not declare generic exceptions that cannot be thrown.
		// Arrange
		// Create a directory where a file is expected -> FileReader will throw.
		Path promptDir = tempDir.resolve("project.txt");
		Files.createDirectories(promptDir);

		Assembly mojo = new Assembly() {
			@Override
			public Log getLog() {
				return new NoopLog();
			}

			@Override
			protected Picker createPicker(Configurator config) {
				throw new AssertionError("Picker should not be created when prompt reading fails");
			}

			@Override
			protected ApplicationAssembly createAssembly(Configurator config) {
				throw new AssertionError("Assembly should not be created when prompt reading fails");
			}
		};
		mojo.assemblyPromptFile = promptDir.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("unused");

		// Act
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions.assertEquals("The project assembly process failed.", ex.getMessage());
		org.junit.jupiter.api.Assertions.assertTrue(ex.getCause() instanceof IOException,
				"Expected IOException to be wrapped as the cause");
	}

	@Test
	void execute_whenPromptFileDoesNotExistAndPrompterFails_wrapsInMojoExecutionException() {
		// Arrange
		File promptFile = tempDir.resolve("project.txt").toFile();
		org.junit.jupiter.api.Assertions.assertFalse(promptFile.exists());

		Assembly mojo = new Assembly() {
			@Override
			public Log getLog() {
				return new NoopLog();
			}

			@Override
			protected Picker createPicker(Configurator config) {
				throw new AssertionError("Picker should not be created when prompter fails");
			}

			@Override
			protected ApplicationAssembly createAssembly(Configurator config) {
				throw new AssertionError("Assembly should not be created when prompter fails");
			}
		};
		mojo.assemblyPromptFile = promptFile;
		mojo.basedir = tempDir.toFile();
		mojo.prompter = failingPrompter(new PrompterException("boom"));

		// Act
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions.assertEquals("The project assembly process failed.", ex.getMessage());
		org.junit.jupiter.api.Assertions.assertTrue(ex.getCause() instanceof PrompterException,
				"Expected PrompterException to be wrapped as the cause");
	}

	@Test
	void execute_whenPromptFileMissing_usesPrompterResultAndContinuesFlow() throws Exception {
		// Arrange
		File promptFile = tempDir.resolve("project.txt").toFile();
		org.junit.jupiter.api.Assertions.assertFalse(promptFile.exists());

		AssemblyExecuteCoverageTest.CapturingLog log = new AssemblyExecuteCoverageTest.CapturingLog();
		AssemblyExecuteCoverageTest.StubApplicationAssembly assembly = AssemblyExecuteCoverageTest.StubApplicationAssembly
				.newInstance();

		Assembly mojo = new Assembly() {
			@Override
			public Log getLog() {
				return log;
			}

			@Override
			protected Picker createPicker(Configurator config) {
				// Return empty list -> ensures prompt-from-prompter branch covered without invoking assembly.
				return AssemblyExecuteCoverageTest.StubPicker.newInstance(java.util.Collections.emptyList());
			}

			@Override
			protected ApplicationAssembly createAssembly(Configurator config) {
				return assembly;
			}
		};
		mojo.assemblyPromptFile = promptFile;
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("typed-query");

		// Act
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions
				.assertTrue(log.messages.stream().anyMatch(m -> m.contains("No libraries were recommended")));
		org.junit.jupiter.api.Assertions.assertFalse(assembly.assemblyCalled,
				"Assembly should not be invoked when picker returns an empty list.");
	}

	private static Prompter fixedPrompter(String reply) {
		return new Prompter() {
			@Override
			public String prompt(String message) {
				return reply;
			}

			@Override
			public String prompt(String message, String defaultReply) {
				return reply;
			}

			@Override
			public String prompt(String message, @SuppressWarnings("rawtypes") java.util.List possibleValues) {
				return reply;
			}

			@Override
			public String prompt(String message, @SuppressWarnings("rawtypes") java.util.List possibleValues,
					String defaultReply) {
				return reply;
			}

			@Override
			public void showMessage(String message) {
				// Sonar java:S1186 - intentionally empty: this stubbed prompter ignores display-only calls.
			}

			@Override
			public String promptForPassword(String message) {
				return reply;
			}
		};
	}

	private static Prompter failingPrompter(PrompterException ex) {
		return new Prompter() {
			@Override
			public String prompt(String message) throws PrompterException {
				throw ex;
			}

			@Override
			public String prompt(String message, String defaultReply) throws PrompterException {
				throw ex;
			}

			@Override
			public String prompt(String message, @SuppressWarnings("rawtypes") java.util.List possibleValues)
					throws PrompterException {
				throw ex;
			}

			@Override
			public String prompt(String message, @SuppressWarnings("rawtypes") java.util.List possibleValues,
					String defaultReply) throws PrompterException {
				throw ex;
			}

			@Override
			public void showMessage(String message) {
				// Sonar java:S1186 - intentionally empty: this stubbed prompter ignores display-only calls.
			}

			@Override
			public String promptForPassword(String message) throws PrompterException {
				throw ex;
			}
		};
	}

	private static final class NoopLog implements Log {
		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void debug(CharSequence content) {
		}

		@Override
		public void debug(CharSequence content, Throwable error) {
		}

		@Override
		public void debug(Throwable error) {
		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public void info(CharSequence content) {
		}

		@Override
		public void info(CharSequence content, Throwable error) {
		}

		@Override
		public void info(Throwable error) {
		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void warn(CharSequence content) {
		}

		@Override
		public void warn(CharSequence content, Throwable error) {
		}

		@Override
		public void warn(Throwable error) {
		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public void error(CharSequence content) {
		}

		@Override
		public void error(CharSequence content, Throwable error) {
		}

		@Override
		public void error(Throwable error) {
		}
	}
}
