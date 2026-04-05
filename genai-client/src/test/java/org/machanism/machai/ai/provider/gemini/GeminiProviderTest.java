package org.machanism.machai.ai.provider.gemini;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;

/**
 * Unit tests for {@link GeminiProvider}.
 */
class GeminiProviderTest {

	@Test
	void init_shouldThrowNotImplementedException() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		Configurator conf = null;

		// Act + Assert
		assertThrows(NotImplementedException.class, () -> provider.init(conf));
	}

	@Test
	void embedding_shouldThrowNotImplementedException() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertThrows(NotImplementedException.class, () -> provider.embedding("text", 3));
	}

	@Test
	void perform_shouldThrowNotImplementedException() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertThrows(NotImplementedException.class, provider::perform);
	}

	@Test
	void prompt_shouldNotThrow() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.prompt("hello"));
	}

	@Test
	void prompt_shouldAcceptNull() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.prompt(null));
	}

	@Test
	void instructions_shouldNotThrow() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.instructions("be helpful"));
	}

	@Test
	void instructions_shouldAcceptNull() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.instructions(null));
	}

	@Test
	void clear_shouldNotThrow() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(provider::clear);
	}

	@Test
	void addTool_shouldNotThrow_withNullFunctionAndNoParams() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.addTool("tool", "desc", null));
	}

	@Test
	void addTool_shouldNotThrow_withNullFunctionAndNullParamsArray() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.addTool("tool", "desc", null, (String[]) null));
	}

	@Test
	void addTool_shouldNotThrow_withRealFunctionAndParams() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		ToolFunction function = args -> "ok";

		// Act + Assert
		assertDoesNotThrow(() -> provider.addTool("tool", "desc", function, "a", "b"));
	}

	@Test
	void inputsLog_shouldNotThrow_withNullDir() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.inputsLog(null));
	}

	@Test
	void inputsLog_shouldNotThrow_withExistingDir() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		File dir = new File("target");

		// Act + Assert
		assertDoesNotThrow(() -> provider.inputsLog(dir));
	}

	@Test
	void setWorkingDir_shouldNotThrow_withNullDir() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.setWorkingDir(null));
	}

	@Test
	void setWorkingDir_shouldNotThrow_withExistingDir() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		File dir = new File(".");

		// Act + Assert
		assertDoesNotThrow(() -> provider.setWorkingDir(dir));
	}

	@Test
	void usage_shouldReturnNull() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act
		org.machanism.machai.ai.manager.Usage usage = provider.usage();

		// Assert
		assertNull(usage);
	}

	@Test
	void promptBundle_shouldNotThrow_withNullBundle() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		ResourceBundle bundle = null;

		// Act + Assert
		assertDoesNotThrow(() -> provider.promptBundle(bundle));
	}

	@Test
	void promptBundle_shouldNotThrow_withNonEmptyBundle() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		ResourceBundle bundle = new ListResourceBundle() {
			@Override
			protected Object[][] getContents() {
				return new Object[][] { { "k1", "v1" }, { "k2", "v2" } };
			}

			@Override
			public Enumeration<String> getKeys() {
				return super.getKeys();
			}
		};

		// Act + Assert
		assertDoesNotThrow(() -> provider.promptBundle(bundle));
	}

	@Test
	void implementsGenai_shouldBeAssignable() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act
		Genai genai = provider;

		// Assert
		assertSame(provider, genai);
		assertNotNull(genai);
	}

	@Test
	void embedding_returnType_shouldBeListOfDouble() throws NoSuchMethodException {
		// Arrange
		Method method = GeminiProvider.class.getMethod("embedding", String.class, long.class);

		// Act
		Class<?> returnType = method.getReturnType();

		// Assert
		assertSame(List.class, returnType);
	}

	private static boolean declaresExceptionAssignableTo(Class<?>[] declared, Class<?> expected) {
		for (Class<?> ex : declared) {
			if (expected.isAssignableFrom(ex)) {
				return true;
			}
		}
		return false;
	}
}
