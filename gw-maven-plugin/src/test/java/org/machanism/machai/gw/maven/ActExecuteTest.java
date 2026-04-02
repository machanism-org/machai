package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.jupiter.api.Test;

class ActExecuteTest {

	@Test
	void execute_shouldConfigureActProcessorAndDelegateToProcess() throws Exception {
		// Arrange
		Act goal = spy(new Act());

		MavenProject project = mock(MavenProject.class);
		doReturn(Collections.singletonList("module-a")).when(project).getModules();
		goal.project = project;
		goal.basedir = new File(".");
		goal.excludes = new String[] { "**/excluded/**" };
		goal.logInputs = true;
		goal.model = "openai:test";
		goal.actPrompt = "my";

		Prompter prompter = mock(Prompter.class);
		goal.prompter = prompter;

		MavenSession session = mock(MavenSession.class);
		doReturn(Collections.singletonList(new MavenProject())).when(session).getAllProjects();
		doReturn(true).when(session).isParallel();
		MavenExecutionRequest request = mock(MavenExecutionRequest.class);
		doReturn(3).when(request).getDegreeOfConcurrency();
		doReturn(request).when(session).getRequest();
		goal.session = session;

		doReturn(new org.machanism.macha.core.commons.configurator.PropertiesConfigurator()).when(goal).getConfiguration();

		org.mockito.Mockito.doNothing().when(goal).process(any());

		// Act
		assertDoesNotThrow(goal::execute);

		// Assert
		verify(goal, times(1)).process(any());
		verify(session, times(1)).isParallel();
		verify(request, times(1)).getDegreeOfConcurrency();
	}
}
