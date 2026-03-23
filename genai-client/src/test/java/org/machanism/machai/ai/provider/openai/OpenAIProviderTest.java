package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Unit tests focused on OpenAIProvider core logic that does not require network access.
 */
class OpenAIProviderTest {

	private final OpenAIProvider provider = new OpenAIProvider();

	@AfterEach
	void tearDown() {
		provider.clear();
	}

	@Test
	void init_shouldLoadModelAndDefaults() {
		// Arrange
		Configurator config = TestConfigurators.mapBacked();
		config.set("chatModel", "gpt-test");
		config.set("OPENAI_API_KEY", "dummy");

		// Act
		provider.init(config);

		// Assert
		assertEquals(OpenAIProvider.MAX_OUTPUT_TOKENS, getField("maxOutputTokens"));
		assertEquals(OpenAIProvider.MAX_TOOL_CALLS, getField("maxToolCalls"));
	}

	@Test
	void init_shouldLoadConfiguredOverrides() {
		// Arrange
		Configurator config = TestConfigurators.mapBacked();
		config.set("chatModel", "gpt-test");
		config.set("OPENAI_API_KEY", "dummy");
		config.set("MAX_OUTPUT_TOKENS", "42");
		config.set("MAX_TOOL_CALLS", "7");

		// Act
		provider.init(config);

		// Assert
		assertEquals(42L, getField("maxOutputTokens"));
		assertEquals(7L, getField("maxToolCalls"));
	}

	@Test
	void prompt_shouldAddUserMessageInput() {
		// Arrange
		provider.init(minimalConfig());

		// Act
		provider.prompt("hello");

		// Assert
		List<?> inputs = (List<?>) getField("inputs");
		assertEquals(1, inputs.size());
		assertNotNull(inputs.get(0));
	}

	@Test
	void clear_shouldRemoveAccumulatedInputs() {
		// Arrange
		provider.init(minimalConfig());
		provider.prompt("one");
		provider.prompt("two");

		// Act
		provider.clear();

		// Assert
		List<?> inputs = (List<?>) getField("inputs");
		assertEquals(0, inputs.size());
	}

	@Test
	void instructions_shouldSetInstructions() {
		// Arrange
		provider.init(minimalConfig());

		// Act
		provider.instructions("be concise");

		// Assert
		assertEquals("be concise", getField("instructions"));
	}

	@Test
	void inputsLog_shouldSetLogInputsFile() {
		// Arrange
		provider.init(minimalConfig());
		File f = new File("target/test-inputs-log.txt");

		// Act
		provider.inputsLog(f);

		// Assert
		assertEquals(f, getField("inputsLog"));
	}

	@Test
	void setWorkingDir_shouldSetWorkingDir() {
		// Arrange
		provider.init(minimalConfig());
		File wd = new File(".");

		// Act
		provider.setWorkingDir(wd);

		// Assert
		assertEquals(wd, getField("workingDir"));
	}

	@Test
	void usage_shouldReturnNonNullDefaultUsage() {
		// Arrange
		provider.init(minimalConfig());

		// Act
		org.machanism.machai.ai.manager.Usage usage = provider.usage();

		// Assert
		assertNotNull(usage);
	}

	@Test
	void embedding_shouldReturnNullWhenTextIsNull_withoutCallingNetwork() {
		// Arrange
		provider.init(minimalConfig());

		// Act
		List<Double> result = provider.embedding(null, 10);

		// Assert
		assertNull(result);
	}

	@Test
	void addFile_byUrl_shouldAddUserMessageInput_withoutNetwork() throws Exception {
		// Arrange
		provider.init(minimalConfig());
		URL url = URI.create("https://example.com/file.txt").toURL();

		// Act
		provider.addFile(url);

		// Assert
		List<?> inputs = (List<?>) getField("inputs");
		assertEquals(1, inputs.size());
	}

	@Test
	void setTimeout_shouldUpdateTimeout_andGetTimeoutShouldReturnSame() {
		// Arrange
		provider.init(minimalConfig());

		// Act
		provider.setTimeout(123L);

		// Assert
		assertEquals(123L, provider.getTimeout());
	}

	private Configurator minimalConfig() {
		Configurator config = TestConfigurators.mapBacked();
		config.set("chatModel", "gpt-test");
		config.set("OPENAI_API_KEY", "dummy");
		return config;
	}

	private Object getField(String name) {
		try {
			Field f = OpenAIProvider.class.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(provider);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
