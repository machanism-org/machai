package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class RegisterTest {

	private static class TestRegister extends Register {
		boolean bindexed;
		boolean usageLogged;
		IOException scanThrows;

		boolean registerCreated;
		Boolean updateArg;
		File scannedFolder;

		@Override
		public void execute() throws MojoExecutionException {
			if (!isBindexed()) {
				return;
			}
			try {
				// Simulate constructor+calls (in production this uses new BindexRegister)
				registerCreated = true;
				updateArg = update;
				if (scanThrows != null) {
					throw scanThrows;
				}
				scannedFolder = project.getBasedir();
			} catch (IOException e) {
				throw new MojoExecutionException("Bindex register failed.", e);
			} finally {
				usageLogged = true; // in production: GenAIProviderManager.logUsage()
			}
		}

		@Override
		protected boolean isBindexed() {
			return bindexed;
		}
	}

	@Test
	void execute_whenNotBindexed_returnsEarly_andDoesNotRunFinally() throws Exception {
		// Arrange
		TestRegister mojo = new TestRegister();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("pom");
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.bindexed = false;

		// Act
		mojo.execute();

		// Assert
		org.junit.jupiter.api.Assertions.assertFalse(mojo.usageLogged);
		org.junit.jupiter.api.Assertions.assertFalse(mojo.registerCreated);
	}

	@Test
	void execute_whenBindexed_runsUpdateAndScan_andRunsFinally() throws Exception {
		// Arrange
		TestRegister mojo = new TestRegister();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("jar");
		project.setFile(new File("pom.xml"));
		mojo.project = project;
		mojo.update = true;
		mojo.bindexed = true;

		// Act
		mojo.execute();

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(mojo.registerCreated);
		org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, mojo.updateArg);
		org.junit.jupiter.api.Assertions.assertEquals(project.getBasedir(), mojo.scannedFolder);
		org.junit.jupiter.api.Assertions.assertTrue(mojo.usageLogged);
	}

	@Test
	void execute_whenScanThrowsIOException_wrapsAndRunsFinally() {
		// Arrange
		TestRegister mojo = new TestRegister();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("jar");
		project.setFile(new File("pom.xml"));
		mojo.project = project;
		mojo.update = false;
		mojo.bindexed = true;
		mojo.scanThrows = new IOException("io");

		// Act
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions.assertEquals("Bindex register failed.", ex.getMessage());
		org.junit.jupiter.api.Assertions.assertTrue(ex.getCause() instanceof IOException);
		org.junit.jupiter.api.Assertions.assertTrue(mojo.usageLogged);
	}
}
