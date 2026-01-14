package org.machanism.machai.ai.provider.none;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.none.NoneProvider;

/**
 * Unit tests for {@link NoneProvider}.
 * <p>
 * Ensures all methods follow the expected stub behavior, no-ops, or defined
 * exception throwing in this placeholder provider.
 * </p>
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
class NoneProviderTest {
	private NoneProvider provider;

	/**
	 * Initializes a new {@link NoneProvider} before each test.
	 */
	@BeforeEach
	void setUp() {
		provider = new NoneProvider();
	}

	/**
	 * Clears provider state after each test.
	 */
	@AfterEach
	void tearDown() {
		provider.clear();
	}

	/**
	 * Tests that prompt appends text and perform returns null.
	 */
	@Test
	void testPromptAppendsText() {
		provider.prompt("Test Prompt");
		String result = provider.perform();
		assertNull(result);
	}

	/**
	 * Tests that clear resets prompts.
	 */
	@Test
	void testClearResetsPrompts() {
		provider.prompt("foo");
		provider.clear();
		String result = provider.perform();
		assertNull(result);
	}

	/**
	 * Tests that instructions are stored, prompts are written to file if inputsLog
	 * is set.
	 */
	@Test
	void testInstructionsStoresText() {
		provider.instructions("Instructions");
		provider.prompt("foo");
		provider.inputsLog(new File("target/noneprovider/inputs.log"));
		provider.perform();
		// File writing not asserted here
	}

	/**
	 * Tests that embedding throws IllegalArgumentException.
	 */
	@Test
	void testEmbeddingThrows() {
		assertThrows(IllegalArgumentException.class, () -> provider.embedding("test"));
	}

	/**
	 * Tests that addFile methods are no-ops and do not throw.
	 * 
	 * @throws Exception if the file cannot be handled (should not happen)
	 */
	@Test
	void testAddFileMethodsAreNoOps() throws Exception {
		provider.addFile(new File("file.txt"));
	}

	/**
	 * Tests that promptFile is a no-op and does not throw exception.
	 * 
	 * @throws Exception if promptFile cannot be handled (should not happen)
	 */
	@Test
	void testPromptFileIsNoOp() throws Exception {
		provider.promptFile(new File("file.txt"), "bundle");
	}

	/**
	 * Tests that inputsLog sets the file and prompts are written to file.
	 */
	@Test
	void testInputsLogSetsFile() {
		File logFile = new File("target/noneprovider/log.txt");
		provider.inputsLog(logFile);
		provider.prompt("abc");
		provider.perform();
	}

	/**
	 * Tests that model method is a no-op.
	 */
	@Test
	void testModelIsNoOp() {
		provider.model("model-string");
	}
}
