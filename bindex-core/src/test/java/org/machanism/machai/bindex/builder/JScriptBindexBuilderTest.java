package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit tests for {@link JScriptBindexBuilder}.
 */
class JScriptBindexBuilderTest {

	@Test
	void projectContext_readsPackageJson_andWalksSrcTree(@TempDir File projectDir) throws Exception {
		File packageJson = new File(projectDir, JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
		Files.write(packageJson.toPath(), "{\"name\":\"demo\"}".getBytes(StandardCharsets.UTF_8));

		File srcDir = new File(projectDir, "src");
		assertTrue(srcDir.mkdirs());

		File ts = new File(srcDir, "a.ts");
		Files.write(ts.toPath(), "export const a = 1;".getBytes(StandardCharsets.UTF_8));
		File vue = new File(srcDir, "b.vue");
		Files.write(vue.toPath(), "<template></template>".getBytes(StandardCharsets.UTF_8));
		File ignored = new File(srcDir, "c.txt");
		Files.write(ignored.toPath(), "ignore".getBytes(StandardCharsets.UTF_8));

		ProjectLayout layout = TestProjectLayouts.projectLayout(projectDir);

		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenAIProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenAIProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenAIProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			JScriptBindexBuilder builder = new JScriptBindexBuilder(layout, "provider", config);
			String ctx = builder.projectContext();

			assertNotNull(ctx);
			assertTrue(ctx.contains("demo"));
			assertTrue(ctx.contains("a.ts"));
			assertTrue(ctx.contains("export const a"));
			assertTrue(ctx.contains("b.vue"));
			assertTrue(ctx.contains("<template>"));
			assertTrue(!ctx.contains("c.txt"));
		}
	}
}
