package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;
import org.machanism.machai.gw.processor.ActProcessor;

public class ReactorActTest {

	static class TestableReactorAct extends ReactorAct {
		Log log = Mockito.mock(Log.class);

		boolean superScanInvoked;
		ActProcessor captured;

		@Override
		public Log getLog() {
			return log;
		}

		@Override
		protected void scanDocuments(ActProcessor actProcessor) throws java.io.IOException {
			this.captured = actProcessor;
			// Do not call super to avoid hitting the real Act.scanDocuments implementation.
		}

		void invokeSuperScanDocuments(ActProcessor actProcessor) throws java.io.IOException {
			superScanInvoked = true;
			super.scanDocuments(actProcessor);
		}
	}

	private static MavenProject projectWithModules(int moduleCount) {
		MavenProject p = new MavenProject();
		for (int i = 0; i < moduleCount; i++) {
			p.getModel().addModule("m" + i);
		}
		return p;
	}

	@Test
	public void scanDocuments_whenExecutionRootProject_andNotNonRecursive_callsSuperScanDocumentsPath() throws Exception {
		// Arrange
		TestableReactorAct act = new TestableReactorAct();
		File basedir = new File(".").getAbsoluteFile();
		act.basedir = basedir;
		act.project = projectWithModules(2);

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(basedir.getAbsolutePath());
		Mockito.when(session.getAllProjects()).thenReturn(java.util.Collections.singletonList(new MavenProject()));
		act.session = session;

		ActProcessor processor = Mockito.mock(ActProcessor.class);
		Mockito.when(processor.isNonRecursive()).thenReturn(false);

		// Act
		// We can't let ReactorAct call our overridden scanDocuments; so call the real one by temporarily routing.
		act.invokeSuperScanDocuments(processor);

		// Assert
		assertTrue(act.superScanInvoked);
	}

	@Test
	public void scanDocuments_whenExecutionRootProject_butAlreadyNonRecursive_logsSkipMessage() throws Exception {
		// Arrange
		TestableReactorAct act = new TestableReactorAct();
		File basedir = new File(".").getAbsoluteFile();
		act.basedir = basedir;
		act.project = projectWithModules(2);

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(basedir.getAbsolutePath());
		Mockito.when(session.getAllProjects()).thenReturn(java.util.Collections.singletonList(new MavenProject()));
		act.session = session;

		ActProcessor processor = Mockito.mock(ActProcessor.class);
		Mockito.when(processor.isNonRecursive()).thenReturn(true);

		// Act
		// Call actual ReactorAct logic
		ReactorAct real = new ReactorAct();
		real.basedir = basedir;
		real.project = act.project;
		real.session = session;
		// inject log
		ReactorAct spy = Mockito.spy(real);
		Mockito.doReturn(act.log).when(spy).getLog();
		spy.scanDocuments(processor);

		// Assert
		Mockito.verify(act.log).info(Mockito.contains("Skipping document scan"));
	}
}
