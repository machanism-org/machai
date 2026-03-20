package org.machanism.machai.ai.provider.gemini;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;

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
		GenAIProvider.ToolFunction function = args -> "ok";

		// Act + Assert
		assertDoesNotThrow(() -> provider.addTool("tool", "desc", function, "a", "b"));
	}

	@Test
	void addFile_file_shouldNotThrow_evenWhenFileDoesNotExist() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		File missing = new File("target/does-not-exist.txt");

		// Act + Assert
		assertDoesNotThrow(() -> provider.addFile(missing));
	}

	@Test
	void addFile_file_shouldAcceptNull() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.addFile((File) null));
	}

	@Test
	void addFile_url_shouldNotThrow() throws Exception {
		// Arrange
		GeminiProvider provider = new GeminiProvider();
		URL url = URI.create("https://example.com/file.txt").toURL();

		// Act + Assert
		assertDoesNotThrow(() -> provider.addFile(url));
	}

	@Test
	void addFile_url_shouldAcceptNull() {
		// Arrange
		GeminiProvider provider = new GeminiProvider();

		// Act + Assert
		assertDoesNotThrow(() -> provider.addFile((URL) null));
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
	void addFile_methodSignatures_shouldDeclareIOException() throws NoSuchMethodException {
		// Arrange
		Method fileMethod = GeminiProvider.class.getMethod("addFile", File.class);
		Method urlMethod = GeminiProvider.class.getMethod("addFile", URL.class);

		// Act
		boolean fileDeclaresIo = declaresExceptionAssignableTo(fileMethod.getExceptionTypes(), IOException.class);
		boolean urlDeclaresIo = declaresExceptionAssignableTo(urlMethod.getExceptionTypes(), IOException.class);

		// Assert
		Assertions.assertTrue(fileDeclaresIo);
		Assertions.assertTrue(urlDeclaresIo);
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
