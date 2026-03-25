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
import java.io.IOException;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
public void scanDocuments_whenNonRecursiveReactorFlagIsSet_shouldSkipScan() throws IOException {
    // TestMate-9737acab471b515c5860f5996676eb9d
    // Arrange
    ReactorAct act = new ReactorAct();
    File basedir = new File(".").getAbsoluteFile();
    act.basedir = basedir;
    MavenProject project = new MavenProject();
    project.getModel().addModule("module-1");
    project.getModel().addModule("module-2");
    act.project = project;
    MavenSession session = mock(MavenSession.class);
    List<MavenProject> singleProjectList = Collections.singletonList(new MavenProject());
    when(session.getAllProjects()).thenReturn(singleProjectList);
    when(session.getExecutionRootDirectory()).thenReturn(basedir.getAbsolutePath());
    act.session = session;
    Log log = mock(Log.class);
    ReactorAct actSpy = spy(act);
    when(actSpy.getLog()).thenReturn(log);
    ActProcessor actProcessor = mock(ActProcessor.class);
    // Act
    actSpy.scanDocuments(actProcessor);
    // Assert
    verify(log).info(contains("Skipping document scan"));
    verify(actProcessor, never()).setNonRecursive(true);
    verify(actProcessor, never()).scanDocuments(any(File.class), anyString());
}
}
