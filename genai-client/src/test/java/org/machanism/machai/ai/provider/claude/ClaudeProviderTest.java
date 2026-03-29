package org.machanism.machai.ai.provider.claude;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

class ClaudeProviderTest {

	private static final String NOT_IMPLEMENTED_MSG = "ClaudeProvider is not implemented yet.";

	@Test
	void init_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> provider.init(null));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void prompt_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> provider.prompt("hi"));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void addFile_file_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();
		File file = new File("some-file.txt");

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> provider.addFile(file));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void addFile_url_shouldThrowUnsupportedOperationException_withExpectedMessage() throws Exception {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();
		URL url = URI.create("file:some-file.txt").toURL();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> provider.addFile(url));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void perform_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, provider::perform);

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void clear_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, provider::clear);

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void addTool_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> provider.addTool("name", "desc", args -> "ok", "p1"));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void instructions_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> provider.instructions("be helpful"));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void promptBundle_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();		
		ResourceBundle bundle = new ResourceBundle() {
			@Override
			protected Object handleGetObject(String key) {
				return null;
			}

			@Override
			public java.util.Enumeration<String> getKeys() {
				return Collections.emptyEnumeration();
			}
		};

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
				() -> provider.promptBundle(bundle));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void inputsLog_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();
		File log = new File("inputs.log");

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> provider.inputsLog(log));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void setWorkingDir_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();
		File dir = new File(".");

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> provider.setWorkingDir(dir));

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void usage_shouldThrowUnsupportedOperationException_withExpectedMessage() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, provider::usage);

		// Assert
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@Test
	void embedding_shouldReturnSameEmptyListInstance_forAnyInput() {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();

		// Act
		List<Double> embedding1 = provider.embedding("text", 1536);
		List<Double> embedding2 = provider.embedding(null, 0);

		// Assert
		assertNotNull(embedding1);
		assertSame(Collections.emptyList(), embedding1);
		assertSame(embedding1, embedding2);
		assertEquals(0, embedding1.size());
	}

	@Test
	void allNotImplementedPublicApiMethods_shouldThrowUnsupportedOperationExceptionWithExpectedMessage() throws Exception {
		// Arrange
		ClaudeProvider provider = new ClaudeProvider();		
		File file = new File("some-file.txt");
		URL url = URI.create("file:some-file.txt").toURL();
		ResourceBundle bundle = new ResourceBundle() {
			@Override
			protected Object handleGetObject(String key) {
				return null;
			}

			@Override
			public java.util.Enumeration<String> getKeys() {
				return Collections.emptyEnumeration();
			}
		};

		// Act + Assert
		assertAll(
				() -> assertNotImplemented(() -> provider.init(null)),
				() -> assertNotImplemented(() -> provider.prompt("x")),
				() -> assertNotImplemented(() -> provider.addFile(file)),
				() -> assertNotImplemented(() -> provider.addFile(url)),
				() -> assertNotImplemented(provider::perform),
				() -> assertNotImplemented(provider::clear),
				() -> assertNotImplemented(() -> provider.addTool("name", "desc", args -> "ok", "p1")),
				() -> assertNotImplemented(() -> provider.instructions("ins")),
				() -> assertNotImplemented(() -> provider.promptBundle(bundle)),
				() -> assertNotImplemented(() -> provider.inputsLog(new File("inputs.log"))),
				() -> assertNotImplemented(() -> provider.setWorkingDir(new File("."))),
				() -> assertNotImplemented(provider::usage));
	}

	// Sonar java:S5778: reduce the number of potentially-throwing invocations inside a lambda.
	private static void assertNotImplemented(ThrowingRunnable action) {
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, action::run);
		assertEquals(NOT_IMPLEMENTED_MSG, ex.getMessage());
	}

	@FunctionalInterface
	private interface ThrowingRunnable extends org.junit.jupiter.api.function.Executable {
		@Override
		void execute() throws Throwable;

		default void run() {
			try {
				execute();
			} catch (RuntimeException e) {
				throw e;
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}
}
