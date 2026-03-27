package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit tests for {@link PythonBindexBuilder}.
 */
class PythonBindexBuilderTest {

	@Test
	void projectContext_whenProjectNamePresent_promptsFilesUnderInferredDirectory(@TempDir File projectDir)
			throws Exception {
		Files.write(new File(projectDir, "pyproject.toml").toPath(), "[project]\nname = \"a.b\"\n".getBytes(StandardCharsets.UTF_8));

		File moduleDir = new File(projectDir, "a/b");
		assertTrue(moduleDir.mkdirs());
		File f1 = new File(moduleDir, "m.py");
		Files.write(f1.toPath(), "print('x')".getBytes(StandardCharsets.UTF_8));
		File f2 = new File(moduleDir, "r.txt");
		Files.write(f2.toPath(), "data".getBytes(StandardCharsets.UTF_8));

		ProjectLayout layout = TestProjectLayouts.projectLayout(projectDir);

		Genai provider = Mockito.mock(Genai.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenaiProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenaiProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenaiProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			PythonBindexBuilder builder = new PythonBindexBuilder(layout, "provider", config);
			String ctx = builder.projectContext();

			assertNotNull(ctx);
			assertTrue(ctx.contains("a.b"));
			assertTrue(ctx.contains("m.py"));
			assertTrue(ctx.contains("print('x')"));
			assertTrue(ctx.contains("r.txt"));
			assertTrue(ctx.contains("data"));
		}
	}

	@Test
	void projectContext_whenProjectNameMissing_doesNotFail(@TempDir File projectDir) throws Exception {
		Files.write(new File(projectDir, "pyproject.toml").toPath(), "[project]\n# name missing\n".getBytes(StandardCharsets.UTF_8));

		ProjectLayout layout = TestProjectLayouts.projectLayout(projectDir);

		Genai provider = Mockito.mock(Genai.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenaiProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenaiProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenaiProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			PythonBindexBuilder builder = new PythonBindexBuilder(layout, "provider", config);
			String ctx = builder.projectContext();

			assertNotNull(ctx);
			assertTrue(ctx.contains("name missing"));
		}
	}
}
