package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Minimal flow tests for {@link Assembly#execute()} that avoid advanced mocking.
 *
 * <p>
 * The project is compiled for Java 8 and uses a Mockito setup that does not support construction mocking.
 * These tests therefore focus on early error paths that can be exercised without mocking new objects.
 * </p>
 */
class AssemblyExecuteFlowTest {

	@TempDir
	Path tempDir;

	@Test
	void execute_whenPromptFileExists_butIsADirectory_wrapsIOExceptionAsMojoExecutionException() throws IOException {
		// Arrange
		Path promptPath = tempDir.resolve("project.txt");
		Files.createDirectory(promptPath);

		Assembly mojo = new Assembly();
		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = throwingPrompter(new PrompterException("should not be called"));
		mojo.pickGenai = "OpenAI:gpt-5-mini";
		mojo.assemblyGenai = "OpenAI:gpt-5";

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);
		assertNotNull(ex.getCause());
	}

	@Test
	void execute_whenPromptFileMissing_andPrompterFails_wrapsAsMojoExecutionException() {
		// Arrange
		Assembly mojo = new Assembly();
		mojo.assemblyPromptFile = tempDir.resolve("missing.txt").toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = throwingPrompter(new PrompterException("boom"));
		mojo.pickGenai = "OpenAI:gpt-5-mini";
		mojo.assemblyGenai = "OpenAI:gpt-5";

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);
		assertNotNull(ex.getCause());
	}

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
			public void showMessage(String message) throws PrompterException {
				throw ex;
			}

			@Override
			public String promptForPassword(String message) throws PrompterException {
				throw ex;
			}
		};
	}
}
