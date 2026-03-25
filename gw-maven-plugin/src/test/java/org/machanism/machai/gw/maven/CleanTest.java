package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.machanism.machai.gw.processor.GuidanceProcessor;

public class CleanTest {

	static class TestableClean extends Clean {
		void setBasedir(File basedir) {
			this.basedir = basedir;
		}
	}

	@Test
	public void execute_whenDeleteTempFilesSucceeds_doesNotThrow() throws Exception {
		// Arrange
		TestableClean clean = new TestableClean();
		File basedir = new File(".");
		clean.setBasedir(basedir);

		try (MockedStatic<GuidanceProcessor> mocked = Mockito.mockStatic(GuidanceProcessor.class)) {
			// Act
			clean.execute();

			// Assert
			mocked.verify(() -> GuidanceProcessor.deleteTempFiles(basedir));
		}
	}

	@Test
	public void execute_whenDeleteTempFilesThrowsRuntime_wrapsInMojoExecutionException() throws Exception {
		// Arrange
		TestableClean clean = new TestableClean();
		File basedir = new File(".");
		clean.setBasedir(basedir);

		RuntimeException boom = new RuntimeException("boom");
		try (MockedStatic<GuidanceProcessor> mocked = Mockito.mockStatic(GuidanceProcessor.class)) {
			mocked.when(() -> GuidanceProcessor.deleteTempFiles(basedir)).thenThrow(boom);

			// Act
			try {
				clean.execute();
				fail("Expected MojoExecutionException");
			} catch (MojoExecutionException e) {
				// Assert
				assertEquals("Failed to delete workflow temporary files.", e.getMessage());
				assertEquals(boom, e.getCause());
			}
		}
	}
}
