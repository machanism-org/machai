package org.machanism.machai.ai.provider.none;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.mockito.Mockito;

class NoneProviderTest {

	@Test
	void prompt_appendsTextWithBlankLineSeparators() {
		// Arrange
		NoneProvider provider = new NoneProvider();

		// Act
		provider.prompt("Hello");
		provider.prompt("World");

		// Assert
		assertEquals("Hello\n\nWorld\n\n", provider.getPrompts());
	}

	@Test
	void clear_resetsPromptBuffer() {
		// Arrange
		NoneProvider provider = new NoneProvider();
		provider.prompt("Hello");

		// Act
		provider.clear();

		// Assert
		assertEquals("", provider.getPrompts());
	}

	@Test
	void embedding_throwsUnsupportedOperationException() {
		// Arrange
		NoneProvider provider = new NoneProvider();

		// Act + Assert
		assertThrows(UnsupportedOperationException.class, () -> provider.embedding("text", 3));
	}

	@Test
	void addFile_file_isNoOp() {
		// Arrange
		NoneProvider provider = new NoneProvider();
		provider.prompt("x");

		// Act + Assert
		assertDoesNotThrow(() -> provider.addFile(new File("does-not-matter.txt")));
		assertEquals("x\n\n", provider.getPrompts());
	}

	@Test
	void addFile_url_isNoOp() {
		// Arrange
		NoneProvider provider = new NoneProvider();
		provider.prompt("x");
		URL url = assertDoesNotThrow(() -> URI.create("https://example.com").toURL());

		// Act + Assert
		assertDoesNotThrow(() -> provider.addFile(url));
		assertEquals("x\n\n", provider.getPrompts());
	}

	@Test
	void addTool_isNoOp() {
		// Arrange
		NoneProvider provider = new NoneProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.addTool("t", "d", args -> "ok", "p1"));
	}

	@Test
	void instructions_arePersistedAndWrittenOnPerformWhenInputsLogConfigured(@TempDir Path tempDir) throws Exception {
		// Arrange
		NoneProvider provider = new NoneProvider();
		Path inputs = tempDir.resolve("inputs.txt");
		provider.inputsLog(inputs.toFile());
		provider.instructions("You are a test.");
		provider.prompt("P1");
		provider.prompt("P2");

		// Act
		String result = provider.perform();

		// Assert
		assertNull(result);
		assertEquals("", provider.getPrompts(), "perform() should clear prompts");

		Path instructionsFile = tempDir.resolve("instructions.txt");
		assertTrue(Files.exists(instructionsFile), "instructions.txt should be created");
		assertEquals("You are a test.", readUtf8(instructionsFile));

		assertTrue(Files.exists(inputs), "inputs log file should be created");
		assertEquals("P1\n\nP2\n\n", readUtf8(inputs));
	}

	@Test
	void perform_withoutInputsLogConfigured_clearsPromptsButDoesNotWrite(@TempDir Path tempDir) {
		// Arrange
		NoneProvider provider = new NoneProvider();
		provider.instructions("I");
		provider.prompt("P");
		Path wouldBeInstructions = tempDir.resolve("instructions.txt");

		// Act
		String result = provider.perform();

		// Assert
		assertNull(result);
		assertEquals("", provider.getPrompts());
		assertTrue(Files.notExists(wouldBeInstructions), "instructions.txt should not be written without inputsLog");
	}

	@Test
	void usage_returnsNonNullZeroUsage() {
		// Arrange
		NoneProvider provider = new NoneProvider();

		// Act
		Usage usage = provider.usage();

		// Assert
		assertNotNull(usage);
		assertEquals(0L, usage.getInputTokens());
		assertEquals(0L, usage.getInputCachedTokens());
		assertEquals(0L, usage.getOutputTokens());
	}

	@Test
	void init_isNoOpButAcceptsConfigurator() {
		// Arrange
		NoneProvider provider = new NoneProvider();
		Configurator configurator = Mockito.mock(Configurator.class);

		// Act + Assert
		assertDoesNotThrow(() -> provider.init(configurator));
	}

	@Test
	void name_constant_isExpectedValue() {
		assertEquals("None", NoneProvider.NAME);
	}

	@Test
	void setWorkingDir_isNoOp() {
		// Arrange
		NoneProvider provider = new NoneProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.setWorkingDir(new File(".")));
	}

	@Test
	void perform_createsInputsLogParentDirectories(@TempDir Path tempDir) throws Exception {
		// Arrange
		NoneProvider provider = new NoneProvider();
		Path nestedDir = tempDir.resolve("a").resolve("b");
		Path inputs = nestedDir.resolve("inputs.txt");
		provider.inputsLog(inputs.toFile());
		provider.prompt("P");

		// Act
		provider.perform();

		// Assert
		assertTrue(Files.exists(inputs));
		assertEquals(Arrays.asList("P", ""), Files.readAllLines(inputs, StandardCharsets.UTF_8));
	}

	private static String readUtf8(Path path) throws Exception {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
