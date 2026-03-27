package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class GenAIAdapterTest {

	private static final class TestAdapter extends GenaiAdapter {
		TestAdapter(Genai provider) {
			setProvider(provider);
		}
	}

	private static final class RecordingProvider implements Genai {
		Configurator initConf;
		String prompted;
		File addedFile;
		URL addedUrl;
		String toolName;
		String toolDescription;
		ToolFunction toolFunction;
		String[] toolParams;
		String instructions;
		String performResult = "ok";
		File inputsLogDir;
		File workingDir;
		Usage usage = new Usage(1, 2, 3);
		String embeddedText;
		long embeddedDimensions;
		List<Double> embeddingResult = Arrays.asList(1.0, 2.0);
		boolean cleared;

		@Override
		public void init(Configurator conf) {
			this.initConf = conf;
		}

		@Override
		public void prompt(String text) {
			this.prompted = text;
		}

		@Override
		public void addFile(File file) throws IOException {
			this.addedFile = file;
		}

		@Override
		public void addFile(URL fileUrl) throws IOException {
			this.addedUrl = fileUrl;
		}

		@Override
		public List<Double> embedding(String text, long dimensions) {
			this.embeddedText = text;
			this.embeddedDimensions = dimensions;
			return embeddingResult;
		}

		@Override
		public void clear() {
			this.cleared = true;
		}

		@Override
		public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
			this.toolName = name;
			this.toolDescription = description;
			this.toolFunction = function;
			this.toolParams = paramsDesc;
		}

		@Override
		public void instructions(String instructions) {
			this.instructions = instructions;
		}

		@Override
		public String perform() {
			return performResult;
		}

		@Override
		public void inputsLog(File bindexTempDir) {
			this.inputsLogDir = bindexTempDir;
		}

		@Override
		public void setWorkingDir(File workingDir) {
			this.workingDir = workingDir;
		}

		@Override
		public Usage usage() {
			return usage;
		}
	}

	@Test
	void setProvider_whenNull_throwsIllegalArgumentException() {
		// Arrange
		GenaiAdapter adapter = new GenaiAdapter();

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> adapter.setProvider(null));
	}

	@Test
	void allMethods_delegateToUnderlyingProvider() throws IOException {
		// Arrange
		RecordingProvider provider = new RecordingProvider();
		Genai adapter = new TestAdapter(provider);
		Configurator conf = null;
		File file = new File("test.txt");
		URL url = URI.create("https://example.test/file").toURL();
		Genai.ToolFunction fn = params -> "done";

		// Act
		adapter.init(conf);
		adapter.prompt("hello");
		adapter.addFile(file);
		adapter.addFile(url);
		List<Double> embedding = adapter.embedding("abc", 42);
		adapter.addTool("t", "d", fn, "p1", "p2");
		adapter.instructions("sys");
		String perform = adapter.perform();
		adapter.inputsLog(new File("logs"));
		adapter.setWorkingDir(new File("wd"));
		Usage usage = adapter.usage();
		adapter.clear();

		// Assert
		assertSame(conf, provider.initConf);
		assertEquals("hello", provider.prompted);
		assertSame(file, provider.addedFile);
		assertSame(url, provider.addedUrl);
		assertEquals(Arrays.asList(1.0, 2.0), embedding);
		assertEquals("abc", provider.embeddedText);
		assertEquals(42, provider.embeddedDimensions);
		assertEquals("t", provider.toolName);
		assertEquals("d", provider.toolDescription);
		assertSame(fn, provider.toolFunction);
		assertEquals(2, provider.toolParams.length);
		assertEquals("p1", provider.toolParams[0]);
		assertEquals("p2", provider.toolParams[1]);
		assertEquals("sys", provider.instructions);
		assertEquals("ok", perform);
		assertEquals("logs", provider.inputsLogDir.getPath());
		assertEquals("wd", provider.workingDir.getPath());
		assertSame(provider.usage, usage);
		assertEquals(true, provider.cleared);
	}
}
