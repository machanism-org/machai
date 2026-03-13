package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AssemblyTest {

	@TempDir
	Path tempDir;

	private static Prompter throwingPrompter(PrompterException ex) {
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
			public String prompt(String message, @SuppressWarnings("rawtypes") List possibleValues)
					throws PrompterException {
				throw ex;
			}

			@Override
			public String prompt(String message, @SuppressWarnings("rawtypes") List possibleValues, String defaultReply)
					throws PrompterException {
				throw ex;
			}

			@Override
			public void showMessage(String message) throws PrompterException {
				throw ex;
			}

			@Override
			public String promptForPassword(String message) throws PrompterException {
				throw ex;
			}
		};
	}

	@Test
	void execute_whenPromptFileMissing_usesPrompterAndWrapsPrompterException() {
		// Arrange
		Assembly mojo = new Assembly();
		mojo.assemblyPromptFile = tempDir.resolve("does-not-exist.txt").toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = throwingPrompter(new PrompterException("boom"));

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);
		assertTrue(ex.getMessage().contains("The project assembly process failed."));
		assertInstanceOf(PrompterException.class, ex.getCause());
	}

	@Test
	void execute_whenPromptFileExistsButUnreadable_wrapsIOException() throws IOException {
		// Arrange
		Assembly mojo = new Assembly();
		Path promptPath = tempDir.resolve("project.txt");
		Files.write(promptPath, "hello".getBytes(StandardCharsets.UTF_8));
		// Replace file with a directory so FileReader fails.
		Files.delete(promptPath);
		Files.createDirectory(promptPath);

		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = throwingPrompter(new PrompterException("should not be called"));

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);
		assertTrue(ex.getMessage().contains("The project assembly process failed."));
		assertInstanceOf(IOException.class, ex.getCause());
	}

	@Test
	void fixedPrompter_showMessage_isNoOp() {
		// Arrange
		Prompter p = fixedPrompter("x");

		// Act + Assert
		assertDoesNotThrow(() -> p.showMessage("hi"));
	}

	@Test
	void fixedPrompter_promptOverloads_returnFixedReply() throws Exception {
		// Arrange
		Prompter p = fixedPrompter("reply");

		// Act + Assert
		assertNotNull(p.prompt("m"));
		assertNotNull(p.prompt("m", "d"));
		assertNotNull(p.prompt("m", java.util.Arrays.asList("a", "b")));
		assertNotNull(p.prompt("m", java.util.Arrays.asList("a", "b"), "d"));
		assertNotNull(p.promptForPassword("pwd"));
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
			public String prompt(String message, @SuppressWarnings("rawtypes") List possibleValues) {
				return reply;
			}

			@Override
			public String prompt(String message, @SuppressWarnings("rawtypes") List possibleValues, String defaultReply) {
				return reply;
			}

			@Override
			public void showMessage(String message) {
				// no-op
			}

			@Override
			public String promptForPassword(String message) {
				return reply;
			}
		};
	}
}
