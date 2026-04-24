package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

class ActScanDocumentsTest {

	@Test
	void scanDocuments_shouldDefaultToBasedirWhenNoScanDirProvided() throws Exception {
		ActMojo goal = new ActMojo();
		goal.basedir = new File(".");
		goal.session = mock(MavenSession.class);
		MavenExecutionRequest request = mock(MavenExecutionRequest.class);
		when(goal.session.getRequest()).thenReturn(request);
		when(request.isProjectPresent()).thenReturn(false);

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		doReturn(conf).when(processor).getConfigurator();

		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		verify(processor).scanDocuments(goal.basedir, goal.basedir.getAbsolutePath());
	}

	@Test
	void scanDocuments_shouldUseSuperScanDirWhenProvided() throws Exception {
		ActMojo goal = new ActMojo();
		goal.basedir = new File(".");
		goal.scanDir = "custom-scan";
		goal.session = mock(MavenSession.class);
		MavenExecutionRequest request = mock(MavenExecutionRequest.class);
		when(goal.session.getRequest()).thenReturn(request);
		when(request.isProjectPresent()).thenReturn(false);

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		conf.set(Ghostwriter.SCAN_DIR_PROP_NAME, "ignored");
		doReturn(conf).when(processor).getConfigurator();

		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		verify(processor).scanDocuments(goal.basedir, "custom-scan");
	}

	@Test
	void scanDocuments_shouldUseConfiguredScanDirWhenGoalScanDirIsNull() throws Exception {
		ActMojo goal = new ActMojo();
		goal.basedir = new File(".");
		goal.session = mock(MavenSession.class);
		MavenExecutionRequest request = mock(MavenExecutionRequest.class);
		when(goal.session.getRequest()).thenReturn(request);
		when(request.isProjectPresent()).thenReturn(false);

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		conf.set(Ghostwriter.SCAN_DIR_PROP_NAME, "configured-scan");
		doReturn(conf).when(processor).getConfigurator();

		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		verify(processor).scanDocuments(goal.basedir, "configured-scan");
	}
}
