package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedStatic;

class BindexBuilderAdditionalTest {

	@TempDir
	File tempDir;

	@Test
	void isLogInputs_and_logInputs_roundTrip() {
		// Arrange
		Genai provider = mock(Genai.class);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, TestProjectLayouts.projectLayout(tempDir));

		// Act
		BindexBuilder returned = builder.logInputs(true);

		// Assert
		assertSame(builder, returned);
		assertTrue(builder.isLogInputs());
	}

	@Test
	void build_whenProviderReturnsNull_returnsNull() throws Exception {
		// Arrange
		ProjectLayout layout = TestProjectLayouts.projectLayout(tempDir);
		Genai provider = mock(Genai.class);
		when(provider.perform()).thenReturn(null);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, layout);

		// Act
		Bindex result = builder.build();

		// Assert
		assertNull(result);
		verify(provider).prompt(anyString());
		verify(provider).perform();
	}

	@Test
	void build_whenOutputIsJsonFencedCodeBlock_stripsFenceAndParses() throws Exception {
		// Arrange
		ProjectLayout layout = TestProjectLayouts.projectLayout(tempDir);
		Genai provider = mock(Genai.class);
		String json = "{\"id\":\"abc\",\"description\":\"Hello\"}";
		when(provider.perform()).thenReturn("```json\n" + json + "\n```");
		BindexBuilder builder = createBuilderWithMockedProvider(provider, layout);

		// Act
		Bindex result = builder.build();

		// Assert
		assertNotNull(result);
		assertEquals("abc", result.getId());
		assertEquals("Hello", result.getDescription());
	}

	@Test
	void build_whenOriginProvided_includesUpdatePromptAndLogsInputsWhenEnabled() throws Exception {
		// Arrange
		ProjectLayout layout = TestProjectLayouts.projectLayout(tempDir);
		Genai provider = mock(Genai.class);
		when(provider.perform()).thenReturn("{\"id\":\"newId\"}");

		Bindex origin = new Bindex();
		origin.setId("oldId");
		origin.setDescription("oldDesc");

		BindexBuilder builder = createBuilderWithMockedProvider(provider, layout).origin(origin).logInputs(true);

		// Act
		Bindex result = builder.build();

		// Assert
		assertNotNull(result);
		verify(provider).inputsLog(new File(tempDir, BindexBuilder.BINDEX_TEMP_DIR));
		verify(provider).prompt(argThat(p -> p.contains("oldId")));
		verify(provider).perform();
	}

	@Test
	void promptFile_whenBundleMessageNameNull_returnsRawFileData() throws Exception {
		// Arrange
		Genai provider = mock(Genai.class);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, TestProjectLayouts.projectLayout(tempDir));

		File file = new File(tempDir, "some.txt");
		Files.write(file.toPath(), "RAW".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = builder.promptFile(file, null);

		// Assert
		assertEquals("RAW", result);
	}

	@Test
	void promptFile_whenBundleMessageNameUnknown_throwsMissingResourceException() throws Exception {
		// Arrange
		Genai provider = mock(Genai.class);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, TestProjectLayouts.projectLayout(tempDir));

		File file = new File(tempDir, "example.json");
		Files.write(file.toPath(), "{}".getBytes(StandardCharsets.UTF_8));

		// Act + Assert
		assertThrows(java.util.MissingResourceException.class, () -> builder.promptFile(file, "does_not_exist"));
	}

	@Test
	void getOrigin_and_origin_roundTrip() {
		// Arrange
		Genai provider = mock(Genai.class);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, TestProjectLayouts.projectLayout(tempDir));
		Bindex origin = new Bindex();

		// Act
		builder.origin(origin);

		// Assert
		assertSame(origin, builder.getOrigin());
	}

	@Test
	void getProjectLayout_returnsProvidedLayout() {
		// Arrange
		Genai provider = mock(Genai.class);
		ProjectLayout layout = TestProjectLayouts.projectLayout(tempDir);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, layout);

		// Act + Assert
		assertSame(layout, builder.getProjectLayout());
	}

	@Test
	void getGenAIProvider_returnsProvider() {
		// Arrange
		Genai provider = mock(Genai.class);
		BindexBuilder builder = createBuilderWithMockedProvider(provider, TestProjectLayouts.projectLayout(tempDir));

		// Act + Assert
		assertSame(provider, builder.getGenAIProvider());
	}

	private static BindexBuilder createBuilderWithMockedProvider(Genai provider, ProjectLayout layout) {
		Configurator configurator = mock(Configurator.class);
		BindexBuilder builder;

		try (MockedStatic<GenaiProviderManager> managerMock = mockStatic(GenaiProviderManager.class)) {
			// Sonar java:S6068 - Mockito eq(...) is redundant in static invocation matching.
			managerMock.when(() -> GenaiProviderManager.getProvider("openai", configurator)).thenReturn(provider);
			builder = new BindexBuilder(layout, "openai", configurator);
		}

		// Avoid mocking FunctionToolsLoader (not mockable on this runtime). Force provider back in.
		try {
			Field providerField = BindexBuilder.class.getDeclaredField("provider");
			providerField.setAccessible(true);
			providerField.set(builder, provider);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return builder;
	}
}
