package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.bindex.BindexRegister;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class RegisterStaticCoverageTest {

	private static class TestRegisterMojo extends Register {
		boolean bindexed = true;

		@Override
		protected boolean isBindexed() {
			return bindexed;
		}
	}

	@Test
	void execute_whenBindexed_andBindexRegisterThrowsIOException_wrapsException_andCallsUsageLog() throws Exception {
		// Arrange
		TestRegisterMojo mojo = new TestRegisterMojo();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("jar");
		project.setFile(new File("pom.xml"));
		mojo.project = project;
		mojo.model = "provider:model";
		mojo.registerUrl = "http://localhost";
		mojo.update = true;
		TestSupport.setPrivateField(mojo, "settings", new Settings());
		TestSupport.setPrivateField(mojo, "serverId", null);

		try (MockedStatic<GenaiProviderManager> mockedStatic = Mockito.mockStatic(GenaiProviderManager.class);
				MockedConstruction<BindexRegister> mockedConstruction = Mockito.mockConstruction(BindexRegister.class,
						(mock, context) -> Mockito.doThrow(new IOException("io")).when(mock).scanFolder(Mockito.any(File.class)))) {

			// Act
			mojo.execute();

			// Assert
			assertEquals(1, mockedConstruction.constructed().size());
			BindexRegister register = mockedConstruction.constructed().get(0);
			Mockito.verify(register).update(true);
			Mockito.verify(register).scanFolder(project.getBasedir());
			mockedStatic.verify(GenaiProviderManager::logUsage);
		}
	}

	@Test
	void execute_whenNotBindexed_returnsEarly_andDoesNotCallUsageLog() throws Exception {
		// Arrange
		TestRegisterMojo mojo = new TestRegisterMojo();
		mojo.bindexed = false;
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("pom");
		project.setFile(new File("pom.xml"));
		mojo.project = project;
		mojo.model = "provider:model";
		mojo.registerUrl = "http://localhost";
		TestSupport.setPrivateField(mojo, "settings", new Settings());
		TestSupport.setPrivateField(mojo, "serverId", null);

		try (MockedStatic<GenaiProviderManager> mockedStatic = Mockito.mockStatic(GenaiProviderManager.class)) {
			// Act
			assertDoesNotThrow(mojo::execute);

			// Assert
			mockedStatic.verifyNoInteractions();
		}
	}

	@Test
	void execute_whenBindexed_callsUpdateAndScanFolder_andCallsUsageLog() throws Exception {
		// Arrange
		TestRegisterMojo mojo = new TestRegisterMojo();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("jar");
		project.setFile(new File("pom.xml"));
		mojo.project = project;
		mojo.model = "provider:model";
		mojo.registerUrl = "http://localhost";
		mojo.update = false;
		TestSupport.setPrivateField(mojo, "settings", new Settings());
		TestSupport.setPrivateField(mojo, "serverId", null);

		try (MockedStatic<GenaiProviderManager> mockedStatic = Mockito.mockStatic(GenaiProviderManager.class);
				MockedConstruction<BindexRegister> mockedConstruction = Mockito.mockConstruction(BindexRegister.class)) {

			// Act
			assertDoesNotThrow(mojo::execute);

			// Assert
			assertEquals(1, mockedConstruction.constructed().size());
			BindexRegister register = mockedConstruction.constructed().get(0);
			Mockito.verify(register).update(false);
			Mockito.verify(register).scanFolder(project.getBasedir());
			mockedStatic.verify(GenaiProviderManager::logUsage);
		}
	}
}
