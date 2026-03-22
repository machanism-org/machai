package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

/**
 * Full-flow tests for {@link Assembly#execute()}.
 *
 * <p>
 * {@link Picker} and {@link ApplicationAssembly} constructors perform heavy
 * provider initialization. To avoid that in unit tests, this class instantiates
 * test doubles without invoking constructors via {@code sun.misc.Unsafe}.
 * </p>
 */
class AssemblyExecuteFullFlowTest {

	@TempDir
	Path tempDir;

	@Test
	void execute_whenPickerReturnsEmptyList_logsAndReturnsWithoutRunningAssembly() throws Exception {
		// Arrange
		Path promptPath = tempDir.resolve("project.txt");
		Files.write(promptPath, "prompt".getBytes(StandardCharsets.UTF_8));

		CapturingLog log = new CapturingLog();
		StubPicker picker = allocateWithoutConstructor(StubPicker.class);
		picker.result = Collections.<Bindex>emptyList();
		picker.scores = Collections.<String, Double>emptyMap();

		TestableAssembly mojo = new TestableAssembly(log, picker, null);
		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("unused");
		mojo.score = 0.9;

		// Act
		mojo.execute();

		// Assert
		assertTrue(log.containsSubstring("No libraries were recommended by the picker."),
				"Expected info log about empty recommendations.");
		assertEquals(0, mojo.assemblyCalls);
	}

	@Test
	void execute_whenRecommendationsPresent_logsFormattedScores_andRunsAssemblyWithPromptAndBindexList()
			throws Exception {
		// Arrange
		Path promptPath = tempDir.resolve("project.txt");
		Files.write(promptPath, "my prompt".getBytes(StandardCharsets.UTF_8));

		Bindex b1 = new Bindex();
		b1.setId("lib-one");
		Bindex b2 = new Bindex();
		b2.setId("lib-two");

		List<Bindex> picks = new ArrayList<Bindex>();
		picks.add(b1);
		picks.add(b2);

		Map<String, Double> scores = new HashMap<String, Double>();
		scores.put("lib-one", 0.95);
		// lib-two deliberately missing score -> empty score string branch

		CapturingLog log = new CapturingLog();
		StubPicker picker = allocateWithoutConstructor(StubPicker.class);
		picker.result = picks;
		picker.scores = scores;

		CapturingAssembly assembly = allocateWithoutConstructor(CapturingAssembly.class);

		TestableAssembly mojo = new TestableAssembly(log, picker, assembly);
		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("unused");
		mojo.score = 0.42;

		// Act
		mojo.execute();

		// Assert
		assertEquals(0.42, picker.capturedScore, 0.0001);

		assertTrue(log.containsSubstring("Recommended libraries:"));
		assertTrue(log.containsSubstring("  1. lib-one 0.95"));
		assertTrue(log.containsSubstring("  2. lib-two ")); // scoreStr = ""

		assertEquals(1, mojo.assemblyCalls);
		assertEquals("my prompt", assembly.capturedQuery);
		assertNotNull(assembly.capturedBindex);
		assertEquals(2, assembly.capturedBindex.size());
		assertEquals("lib-one", assembly.capturedBindex.get(0).getId());
		assertEquals("lib-two", assembly.capturedBindex.get(1).getId());
		assertEquals(tempDir.toFile(), assembly.capturedProjectDir);
	}

	@Test
	void execute_whenPickerThrowsIOException_wrapsAsMojoExecutionException() throws Exception {
		// Arrange
		Path promptPath = tempDir.resolve("project.txt");
		Files.write(promptPath, "prompt".getBytes(StandardCharsets.UTF_8));

		CapturingLog log = new CapturingLog();
		ThrowingPicker picker = allocateWithoutConstructor(ThrowingPicker.class);
		picker.ex = new IOException("picker down");

		TestableAssembly mojo = new TestableAssembly(log, picker, null);
		mojo.assemblyPromptFile = promptPath.toFile();
		mojo.basedir = tempDir.toFile();
		mojo.prompter = fixedPrompter("unused");

		// Act
		MojoExecutionException ex = org.junit.jupiter.api.Assertions.assertThrows(MojoExecutionException.class,
				mojo::execute);

		// Assert
		assertNotNull(ex.getCause());
		assertEquals(IOException.class, ex.getCause().getClass());
		assertTrue(ex.getMessage().contains("The project assembly process failed."));
	}

	private static Prompter fixedPrompter(final String reply) {
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
			public String prompt(String message, @SuppressWarnings("rawtypes") List possibleValues,
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

	private static final class TestableAssembly extends Assembly {
		private final Log log;
		private final Picker picker;
		private final ApplicationAssembly assembly;

		int assemblyCalls;

		private TestableAssembly(Log log, Picker picker, ApplicationAssembly assembly) {
			this.log = log;
			this.picker = picker;
			this.assembly = assembly;
		}

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
			assemblyCalls++;
			return assembly;
		}
	}

	private static class StubPicker extends Picker {
		private List<Bindex> result;
		private Map<String, Double> scores;

		double capturedScore;

		private StubPicker() {
			super("unused", null, null);
		}

		@Override
		public void setScore(Double score) {
			capturedScore = score != null ? score.doubleValue() : Double.NaN;
		}

		@Override
		public List<Bindex> pick(String query) throws IOException {
			return result;
		}

		@Override
		public Double getScore(String id) {
			return scores.get(id);
		}

	}

	private static final class ThrowingPicker extends Picker {
		private IOException ex;

		private ThrowingPicker() {
			super("unused", null, null);
		}

		@Override
		public List<Bindex> pick(String query) throws IOException {
			throw ex;
		}

	}

	private static final class CapturingAssembly extends ApplicationAssembly {
		File capturedProjectDir;
		String capturedQuery;
		List<Bindex> capturedBindex;

		private CapturingAssembly() {
			super("unused", null, new File("."));
		}

		@Override
		public ApplicationAssembly projectDir(File dir) {
			capturedProjectDir = dir;
			return this;
		}

		@Override
		public void assembly(String query, List<Bindex> bindexList) {
			capturedQuery = query;
			capturedBindex = bindexList;
		}
	}

	private static final class CapturingLog implements Log {
		private final List<String> info = new ArrayList<String>();

		boolean containsSubstring(String s) {
			for (String line : info) {
				if (line != null && line.contains(s)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public void debug(CharSequence content) {
			// ignore
		}

		@Override
		public void debug(CharSequence content, Throwable error) {
			// ignore
		}

		@Override
		public void debug(Throwable error) {
			// ignore
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public void info(CharSequence content) {
			info.add(content != null ? content.toString() : null);
		}

		@Override
		public void info(CharSequence content, Throwable error) {
			info(content);
		}

		@Override
		public void info(Throwable error) {
			info.add(error != null ? error.toString() : null);
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public void warn(CharSequence content) {
			// ignore
		}

		@Override
		public void warn(CharSequence content, Throwable error) {
			// ignore
		}

		@Override
		public void warn(Throwable error) {
			// ignore
		}

		@Override
		public boolean isErrorEnabled() {
			return true;
		}

		@Override
		public void error(CharSequence content) {
			// ignore
		}

		@Override
		public void error(CharSequence content, Throwable error) {
			// ignore
		}

		@Override
		public void error(Throwable error) {
			// ignore
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		Field f = unsafeClass.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		Object unsafe = f.get(null);

		java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
		return (T) allocateInstance.invoke(unsafe, type);
	}
}
