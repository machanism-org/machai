package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MavenBindexBuilderAdditionalTest {

	private static MavenProjectLayout layoutWithProjectDirAndModel(File dir, Model model) {
		return new MavenProjectLayout() {
			@Override
			public File getProjectDir() {
				return dir;
			}

			@Override
			public Model getModel() {
				return model;
			}
		};
	}

	@Test
	void projectContext_whenSourceDirectoryBlank_andResourcesNull_stillIncludesPomAndRules(@TempDir File projectDir)
			throws Exception {
		Build build = new Build();
		build.setSourceDirectory("   ");
		build.setResources(null);

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setBuild(build);

		MavenProjectLayout layout = layoutWithProjectDirAndModel(projectDir, model);
		Genai provider = Mockito.mock(Genai.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenaiProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenaiProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenaiProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			MavenBindexBuilder builder = new MavenBindexBuilder(layout, "provider", config);

			String ctx = builder.projectContext();

			assertNotNull(ctx);
			assertTrue(ctx.contains("<project"));
		}
	}
}
