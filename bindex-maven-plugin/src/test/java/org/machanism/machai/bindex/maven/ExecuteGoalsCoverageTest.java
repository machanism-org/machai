package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.bindex.BindexRegister;

class ExecuteGoalsCoverageTest {

	private static MavenProject newProject(String packaging) {
		MavenProject project = new MavenProject(new Model());
		project.setArtifactId("a");
		project.setPackaging(packaging);
		return project;
	}

	private static Create createMojo(MavenProject project) throws Exception {
		Create mojo = new Create();
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.model = "provider:model";
		TestSupport.setPrivateField(mojo, "settings", new org.apache.maven.settings.Settings());
		return mojo;
	}

	private static Update updateMojo(MavenProject project) throws Exception {
		Update mojo = new Update();
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.model = "provider:model";
		TestSupport.setPrivateField(mojo, "settings", new org.apache.maven.settings.Settings());
		return mojo;
	}

	private static Register registerMojo(MavenProject project) {
		Register mojo = new Register();
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.model = "provider:model";
		mojo.registerUrl = "http://example.test";
		mojo.update = true;
		return mojo;
	}

	@Test
	void create_execute_whenPomPackaging_doesNotCallBindexCreatorButLogsUsage() throws Exception {
		// Arrange
		MavenProject project = newProject("pom");
		Create mojo = createMojo(project);

		try (org.mockito.MockedStatic<GenaiProviderManager> mockedUsage = mockStatic(GenaiProviderManager.class);
				org.mockito.MockedConstruction<BindexCreator> mockedCreator = mockConstruction(BindexCreator.class)) {

			// Act
			mojo.execute();

			// Assert
			assertEquals(0, mockedCreator.constructed().size());
			mockedUsage.verify(GenaiProviderManager::logUsage);
		}
	}

	@Test
	void update_execute_whenPomPackaging_doesNotCallBindexCreatorButLogsUsage() throws Exception {
		// Arrange
		MavenProject project = newProject("pom");
		Update mojo = updateMojo(project);

		try (org.mockito.MockedStatic<GenaiProviderManager> mockedUsage = mockStatic(GenaiProviderManager.class);
				org.mockito.MockedConstruction<BindexCreator> mockedCreator = mockConstruction(BindexCreator.class)) {

			// Act
			mojo.execute();

			// Assert
			assertEquals(0, mockedCreator.constructed().size());
			mockedUsage.verify(GenaiProviderManager::logUsage);
		}
	}

	@Test
	void create_execute_whenJarPackaging_constructsAndRunsBindexCreator() throws Exception {
		// Arrange
		MavenProject project = newProject("jar");
		Create mojo = createMojo(project);

		try (org.mockito.MockedStatic<GenaiProviderManager> mockedUsage = mockStatic(GenaiProviderManager.class);
				org.mockito.MockedConstruction<BindexCreator> mockedCreator = mockConstruction(BindexCreator.class, (mock, context) -> {
					when(mock.update(anyBoolean())).thenReturn(mock);
				})) {

			// Act
			mojo.execute();

			// Assert
			assertEquals(1, mockedCreator.constructed().size());
			BindexCreator creator = mockedCreator.constructed().get(0);
			verify(creator).update(false);
			verify(creator).processFolder(any());
			mockedUsage.verify(GenaiProviderManager::logUsage);
		}
	}

	@Test
	void update_execute_whenJarPackaging_constructsAndRunsBindexCreatorWithUpdateTrue() throws Exception {
		// Arrange
		MavenProject project = newProject("jar");
		Update mojo = updateMojo(project);

		try (org.mockito.MockedStatic<GenaiProviderManager> mockedUsage = mockStatic(GenaiProviderManager.class);
				org.mockito.MockedConstruction<BindexCreator> mockedCreator = mockConstruction(BindexCreator.class, (mock, context) -> {
					when(mock.update(anyBoolean())).thenReturn(mock);
				})) {

			// Act
			mojo.execute();

			// Assert
			assertEquals(1, mockedCreator.constructed().size());
			BindexCreator creator = mockedCreator.constructed().get(0);
			verify(creator).update(true);
			verify(creator).processFolder(any());
			mockedUsage.verify(GenaiProviderManager::logUsage);
		}
	}

	@Test
	void register_execute_whenPomPackaging_returnsBeforeTryFinally_andDoesNotLogUsage() throws Exception {
		// Arrange
		MavenProject project = newProject("pom");
		Register mojo = registerMojo(project);

		try (org.mockito.MockedStatic<GenaiProviderManager> mockedUsage = mockStatic(GenaiProviderManager.class);
				org.mockito.MockedConstruction<BindexRegister> mockedRegister = mockConstruction(BindexRegister.class)) {
			// Act
			mojo.execute();

			// Assert
			assertEquals(0, mockedRegister.constructed().size());
			mockedUsage.verify(GenaiProviderManager::logUsage, never());
		}
	}

	@Test
	void register_execute_whenJarPackaging_constructsAndRunsBindexRegister_andLogsUsage() throws Exception {
		// Arrange
		MavenProject project = newProject("jar");
		Register mojo = registerMojo(project);
		TestSupport.setPrivateField(mojo, "settings", new org.apache.maven.settings.Settings());

		try (org.mockito.MockedStatic<GenaiProviderManager> mockedUsage = mockStatic(GenaiProviderManager.class);
				org.mockito.MockedConstruction<BindexRegister> mockedRegister = mockConstruction(BindexRegister.class)) {
			// Act
			mojo.execute();

			// Assert
			assertEquals(1, mockedRegister.constructed().size());
			BindexRegister register = mockedRegister.constructed().get(0);
			verify(register).update(true);
			verify(register).scanFolder(project.getBasedir());
			mockedUsage.verify(GenaiProviderManager::logUsage);
		}
	}

	@Test
	void abstractCreateBindex_readsLogInputsAndPropagatesToCreator() throws Exception {
		// Arrange
		class Mojo extends AbstractBindexMojo {
			@Override
			public void execute() {
				// not used
			}

			@Override
			protected PropertiesConfigurator getConfigurator() {
				PropertiesConfigurator c = new PropertiesConfigurator();
				c.set(org.machanism.machai.ai.manager.Genai.LOG_INPUTS_PROP_NAME, "true");
				return c;
			}
		}

		Mojo mojo = new Mojo();
		MavenProject project = newProject("jar");
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.model = "provider:model";

		try (org.mockito.MockedConstruction<BindexCreator> mockedCreator = mockConstruction(BindexCreator.class, (mock, context) -> {
			when(mock.update(anyBoolean())).thenReturn(mock);
		})) {
			// Act
			mojo.createBindex(false);

			// Assert
			BindexCreator creator = mockedCreator.constructed().get(0);
			verify(creator).setLogInputs(true);
		}
	}
}
