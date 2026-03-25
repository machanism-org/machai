package org.machanism.machai.gw.maven;

import java.io.File;
import java.util.Collections;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

public class ReactorActTest {

	private static MavenProject projectWithModules(int moduleCount) {
		MavenProject p = new MavenProject();
		for (int i = 0; i < moduleCount; i++) {
			p.getModel().addModule("m" + i);
		}
		return p;
	}

	@Test
	public void scanDocuments_whenNotExecutionRootProject_butActProcessorAlreadyNonRecursive_logsSkipMessage() throws Exception {
		// Arrange
		ReactorAct act = new ReactorAct();
		File basedir = new File(".").getAbsoluteFile();
		act.basedir = basedir;
		act.project = projectWithModules(0);

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(new File("..")
				.getCanonicalFile().getAbsolutePath());
		Mockito.when(session.getAllProjects()).thenReturn(Collections.singletonList(new MavenProject()));
		act.session = session;

		Log log = Mockito.mock(Log.class);
		ReactorAct spy = Mockito.spy(act);
		Mockito.doReturn(log).when(spy).getLog();

		ActProcessor processor = Mockito.mock(ActProcessor.class);
		Mockito.when(processor.isNonRecursive()).thenReturn(true);

		// Act
		spy.scanDocuments(processor);

		// Assert
		Mockito.verify(log).info(Mockito.contains("Skipping document scan"));
		Mockito.verify(processor, Mockito.never()).setNonRecursive(true);
		Mockito.verify(processor, Mockito.never()).scanDocuments(Mockito.any(File.class), Mockito.anyString());
	}
}
