package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

class ActConfigureAndScanTest {

	@Test
	void configureAndScan_shouldUseExistingActPromptWithoutApplyingAndScan() throws Exception {
		// Arrange
		Act goal = spy(new Act());
		goal.basedir = new File(".");
		goal.actPrompt = "saved";

		MavenSession session = mock(MavenSession.class);
		doReturn(new Properties()).when(session).getUserProperties();
		goal.session = session;

		ActProcessor processor = mock(ActProcessor.class);
		doReturn(new PropertiesConfigurator()).when(processor).getConfigurator();

		// Act
		assertDoesNotThrow(() -> goal.configureAndScan(processor));

		// Assert
		verify(goal, times(0)).applyActPrompt(any());
		verify(processor, times(1)).setDefaultPrompt(eq("saved"));
		verify(goal, times(1)).scanDocuments(eq(processor));
	}

	@Test
	void configureAndScan_shouldApplyActPromptWhenNullAndReadFromUserProperties() throws Exception {
		// Arrange
		Act goal = spy(new Act());
		goal.basedir = new File(".");
		goal.actPrompt = null;

		Properties userProps = new Properties();
		userProps.setProperty(Ghostwriter.ACT_PROP_NAME, "fromProps");
		MavenSession session = mock(MavenSession.class);
		doReturn(userProps).when(session).getUserProperties();
		goal.session = session;

		ActProcessor processor = mock(ActProcessor.class);
		doReturn(new PropertiesConfigurator()).when(processor).getConfigurator();

		doAnswer(inv -> {
			userProps.setProperty(Ghostwriter.ACT_PROP_NAME, "fromProps");
			return null;
		}).when(goal).applyActPrompt(any());

		// Act
		assertDoesNotThrow(() -> goal.configureAndScan(processor));

		// Assert
		verify(goal, times(1)).applyActPrompt(any());
		verify(processor, times(1)).setDefaultPrompt(eq("fromProps"));
		verify(goal, times(1)).scanDocuments(eq(processor));
	}
}
