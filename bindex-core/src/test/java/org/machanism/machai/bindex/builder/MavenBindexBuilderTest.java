package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit tests for {@link MavenBindexBuilder}.
 */
class MavenBindexBuilderTest {

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
	void removeNotImportantData_shouldNotThrowAndShouldReducePomNoise() {
		Model model = new Model();
		model.setBuild(new Build());
		model.setProperties(new java.util.Properties());
		model.setDependencyManagement(new org.apache.maven.model.DependencyManagement());
		model.setReporting(new org.apache.maven.model.Reporting());
		model.setScm(new org.apache.maven.model.Scm());
		model.setPluginRepositories(Collections.singletonList(new org.apache.maven.model.Repository()));
		model.setDistributionManagement(new org.apache.maven.model.DistributionManagement());

		MavenProjectLayout layout = layoutWithProjectDirAndModel(new File("."), new Model());
		Genai provider = Mockito.mock(Genai.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenaiProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenaiProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenaiProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			MavenBindexBuilder builder = new MavenBindexBuilder(layout, "provider", config);
			builder.removeNotImportantData(model);

			assertTrue(model.getBuild() == null || model.getBuild().toString().isEmpty());
			assertTrue(model.getProperties() == null || model.getProperties().isEmpty());
			assertTrue(model.getDependencyManagement() == null || model.getDependencyManagement().toString().isEmpty());
			assertTrue(model.getReporting() == null || model.getReporting().toString().isEmpty());
			assertTrue(model.getScm() == null || model.getScm().toString().isEmpty());
			assertTrue(model.getPluginRepositories() == null || model.getPluginRepositories().isEmpty());
		}
	}

	@Test
	void projectContext_whenBuildNull_returnsEmptyString() throws Exception {
		Model model = new Model();
		model.setBuild(null);

		MavenProjectLayout layout = layoutWithProjectDirAndModel(new File("."), model);

		Genai provider = Mockito.mock(Genai.class);
		Configurator config = Mockito.mock(Configurator.class);

		try (MockedStatic<org.machanism.machai.ai.manager.GenaiProviderManager> mocked = Mockito
				.mockStatic(org.machanism.machai.ai.manager.GenaiProviderManager.class)) {
			mocked.when(() -> org.machanism.machai.ai.manager.GenaiProviderManager.getProvider(Mockito.anyString(),
					Mockito.same(config))).thenReturn(provider);

			MavenBindexBuilder builder = new MavenBindexBuilder(layout, "provider", config);
			assertTrue(builder.projectContext().isEmpty());
		}
	}

	@Test
	void projectContext_includesSourceAndResourceFiles_andPomSection(@TempDir File projectDir) throws Exception {
		File srcDir = new File(projectDir, "src/main/java");
		assertTrue(srcDir.mkdirs());
		File javaFile = new File(srcDir, "App.java");
		Files.write(javaFile.toPath(), "class App {}".getBytes(StandardCharsets.UTF_8));

		File resDir = new File(projectDir, "src/main/resources");
		assertTrue(resDir.mkdirs());
		File resFile = new File(resDir, "a.txt");
		Files.write(resFile.toPath(), "data".getBytes(StandardCharsets.UTF_8));

		Build build = new Build();
		build.setSourceDirectory(srcDir.getPath());

		Resource resource = new Resource();
		resource.setDirectory(resDir.getPath());
		build.setResources(Collections.singletonList(resource));

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
			assertTrue(ctx.contains("App.java"));
			assertTrue(ctx.contains("class App"));
			assertTrue(ctx.contains("a.txt"));
			assertTrue(ctx.contains("data"));
			assertTrue(ctx.contains("<project"));
		}
	}
}
