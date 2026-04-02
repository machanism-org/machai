package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

class ActScanDocumentsTest {

	@Test
	void scanDocuments_shouldDefaultToBasedirWhenNoScanDirProvided() throws Exception {
		// Arrange
		Act goal = new Act();
		goal.basedir = new File(".");

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		doReturn(conf).when(processor).getConfigurator();

		// Act
		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		// Assert
		verify(processor).scanDocuments(eq(goal.basedir), eq(goal.basedir.getAbsolutePath()));
	}

	@Test
	void scanDocuments_shouldUseSuperScanDirWhenProvided() throws Exception {
		// Arrange
		Act goal = new Act();
		goal.basedir = new File(".");
		goal.scanDir = "custom-scan";

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		conf.set(Ghostwriter.SCAN_DIR_PROP_NAME, "ignored");
		doReturn(conf).when(processor).getConfigurator();

		// Act
		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		// Assert
		verify(processor).scanDocuments(eq(goal.basedir), eq("custom-scan"));
	}
}
