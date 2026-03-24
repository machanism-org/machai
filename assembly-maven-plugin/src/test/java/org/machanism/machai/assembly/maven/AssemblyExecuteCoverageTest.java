package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

/**
 * Coverage-focused tests for {@link Assembly#execute()} that exercise branches
 * not covered by existing flow tests by overriding factory methods.
 */
class AssemblyExecuteCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void execute_whenPickerReturnsEmptyList_logsAndReturnsBeforeAssemblyInvocation() throws IOException {
		// Arrange
		Path promptPath = tempDir.resolve("project.txt");
		Files.write(promptPath, "query".getBytes(StandardCharsets.UTF_8));

		CapturingLog log = new CapturingLog();
		StubApplicationAssembly assembly = StubApplicationAssembly.newInstance();

		Assembly mojo = new Assembly() {
			@Override
			public Log getLog() {
				return log;
			}

			@Override
			protected Picker createPicker(Configurator config) {
				return StubPicker.newInstance(Collections.<Bindex>emptyList());
			}

			@Override
			protected ApplicationAssembly createAssembly(Configurator config) {
				return assembly;
			}
		};
		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("should not be used");

		// Act
		assertDoesNotThrow(mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(
				log.messages.stream().anyMatch(m -> m.contains("No libraries were recommended by the picker.")),
				"Expected a log message indicating that no libraries were recommended.");
		org.junit.jupiter.api.Assertions.assertFalse(assembly.assemblyCalled,
				"Assembly should not be invoked when picker returns an empty list.");
	}

	@Test
	void execute_whenPickerReturnsItems_logsRecommendationsAndInvokesAssemblyWithQueryAndBindexList() throws Exception {
		// Arrange
		Path promptPath = tempDir.resolve("project.txt");
		Files.write(promptPath, "my-query".getBytes(StandardCharsets.UTF_8));

		Bindex b1 = new Bindex();
		b1.setId("g:a:1");
		Bindex b2 = new Bindex();
		b2.setId("g:b:2");
		List<Bindex> picked = Arrays.asList(b1, b2);

		CapturingLog log = new CapturingLog();
		StubPicker picker = StubPicker.newInstance(picked);
		StubApplicationAssembly assembly = StubApplicationAssembly.newInstance();

		Assembly mojo = new Assembly() {
			@Override
			public Log getLog() {
				return log;
			}

			@Override
			protected Picker createPicker(Configurator config) {
				return picker;
			}

			@Override
			protected ApplicationAssembly createAssembly(Configurator config) {
				return assembly;
			}
		};
		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("unused");

		// Act
		assertDoesNotThrow(mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(
				log.messages.stream().anyMatch(m -> m.contains("Recommended libraries:")),
				"Expected header log message for recommended libraries.");
		org.junit.jupiter.api.Assertions.assertTrue(
				log.messages.stream().anyMatch(m -> m.contains("g:a:1")),
				"Expected first bindex id to be logged.");
		org.junit.jupiter.api.Assertions.assertTrue(
				log.messages.stream().anyMatch(m -> m.contains("g:b:2")),
				"Expected second bindex id to be logged.");
		org.junit.jupiter.api.Assertions.assertTrue(assembly.assemblyCalled, "Expected assembly to be invoked.");
		org.junit.jupiter.api.Assertions.assertEquals("my-query", assembly.query);
		org.junit.jupiter.api.Assertions.assertEquals(picked, assembly.bindexes);
		org.junit.jupiter.api.Assertions.assertEquals(tempDir.toFile(), assembly.projectDir);
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
				// no-op
			}

			@Override
			public String promptForPassword(String message) {
				return reply;
			}
		};
	}

	private static final class CapturingLog implements Log {
		final java.util.List<String> messages = new java.util.ArrayList<>();

		private void add(String msg) {
			messages.add(msg);
		}

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public void debug(CharSequence content) {
			add(String.valueOf(content));
		}

		@Override
		public void debug(CharSequence content, Throwable error) {
			add(String.valueOf(content));
		}

		@Override
		public void debug(Throwable error) {
			add(String.valueOf(error));
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public void info(CharSequence content) {
			add(String.valueOf(content));
		}

		@Override
		public void info(CharSequence content, Throwable error) {
			add(String.valueOf(content));
		}

		@Override
		public void info(Throwable error) {
			add(String.valueOf(error));
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public void warn(CharSequence content) {
			add(String.valueOf(content));
		}

		@Override
		public void warn(CharSequence content, Throwable error) {
			add(String.valueOf(content));
		}

		@Override
		public void warn(Throwable error) {
			add(String.valueOf(error));
		}

		@Override
		public boolean isErrorEnabled() {
			return true;
		}

		@Override
		public void error(CharSequence content) {
			add(String.valueOf(content));
		}

		@Override
		public void error(CharSequence content, Throwable error) {
			add(String.valueOf(content));
		}

		@Override
		public void error(Throwable error) {
			add(String.valueOf(error));
		}
	}

	/**
	 * Avoid calling the real Picker constructor (it attempts to initialize a GenAI provider).
	 *
	 * We allocate without constructors using Objenesis.
	 */
	private static final class StubPicker extends Picker {

		private List<Bindex> result;
		double setScoreValue = Double.NaN;

		private StubPicker() {
			super(null, null, null);
		}

		static StubPicker newInstance(List<Bindex> result) {
			StubPicker p = new org.objenesis.ObjenesisStd().newInstance(StubPicker.class);
			p.result = result;
			return p;
		}

		@Override
		public void setScore(Double score) {
			this.setScoreValue = score != null ? score.doubleValue() : Double.NaN;
		}

		@Override
		public List<Bindex> pick(String query) {
			return result;
		}

		@Override
		public Double getScore(String bindexId) {
			// Return a score for the first entry only (exercise both branches).
			return result != null && !result.isEmpty() && bindexId != null && bindexId.equals(result.get(0).getId()) ? 0.42d : null;
		}
	}

	/**
	 * Avoid calling the real ApplicationAssembly constructor (it attempts to initialize a GenAI provider).
	 */
	private static final class StubApplicationAssembly extends ApplicationAssembly {
		boolean assemblyCalled;
		File projectDir;
		String query;
		List<Bindex> bindexes;

		private StubApplicationAssembly() {
			super(null, null, null);
		}

		static StubApplicationAssembly newInstance() {
			return new org.objenesis.ObjenesisStd().newInstance(StubApplicationAssembly.class);
		}

		@Override
		public ApplicationAssembly projectDir(File dir) {
			this.projectDir = dir;
			return this;
		}

		@Override
		public void setLogInputs(boolean logInputs) {
			// ignored
		}

		@Override
		public void assembly(String query, List<Bindex> bindexes) {
			this.assemblyCalled = true;
			this.query = query;
			this.bindexes = bindexes;
		}
	}
}
