package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.machanism.machai.gw.processor.GuidanceProcessor;

class AbstractGWGoalScanDocumentsTest {

	static class TestGoal extends AbstractGWGoal {
		@Override
		public void execute() {
			// not used
		}
	}

	@Test
	void scanDocuments_shouldDefaultScanDirToExecutionRootAndInvokeProcessor() throws Exception {
		// Arrange
		TestGoal goal = new TestGoal();
		goal.excludes = new String[] { "**/skip/**" };
		goal.instructions = "some-instructions";
		goal.scanDir = null;
		goal.basedir = new File(".");
		goal.logInputs = true;

		MavenProject project = mock(MavenProject.class);
		doReturn(new File(".")).when(project).getBasedir();
		goal.project = project;

		MavenSession session = mock(MavenSession.class);
		doReturn(new File(".").getAbsolutePath()).when(session).getExecutionRootDirectory();
		goal.session = session;

		GuidanceProcessor processor = mock(GuidanceProcessor.class);

		Log log = mock(Log.class);
		goal.setLog(log);

		// Act
		goal.scanDocuments(processor);

		// Assert
		verify(processor, times(1)).setExcludes(eq(goal.excludes));
		verify(processor, times(1)).setInstructions(eq("some-instructions"));
		verify(processor, times(1)).setLogInputs(eq(true));
		verify(processor, times(1)).scanDocuments(eq(new File(".")), eq(new File(".").getAbsolutePath()));
	}

	@Test
	void scanDocuments_shouldWrapProcessorExceptionsInMojoExecutionException() throws Exception {
		// Arrange
		TestGoal goal = new TestGoal();
		goal.basedir = new File(".");

		MavenProject project = mock(MavenProject.class);
		doReturn(new File(".")).when(project).getBasedir();
		goal.project = project;

		MavenSession session = mock(MavenSession.class);
		doReturn(new File(".").getAbsolutePath()).when(session).getExecutionRootDirectory();
		goal.session = session;

		GuidanceProcessor processor = mock(GuidanceProcessor.class);
		RuntimeException boom = new RuntimeException("boom");
		org.mockito.Mockito.doThrow(boom).when(processor).scanDocuments(any(File.class), any(String.class));

		Log log = mock(Log.class);
		goal.setLog(log);

		// Act + Assert
		assertThrows(MojoExecutionException.class, () -> goal.scanDocuments(processor));
		verify(log, times(1)).error(eq(boom));
	}
}
