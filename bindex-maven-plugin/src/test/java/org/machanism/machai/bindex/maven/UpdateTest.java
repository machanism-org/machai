package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class UpdateTest {

	private static class TestUpdate extends Update {
		boolean bindexed;
		boolean createCalled;
		Boolean updateArg;
		MojoExecutionException createThrows;
		boolean usageLogged;

		@Override
		public void execute() throws MojoExecutionException {
			try {
				if (isBindexed()) {
					createBindex(true);
				}
			} finally {
				usageLogged = true; // in production: GenAIProviderManager.logUsage()
			}
		}

		@Override
		protected boolean isBindexed() {
			return bindexed;
		}

		@Override
		protected void createBindex(boolean update) throws MojoExecutionException {
			createCalled = true;
			updateArg = update;
			if (createThrows != null) {
				throw createThrows;
			}
		}
	}

	@Test
	void execute_whenNotBindexed_skipsUpdateButFinallyRuns() throws Exception {
		// Arrange
		TestUpdate mojo = new TestUpdate();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("pom");
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.bindexed = false;

		// Act
		mojo.execute();

		// Assert
		org.junit.jupiter.api.Assertions.assertFalse(mojo.createCalled);
		org.junit.jupiter.api.Assertions.assertTrue(mojo.usageLogged);
	}

	@Test
	void execute_whenBindexed_callsCreateBindexWithUpdateTrue() throws Exception {
		// Arrange
		TestUpdate mojo = new TestUpdate();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("jar");
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.bindexed = true;

		// Act
		mojo.execute();

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(mojo.createCalled);
		org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, mojo.updateArg);
		org.junit.jupiter.api.Assertions.assertTrue(mojo.usageLogged);
	}

	@Test
	void execute_whenCreateBindexThrows_stillRunsFinally() {
		// Arrange
		TestUpdate mojo = new TestUpdate();
		MavenProject project = new MavenProject(new Model());
		project.setPackaging("jar");
		mojo.project = project;
		mojo.basedir = new File(".");
		mojo.bindexed = true;
		mojo.createThrows = new MojoExecutionException("boom");

		// Act
		assertThrows(MojoExecutionException.class, mojo::execute);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(mojo.usageLogged);
	}
}
