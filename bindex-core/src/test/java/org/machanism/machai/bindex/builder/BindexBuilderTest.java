package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit tests for {@link BindexBuilder}.
 */
class BindexBuilderTest {

	@Test
	void origin_getOrigin_returnsSameInstance() {
		ProjectLayout layout = TestProjectLayouts.projectLayout(new File("."));

		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			Bindex origin = new Bindex();
			BindexBuilder returned = builder.origin(origin);

			assertSame(builder, returned);
			assertSame(origin, builder.getOrigin());
		}
	}

	@Test
	void getProjectLayout_returnsProvidedInstance() {
		ProjectLayout layout = TestProjectLayouts.projectLayout(new File("."));
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			assertSame(layout, builder.getProjectLayout());
		}
	}

	@Test
	void getGenAIProvider_returnsProviderFromManager() {
		ProjectLayout layout = TestProjectLayouts.projectLayout(new File("."));
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			assertSame(provider, builder.getGenAIProvider());
		}
	}

	@Test
	void build_whenProviderReturnsNull_returnsNull() throws Exception {
		File projectDir = new File(".");
		ProjectLayout layout = TestProjectLayouts.projectLayout(projectDir);

		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Mockito.when(provider.perform()).thenReturn(null);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			Bindex result = builder.build();

			assertNull(result);
			Mockito.verify(provider).inputsLog(new File(projectDir, BindexBuilder.BINDEX_TEMP_DIR));
			Mockito.verify(provider).prompt(Mockito.anyString());
		}
	}

	@Test
	void build_whenProviderReturnsJsonInCodeFence_parsesBindex() throws Exception {
		File projectDir = new File(".");
		ProjectLayout layout = TestProjectLayouts.projectLayout(projectDir);

		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Mockito.when(provider.perform()).thenReturn("```json\n{\"id\":\"abc\",\"name\":\"n\",\"version\":\"1\"}\n```");
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			Bindex result = builder.build();

			assertNotNull(result);
			assertEquals("abc", result.getId());
			assertEquals("n", result.getName());
			assertEquals("1", result.getVersion());
			Mockito.verify(provider).inputsLog(new File(projectDir, BindexBuilder.BINDEX_TEMP_DIR));
		}
	}

	@Test
	void promptFile_whenBundleMessageNull_returnsRawFileContent(@TempDir File tempDir) throws Exception {
		File file = new File(tempDir, "file.txt");
		Files.write(file.toPath(), "hello".getBytes(StandardCharsets.UTF_8));

		ProjectLayout layout = TestProjectLayouts.projectLayout(new File("."));
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			assertEquals("hello", builder.promptFile(file, null));
		}
	}

	@Test
	void promptFile_whenBundleMessageProvided_includesFilenameAndContent(@TempDir File tempDir) throws Exception {
		File file = new File(tempDir, "file.js");
		Files.write(file.toPath(), "console.log('x');".getBytes(StandardCharsets.UTF_8));

		ProjectLayout layout = TestProjectLayouts.projectLayout(new File("."));
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			BindexBuilder builder = new BindexBuilder(layout, "provider", config);
			String prompt = builder.promptFile(file, "source_resource_section");

			assertNotNull(prompt);
			assertTrue(prompt.contains("file.js"));
			assertTrue(prompt.contains("js"));
			assertTrue(prompt.contains("console.log('x');"));
		}
	}
}
