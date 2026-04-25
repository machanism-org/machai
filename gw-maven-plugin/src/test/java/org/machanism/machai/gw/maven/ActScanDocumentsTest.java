package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

class ActScanDocumentsTest {

	static class RecordingActProcessor extends ActProcessor {
		File scannedBasedir;
		String scannedDir;
		private final PropertiesConfigurator configurator = new PropertiesConfigurator();

		RecordingActProcessor() {
			super(new File("."), new PropertiesConfigurator(), null);
		}

		@Override
		public PropertiesConfigurator getConfigurator() {
			return configurator;
		}

		@Override
		public void scanDocuments(File basedir, String scanDir) {
			this.scannedBasedir = basedir;
			this.scannedDir = scanDir;
		}
	}

	@Test
	void scanDocuments_shouldDefaultToBasedirWhenNoScanDirProvided() {
		ActMojo goal = new ActMojo();
		goal.basedir = new File(".").getAbsoluteFile();
		goal.project = new MavenProject();
		goal.project.setFile(new File(goal.basedir, "pom.xml"));
		goal.session = newSession();

		RecordingActProcessor processor = new RecordingActProcessor();

		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		assertEquals(goal.basedir, processor.scannedBasedir);
		assertEquals(goal.basedir.getAbsolutePath(), processor.scannedDir);
	}

	@Test
	void scanDocuments_shouldUseSuperScanDirWhenProvided() {
		ActMojo goal = new ActMojo();
		goal.basedir = new File(".").getAbsoluteFile();
		goal.project = new MavenProject();
		goal.project.setFile(new File(goal.basedir, "pom.xml"));
		goal.scanDir = "custom-scan";
		goal.session = newSession();

		RecordingActProcessor processor = new RecordingActProcessor();
		processor.getConfigurator().set(Ghostwriter.SCAN_DIR_PROP_NAME, "ignored");

		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		assertEquals("custom-scan", processor.scannedDir);
	}

	@Test
	void scanDocuments_shouldUseConfiguredScanDirWhenGoalScanDirIsNull() {
		ActMojo goal = new ActMojo();
		goal.basedir = new File(".").getAbsoluteFile();
		goal.project = new MavenProject();
		goal.project.setFile(new File(goal.basedir, "pom.xml"));
		goal.session = newSession();

		RecordingActProcessor processor = new RecordingActProcessor();
		processor.getConfigurator().set(Ghostwriter.SCAN_DIR_PROP_NAME, "configured-scan");

		assertDoesNotThrow(() -> goal.scanDocuments(processor));

		assertEquals("configured-scan", processor.scannedDir);
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession() {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setProjectPresent(true);
		return new MavenSession(null, null, request, null);
	}
}
